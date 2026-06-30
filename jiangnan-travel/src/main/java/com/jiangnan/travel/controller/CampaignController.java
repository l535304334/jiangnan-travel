package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaign")
@RequiredArgsConstructor
@Tag(name = "活动系统", description = "用户端活动浏览和领券")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping("/list")
    @Operation(summary = "活动列表", description = "获取当前可参与的活动列表")
    public Result<?> list() {
        return Result.ok(campaignService.listActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "活动详情", description = "获取活动详情（含关联优惠券）")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(campaignService.getDetail(id));
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "领券", description = "领取活动的所有关联优惠券")
    public Result<?> claim(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        campaignService.claimCoupons(userId, id);
        return Result.ok("领取成功");
    }
}
