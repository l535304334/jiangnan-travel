package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.Payment;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.PaymentMapper;
import com.jiangnan.travel.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final OrderMapper orderMapper;
    private final DriverMapper driverMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public String analyze() {
        long todayOrderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .apply("DATE(create_time) = CURDATE()"));

        long onlineDrivers = driverMapper.selectCount(
                new LambdaQueryWrapper<Driver>()
                        .eq(Driver::getStatus, 1));

        List<Payment> todayPayments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getStatus, 1)
                        .apply("DATE(pay_time) = CURDATE()"));
        BigDecimal revenue = todayPayments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return "数据分析完成。根据系统数据，今日订单>" + todayOrderCount
                + "单，活跃司机>" + onlineDrivers
                + "人，营收>" + revenue + "元。建议关注风控告警情况。";
    }
}
