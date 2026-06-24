package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "司机注册请求")
public class DriverRegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13900001111")
    private String phone;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", example = "李四")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号", example = "360101199001011234")
    private String idCard;

    @NotBlank(message = "驾驶证号不能为空")
    @Schema(description = "驾驶证号", example = "360101199001011234")
    private String driverLicense;

    @NotBlank(message = "车牌号不能为空")
    @Schema(description = "车牌号", example = "赣A·88888")
    private String carPlate;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "123456")
    private String code;
}
