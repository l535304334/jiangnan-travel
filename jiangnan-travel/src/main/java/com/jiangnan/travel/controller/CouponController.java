package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Tag(name = "优惠券", description = "优惠券查询和领取")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.ok(couponService.listAvailable());
    }

    @GetMapping("/my")
    @Operation(summary = "我的优惠券", description = "获取用户已领取的优惠券")
    public Result<?> myCoupons(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(couponService.listUserCoupons(userId));
    }

    @PostMapping("/claim")
    @Operation(summary = "领取优惠券", description = "用户领取优惠券")
    public Result<?> claim(@RequestBody Map<String, Long> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        couponService.claim(userId, body.get("couponId"));
        return Result.ok("领取成功");
    }
}
