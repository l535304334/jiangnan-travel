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

    @Schema(description = "地址", example = "江南大学")
    private String address;
    @Schema(description = "纬度", example = "31.2304")
    private BigDecimal lat;
    @Schema(description = "经度", example = "121.4737")
    private BigDecimal lng;
    @Schema(description = "订单数量", example = "150")
    private Integer orderCount;
    @Schema(description = "时间段", example = "晚高峰")
    private String timeSlot;
}
