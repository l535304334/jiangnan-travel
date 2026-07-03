package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 支付追踪日志 — 记录每次支付尝试（含重试）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_trace")
public class PaymentTrace extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paymentId;
    private Long orderId;
    private Long userId;

    /** 尝试序号（首次=1，重试=N） */
    private Integer attemptNo;

    /** pending / paid / failed / refunded */
    private Integer status;

    /** 支付方式 */
    private String payMethod;

    /** 流水号 */
    private String payNo;

    /** 幂等键 */
    private String idempotentKey;

    /** 金额 */
    private java.math.BigDecimal amount;

    /** 失败原因 */
    private String failReason;

    /** 耗时(ms) */
    private Integer costMs;

    private LocalDateTime traceTime;
}
