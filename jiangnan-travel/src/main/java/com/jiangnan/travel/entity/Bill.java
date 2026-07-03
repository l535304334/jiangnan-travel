package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账单 — 订单完成时自动生成。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_bill")
public class Bill extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long driverId;

    /** 里程费 = distance × unitPrice */
    private BigDecimal distanceFee;
    /** 时长费 = duration × minutePrice */
    private BigDecimal durationFee;
    /** 高峰加价 */
    private BigDecimal peakSurcharge;
    /** 优惠券抵扣 */
    private BigDecimal couponDiscount;
    /** 过路费 */
    private BigDecimal tollFee;
    /** 合计 = distanceFee + durationFee + peakSurcharge + tollFee - couponDiscount */
    private BigDecimal totalAmount;
}
