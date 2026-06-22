package com.jiangnan.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverRegisterRequest {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "驾驶证号不能为空")
    private String driverLicense;

    @NotBlank(message = "车牌号不能为空")
    private String carPlate;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
