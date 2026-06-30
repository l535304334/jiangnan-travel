package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "审核司机请求")
public class VerifyDriverRequest {

    @NotNull(message = "司机ID不能为空")
    @Schema(description = "司机ID", example = "1")
    private Long driverId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-通过，2-拒绝", example = "1")
    private Integer status;

    @Schema(description = "审核备注", example = "资料齐全，审核通过")
    private String remark;
}
