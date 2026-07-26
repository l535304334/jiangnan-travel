package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.entity.CityLandmark;
import com.jiangnan.travel.entity.CityQuote;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.enums.OrderStatus;
import com.jiangnan.travel.mapper.CityLandmarkMapper;
import com.jiangnan.travel.mapper.CityQuoteMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.service.TravelInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TravelInsightServiceImpl implements TravelInsightService {

    private final CityQuoteMapper cityQuoteMapper;
    private final OrderMapper orderMapper;
    private final CityLandmarkMapper cityLandmarkMapper;

    /** 热度统计的时间窗口（天）。 */
    private static final int HOT_WINDOW_DAYS = 30;
    /** 订单终点距地标多少米以内计入热度。 */
    private static final double NEARBY_RADIUS_METERS = 5000;
    /** 常走路线返回条数。 */
    private static final int FREQUENT_ROUTE_LIMIT = 2;

    @Override
    public List<Map<String, Object>> hotspots() {
        List<CityLandmark> landmarks = cityLandmarkMapper.selectList(
                new LambdaQueryWrapper<CityLandmark>()
                        .eq(CityLandmark::getStatus, 1)
                        .orderByAsc(CityLandmark::getSort));
        if (landmarks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Order> completedOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, OrderStatus.COMPLETED.getCode())
                        .ge(Order::getCreateTime, LocalDateTime.now().minusDays(HOT_WINDOW_DAYS)));

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
        return spots;
    }

    @Override
    public List<CityQuote> cityQuotes() {
        return cityQuoteMapper.selectList(
                new LambdaQueryWrapper<CityQuote>()
                        .eq(CityQuote::getStatus, 1)
                        .orderByAsc(CityQuote::getSort));
    }

    @Override
    public List<Map<String, Object>> frequentRoutes(Long userId) {
        LocalDateTime windowStart = LocalDateTime.now().minusDays(HOT_WINDOW_DAYS);
        List<Order> recentOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .ge(Order::getCreateTime, windowStart));

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

        return routeCount.values().stream()
                .sorted((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")))
                .limit(FREQUENT_ROUTE_LIMIT)
                .toList();
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
            if (distance <= NEARBY_RADIUS_METERS) {
                count++;
            }
        }
        return count;
    }

    /** 球面距离（米）。 */
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
