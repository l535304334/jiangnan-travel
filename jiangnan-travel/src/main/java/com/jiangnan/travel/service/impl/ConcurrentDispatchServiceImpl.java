package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.OrderEvent;
import com.jiangnan.travel.enums.DriverStatus;
import com.jiangnan.travel.enums.OrderStatus;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.mapper.OrderEventMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.service.AnomalyDetectionService;
import com.jiangnan.travel.service.ConcurrentDispatchService;
import com.jiangnan.travel.service.DispatchMetricsService;
import com.jiangnan.travel.service.DriverContext;
import com.jiangnan.travel.service.NotificationService;
import com.jiangnan.travel.service.ScoringEngine;
import com.jiangnan.travel.vo.OrderVO;
import com.jiangnan.travel.websocket.OrderTrackingServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 并发安全调度实现。
 *
 * 锁层次:
 *   1. order:dispatch:{orderId}   — 订单级锁，防重复派单
 *   2. driver:lock:{driverId}     — 司机级锁，防重复分配
 *
 * 流程:
 *   lock order → find candidates (ScoringEngine) → for each: tryLock driver
 *   → if ok: assign → record events → release locks
 *   → if fail: try next candidate
 *
 * ponytail: two-phase lock with fallback; upgrade to auction model if latency becomes an issue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConcurrentDispatchServiceImpl implements ConcurrentDispatchService {

    private final OrderMapper orderMapper;
    private final DriverMapper driverMapper;
    private final OrderEventMapper orderEventMapper;
    private final ScoringEngine scoringEngine;
    private final DynamicScoringEngine dynamicScoringEngine;
    private final DispatchMetricsService metricsService;
    private final AnomalyDetectionService anomalyService;
    private final NotificationService notificationService;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final String ORDER_DISPATCH_LOCK = "order:dispatch:";
    private static final String DRIVER_LOCK_PREFIX = "driver:lock:";
    private static final int MAX_CANDIDATES = 10;
    private static final long DRIVER_LOCK_WAIT_MS = 500;  // 司机锁最大等待0.5s
    private static final int MAX_ASSIGN_ATTEMPTS = 3;

    @Override
    public OrderVO dispatchWithDriverLock(Long orderId) {
        // 1. 订单级锁
        String orderLockKey = ORDER_DISPATCH_LOCK + orderId;
        RLock orderLock = redissonClient.getLock(orderLockKey);
        try {
            if (!orderLock.tryLock(10, 15, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY, "订单调度锁竞争失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        }

        long startMs = System.currentTimeMillis();
        try {
            return transactionTemplate.execute(status -> {
                Order order = orderMapper.selectById(orderId);
                if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
                if (order.getStatus() != OrderStatus.PAID.getCode()
                        && order.getStatus() != OrderStatus.DRIVER_ASSIGNED.getCode()) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                            "不允许派单: status=" + order.getStatus());
                }

                int attemptCount = countAttempts(orderId);
                if (attemptCount >= MAX_ASSIGN_ATTEMPTS) {
                    recordEvent(order, order.getStatus(), 0L, "system",
                            "ORDER_ASSIGN_FAILED: 超过最大尝试次数");
                    metricsService.recordFailure(orderId, "max attempts exceeded", attemptCount,
                            System.currentTimeMillis() - startMs);
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                            "已超过最大派单次数");
                }

                // 2. 找候选司机（按 ScoringEngine 排序）
                List<Driver> candidates = findCandidates(order);
                if (candidates.isEmpty()) {
                    recordEvent(order, order.getStatus(), 0L, "system",
                            "ORDER_ASSIGN_FAILED: 无可派司机");
                    metricsService.recordFailure(orderId, "no available drivers", 0,
                            System.currentTimeMillis() - startMs);
                    throw new BusinessException(ErrorCode.SYSTEM_BUSY,
                            "当前无可用司机，请稍后重试");
                }

                // 3. 遍历候选，尝试锁司机
                for (int i = 0; i < Math.min(candidates.size(), MAX_CANDIDATES); i++) {
                    Driver candidate = candidates.get(i);
                    String driverLockKey = DRIVER_LOCK_PREFIX + candidate.getId();
                    RLock driverLock = redissonClient.getLock(driverLockKey);

                    boolean driverLocked = false;
                    try {
                        driverLocked = driverLock.tryLock(DRIVER_LOCK_WAIT_MS, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        continue;
                    }

                    if (!driverLocked) {
                        // 司机被其他订单锁定 → 跳过，试下一个
                        anomalyService.recordLockContention(DRIVER_LOCK_PREFIX + candidate.getId());
                        log.debug("Driver[{}] locked by another order, skipping", candidate.getId());
                        continue;
                    }

                    try {
                        // 获取锁后重新读司机状态（防止并发修改）
                        Driver fresh = driverMapper.selectById(candidate.getId());
                        if (fresh == null || !DriverStatus.fromCode(fresh.getStatus()).canAcceptDispatch()) {
                            log.debug("Driver[{}] no longer available, skipping", candidate.getId());
                            continue;
                        }

                        // 4. 分配成功
                        assignOrderToDriver(order, fresh, attemptCount + 1);
                        long costMs = System.currentTimeMillis() - startMs;
                        metricsService.recordSuccess(orderId, fresh.getId(), attemptCount + 1, costMs);
                        dynamicScoringEngine.recordAccept(fresh.getId());
                        return toVO(order, fresh);

                    } finally {
                        // 不释放 driverLock — 司机已被分配，锁伴随订单生命周期
                        // 订单完成/取消时 releaseDriver() 释放
                    }
                }

                // 5. 所有候选都失败
                recordEvent(order, order.getStatus(), 0L, "system",
                        "ORDER_ASSIGN_FAILED: 候选司机均被锁定");
                long costMs = System.currentTimeMillis() - startMs;
                metricsService.recordFailure(orderId, "all candidates locked", attemptCount + 1, costMs);
                throw new BusinessException(ErrorCode.SYSTEM_BUSY,
                        "当前所有司机正忙，请稍后重试");
            });
        } finally {
            if (orderLock.isHeldByCurrentThread()) {
                orderLock.unlock();
            }
        }
    }

    @Override
    public int batchDispatch(List<Long> orderIds) {
        int success = 0;
        List<String> errors = new ArrayList<>();

        for (Long orderId : orderIds) {
            try {
                dispatchWithDriverLock(orderId);
                success++;
            } catch (BusinessException e) {
                errors.add("order " + orderId + ": " + e.getMessage());
                log.warn("批量派单失败: orderId={}, error={}", orderId, e.getMessage());
            }
        }

        log.info("批量派单完成: {}/{} 成功, 失败: {}", success, orderIds.size(), errors.size());
        if (!errors.isEmpty()) {
            log.info("失败详情: {}", String.join("; ", errors));
        }
        return success;
    }

    @Override
    public void heartbeat(Long driverId, BigDecimal lat, BigDecimal lng) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) return;
        driver.setLat(lat);
        driver.setLng(lng);
        driver.setLastActiveTime(LocalDateTime.now());
        driverMapper.updateById(driver);
    }

    @Override
    public void releaseDriver(Long driverId) {
        String driverLockKey = DRIVER_LOCK_PREFIX + driverId;
        RLock driverLock = redissonClient.getLock(driverLockKey);
        if (driverLock.isHeldByCurrentThread() || driverLock.isLocked()) {
            driverLock.forceUnlock();
        }
        // 恢复司机状态为空闲
        Driver driver = driverMapper.selectById(driverId);
        if (driver != null && driver.getStatus() == DriverStatus.BUSY.getCode()) {
            driver.setStatus(DriverStatus.ONLINE_IDLE.getCode());
            driverMapper.updateById(driver);
        }
        log.info("Driver[{}] released — DRIVER_RELEASED", driverId);
    }

    // ── private ──

    /** 找候选司机：仅 ONLINE_IDLE，按 ScoringEngine 排序 */
    private List<Driver> findCandidates(Order order) {
        List<Driver> idleDrivers = driverMapper.selectList(
                new LambdaQueryWrapper<Driver>()
                        .eq(Driver::getStatus, DriverStatus.ONLINE_IDLE.getCode())
                        .ge(Driver::getLat, BigDecimal.ZERO));

        final long now = System.currentTimeMillis();

        return idleDrivers.stream()
                .sorted(Comparator.<Driver>comparingDouble(d -> {
                    DriverContext ctx = new DriverContext(
                            computeIdleMinutes(d),
                            d.getRejectionCount() != null ? d.getRejectionCount() : 0,
                            now);
                    return -scoringEngine.score(d, order, ctx); // 降序
                }))
                .collect(Collectors.toList());
    }

    private long computeIdleMinutes(Driver d) {
        if (d.getLastActiveTime() == null) return 60; // 无数据默认1h空闲
        return Math.max(0, ChronoUnit.MINUTES.between(d.getLastActiveTime(), LocalDateTime.now()));
    }

    private void assignOrderToDriver(Order order, Driver driver, int attempt) {
        int fromStatus = order.getStatus();
        order.setDriverId(driver.getId());
        order.setStatus(OrderStatus.DRIVER_ASSIGNED.getCode());
        orderMapper.updateById(order);

        // 司机 → BUSY
        driver.setStatus(DriverStatus.BUSY.getCode());
        driverMapper.updateById(driver);

        // 事件: DRIVER_ASSIGNED
        recordEvent(order, OrderStatus.DRIVER_ASSIGNED.getCode(), driver.getId(), "system",
                "派单给 " + driver.getRealName() + " (第" + attempt + "次, score引擎)");

        // 事件: DRIVER_BUSY
        OrderEvent busyEvent = new OrderEvent();
        busyEvent.setOrderId(order.getId());
        busyEvent.setOrderNo(order.getOrderNo());
        busyEvent.setFromStatus(fromStatus);
        busyEvent.setToStatus(OrderStatus.DRIVER_ASSIGNED.getCode());
        busyEvent.setOperatorId(driver.getId());
        busyEvent.setOperatorType("system");
        busyEvent.setRemark("DRIVER_BUSY: driver " + driver.getId() + " assigned");
        busyEvent.setEventTime(LocalDateTime.now());
        orderEventMapper.insert(busyEvent);

        notificationService.create(driver.getUserId(),
                "NEW_ORDER", "新订单派送",
                "订单 " + order.getOrderNo() + " 已派送，请及时确认", order.getId());

        OrderTrackingServer.pushOrderUpdate(order.getId(),
                String.format("{\"orderId\":%d,\"status\":%d,\"action\":\"DRIVER_ASSIGNED\",\"driverId\":%d}",
                        order.getId(), OrderStatus.DRIVER_ASSIGNED.getCode(), driver.getId()));

        log.info("并发派单成功: orderId={}, driverId={}, attempt={}", order.getId(), driver.getId(), attempt);
    }

    private int countAttempts(Long orderId) {
        Long cnt = orderEventMapper.selectCount(
                new LambdaQueryWrapper<OrderEvent>()
                        .eq(OrderEvent::getOrderId, orderId)
                        .eq(OrderEvent::getToStatus, OrderStatus.DRIVER_ASSIGNED.getCode()));
        return cnt != null ? cnt.intValue() : 0;
    }

    private void recordEvent(Order order, int toStatus, Long operatorId, String operatorType, String remark) {
        OrderEvent event = new OrderEvent();
        event.setOrderId(order.getId());
        event.setOrderNo(order.getOrderNo());
        event.setFromStatus(order.getStatus());
        event.setToStatus(toStatus);
        event.setOperatorId(operatorId);
        event.setOperatorType(operatorType);
        event.setRemark(remark);
        event.setEventTime(LocalDateTime.now());
        orderEventMapper.insert(event);
    }

    private OrderVO toVO(Order order, Driver driver) {
        return OrderVO.builder()
                .id(order.getId()).orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .driverId(order.getDriverId())
                .driverName(driver != null ? driver.getRealName() : null)
                .carPlate(driver != null ? driver.getCarPlate() : null)
                .startAddress(order.getStartAddress())
                .endAddress(order.getEndAddress())
                .distance(order.getDistance()).duration(order.getDuration())
                .basePrice(order.getBasePrice()).finalPrice(order.getFinalPrice())
                .status(order.getStatus())
                .statusText(OrderStatus.fromCode(order.getStatus()).getLabel())
                .createTime(order.getCreateTime())
                .build();
    }
}
