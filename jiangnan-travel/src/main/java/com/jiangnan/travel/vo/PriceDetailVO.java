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
@Schema(description = "阶梯计价明细")
public class PriceDetailVO {

    @Schema(description = "起步价（含3公里）", example = "8.00")
    private BigDecimal basePrice;
    @Schema(description = "起步价说明", example = "含3公里")
    private String baseDesc;

    @Schema(description = "中程里程(公里)", example = "3")
    private BigDecimal midDistance;
    @Schema(description = "中程费用", example = "5.40")
    private BigDecimal midPrice;

    @Schema(description = "远程里程(公里)", example = "8")
    private BigDecimal longDistance;
    @Schema(description = "远程费用", example = "8.00")
    private BigDecimal longPrice;

    @Schema(description = "超远程里程(公里)", example = "13")
    private BigDecimal superLongDistance;
    @Schema(description = "超远程费用", example = "7.00")
    private BigDecimal superLongPrice;

    @Schema(description = "时长费", example = "3.00")
    private BigDecimal timeFee;

    @Schema(description = "小计（加价前）", example = "31.40")
    private BigDecimal subtotal;
    @Schema(description = "加价系数", example = "1.20")
    private BigDecimal surgeFactor;
    @Schema(description = "加价金额", example = "6.28")
    private BigDecimal surgePrice;

    @Schema(description = "过路费", example = "0.00")
    private BigDecimal tollFee;
    @Schema(description = "优惠券抵扣", example = "2.00")
    private BigDecimal couponDiscount;
    @Schema(description = "VIP折扣率", example = "0.95")
    private BigDecimal vipDiscount;
    @Schema(description = "VIP折扣金额", example = "1.68")
    private BigDecimal vipDiscountAmount;
    @Schema(description = "最终预估价格", example = "35.68")
    private BigDecimal totalPrice;
}
