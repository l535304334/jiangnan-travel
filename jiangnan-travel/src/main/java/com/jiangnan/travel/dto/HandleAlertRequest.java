package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "处理告警请求")
public class HandleAlertRequest {

    @NotNull(message = "告警ID不能为空")
    @Schema(description = "告警ID", example = "1")
    private Long id;

    @Schema(description = "处理备注", example = "已核实，误报")
    private String handleRemark;
}
