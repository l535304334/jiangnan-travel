package com.jiangnan.travel.controller;

import com.jiangnan.travel.annotation.LogOperation;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.DispatchService;
import com.jiangnan.travel.service.DriverService;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/driver/order")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Tag(name = "司机订单", description = "司机接单、行程管理")
public class DriverOrderController {

    private final OrderService orderService;
    private final DispatchService dispatchService;
    private final DriverService driverService;

    @GetMapping("/nearby")
    @PreAuthorize("permitAll()")
    @Operation(summary = "附近订单", description = "获取司机附近的待接订单")
    public Result<?> nearby(@RequestParam BigDecimal lat,
                            @RequestParam BigDecimal lng,
                            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(orderService.findNearbyOrders(lat, lng, limit));
    }

    @PostMapping("/{id}/accept")
    @LogOperation("司机接单")
    @Operation(summary = "接单", description = "司机接单")
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
    @Operation(summary = "开始行程", description = "司机开始行程")
    public Result<?> start(@PathVariable Long id, Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.startTrip(id, driverId));
    }

    @PutMapping("/{id}/complete")
    @LogOperation("司机完成行程")
    @Operation(summary = "完成行程", description = "司机完成行程")
    public Result<?> complete(@PathVariable Long id, Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.complete(id, driverId));
    }

    @GetMapping("/best/{orderId}")
    @Operation(summary = "最佳司机", description = "为指定订单匹配最佳司机")
    public Result<?> bestDriver(@PathVariable Long orderId) {
        return Result.ok(dispatchService.findBestDriver(orderId));
    }

    @GetMapping("/pending")
    @Operation(summary = "待接订单列表", description = "获取司机当前的待接订单")
    public Result<List<OrderVO>> pending(Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.listByDriver(driverId, null, 1, 20));
    }

    @GetMapping("/history")
    @Operation(summary = "历史订单列表", description = "获取司机的历史订单（已完成/已取消）")
    public Result<List<OrderVO>> history(@RequestParam(defaultValue = "4") Integer status,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "20") Integer pageSize,
                                         Authentication authentication) {
        Long driverId = getDriverIdFromAuth(authentication);
        return Result.ok(orderService.listByDriver(driverId, status, page, pageSize));
    }

    private Long getDriverIdFromAuth(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return driverService.getDriverIdByUserId(userId);
    }
}
