package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.AdminLoginRequest;
import com.jiangnan.travel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理员登录", description = "管理后台登录")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理后台账号密码登录")
    public Result<?> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.ok(adminService.login(request.getUsername(), request.getPassword()));
    }
}
