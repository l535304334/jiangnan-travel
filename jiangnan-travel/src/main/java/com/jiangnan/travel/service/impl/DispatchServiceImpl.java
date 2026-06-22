package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.service.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private final DriverMapper driverMapper;
    private final OrderMapper orderMapper;

    @Override
    public Driver findBestDriver(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);

        // 查找附近在线司机
        double radius = 0.05; // ~5km
        List<Driver> candidates = driverMapper.selectList(
                new LambdaQueryWrapper<Driver>()
                        .eq(Driver::getStatus, 1)
                        .ge(Driver::getLat, order.getStartLat().subtract(BigDecimal.valueOf(radius)))
                        .le(Driver::getLat, order.getStartLat().add(BigDecimal.valueOf(radius)))
                        .ge(Driver::getLng, order.getStartLng().subtract(BigDecimal.valueOf(radius)))
                        .le(Driver::getLng, order.getStartLng().add(BigDecimal.valueOf(radius))));

        if (candidates.isEmpty()) {
            return null;
        }

        // 多因素加权评分
        return candidates.stream()
                .max(Comparator.comparingDouble(d -> scoreDriver(d, order)))
                .orElse(null);
    }

    @Override
    public List<Driver> findNearbyDrivers(Long orderId, int limit) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);

        double radius = 0.05;
        List<Driver> candidates = driverMapper.selectList(
                new LambdaQueryWrapper<Driver>()
                        .eq(Driver::getStatus, 1)
                        .ge(Driver::getLat, order.getStartLat().subtract(BigDecimal.valueOf(radius)))
                        .le(Driver::getLat, order.getStartLat().add(BigDecimal.valueOf(radius)))
                        .ge(Driver::getLng, order.getStartLng().subtract(BigDecimal.valueOf(radius)))
                        .le(Driver::getLng, order.getStartLng().add(BigDecimal.valueOf(radius))));

        return candidates.stream()
                .sorted(Comparator.comparingDouble(d -> -scoreDriver(d, order)))
                .limit(limit)
                .toList();
    }

    /**
     * 多因素加权评分
     * 距离权重 0.5 + 评分权重 0.3 + 空闲时长权重 0.2
     */
    private double scoreDriver(Driver driver, Order order) {
        double distScore = distanceScore(driver, order); // 距离越近分越高
        double ratingScore = driver.getAvgRating() != null ? driver.getAvgRating().doubleValue() / 5.0 : 0.8;
        double idleScore = Math.min(driver.getOnlineDuration() / 120.0, 1.0); // 空闲时长，上限2小时

        return distScore * 0.5 + ratingScore * 0.3 + idleScore * 0.2;
    }

    private double distanceScore(Driver driver, Order order) {
        double dlat = driver.getLat().subtract(order.getStartLat()).abs().doubleValue();
        double dlng = driver.getLng().subtract(order.getStartLng()).abs().doubleValue();
        double dist = Math.sqrt(dlat * dlat + dlng * dlng) * 111000; // 近似换算为米
        return Math.max(0, 1.0 - dist / 5000.0); // 5km内线性衰减
    }
}
