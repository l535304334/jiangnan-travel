package com.jiangnan.travel.controller;

import com.jiangnan.travel.annotation.LogOperation;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.PaymentRequest;
import com.jiangnan.travel.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "支付管理", description = "订单支付、查询、回调")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @LogOperation("创建支付")
    @Operation(summary = "创建支付", description = "支付指定订单")
    public Result<?> create(@Valid @RequestBody PaymentRequest request,
                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String payMethod = request.getPayMethod() != null ? request.getPayMethod() : "balance";
        return Result.ok(paymentService.pay(request.getOrderId(), userId, payMethod, request.getIdempotentKey()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "支付查询", description = "查询订单支付信息")
    public Result<?> getPayment(@PathVariable Long orderId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(paymentService.getPaymentByOrder(orderId, userId));
    }

    @GetMapping("/list")
    @Operation(summary = "支付记录", description = "用户支付记录列表")
    public Result<?> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(paymentService.listByUser(userId));
    }

    @PostMapping("/callback")
    @PreAuthorize("permitAll()")
    @Operation(summary = "支付回调(模拟)")
    public Result<?> callback(@RequestBody Map<String, String> body) {
        String payNo = body.get("payNo");
        if (payNo == null) return Result.fail("payNo不能为空");
        return Result.ok(paymentService.mockCallback(payNo));
    }
}
