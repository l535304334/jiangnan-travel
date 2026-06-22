package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.service.AiPredictionService;
import com.jiangnan.travel.vo.RecommendDestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiPredictionServiceImpl implements AiPredictionService {

    private final OrderMapper orderMapper;

    @Override
    public List<RecommendDestVO> recommendDestinations(Long userId) {
        // 查询用户过去30天的已完成订单
        List<Order> historyOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, 4) // 已完成
                        .ge(Order::getCreateTime, LocalDateTime.now().minusDays(30))
                        .orderByDesc(Order::getCreateTime));

        if (historyOrders.isEmpty()) {
            return getHotDestinations();
        }

        // 按目的地分组统计
        Map<String, List<Order>> grouped = historyOrders.stream()
                .collect(Collectors.groupingBy(Order::getEndAddress));

        // 确定当前时段
        String currentTimeSlot = getCurrentTimeSlot();

        // 转换为推荐VO，按次数排序，取 Top 5
        return grouped.entrySet().stream()
                .map(entry -> RecommendDestVO.builder()
                        .address(entry.getKey())
                        .lat(entry.getValue().get(0).getEndLat())
                        .lng(entry.getValue().get(0).getEndLng())
                        .orderCount(entry.getValue().size())
                        .timeSlot(getMostFrequentTimeSlot(entry.getValue()))
                        .build())
                .sorted((a, b) -> b.getOrderCount().compareTo(a.getOrderCount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<RecommendDestVO> getHotDestinations() {
        // 冷启动：返回平台热门目的地 Top 5
        List<Order> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 4)
                        .ge(Order::getCreateTime, LocalDateTime.now().minusDays(7))
                        .orderByDesc(Order::getCreateTime));

        Map<String, Long> destCount = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getEndAddress, Collectors.counting()));

        return destCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Order sample = allOrders.stream()
                            .filter(o -> o.getEndAddress().equals(entry.getKey()))
                            .findFirst().orElse(null);
                    return RecommendDestVO.builder()
                            .address(entry.getKey())
                            .lat(sample != null ? sample.getEndLat() : BigDecimal.ZERO)
                            .lng(sample != null ? sample.getEndLng() : BigDecimal.ZERO)
                            .orderCount(entry.getValue().intValue())
                            .timeSlot("全时段热门")
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getCurrentTimeSlot() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 12) return "早间";
        if (hour >= 12 && hour < 18) return "午后";
        if (hour >= 18 && hour < 22) return "傍晚";
        return "夜间";
    }

    private String getMostFrequentTimeSlot(List<Order> orders) {
        Map<String, Long> slotCount = new HashMap<>();
        for (Order o : orders) {
            if (o.getCreateTime() == null) continue;
            int hour = o.getCreateTime().getHour();
            String slot;
            if (hour >= 6 && hour < 12) slot = "早间";
            else if (hour >= 12 && hour < 18) slot = "午后";
            else if (hour >= 18 && hour < 22) slot = "傍晚";
            else slot = "夜间";
            slotCount.merge(slot, 1L, Long::sum);
        }
        return slotCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("全天");
    }
}
