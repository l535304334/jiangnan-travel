package com.jiangnan.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EstimateRequest {

    @NotBlank(message = "起点地址不能为空")
    private String startAddress;
    @NotNull(message = "起点纬度不能为空")
    private BigDecimal startLat;
    @NotNull(message = "起点经度不能为空")
    private BigDecimal startLng;

    @NotBlank(message = "终点地址不能为空")
    private String endAddress;
    @NotNull(message = "终点纬度不能为空")
    private BigDecimal endLat;
    @NotNull(message = "终点经度不能为空")
    private BigDecimal endLng;

    private Integer distance; // 米，若传了则跳过路径规划
    private Integer duration; // 秒，若传了则跳过路径规划
    private Long carTypeId;   // 车型ID，默认快车
}
