package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@Tag(name = "司机收入", description = "司机收入统计查询")
public class DriverEarningController {

    private final OrderMapper orderMapper;
    private final DriverMapper driverMapper;

    @GetMapping("/earning")
    @Operation(summary = "收入统计", description = "查询司机收入统计数据")
    public Result<Map<String, Object>> earning(@RequestParam Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) {
            return Result.fail("司机不存在");
        }

        List<Order> todayCompletedOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDriverId, driverId)
                        .eq(Order::getStatus, 4)
                        .apply("DATE(create_time) = CURDATE()"));

        long todayOrders = todayCompletedOrders.size();

        BigDecimal todayEarnings = todayCompletedOrders.stream()
                .map(o -> o.getFinalPrice() != null ? o.getFinalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer onlineMinutes = driver.getOnlineDuration() != null
                ? driver.getOnlineDuration() : 0;

        Integer totalOrders = driver.getTotalOrders() != null
                ? driver.getTotalOrders() : 0;

        BigDecimal avgRating = driver.getAvgRating() != null
                ? driver.getAvgRating() : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("todayOrders", todayOrders);
        result.put("todayEarnings", todayEarnings);
        result.put("onlineMinutes", onlineMinutes);
        result.put("totalOrders", totalOrders);
        result.put("avgRating", avgRating);

        return Result.ok(result);
    }
}
