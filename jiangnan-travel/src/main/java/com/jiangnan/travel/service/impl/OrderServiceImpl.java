package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.entity.*;
import com.jiangnan.travel.entity.RiskAlert;
import com.jiangnan.travel.mapper.*;
import com.jiangnan.travel.service.NotificationService;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.service.RiskAlertService;
import com.jiangnan.travel.vo.DailyOrderStatVO;
import com.jiangnan.travel.vo.OrderVO;
import com.jiangnan.travel.websocket.OrderTrackingServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final CarTypeMapper carTypeMapper;
    private final DriverMapper driverMapper;
    private final ReviewMapper reviewMapper;
    private final PaymentMapper paymentMapper;
    private final UserCouponMapper userCouponMapper;
    private final RiskAlertMapper riskAlertMapper;
    private final PricingServiceImpl pricingService;
    private final RedissonClient redissonClient;
    private final NotificationService notificationService;
    private final RiskAlertService riskAlertService;
    private final TransactionTemplate transactionTemplate;

    private static final String IDEMPOTENT_PREFIX = "order:idempotent:";
    private static final String ACCEPT_LOCK_PREFIX = "order:lock:accept:";
    private static final String PAY_LOCK_PREFIX = "order:lock:pay:";

    @Override
    @Transactional
    public OrderVO create(CreateOrderRequest request, Long userId) {
        // 1. 幂等键
        String idempotentKey = request.getIdempotentKey();
        if (idempotentKey == null || idempotentKey.isEmpty()) {
            idempotentKey = userId + "_" + System.currentTimeMillis();
        }
        RLock lock = redissonClient.getLock(IDEMPOTENT_PREFIX + idempotentKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        }

        try {
            // 2. 风控R4：深夜跨城安全提示
            checkRiskR4(userId, request);

            // 3. 风控R2：取消频率限制
            checkRiskR2(userId);
            Long carTypeId = request.getCarTypeId() != null ? request.getCarTypeId() : 1L;
            CarType carType = carTypeMapper.selectById(carTypeId);
            if (carType == null || carType.getStatus() == 0) {
                throw new BusinessException(ErrorCode.ORDER_CAR_TYPE_INVALID);
            }

            // 4. 计算价格
            var estimateReq = new com.jiangnan.travel.dto.EstimateRequest();
            estimateReq.setCarTypeId(carTypeId);
            estimateReq.setDistance(request.getDistance());
            estimateReq.setDuration(request.getDuration());
            estimateReq.setStartAddress(request.getStartAddress());
            estimateReq.setStartLat(request.getStartLat());
            estimateReq.setStartLng(request.getStartLng());
            estimateReq.setEndAddress(request.getEndAddress());
            estimateReq.setEndLat(request.getEndLat());
            estimateReq.setEndLng(request.getEndLng());

            var estimateVO = pricingService.estimate(estimateReq);
            BigDecimal basePrice = estimateVO.getPriceDetail().getSubtotal();
            BigDecimal surgeFactor = estimateVO.getSurgeFactor();
            BigDecimal couponDiscount = pricingService.calcCouponDiscount(
                    request.getCouponId(), userId, basePrice);
            BigDecimal finalPrice = basePrice.multiply(surgeFactor)
                    .subtract(couponDiscount)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            if (finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                finalPrice = BigDecimal.valueOf(0.01);
            }

            // 5. 生成订单号
            String orderNo = "JN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%04d", new Random().nextInt(10000));

            // 6. 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setCarTypeId(carTypeId);
            order.setStartAddress(request.getStartAddress());
            order.setStartLat(request.getStartLat());
            order.setStartLng(request.getStartLng());
            order.setEndAddress(request.getEndAddress());
            order.setEndLat(request.getEndLat());
            order.setEndLng(request.getEndLng());
            order.setDistance(request.getDistance());
            order.setDuration(request.getDuration());
            order.setBasePrice(basePrice);
            order.setSurgeFactor(surgeFactor);
            order.setCouponDiscount(couponDiscount);
            order.setFinalPrice(finalPrice);
            order.setTollFee(BigDecimal.ZERO);
            order.setStatus(0);
            orderMapper.insert(order);

            // 7. 标记优惠券已使用
            if (request.getCouponId() != null && couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
                UserCoupon uc = userCouponMapper.selectById(request.getCouponId());
                if (uc != null) {
                    uc.setStatus(1);
                    uc.setUseOrderId(order.getId());
                    userCouponMapper.updateById(uc);
                }
            }

            // 8. 发送通知
            notificationService.create(userId, "ORDER_CREATED",
                    "订单已创建", "订单 " + orderNo + " 已提交，等待司机接单", order.getId());

            // 9. 风控规则检查（异步非阻塞）
            try {
                riskAlertService.checkR1(userId);
                riskAlertService.checkR3(request.getStartLat(), request.getStartLng(),
                        request.getEndLat(), request.getEndLng(), userId);
                riskAlertService.checkR5(userId);
            } catch (Exception e) {
                log.warn("风控检查异常", e);
            }

            return toVO(order);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public OrderVO getById(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) {
            Driver driver = driverMapper.selectOne(
                    new LambdaQueryWrapper<Driver>().eq(Driver::getUserId, userId));
            boolean isAssignedDriver = driver != null && driver.getId().equals(order.getDriverId());
            boolean canAcceptPending = driver != null && order.getStatus() == 0;
            if (!isAssignedDriver && !canAcceptPending) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }
        return toVO(order);
    }

    @Override
    public List<OrderVO> listByUser(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        IPage<Order> page = orderMapper.selectPage(
                new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10), wrapper);
        return toVOList(page.getRecords());
    }

    @Override
    public List<OrderVO> listByDriver(Long driverId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getDriverId, driverId)
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        IPage<Order> page = orderMapper.selectPage(
                new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10), wrapper);
        return toVOList(page.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() > 1) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        checkRiskR2(userId);

        // 已接单订单取消后恢复司机在线状态
        if (order.getStatus() == 1 && order.getDriverId() != null) {
            Driver driver = driverMapper.selectById(order.getDriverId());
            if (driver != null) {
                driver.setStatus(1);
                driverMapper.updateById(driver);
            }
        }

        order.setStatus(5);
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);

        notificationService.create(order.getUserId(), "ORDER_CANCELLED",
                "订单已取消", "订单 " + order.getOrderNo() + " 已取消" + (reason != null ? "：" + reason : ""), order.getId());
    }

    @Override
    public OrderVO accept(Long orderId, Long driverId) {
        String lockKey = ACCEPT_LOCK_PREFIX + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        }

        try {
            // 在锁内执行事务，确保事务提交后才释放锁
            return transactionTemplate.execute(status -> {
                // 加锁后重新检查订单状态，防止并发抢单
                Order order = orderMapper.selectById(orderId);
                if (order == null || order.getStatus() != 0) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
                }
                Driver driver = driverMapper.selectById(driverId);
                if (driver == null || driver.getStatus() != 1) {
                    throw new BusinessException(ErrorCode.DRIVER_OFFLINE);
                }
                order.setDriverId(driverId);
                order.setStatus(1);
                order.setAcceptTime(LocalDateTime.now());
                orderMapper.updateById(order);

                driver.setStatus(2);
                driverMapper.updateById(driver);

                notificationService.create(order.getUserId(), "ORDER_ACCEPTED",
                        "司机已接单", "司机 " + driver.getRealName() + " 已接单，正在前往上车点", order.getId());
                OrderTrackingServer.pushOrderUpdate(orderId,
                        String.format("{\"orderId\":%d,\"status\":1,\"action\":\"ORDER_ACCEPTED\"}", orderId));
                return toVO(order);
            });
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public OrderVO arrive(Long orderId, Long driverId) {
        String lockKey = "order:status:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) throw new BusinessException(9002, "操作过于频繁");
            Order order = checkDriverOrder(orderId, driverId);
            if (order.getStatus() != 1) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
            order.setStatus(2);
            order.setArriveTime(LocalDateTime.now());
            orderMapper.updateById(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(9002, "系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }

        Order freshOrder = orderMapper.selectById(orderId);
        notificationService.create(freshOrder.getUserId(), "ORDER_ARRIVED",
                "司机已到达", "司机已到达上车点，请尽快上车", orderId);
        OrderTrackingServer.pushOrderUpdate(orderId,
                String.format("{\"orderId\":%d,\"status\":2,\"action\":\"ORDER_ARRIVED\"}", orderId));
        return toVO(freshOrder);
    }

    @Override
    public OrderVO startTrip(Long orderId, Long driverId) {
        String lockKey = "order:status:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) throw new BusinessException(9002, "操作过于频繁");
            Order order = checkDriverOrder(orderId, driverId);
            if (order.getStatus() != 2) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
            order.setStatus(3);
            order.setStartTime(LocalDateTime.now());
            orderMapper.updateById(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(9002, "系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }

        Order freshOrder = orderMapper.selectById(orderId);
        notificationService.create(freshOrder.getUserId(), "ORDER_STARTED",
                "行程已开始", "您的行程已开始，预计 " + (freshOrder.getDuration() != null ? (freshOrder.getDuration() / 60) + " 分钟" : "即将到达") + " 到达目的地", orderId);
        OrderTrackingServer.pushOrderUpdate(orderId,
                String.format("{\"orderId\":%d,\"status\":3,\"action\":\"ORDER_STARTED\"}", orderId));
        return toVO(freshOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderVO complete(Long orderId, Long driverId) {
        String lockKey = "order:status:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) throw new BusinessException(9002, "操作过于频繁");
            Order order = checkDriverOrder(orderId, driverId);
            if (order.getStatus() != 3) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
            order.setStatus(4);
            order.setEndTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // R7 疲劳提醒：行程超过2小时
            checkRiskR7(order);

            Driver driver = driverMapper.selectById(driverId);
            if (driver != null) {
                driver.setStatus(1);
                driver.setTotalOrders(driver.getTotalOrders() + 1);
                driverMapper.updateById(driver);
            }

            notificationService.create(order.getUserId(), "ORDER_COMPLETED",
                    "行程已完成", "订单 " + order.getOrderNo() + " 已完成，费用 ¥" + order.getFinalPrice(), order.getId());
            OrderTrackingServer.pushOrderUpdate(orderId,
                    String.format("{\"orderId\":%d,\"status\":4,\"action\":\"ORDER_COMPLETED\"}", orderId));
            return toVO(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(9002, "系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Override
    public void pay(Long orderId, Long userId) {
        String lockKey = PAY_LOCK_PREFIX + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                Order order = orderMapper.selectById(orderId);
                if (order == null || !order.getUserId().equals(userId)) {
                    throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
                }
                if (order.getStatus() != 4) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);

                // 检查是否已支付，防止重复支付
                Payment existing = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>()
                                .eq(Payment::getOrderId, orderId)
                                .eq(Payment::getStatus, 1)
                                .last("LIMIT 1"));
                if (existing != null) {
                    throw new BusinessException(ErrorCode.PAY_FAILED, "该订单已支付");
                }

                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setUserId(userId);
                payment.setAmount(order.getFinalPrice());
                payment.setPayMethod("balance");
                payment.setStatus(1);
                payment.setPayTime(LocalDateTime.now());
                paymentMapper.insert(payment);
            });
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long orderId, Long userId, Integer rating, String tags, String content) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 4) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        if (rating == null || rating < 1 || rating > 5) throw new BusinessException(ErrorCode.BAD_REQUEST);

        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setDriverId(order.getDriverId());
        review.setRating(rating);
        review.setTags(tags);
        review.setContent(content);
        reviewMapper.insert(review);

        // 同步更新司机平均评分（DB 聚合，避免加载全量评价到内存）
        Long driverId = order.getDriverId();
        if (driverId != null) {
            var avgResult = reviewMapper.selectMaps(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Review>()
                            .select("COALESCE(AVG(rating),0) as avgRating")
                            .eq("driver_id", driverId));
            BigDecimal avg = avgResult.isEmpty() || avgResult.get(0).get("avgRating") == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(avgResult.get(0).get("avgRating").toString()).setScale(2, RoundingMode.HALF_UP);
            Driver driver = driverMapper.selectById(driverId);
            if (driver != null) {
                driver.setAvgRating(avg);
                driverMapper.updateById(driver);
            }
        }
    }

    @Override
    public Page<Order> listOrders(Integer status, int page, int size) {
        Page<Order> orderPage = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        return orderMapper.selectPage(orderPage, wrapper);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public long countTodayOrders() {
        return orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .apply("DATE(create_time) = CURDATE()"));
    }

    @Override
    public BigDecimal getTodayRevenue() {
        // ponytail: SQL SUM instead of loading all rows into memory
        var result = orderMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .select("COALESCE(SUM(final_price),0) as total")
                        .eq("status", 4)
                        .apply("DATE(create_time) = CURDATE()"));
        if (result.isEmpty() || result.get(0).get("total") == null) return BigDecimal.ZERO;
        return new BigDecimal(result.get(0).get("total").toString());
    }

    @Override
    public List<DailyOrderStatVO> getLast7DaysStats() {
        return orderMapper.selectDailyOrderStats();
    }

    @Override
    public List<OrderVO> findNearbyOrders(BigDecimal lat, BigDecimal lng, Integer limit) {
        double radius = 0.05; // ~5km
        List<Order> pendingOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 0)
                        .ge(Order::getStartLat, lat.subtract(BigDecimal.valueOf(radius)))
                        .le(Order::getStartLat, lat.add(BigDecimal.valueOf(radius)))
                        .ge(Order::getStartLng, lng.subtract(BigDecimal.valueOf(radius)))
                        .le(Order::getStartLng, lng.add(BigDecimal.valueOf(radius))));

        int resultLimit = limit != null && limit > 0 ? limit : 20;
        List<Order> sorted = pendingOrders.stream()
                .sorted(Comparator.comparingDouble(o -> distanceMeters(lat, lng, o.getStartLat(), o.getStartLng())))
                .limit(resultLimit)
                .toList();
        return toVOList(sorted);
    }

    private double distanceMeters(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        double dlat = lat1.subtract(lat2).abs().doubleValue();
        double dlng = lng1.subtract(lng2).abs().doubleValue();
        return Math.sqrt(dlat * dlat + dlng * dlng) * 111000;
    }

    /* ---- 风控 ---- */
    /** R4 深夜(23-5)跨城 -> 安全提示 */
    private void checkRiskR4(Long userId, CreateOrderRequest request) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour >= 23 || hour < 5) {
            String startCity = extractCity(request.getStartAddress());
            String endCity = extractCity(request.getEndAddress());
            if (startCity != null && endCity != null && !startCity.equals(endCity)) {
                log.info("用户[{}]深夜跨城({}→{})，建议开启安全分享", userId, startCity, endCity);
            }
        }
    }

    /** R2 同用户1小时取消>5次 -> 当日禁下单 */
    private void checkRiskR2(Long userId) {
        Long cancelCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, 5)
                        .ge(Order::getCreateTime, LocalDateTime.now().minusHours(1)));
        if (cancelCount >= 5) {
            log.warn("用户[{}]1小时取消{}次，触发R2风控禁下单", userId, cancelCount);
            throw new BusinessException(ErrorCode.RISK_ORDER_FREQUENCY);
        }
    }

    /** R7 司机行程>2小时未到达 -> 疲劳提醒 */
    private void checkRiskR7(Order order) {
        if (order.getStartTime() != null) {
            long minutes = java.time.Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes();
            if (minutes >= 120) {
                log.warn("订单[{}]行程{}分钟>2小时，触发R7疲劳提醒", order.getOrderNo(), minutes);
                RiskAlert alert = new RiskAlert();
                alert.setUserId(order.getUserId());
                alert.setOrderId(order.getId());
                alert.setAlertLevel(2);
                alert.setRuleCode("RISK-FATIGUE-007");
                alert.setDetail("司机行程已超过2小时，建议休息");
                alert.setHandled(0);
                riskAlertMapper.insert(alert);
            }
        }
    }

    private String extractCity(String address) {
        if (address == null) return null;
        if (address.contains("南昌")) return "南昌";
        if (address.contains("赣州")) return "赣州";
        if (address.contains("宁都")) return "宁都";
        if (address.contains("九江")) return "九江";
        if (address.contains("庐山")) return "庐山";
        if (address.contains("井冈山")) return "井冈山";
        if (address.contains("景德镇")) return "景德镇";
        return null;
    }

    /* ---- 辅助 ---- */
    private Order checkDriverOrder(Long orderId, Long driverId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getDriverId().equals(driverId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    // ponytail: batch CarType/Driver lookup to avoid N+1 in list methods
    private List<OrderVO> toVOList(List<Order> orders) {
        if (orders.isEmpty()) return List.of();
        var carTypeIds = orders.stream().map(Order::getCarTypeId).filter(Objects::nonNull).distinct().toList();
        var driverIds = orders.stream().map(Order::getDriverId).filter(Objects::nonNull).distinct().toList();
        var carTypeMap = carTypeIds.isEmpty() ? java.util.Collections.<Long, CarType>emptyMap()
                : carTypeMapper.selectBatchIds(carTypeIds).stream()
                        .collect(Collectors.toMap(CarType::getId, c -> c));
        var driverMap = driverIds.isEmpty() ? java.util.Collections.<Long, Driver>emptyMap()
                : driverMapper.selectBatchIds(driverIds).stream()
                        .collect(Collectors.toMap(Driver::getId, d -> d));
        return orders.stream().map(o -> toVO(o, carTypeMap, driverMap)).collect(Collectors.toList());
    }

    private OrderVO toVO(Order order, java.util.Map<Long, CarType> carTypeMap, java.util.Map<Long, Driver> driverMap) {
        CarType ct = carTypeMap.get(order.getCarTypeId());
        Driver d = driverMap.get(order.getDriverId());
        return buildVO(order,
                ct != null ? ct.getName() : "",
                d != null ? d.getRealName() : "",
                d != null ? d.getCarPlate() : "");
    }

    private OrderVO toVO(Order order) {
        String carTypeName = "", driverName = "", carPlate = "";
        if (order.getCarTypeId() != null) {
            CarType ct = carTypeMapper.selectById(order.getCarTypeId());
            if (ct != null) carTypeName = ct.getName();
        }
        if (order.getDriverId() != null) {
            Driver d = driverMapper.selectById(order.getDriverId());
            if (d != null) { driverName = d.getRealName(); carPlate = d.getCarPlate(); }
        }
        return buildVO(order, carTypeName, driverName, carPlate);
    }

    private OrderVO buildVO(Order order, String carTypeName, String driverName, String carPlate) {
        return OrderVO.builder()
                .id(order.getId()).orderNo(order.getOrderNo())
                .userId(order.getUserId()).driverId(order.getDriverId())
                .driverName(driverName).carPlate(carPlate)
                .startAddress(order.getStartAddress())
                .startLat(order.getStartLat()).startLng(order.getStartLng())
                .endAddress(order.getEndAddress())
                .endLat(order.getEndLat()).endLng(order.getEndLng())
                .distance(order.getDistance()).duration(order.getDuration())
                .basePrice(order.getBasePrice()).surgeFactor(order.getSurgeFactor())
                .couponDiscount(order.getCouponDiscount()).finalPrice(order.getFinalPrice())
                .tollFee(order.getTollFee()).status(order.getStatus())
                .statusText(switch (order.getStatus()) {
                    case 0 -> "待接单"; case 1 -> "已接单"; case 2 -> "已到达";
                    case 3 -> "行程中"; case 4 -> "已完成"; case 5 -> "已取消";
                    case 6 -> "风控拦截"; default -> "未知";
                })
                .cancelReason(order.getCancelReason())
                .isSafetyShare(order.getIsSafetyShare())
                .carTypeName(carTypeName)
                .createTime(order.getCreateTime()).acceptTime(order.getAcceptTime())
                .arriveTime(order.getArriveTime()).startTime(order.getStartTime())
                .endTime(order.getEndTime()).cancelTime(order.getCancelTime())
                .build();
    }
}
