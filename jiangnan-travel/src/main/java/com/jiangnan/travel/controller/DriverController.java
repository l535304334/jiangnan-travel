package com.jiangnan.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.DriverLocationUpdateRequest;
import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@Tag(name = "司机管理", description = "司机注册、登录、状态管理")
public class DriverController {

    private final DriverService driverService;
    private final DriverMapper driverMapper;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        return Result.ok(driverService.login(body.get("phone")));
    }

    @PostMapping("/register")
    @Operation(summary = "司机注册", description = "注册新司机")
    public Result<?> register(@Valid @RequestBody DriverRegisterRequest request) {
        driverService.register(request);
        return Result.ok("提交成功，等待审核");
    }

    @PutMapping("/status")
    public Result<?> updateStatus(@RequestParam Integer status, Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        driverService.updateStatus(driverId, status);
        return Result.ok();
    }

    @PutMapping("/location")
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
        Driver driver = driverMapper.selectOne(
                new LambdaQueryWrapper<Driver>().eq(Driver::getUserId, userId));
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
        return driver.getId();
    }
}
