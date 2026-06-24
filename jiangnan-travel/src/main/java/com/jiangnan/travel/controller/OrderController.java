package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.CancelOrderRequest;
import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.dto.EstimateRequest;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.service.PricingService;
import com.jiangnan.travel.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "打车订单的创建、查询、支付、取消")
public class OrderController {

    private final OrderService orderService;
    private final PricingService pricingService;

    @PostMapping("/estimate")
    @Operation(summary = "预估价格", description = "预估行程距离/时长/费用")
    public Result<?> estimate(@Valid @RequestBody EstimateRequest request) {
        return Result.ok(pricingService.estimate(request));
    }

    @PostMapping("/create")
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderRequest request,
                                   Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(orderService.create(request, userId));
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表", description = "分页查询用户订单列表")
    public Result<List<OrderVO>> list(@RequestParam(required = false) Integer status,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(orderService.listByUser(userId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(orderService.getById(id, userId));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消指定订单")
    public Result<?> cancel(@PathVariable Long id,
                             @RequestBody CancelOrderRequest request,
                             Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.cancel(id, userId, request.getReason());
        return Result.ok("已取消");
    }

    @GetMapping("/{id}/share")
    @Operation(summary = "行程分享", description = "分享行程信息")
    public Result<Map<String, String>> share(@PathVariable Long id) {
        return Result.ok(Map.of("shareUrl",
                "https://travel.jiangnan.com/s/" + id,
                "shareText", "我正在使用江南出行，点击查看我的行程"));
    }

    @PostMapping("/{id}/reorder")
    public Result<OrderVO> reorder(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        OrderVO old = orderService.getById(id, userId);
        var req = new com.jiangnan.travel.dto.CreateOrderRequest();
        req.setStartAddress(old.getStartAddress()); req.setStartLat(old.getStartLat()); req.setStartLng(old.getStartLng());
        req.setEndAddress(old.getEndAddress()); req.setEndLat(old.getEndLat()); req.setEndLng(old.getEndLng());
        req.setDistance(old.getDistance()); req.setDuration(old.getDuration());
        req.setCarTypeId(1L);
        return Result.ok(orderService.create(req, userId));
    }

    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.pay(id, userId);
        return Result.ok("支付成功");
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "评价订单", description = "对已完成的订单进行评价")
    public Result<?> review(@PathVariable Long id,
                             @RequestBody Map<String, Object> body,
                             Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Integer rating = body.get("rating") != null ? ((Number) body.get("rating")).intValue() : null;
        String tags = (String) body.get("tags");
        String content = (String) body.get("content");
        orderService.review(id, userId, rating, tags, content);
        return Result.ok("评价成功");
    }
}
