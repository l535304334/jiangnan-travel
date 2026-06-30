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

    @Schema(description = "JWT令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    @Schema(description = "手机号", example = "13900001111")
    private String phone;
    @Schema(description = "昵称", example = "测试用户")
    private String nickname;
    @Schema(description = "头像URL", example = "https://example.com/avatar.png")
    private String avatar;
}
