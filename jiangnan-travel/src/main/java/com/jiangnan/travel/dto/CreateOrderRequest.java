package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotBlank(message = "起点地址不能为空")
    @Schema(description = "起点地址")
    private String startAddress;
    @NotNull(message = "起点纬度不能为空")
    @Schema(description = "起点纬度")
    private BigDecimal startLat;
    @NotNull(message = "起点经度不能为空")
    @Schema(description = "起点经度")
    private BigDecimal startLng;

    @NotBlank(message = "终点地址不能为空")
    @Schema(description = "终点地址")
    private String endAddress;
    @NotNull(message = "终点纬度不能为空")
    @Schema(description = "终点纬度")
    private BigDecimal endLat;
    @NotNull(message = "终点经度不能为空")
    @Schema(description = "终点经度")
    private BigDecimal endLng;

    @NotNull(message = "预估距离不能为空")
    @Positive(message = "距离必须为正数")
    @Schema(description = "距离(米)")
    private Integer distance;
    @NotNull(message = "预估时长不能为空")
    @Positive(message = "时长必须为正数")
    @Schema(description = "时长(秒)")
    private Integer duration;

    @Schema(description = "车型ID")
    private Long carTypeId;
    @Schema(description = "优惠券ID")
    private Long couponId;
    @Schema(description = "幂等键")
    private String idempotentKey;
}
