package com.jiangnan.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

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

    @NotNull(message = "预估距离不能为空")
    private Integer distance;
    @NotNull(message = "预估时长不能为空")
    private Integer duration;

    private Long carTypeId;
    private Long couponId;       // 选择使用的优惠券
    private String idempotentKey; // 幂等键（客户端生成）
}
