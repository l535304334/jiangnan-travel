package com.jiangnan.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.*;
import com.jiangnan.travel.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AIDataController {

    private final DemandHotspotMapper demandHotspotMapper;
    private final CityQuoteMapper cityQuoteMapper;
    private final OrderMapper orderMapper;

    @GetMapping("/api/ai/hotspots")
    public Result<?> hotspots() {
        List<DemandHotspot> hotspots = demandHotspotMapper.selectList(
                new LambdaQueryWrapper<DemandHotspot>()
                        .orderByDesc(DemandHotspot::getDemandCount));
        if (hotspots.isEmpty()) {
            List<Map<String, Object>> randomSpots = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < 5; i++) {
                double offsetLat = (random.nextDouble() - 0.5) * 0.1;
                double offsetLng = (random.nextDouble() - 0.5) * 0.1;
                BigDecimal lat = BigDecimal.valueOf(28.68 + offsetLat).setScale(6, RoundingMode.HALF_UP);
                BigDecimal lng = BigDecimal.valueOf(115.87 + offsetLng).setScale(6, RoundingMode.HALF_UP);
                randomSpots.add(Map.of(
                        "lat", lat,
                        "lng", lng,
                        "radius", 800,
                        "demandCount", random.nextInt(30) + 10
                ));
            }
            return Result.ok(randomSpots);
        }
        return Result.ok(hotspots);
    }

    @GetMapping("/api/common/city-quote")
    public Result<?> cityQuotes() {
        List<CityQuote> quotes = cityQuoteMapper.selectList(
                new LambdaQueryWrapper<CityQuote>()
                        .eq(CityQuote::getStatus, 1)
                        .orderByAsc(CityQuote::getSort));
        return Result.ok(quotes);
    }

    @GetMapping("/api/user/frequent-routes")
    public Result<?> frequentRoutes(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Order> recentOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .ge(Order::getCreateTime, thirtyDaysAgo));

        Map<String, Map<String, Object>> routeCount = new LinkedHashMap<>();
        for (Order order : recentOrders) {
            String start = order.getStartAddress();
            String end = order.getEndAddress();
            if (start == null || end == null) {
                continue;
            }
            String key = start + "→" + end;
            routeCount.compute(key, (k, v) -> {
                if (v == null) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("startAddress", start);
                    entry.put("endAddress", end);
                    entry.put("startLat", order.getStartLat());
                    entry.put("startLng", order.getStartLng());
                    entry.put("endLat", order.getEndLat());
                    entry.put("endLng", order.getEndLng());
                    entry.put("count", 1);
                    return entry;
                }
                v.put("count", (int) v.get("count") + 1);
                return v;
            });
        }

        List<Map<String, Object>> topRoutes = routeCount.values().stream()
                .sorted((a, b) -> Integer.compare(
                        (int) b.get("count"), (int) a.get("count")))
                .limit(2)
                .collect(Collectors.toList());

        return Result.ok(topRoutes);
    }
}
