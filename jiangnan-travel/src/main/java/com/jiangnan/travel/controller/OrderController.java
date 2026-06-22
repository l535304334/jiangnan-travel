package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.dto.EstimateRequest;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.service.PricingService;
import com.jiangnan.travel.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PricingService pricingService;

    @PostMapping("/estimate")
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
    public Result<?> cancel(@PathVariable Long id,
                             @RequestBody Map<String, String> body,
                             Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.cancel(id, userId, body.get("reason"));
        return Result.ok("已取消");
    }

    @GetMapping("/{id}/share")
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
