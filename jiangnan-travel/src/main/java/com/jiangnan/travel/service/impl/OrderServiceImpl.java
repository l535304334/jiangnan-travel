package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.entity.*;
import com.jiangnan.travel.mapper.*;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private final PricingServiceImpl pricingService;
    private final RedissonClient redissonClient;

    private static final String IDEMPOTENT_PREFIX = "order:idempotent:";

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
            // 2. 风控R4：30分钟内最多3单
            checkRiskR4(userId);

            // 3. 获取车型
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
            if (driver == null || !driver.getId().equals(order.getDriverId())) {
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
        return page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void cancel(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() > 1) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        checkRiskR2(userId);

        order.setStatus(5);
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public OrderVO accept(Long orderId, Long driverId) {
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
        return toVO(order);
    }

    @Override
    public OrderVO arrive(Long orderId, Long driverId) {
        Order order = checkDriverOrder(orderId, driverId);
        if (order.getStatus() != 1) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        order.setStatus(2);
        order.setArriveTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return toVO(order);
    }

    @Override
    public OrderVO startTrip(Long orderId, Long driverId) {
        Order order = checkDriverOrder(orderId, driverId);
        if (order.getStatus() != 2) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        order.setStatus(3);
        order.setStartTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return toVO(order);
    }

    @Override
    public OrderVO complete(Long orderId, Long driverId) {
        Order order = checkDriverOrder(orderId, driverId);
        if (order.getStatus() != 3) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        order.setStatus(4);
        order.setEndTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Driver driver = driverMapper.selectById(driverId);
        if (driver != null) {
            driver.setStatus(1);
            driver.setTotalOrders(driver.getTotalOrders() + 1);
            driverMapper.updateById(driver);
        }
        return toVO(order);
    }

    @Override
    public void pay(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 4) throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(order.getFinalPrice());
        payment.setPayMethod("balance");
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);
    }

    @Override
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
    }

    /* ---- 风控 ---- */
    private void checkRiskR4(Long userId) {
        Long count = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .ge(Order::getCreateTime, LocalDateTime.now().minusMinutes(30)));
        if (count >= 3) throw new BusinessException(ErrorCode.RISK_ORDER_FREQUENCY);
    }

    private void checkRiskR2(Long userId) {
        Long cancelCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, 5)
                        .ge(Order::getCreateTime, LocalDateTime.now().minusDays(30)));
        if (cancelCount >= 5) {
            log.warn("用户[{}]30天取消{}次，触发R2提醒", userId, cancelCount);
        }
    }

    /* ---- 辅助 ---- */
    private Order checkDriverOrder(Long orderId, Long driverId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getDriverId().equals(driverId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
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
