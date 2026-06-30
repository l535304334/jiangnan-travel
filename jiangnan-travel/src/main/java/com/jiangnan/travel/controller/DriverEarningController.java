package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "司机收入", description = "司机收入统计查询")
public class DriverEarningController {

    private final DriverService driverService;

    @GetMapping("/earning")
    @Operation(summary = "收入统计", description = "查询司机收入统计数据")
    public Result<Map<String, Object>> earning(Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(driverService.getEarningStats(driverId));
    }

    @GetMapping("/earning/weekly")
    @Operation(summary = "周收入汇总", description = "查询司机本周每日收入")
    public Result<Map<String, Object>> weekly(Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(driverService.getWeeklyEarnings(driverId));
    }

    private Long getDriverIdFromAuth(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return driverService.getDriverIdByUserId(userId);
    }
}
