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
import com.jiangnan.travel.service.DriverAssignmentService;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 司机派单实现。
 * 策略：优先距离最近 → 其次评分最高。
 * ponytail: simple priority queue, upgrade to ML matching if dispatch latency becomes an issue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAssignmentServiceImpl implements DriverAssignmentService {

    private final OrderMapper orderMapper;
    private final DriverMapper driverMapper;
    private final OrderEventMapper orderEventMapper;
    private final NotificationService notificationService;
    private final ScoringEngine scoringEngine;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final String ASSIGN_LOCK_PREFIX = "order:assign:";
    private static final int MAX_ASSIGN_ATTEMPTS = 3;

    @Override
    public OrderVO assignDriver(Long orderId) {
        String lockKey = ASSIGN_LOCK_PREFIX + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        }
        try {
            return transactionTemplate.execute(status -> {
                Order order = orderMapper.selectById(orderId);
                if (order == null) {
                    throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
                }
                // 仅 PAID 状态可派单
                if (order.getStatus() != OrderStatus.PAID.getCode()
                        && order.getStatus() != OrderStatus.DRIVER_ASSIGNED.getCode()) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                            "当前状态不允许派单: " + order.getStatus());
                }

                // 计算已尝试次数
                int attemptCount = countAssignmentAttempts(orderId);
                if (attemptCount >= MAX_ASSIGN_ATTEMPTS) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                            "已超过最大派单次数(" + MAX_ASSIGN_ATTEMPTS + ")，请手动处理");
                }

                // 找最优司机
                Driver best = findBestDriver(order, attemptCount);
                if (best == null) {
                    throw new BusinessException(ErrorCode.SYSTEM_BUSY, "当前无可用司机，请稍后重试");
                }

                // 状态 → DRIVER_ASSIGNED
                int fromStatus = order.getStatus();
                order.setDriverId(best.getId());
                order.setStatus(OrderStatus.DRIVER_ASSIGNED.getCode());
                orderMapper.updateById(order);

                // 司机状态 → 派单待确认
                best.setStatus(DriverStatus.ASSIGNMENT_PENDING.getCode());
                driverMapper.updateById(best);

                // 事件: DRIVER_ASSIGNED + DRIVER_BUSY
                recordEvent(order, OrderStatus.DRIVER_ASSIGNED.getCode(), best.getId(), "system",
                        "派单给 " + best.getRealName() + " (第" + (attemptCount + 1) + "次尝试)");
                // DRIVER_BUSY 事件
                OrderEvent busyEvent = new OrderEvent();
                busyEvent.setOrderId(order.getId());
                busyEvent.setOrderNo(order.getOrderNo());
                busyEvent.setFromStatus(order.getStatus());
                busyEvent.setToStatus(OrderStatus.DRIVER_ASSIGNED.getCode());
                busyEvent.setOperatorId(best.getId());
                busyEvent.setOperatorType("system");
                busyEvent.setRemark("DRIVER_BUSY: driver " + best.getId());
                busyEvent.setEventTime(LocalDateTime.now());
                orderEventMapper.insert(busyEvent);

                // 通知
                notificationService.create(best.getUserId() != null ? best.getUserId() : order.getUserId(),
                        "NEW_ORDER", "新订单", "您有一个新订单待接单", orderId);

                log.info("派单成功: orderId={}, driverId={}, driverName={}, attempt={}",
                        orderId, best.getId(), best.getRealName(), attemptCount + 1);

                // 推送
                OrderTrackingServer.pushOrderUpdate(orderId,
                        String.format("{\"orderId\":%d,\"status\":%d,\"action\":\"DRIVER_ASSIGNED\",\"driverId\":%d}",
                                orderId, OrderStatus.DRIVER_ASSIGNED.getCode(), best.getId()));

                return toVO(order, best);
            });
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Override
    public OrderVO acceptAssignment(Long orderId, Long driverId) {
        return transactionTemplate.execute(status -> {
            Order order = orderMapper.selectById(orderId);
            if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
            if (order.getStatus() != OrderStatus.DRIVER_ASSIGNED.getCode()) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单未处于待接单状态");
            }
            if (!order.getDriverId().equals(driverId)) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "该订单已派给其他司机");
            }

            int fromStatus = order.getStatus();
            order.setStatus(OrderStatus.ASSIGNED.getCode());
            order.setAcceptTime(LocalDateTime.now());
            orderMapper.updateById(order);

            Driver driver = driverMapper.selectById(driverId);
            if (driver != null) {
                driver.setStatus(DriverStatus.BUSY.getCode()); // 接单中
                driverMapper.updateById(driver);
            }

            recordEvent(order, OrderStatus.ASSIGNED.getCode(), driverId, "driver",
                    "司机 " + (driver != null ? driver.getRealName() : "") + " 接受派单");
            if (driver != null) {
                notificationService.create(order.getUserId(), "ORDER_ACCEPTED",
                        "司机已接单", "司机 " + driver.getRealName() + " 已接单", orderId);
            }
            return toVO(order, driver);
        });
    }

    @Override
    public OrderVO rejectAssignment(Long orderId, Long driverId, String reason) {
        return transactionTemplate.execute(status -> {
            Order order = orderMapper.selectById(orderId);
            if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
            if (order.getStatus() != OrderStatus.DRIVER_ASSIGNED.getCode()) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单未处于待接单状态");
            }
            if (!order.getDriverId().equals(driverId)) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "该订单已派给其他司机");
            }

            // 记录拒单事件
            Driver driver = driverMapper.selectById(driverId);
            recordEvent(order, order.getStatus(), driverId, "driver",
                    "司机 " + (driver != null ? driver.getRealName() : "") + " 拒单"
                            + (reason != null ? "：" + reason : ""));

            // 恢复司机状态 + 记录拒单惩罚
            if (driver != null) {
                driver.setStatus(DriverStatus.ONLINE_IDLE.getCode());
                driver.setRejectionCount((driver.getRejectionCount() != null ? driver.getRejectionCount() : 0) + 1);
                // DRIVER_RELEASED 事件
                recordEvent(order, order.getStatus(), driverId, "system",
                        "DRIVER_RELEASED: driver " + driverId + " released after rejection");
                driverMapper.updateById(driver);
            }

            // 清除 driverId，准备重新派单
            order.setDriverId(null);
            order.setStatus(OrderStatus.PAID.getCode()); // 回到 PAID 以便重新派单
            orderMapper.updateById(order);

            log.info("司机拒单: orderId={}, driverId={}, reason={}", orderId, driverId, reason);

            // ORDER_REASSIGNED 事件
            recordEvent(order, order.getStatus(), 0L, "system",
                    "ORDER_REASSIGNED: 司机 " + driverId + " 拒单，自动重派");

            // 自动重新派单
            try {
                return assignDriver(orderId);
            } catch (BusinessException e) {
                log.warn("重新派单失败: orderId={}, error={}", orderId, e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_BUSY,
                        "司机已拒单，但重新派单失败: " + e.getMessage());
            }
        });
    }

    // ── private helpers ──

    /** 找最优司机：ScoringEngine 评分排序 */
    private Driver findBestDriver(Order order, int excludeCount) {
        // 仅 ONLINE_IDLE 司机 + 有位置
        List<Driver> candidates = driverMapper.selectList(
                new LambdaQueryWrapper<Driver>()
                        .eq(Driver::getStatus, DriverStatus.ONLINE_IDLE.getCode())
                        .ge(Driver::getLat, BigDecimal.ZERO)
                        .ge(Driver::getLng, BigDecimal.ZERO));

        if (candidates.isEmpty()) return null;

        final long now = System.currentTimeMillis();
        candidates.sort(Comparator.<Driver>comparingDouble(d -> {
            DriverContext ctx = new DriverContext(
                    computeIdleMinutes(d),
                    d.getRejectionCount() != null ? d.getRejectionCount() : 0,
                    now);
            return -scoringEngine.score(d, order, ctx); // 降序
        }));

        if (excludeCount >= candidates.size()) return null;
        return candidates.get(excludeCount);
    }

    private long computeIdleMinutes(Driver d) {
        if (d.getLastActiveTime() == null) return 60;
        return Math.max(0, java.time.temporal.ChronoUnit.MINUTES
                .between(d.getLastActiveTime(), LocalDateTime.now()));
    }

    /** 统计已有派单尝试次数（查 order_event） */
    private int countAssignmentAttempts(Long orderId) {
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
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .driverId(order.getDriverId())
                .driverName(driver != null ? driver.getRealName() : null)
                .carPlate(driver != null ? driver.getCarPlate() : null)
                .startAddress(order.getStartAddress())
                .endAddress(order.getEndAddress())
                .distance(order.getDistance())
                .duration(order.getDuration())
                .basePrice(order.getBasePrice())
                .finalPrice(order.getFinalPrice())
                .status(order.getStatus())
                .statusText(OrderStatus.fromCode(order.getStatus()).getLabel())
                .createTime(order.getCreateTime())
                .build();
    }
}
