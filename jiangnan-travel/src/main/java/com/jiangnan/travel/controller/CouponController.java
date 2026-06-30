package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.ClaimCouponRequest;
import com.jiangnan.travel.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Tag(name = "优惠券", description = "优惠券查询和领取")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/list")
    @Operation(summary = "优惠券列表", description = "获取所有可领取优惠券")
    public Result<?> list() {
        return Result.ok(couponService.listAvailable());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "我的优惠券", description = "获取用户已领取的优惠券")
    public Result<?> myCoupons(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(couponService.listUserCoupons(userId));
    }

    @PostMapping("/claim")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "领取优惠券", description = "用户领取优惠券")
    public Result<?> claim(@Valid @RequestBody ClaimCouponRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        couponService.claim(userId, request.getCouponId());
        return Result.ok("领取成功");
    }
}
