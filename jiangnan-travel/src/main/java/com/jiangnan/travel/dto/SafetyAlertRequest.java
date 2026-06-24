package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "安全预警请求")
public class SafetyAlertRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "纬度", example = "31.2304")
    private BigDecimal lat;

    @Schema(description = "经度", example = "121.4737")
    private BigDecimal lng;
}
