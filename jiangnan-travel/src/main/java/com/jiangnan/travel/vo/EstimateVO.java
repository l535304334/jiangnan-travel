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
public class EstimateVO {

    private Integer distance;       // 米
    private Integer duration;       // 秒

    private BigDecimal basePrice;   // 基础价格
    private BigDecimal surgeFactor; // 动态加价系数
    private BigDecimal surgeAmount; // 加价金额

    private BigDecimal couponDiscount; // 可用优惠券抵扣
    private BigDecimal estimateTotal;  // 预估总价

    private String carTypeName;     // 车型名称

    /* 阶梯计价明细 */
    private PriceDetailVO priceDetail;
}
