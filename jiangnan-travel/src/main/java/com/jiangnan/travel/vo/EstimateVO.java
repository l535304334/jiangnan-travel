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

    @Schema(description = "距离(米)", example = "5000")
    private Integer distance;
    @Schema(description = "时长(秒)", example = "600")
    private Integer duration;

    @Schema(description = "基础价格", example = "14.60")
    private BigDecimal basePrice;
    @Schema(description = "动态加价系数", example = "1.20")
    private BigDecimal surgeFactor;
    @Schema(description = "加价金额", example = "2.92")
    private BigDecimal surgeAmount;

    @Schema(description = "可用优惠券抵扣", example = "2.00")
    private BigDecimal couponDiscount;
    @Schema(description = "预估总价", example = "17.52")
    private BigDecimal estimateTotal;

    @Schema(description = "车型名称", example = "经济型")
    private String carTypeName;

    @Schema(description = "阶梯计价明细", example = "null")
    private PriceDetailVO priceDetail;

    @Schema(description = "行程类型 0=短途 1=长途", example = "0")
    private Integer tripType;
    @Schema(description = "行程类型名称", example = "短途出行")
    private String tripTypeName;
    @Schema(description = "路线规划详情")
    private RoutePlanVO routePlan;
}
