package com.jiangnan.travel.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * 订单状态机 — 严格流转规则。
 * ponytail: enum-based state machine, no external DSL needed.
 */
public enum OrderStatus {

    CREATED(0, "待接单"),
    PAID(7, "已支付"),
    DRIVER_ASSIGNED(9, "已派单"),
    ASSIGNED(1, "司机已接单"),
    ARRIVED(2, "已到达"),
    IN_PROGRESS(3, "行程中"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消"),
    RISK_BLOCKED(6, "风控拦截"),
    REFUNDED(8, "已退款");

    private final int code;
    private final String label;

    OrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    /** 允许从 from 流转到 this */
    public boolean canTransitionFrom(OrderStatus from) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(from);
    }

    // ── 合法流转表（仅增量，不删除已有流转）──
    private static final java.util.Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = java.util.Map.of(
        PAID,             EnumSet.of(CREATED),
        DRIVER_ASSIGNED,  EnumSet.of(PAID),
        ASSIGNED,         EnumSet.of(PAID, DRIVER_ASSIGNED),     // 兼容老路径 + 新路径
        ARRIVED,          EnumSet.of(ASSIGNED),
        IN_PROGRESS,      EnumSet.of(ARRIVED),
        COMPLETED,        EnumSet.of(IN_PROGRESS),
        CANCELLED,        EnumSet.of(CREATED, PAID, DRIVER_ASSIGNED, ASSIGNED, ARRIVED),
        REFUNDED,         EnumSet.of(COMPLETED, CANCELLED),
        RISK_BLOCKED,     EnumSet.of(CREATED)
    );

    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}
