package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "价格预估请求")
public class EstimateRequest {

    @NotBlank(message = "起点地址不能为空")
    @Schema(description = "起点地址", example = "江南大学东门")
    private String startAddress;
    @NotNull(message = "起点纬度不能为空")
    @Schema(description = "起点纬度", example = "31.2304")
    private BigDecimal startLat;
    @NotNull(message = "起点经度不能为空")
    @Schema(description = "起点经度", example = "121.4737")
    private BigDecimal startLng;

    @NotBlank(message = "终点地址不能为空")
    @Schema(description = "终点地址", example = "无锡火车站")
    private String endAddress;
    @NotNull(message = "终点纬度不能为空")
    @Schema(description = "终点纬度", example = "31.2504")
    private BigDecimal endLat;
    @NotNull(message = "终点经度不能为空")
    @Schema(description = "终点经度", example = "121.4937")
    private BigDecimal endLng;

    @NotNull(message = "预估距离不能为空")
    @Positive(message = "距离必须为正数")
    @Schema(description = "距离(米)", example = "5000")
    private Integer distance;
    @NotNull(message = "预估时长不能为空")
    @Positive(message = "时长必须为正数")
    @Schema(description = "时长(秒)", example = "600")
    private Integer duration;
    @Schema(description = "车型ID", example = "1")
    private Long carTypeId;
    @Schema(description = "行程类型 0=短途 1=长途", example = "0")
    private Integer tripType;
    @Schema(description = "优惠券ID(可选)", example = "1")
    private Long couponId;
    @Schema(description = "用户ID（由后端填充）", hidden = true)
    private Long userId;
}
