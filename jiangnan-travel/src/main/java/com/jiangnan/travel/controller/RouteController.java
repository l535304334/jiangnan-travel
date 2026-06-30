package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.AmapRouteService;
import com.jiangnan.travel.vo.RoutePlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
@Tag(name = "路线规划", description = "高德地图实时路线规划")
public class RouteController {

    private final AmapRouteService amapRouteService;

    @GetMapping("/plan")
    @Operation(summary = "路线规划", description = "调用高德驾车路径规划API获取实时路线")
    public Result<RoutePlanVO> planRoute(
            @RequestParam double originLng,
            @RequestParam double originLat,
            @RequestParam double destLng,
            @RequestParam double destLat) {
        return Result.ok(amapRouteService.planRoute(originLng, originLat, destLng, destLat));
    }
}
