package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        return Result.ok(driverService.login(body.get("phone")));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody DriverRegisterRequest request) {
        driverService.register(request);
        return Result.ok("提交成功，等待审核");
    }

    @PutMapping("/status")
    public Result<?> updateStatus(@RequestBody Map<String, Object> body) {
        Long driverId = Long.valueOf(body.get("driverId").toString());
        Integer status = ((Number) body.get("status")).intValue();
        driverService.updateStatus(driverId, status);
        return Result.ok();
    }

    @PutMapping("/location")
    public Result<?> updateLocation(@RequestBody Map<String, Object> body) {
        Long driverId = Long.valueOf(body.get("driverId").toString());
        BigDecimal lat = new BigDecimal(body.get("lat").toString());
        BigDecimal lng = new BigDecimal(body.get("lng").toString());
        driverService.updateLocation(driverId, lat, lng);
        return Result.ok();
    }

    @GetMapping("/profile")
    public Result<?> profile(@RequestParam Long driverId) {
        return Result.ok(driverService.getProfile(driverId));
    }
}
