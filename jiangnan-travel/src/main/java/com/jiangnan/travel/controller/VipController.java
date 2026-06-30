package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.PurchaseVipRequest;
import com.jiangnan.travel.service.VipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
@Tag(name = "VIP会员", description = "用户端VIP等级浏览和购买")
public class VipController {

    private final VipService vipService;

    @GetMapping("/levels")
    @Operation(summary = "VIP等级列表", description = "获取所有启用的VIP等级")
    public Result<?> levels() {
        return Result.ok(vipService.listLevels());
    }

    @GetMapping("/benefits")
    @Operation(summary = "VIP权益", description = "获取VIP权益说明")
    public Result<?> benefits() {
        return Result.ok(vipService.listLevels());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "我的VIP", description = "获取当前用户的VIP信息")
    public Result<?> myVip(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(vipService.getMyVip(userId));
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "购买/续费VIP", description = "购买或续费VIP会员")
    public Result<?> purchase(@Valid @RequestBody PurchaseVipRequest request,
                              Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        vipService.purchase(userId, request.getLevelId(), request.getFeeType());
        return Result.ok("购买成功");
    }
}
