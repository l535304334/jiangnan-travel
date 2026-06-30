package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新用户状态请求")
public class UpdateUserStatusRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
