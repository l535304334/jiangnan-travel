package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.LoginRequest;
import com.jiangnan.travel.dto.PasswordLoginRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、个人信息管理")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "向指定手机号发送登录验证码")
    public Result<?> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        userService.sendCode(phone);
        return Result.ok("验证码已发送");
    }

    @PostMapping("/login")
    @Operation(summary = "验证码登录", description = "使用手机号+验证码登录")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    @PostMapping("/login-password")
    @Operation(summary = "密码登录", description = "使用手机号+密码登录")
    public Result<?> passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return Result.ok(userService.passwordLogin(request));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(userService.register(request));
    }

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    public Result<User> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "修改个人信息", description = "更新当前登录用户的个人信息")
    public Result<User> updateProfile(@RequestBody Map<String, String> body,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.updateProfile(userId,
                body.get("nickname"),
                body.get("avatar"));
        return Result.ok(user);
    }
}
