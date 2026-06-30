package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.BusLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bus-line")
@RequiredArgsConstructor
@Tag(name = "班线管理", description = "城际班线查询、购票")
public class BusLineController {

    private final BusLineService busLineService;

    @GetMapping("/list")
    @Operation(summary = "班线列表", description = "查询城际班线列表，支持按城市筛选")
    public Result<?> listLines(@RequestParam(required = false) String startCity,
                                @RequestParam(required = false) String endCity) {
        return Result.ok(busLineService.listLines(startCity, endCity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "班线详情", description = "查询班线详情含时刻表")
    public Result<?> lineDetail(@PathVariable Long id) {
        return Result.ok(busLineService.getLineDetail(id));
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "购票", description = "购买指定班次的车票")
    public Result<?> purchase(@RequestBody Map<String, Long> body,
                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long scheduleId = body.get("scheduleId");
        if (scheduleId == null) return Result.fail("scheduleId不能为空");
        return Result.ok(busLineService.purchaseTicket(scheduleId, userId));
    }
}
