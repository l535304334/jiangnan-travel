package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.service.SurgePricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SurgePricingServiceImpl implements SurgePricingService {

    private final DriverMapper driverMapper;
    private final OrderMapper orderMapper;

    private static final BigDecimal MAX_SURGE_FACTOR = BigDecimal.valueOf(1.50);
    private static final BigDecimal MIN_SURGE_FACTOR = BigDecimal.valueOf(1.00);

    @Override
    public BigDecimal calculateSurgeFactor(BigDecimal lat, BigDecimal lng) {
        // 统计附近5公里内的在线司机数
        double radius = 0.05; // 约5公里
        Long onlineDrivers = driverMapper.selectCount(
                new LambdaQueryWrapper<Driver>()
                        .eq(Driver::getStatus, 1)
                        .ge(Driver::getLat, lat.subtract(BigDecimal.valueOf(radius)))
                        .le(Driver::getLat, lat.add(BigDecimal.valueOf(radius)))
                        .ge(Driver::getLng, lng.subtract(BigDecimal.valueOf(radius)))
                        .le(Driver::getLng, lng.add(BigDecimal.valueOf(radius))));

        // 统计最近30分钟该区域的待接单数量
        Long pendingOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 0)
                        .ge(Order::getCreateTime, LocalDateTime.now().minusMinutes(30)));

        // 供需比 = 待接单数 / (在线司机数 + 1)
        double ratio = onlineDrivers == 0 ? pendingOrders.doubleValue()
                : pendingOrders.doubleValue() / (onlineDrivers.doubleValue() + 1);

        // ratio<0.5: 1.00  /  0.5-1.0: 1.00-1.20  /  1.0-2.0: 1.20-1.40  /  >2.0: 1.40-1.50
        BigDecimal factor;
        if (ratio < 0.5) {
            factor = BigDecimal.valueOf(1.00);
        } else if (ratio < 1.0) {
            factor = BigDecimal.valueOf(1.00 + (ratio - 0.5) * 0.4); // 1.00 -> 1.20
        } else if (ratio < 2.0) {
            factor = BigDecimal.valueOf(1.20 + (ratio - 1.0) * 0.2); // 1.20 -> 1.40
        } else {
            factor = BigDecimal.valueOf(1.40 + Math.min(ratio - 2.0, 0.5) * 0.2); // 1.40 -> 1.50
        }

        factor = factor.setScale(2, RoundingMode.HALF_UP);
        if (factor.compareTo(MAX_SURGE_FACTOR) > 0) factor = MAX_SURGE_FACTOR;
        if (factor.compareTo(MIN_SURGE_FACTOR) < 0) factor = MIN_SURGE_FACTOR;

        return factor;
    }
}
