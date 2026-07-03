package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.vo.OrderVO;

/**
 * 司机派单服务 — PAID 后自动派单。
 */
public interface DriverAssignmentService {

    /**
     * 为已支付订单指派司机。
     * 策略：优先距离最近，其次评分最高。
     */
    OrderVO assignDriver(Long orderId);

    /**
     * 司机接受派单。
     */
    OrderVO acceptAssignment(Long orderId, Long driverId);

    /**
     * 司机拒绝派单 — 自动重新派单（最多3次）。
     */
    OrderVO rejectAssignment(Long orderId, Long driverId, String reason);
}
