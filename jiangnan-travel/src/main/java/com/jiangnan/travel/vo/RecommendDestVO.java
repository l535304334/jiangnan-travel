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
@Schema(description = "推荐目的地")
public class RecommendDestVO {

    @Schema(description = "地址")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal lat;
    @Schema(description = "经度")
    private BigDecimal lng;
    @Schema(description = "订单数量")
    private Integer orderCount;
    @Schema(description = "时间段")
    private String timeSlot;
}
