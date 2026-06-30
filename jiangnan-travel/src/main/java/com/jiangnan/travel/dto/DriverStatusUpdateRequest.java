package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "司机状态更新请求")
public class DriverStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-离线，1-在线，2-接单中", example = "1")
    private Integer status;
}
