package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.ok(couponService.listAvailable());
    }

    @GetMapping("/my")
    public Result<?> myCoupons(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(couponService.listUserCoupons(userId));
    }

    @PostMapping("/claim")
    public Result<?> claim(@RequestBody Map<String, Long> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        couponService.claim(userId, body.get("couponId"));
        return Result.ok("领取成功");
    }
}
