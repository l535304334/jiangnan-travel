package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.TravelInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "文旅数据", description = "文旅融合数据接口")
public class AIDataController {

    private final TravelInsightService travelInsightService;

    @GetMapping("/ai/hotspots")
    @Operation(summary = "热门景点", description = "基于近期 completed 订单热度返回热门景点")
    public Result<?> hotspots() {
        return Result.ok(travelInsightService.hotspots());
    }

    @GetMapping("/common/city-quote")
    @Operation(summary = "文化短句", description = "获取城市文化短句")
    public Result<?> cityQuotes() {
        return Result.ok(travelInsightService.cityQuotes());
    }

    @GetMapping("/user/frequent-routes")
    @Operation(summary = "常走路线", description = "获取用户常走路线")
    public Result<?> frequentRoutes(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(travelInsightService.frequentRoutes(userId));
    }
}
