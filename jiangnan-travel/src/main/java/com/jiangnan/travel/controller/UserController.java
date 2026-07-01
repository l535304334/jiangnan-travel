package com.jiangnan.travel.controller;

import com.jiangnan.travel.annotation.LogOperation;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.*;
import com.jiangnan.travel.security.TokenBlacklistService;
import com.jiangnan.travel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "用户管理", description = "用户注册、登录、个人信息管理")
public class UserController {

    private final UserService userService;
    private final TokenBlacklistService blacklistService;

    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "向指定手机号发送登录验证码")
    public Result<?> sendCode(@Valid @RequestBody SendCodeRequest request) {
        userService.sendCode(request.getPhone());
        return Result.ok("验证码已发送");
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(summary = "验证码登录", description = "使用手机号+验证码登录")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    @PostMapping("/login-password")
    @PreAuthorize("permitAll()")
    @Operation(summary = "密码登录", description = "使用手机号+密码登录")
    public Result<?> passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return Result.ok(userService.passwordLogin(request));
    }

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(userService.register(request));
    }

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    public Result<?> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    @LogOperation("修改个人信息")
    @Operation(summary = "修改个人信息", description = "更新当前登录用户的个人信息（昵称/头像/手机号）")
    public Result<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.updateProfile(userId,
                request.getNickname(),
                request.getAvatar(),
                request.getPhone()));
    }

    @PutMapping("/password")
    @LogOperation(value = "修改登录密码", saveRequestParams = false)
    @Operation(summary = "修改登录密码", description = "验证旧密码并更新登录密码，同时使当前 token 失效")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                     Authentication authentication,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
        blacklistCurrentToken(httpRequest);
        return Result.ok("密码修改成功，请重新登录");
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "登出并使当前 token 失效")
    public Result<?> logout(HttpServletRequest request) {
        blacklistCurrentToken(request);
        return Result.ok("已登出");
    }

    /** 从 Authorization header 提取 token 并加入黑名单 */
    private void blacklistCurrentToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            blacklistService.blacklist(bearer.substring(7));
        }
    }
}
