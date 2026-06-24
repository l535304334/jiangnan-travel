package com.jiangnan.travel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "价格预估结果")
public class EstimateVO {

    @Schema(description = "距离(米)")
    private Integer distance;
    @Schema(description = "时长(秒)")
    private Integer duration;

    @Schema(description = "基础价格")
    private BigDecimal basePrice;
    @Schema(description = "动态加价系数")
    private BigDecimal surgeFactor;
    @Schema(description = "加价金额")
    private BigDecimal surgeAmount;

    @Schema(description = "可用优惠券抵扣")
    private BigDecimal couponDiscount;
    @Schema(description = "预估总价")
    private BigDecimal estimateTotal;

    @Schema(description = "车型名称")
    private String carTypeName;

    @Schema(description = "阶梯计价明细")
    private PriceDetailVO priceDetail;
}
