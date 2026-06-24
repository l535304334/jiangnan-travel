package com.jiangnan.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.service.DispatchService;
import com.jiangnan.travel.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/driver/order")
@RequiredArgsConstructor
@Tag(name = "司机订单", description = "司机接单、行程管理")
public class DriverOrderController {

    private final OrderService orderService;
    private final DispatchService dispatchService;
    private final DriverMapper driverMapper;

    @GetMapping("/nearby")
    public Result<?> nearby(@RequestParam Long orderId, @RequestParam(defaultValue = "5") int limit) {
        return Result.ok(dispatchService.findNearbyDrivers(orderId, limit));
    }

    @PostMapping("/{id}/accept")
    public Result<?> accept(@PathVariable Long id, Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.accept(id, driverId));
    }

    @PutMapping("/{id}/arrive")
    @Operation(summary = "到达", description = "司机到达上车点")
    public Result<?> arrive(@PathVariable Long id, Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.arrive(id, driverId));
    }

    @PutMapping("/{id}/start")
    public Result<?> start(@PathVariable Long id, Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.startTrip(id, driverId));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成行程", description = "司机完成行程")
    public Result<?> complete(@PathVariable Long id, Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.complete(id, driverId));
    }

    @GetMapping("/best/{orderId}")
    public Result<?> bestDriver(@PathVariable Long orderId) {
        return Result.ok(dispatchService.findBestDriver(orderId));
    }

    private Long getDriverIdFromAuth(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Driver driver = driverMapper.selectOne(
                new LambdaQueryWrapper<Driver>().eq(Driver::getUserId, userId));
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
        return driver.getId();
    }
}
