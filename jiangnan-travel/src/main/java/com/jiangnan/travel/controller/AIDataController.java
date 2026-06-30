package com.jiangnan.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.*;
import com.jiangnan.travel.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "文旅数据", description = "文旅融合数据接口")
public class AIDataController {

    private final CityQuoteMapper cityQuoteMapper;
    private final OrderMapper orderMapper;
    private final CityLandmarkMapper cityLandmarkMapper;

    @GetMapping("/ai/hotspots")
    @Operation(summary = "热门景点", description = "基于近期 completed 订单热度返回热门景点")
    public Result<?> hotspots() {
        // 以城市地标作为热门景点，按实际订单热度排序
        List<CityLandmark> landmarks = cityLandmarkMapper.selectList(
                new LambdaQueryWrapper<CityLandmark>()
                        .eq(CityLandmark::getStatus, 1)
                        .orderByAsc(CityLandmark::getSort));
        if (landmarks.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        List<Order> completedOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 4)
                        .ge(Order::getCreateTime, LocalDateTime.now().minusDays(30)));

        List<Map<String, Object>> spots = new ArrayList<>();
        for (CityLandmark landmark : landmarks) {
            int count = countNearbyOrders(landmark, completedOrders);
            Map<String, Object> spot = new HashMap<>();
            spot.put("address", landmark.getName());
            spot.put("name", landmark.getName());
            spot.put("city", landmark.getCity());
            spot.put("lat", landmark.getLat());
            spot.put("lng", landmark.getLng());
            spot.put("count", count);
            spot.put("orderCount", count);
            spots.add(spot);
        }

        spots.sort((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")));
        return Result.ok(spots);
    }

    private int countNearbyOrders(CityLandmark landmark, List<Order> orders) {
        if (landmark.getLat() == null || landmark.getLng() == null) {
            return 0;
        }
        double landmarkLat = landmark.getLat().doubleValue();
        double landmarkLng = landmark.getLng().doubleValue();
        int count = 0;
        for (Order order : orders) {
            if (order.getEndLat() == null || order.getEndLng() == null) {
                continue;
            }
            double distance = haversine(landmarkLat, landmarkLng,
                    order.getEndLat().doubleValue(), order.getEndLng().doubleValue());
            if (distance <= 5000) {
                count++;
            }
        }
        return count;
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @GetMapping("/common/city-quote")
    @Operation(summary = "文化短句", description = "获取城市文化短句")
    public Result<?> cityQuotes() {
        List<CityQuote> quotes = cityQuoteMapper.selectList(
                new LambdaQueryWrapper<CityQuote>()
                        .eq(CityQuote::getStatus, 1)
                        .orderByAsc(CityQuote::getSort));
        return Result.ok(quotes);
    }

    @GetMapping("/user/frequent-routes")
    @Operation(summary = "常走路线", description = "获取用户常走路线")
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
