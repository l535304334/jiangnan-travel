package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.LoginRequest;
import com.jiangnan.travel.dto.PasswordLoginRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        userService.sendCode(phone);
        return Result.ok("验证码已发送");
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    @PostMapping("/login-password")
    public Result<?> passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return Result.ok(userService.passwordLogin(request));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(userService.register(request));
    }

    @GetMapping("/profile")
    public Result<User> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody Map<String, String> body,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.updateProfile(userId,
                body.get("nickname"),
                body.get("avatar"));
        return Result.ok(user);
    }
}
