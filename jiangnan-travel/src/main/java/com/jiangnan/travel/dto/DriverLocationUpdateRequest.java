package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "司机位置更新请求")
public class DriverLocationUpdateRequest {

    @NotNull(message = "纬度不能为空")
    @Schema(description = "纬度", example = "31.2304")
    private BigDecimal lat;

    @NotNull(message = "经度不能为空")
    @Schema(description = "经度", example = "121.4737")
    private BigDecimal lng;
}
