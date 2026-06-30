package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新车型定价请求")
public class UpdateCarTypeRequest {

    @Schema(description = "车型名称", example = "经济型")
    private String name;

    @Schema(description = "起步价", example = "10.00")
    private BigDecimal basePrice;

    @Schema(description = "中等距离每公里价格", example = "2.50")
    private BigDecimal midPerKm;

    @Schema(description = "长距离每公里价格", example = "2.00")
    private BigDecimal longPerKm;

    @Schema(description = "超长距离每公里价格", example = "1.50")
    private BigDecimal superLongPerKm;

    @Schema(description = "每分钟价格", example = "0.50")
    private BigDecimal perMinPrice;

    @Schema(description = "最大载客数", example = "4")
    private Integer maxPassengers;

    @Schema(description = "状态：0-停用，1-启用", example = "1")
    private Integer status;
}
