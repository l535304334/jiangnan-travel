package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Bill;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.mapper.BillMapper;
import com.jiangnan.travel.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 计费实现：distance × unitPrice + duration × minutePrice + peak surcharge。
 * ponytail: simple linear pricing, no dynamic surge model yet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillMapper billMapper;

    /** 每公里单价（元/km） */
    private static final BigDecimal UNIT_PRICE = new BigDecimal("2.00");
    /** 每分钟单价（元/min） */
    private static final BigDecimal MINUTE_PRICE = new BigDecimal("0.30");

    @Override
    public Bill generateBill(Order order) {
        // 距离费
        BigDecimal distanceKm = BigDecimal.valueOf(order.getDistance() != null ? order.getDistance() : 0)
                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        BigDecimal distanceFee = distanceKm.multiply(UNIT_PRICE).setScale(2, RoundingMode.HALF_UP);

        // 时长费
        BigDecimal durationMin = BigDecimal.valueOf(order.getDuration() != null ? order.getDuration() : 0)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal durationFee = durationMin.multiply(MINUTE_PRICE).setScale(2, RoundingMode.HALF_UP);

        // 高峰加价（7:00-9:00 或 17:00-19:00）
        BigDecimal peakSurcharge = BigDecimal.ZERO;
        LocalTime now = LocalDateTime.now().toLocalTime();
        boolean isPeak = (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 0)))
                || (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(19, 0)));
        if (isPeak) {
            peakSurcharge = distanceFee.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        }

        // 优惠券
        BigDecimal couponDiscount = order.getCouponDiscount() != null
                ? order.getCouponDiscount() : BigDecimal.ZERO;

        // 过路费
        BigDecimal tollFee = order.getTollFee() != null ? order.getTollFee() : BigDecimal.ZERO;

        // 合计
        BigDecimal total = distanceFee.add(durationFee).add(peakSurcharge).add(tollFee)
                .subtract(couponDiscount).max(BigDecimal.ZERO);

        Bill bill = new Bill();
        bill.setOrderId(order.getId());
        bill.setUserId(order.getUserId());
        bill.setDriverId(order.getDriverId());
        bill.setDistanceFee(distanceFee);
        bill.setDurationFee(durationFee);
        bill.setPeakSurcharge(peakSurcharge);
        bill.setCouponDiscount(couponDiscount);
        bill.setTollFee(tollFee);
        bill.setTotalAmount(total);
        billMapper.insert(bill);

        log.info("账单生成: orderId={}, total={}, distanceFee={}, peakSurcharge={}",
                order.getId(), total, distanceFee, peakSurcharge);
        return bill;
    }

    @Override
    public Bill getByOrderId(Long orderId) {
        Bill bill = billMapper.selectOne(
                new LambdaQueryWrapper<Bill>().eq(Bill::getOrderId, orderId));
        if (bill == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "账单不存在");
        }
        return bill;
    }
}
