package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "司机登录请求")
public class DriverLoginRequest {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13900001111")
    private String phone;

    @Schema(description = "验证码（暂未启用）", example = "123456")
    private String code;
}
