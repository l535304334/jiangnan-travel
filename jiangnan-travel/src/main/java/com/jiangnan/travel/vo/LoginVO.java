package com.jiangnan.travel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录返回结果")
public class LoginVO {

    @Schema(description = "JWT令牌")
    private String token;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像URL")
    private String avatar;
}
