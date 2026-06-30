package com.jiangnan.travel.controller;

import com.jiangnan.travel.annotation.LogOperation;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.DriverLoginRequest;
import com.jiangnan.travel.dto.DriverLocationUpdateRequest;
import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.dto.DriverStatusUpdateRequest;
import com.jiangnan.travel.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "司机管理", description = "司机注册、登录、状态管理")
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(summary = "司机登录", description = "司机登录")
    public Result<?> login(@Valid @RequestBody DriverLoginRequest request) {
        return Result.ok(driverService.login(request.getPhone()));
    }

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    @Operation(summary = "司机注册", description = "注册新司机")
    public Result<?> register(@Valid @RequestBody DriverRegisterRequest request) {
        driverService.register(request);
        return Result.ok("提交成功，等待审核");
    }

    @PutMapping("/status")
    @LogOperation("修改司机状态")
    @Operation(summary = "更新状态", description = "更新司机在线状态")
    public Result<?> updateStatus(@Valid @RequestBody DriverStatusUpdateRequest request,
                                  Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        driverService.updateStatus(driverId, request.getStatus());
        return Result.ok();
    }

    @PutMapping("/location")
    @LogOperation("更新司机位置")
    @Operation(summary = "更新位置", description = "更新司机当前位置坐标")
    public Result<?> updateLocation(@Valid @RequestBody DriverLocationUpdateRequest request,
                                     Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        driverService.updateLocation(driverId, request.getLat(), request.getLng());
        return Result.ok();
    }

    @GetMapping("/profile")
    @Operation(summary = "获取信息", description = "获取当前司机的详细信息")
    public Result<?> profile(Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(driverService.getProfile(driverId));
    }

    private Long getDriverIdFromAuth(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return driverService.getDriverIdByUserId(userId);
    }
}
