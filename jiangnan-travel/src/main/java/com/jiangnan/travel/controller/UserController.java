package com.jiangnan.travel.controller;

import com.jiangnan.travel.annotation.LogOperation;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.*;
import com.jiangnan.travel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "用户管理", description = "用户注册、登录、个人信息管理")
public class UserController {

    private final UserService userService;

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
    @LogOperation("修改登录密码")
    @Operation(summary = "修改登录密码", description = "验证旧密码并更新登录密码")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.ok("密码修改成功");
    }
}
