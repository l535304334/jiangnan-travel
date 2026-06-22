package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.DispatchService;
import com.jiangnan.travel.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/driver/order")
@RequiredArgsConstructor
public class DriverOrderController {

    private final OrderService orderService;
    private final DispatchService dispatchService;

    @GetMapping("/nearby")
    public Result<?> nearby(@RequestParam Long orderId, @RequestParam(defaultValue = "5") int limit) {
        return Result.ok(dispatchService.findNearbyDrivers(orderId, limit));
    }

    @PostMapping("/{id}/accept")
    public Result<?> accept(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long driverId = body.get("driverId");
        return Result.ok(orderService.accept(id, driverId));
    }

    @PutMapping("/{id}/arrive")
    public Result<?> arrive(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return Result.ok(orderService.arrive(id, body.get("driverId")));
    }

    @PutMapping("/{id}/start")
    public Result<?> start(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return Result.ok(orderService.startTrip(id, body.get("driverId")));
    }

    @PutMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return Result.ok(orderService.complete(id, body.get("driverId")));
    }

    @GetMapping("/best/{orderId}")
    public Result<?> bestDriver(@PathVariable Long orderId) {
        return Result.ok(dispatchService.findBestDriver(orderId));
    }
}
