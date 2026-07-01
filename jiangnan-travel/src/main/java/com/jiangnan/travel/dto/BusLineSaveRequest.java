package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "班线创建/更新请求")
public class BusLineSaveRequest {

    @NotBlank
    @Schema(description = "出发城市")
    private String startCity;

    @NotBlank
    @Schema(description = "目的城市")
    private String endCity;

    @NotBlank
    @Schema(description = "线路名称")
    private String lineName;

    @Schema(description = "车型")
    private String busType;

    @Schema(description = "行程时长(分钟)")
    private Integer duration;

    @Schema(description = "距离(公里)")
    private Integer distance;

    @NotNull @Positive
    @Schema(description = "票价")
    private BigDecimal price;

    @Schema(description = "状态 1启用 0禁用")
    private Integer status;
}
