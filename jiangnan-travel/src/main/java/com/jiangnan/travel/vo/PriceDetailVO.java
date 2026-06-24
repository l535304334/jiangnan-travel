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

    @Schema(description = "起步价（含3公里）")
    private BigDecimal basePrice;
    @Schema(description = "起步价说明")
    private String baseDesc;

    @Schema(description = "中程里程(公里)")
    private BigDecimal midDistance;
    @Schema(description = "中程费用")
    private BigDecimal midPrice;

    @Schema(description = "远程里程(公里)")
    private BigDecimal longDistance;
    @Schema(description = "远程费用")
    private BigDecimal longPrice;

    @Schema(description = "超远程里程(公里)")
    private BigDecimal superLongDistance;
    @Schema(description = "超远程费用")
    private BigDecimal superLongPrice;

    @Schema(description = "时长费")
    private BigDecimal timeFee;

    @Schema(description = "小计（加价前）")
    private BigDecimal subtotal;
    @Schema(description = "加价系数")
    private BigDecimal surgeFactor;
    @Schema(description = "加价金额")
    private BigDecimal surgePrice;

    @Schema(description = "过路费")
    private BigDecimal tollFee;
    @Schema(description = "优惠券抵扣")
    private BigDecimal couponDiscount;
    @Schema(description = "最终预估价格")
    private BigDecimal totalPrice;
}
