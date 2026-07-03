package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;

/**
 * 司机评分引擎 — 模块化设计，可替换策略。
 * ponytail: simple weighted sum; swap implementation for ML model later.
 */
public interface ScoringEngine {

    /**
     * 计算司机与订单的匹配分数 (0~100)。
     * 分数越高越优先分配。
     */
    double score(Driver driver, Order order, DriverContext context);

    /**
     * v1.3 反馈闭环: 司机接受派单 → 奖励。
     */
    void recordAccept(Long driverId);

    /**
     * v1.3 反馈闭环: 司机拒绝派单 → 惩罚。
     */
    void recordReject(Long driverId);

    /**
     * v1.3 反馈闭环: 订单取消 → 轻惩罚。
     */
    void recordCancel(Long driverId);
}
