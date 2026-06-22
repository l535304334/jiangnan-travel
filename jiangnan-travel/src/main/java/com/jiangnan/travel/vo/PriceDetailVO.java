package com.jiangnan.travel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDetailVO {

    private BigDecimal basePrice;       // 起步价（含3公里）
    private String baseDesc;

    private BigDecimal midDistance;     // 3-50公里里程（公里）
    private BigDecimal midPrice;        // 中程费用

    private BigDecimal longDistance;    // 50-200公里里程
    private BigDecimal longPrice;       // 远程费用

    private BigDecimal superLongDistance; // >200公里里程
    private BigDecimal superLongPrice;    // 超远程费用

    private BigDecimal timeFee;         // 时长费

    private BigDecimal subtotal;        // 小计（加价前）
    private BigDecimal surgeFactor;     // 加价系数
    private BigDecimal surgePrice;      // 加价金额

    private BigDecimal tollFee;         // 过路费
    private BigDecimal couponDiscount;  // 优惠券抵扣
    private BigDecimal totalPrice;      // 最终预估
}
