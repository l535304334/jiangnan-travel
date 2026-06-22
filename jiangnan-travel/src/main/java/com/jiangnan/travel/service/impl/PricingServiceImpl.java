package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.EstimateRequest;
import com.jiangnan.travel.entity.CarType;
import com.jiangnan.travel.entity.Coupon;
import com.jiangnan.travel.entity.UserCoupon;
import com.jiangnan.travel.mapper.CarTypeMapper;
import com.jiangnan.travel.mapper.CouponMapper;
import com.jiangnan.travel.mapper.UserCouponMapper;
import com.jiangnan.travel.service.PricingService;
import com.jiangnan.travel.vo.EstimateVO;
import com.jiangnan.travel.vo.PriceDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final CarTypeMapper carTypeMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public EstimateVO estimate(EstimateRequest request) {
        // 获取车型
        Long carTypeId = request.getCarTypeId() != null ? request.getCarTypeId() : 1L;
        CarType carType = carTypeMapper.selectById(carTypeId);
        if (carType == null || carType.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ORDER_CAR_TYPE_INVALID);
        }

        // 距离和时长
        int distMeters = request.getDistance() != null ? request.getDistance() : 5000;
        int durSeconds = request.getDuration() != null ? request.getDuration() : 600;

        // 计算阶梯价格
        PriceDetailVO detail = calcStepPrice(carType, distMeters, durSeconds);

        // 动态加价系数（默认1.0，实际由调价引擎注入）
        BigDecimal surgeFactor = BigDecimal.valueOf(1.00);
        BigDecimal surgeAmount = detail.getSubtotal().multiply(surgeFactor.subtract(BigDecimal.ONE))
                .setScale(2, RoundingMode.HALF_UP);
        detail.setSurgeFactor(surgeFactor);
        detail.setSurgePrice(surgeAmount);

        // 过路费默认为0
        detail.setTollFee(BigDecimal.ZERO);
        detail.setCouponDiscount(BigDecimal.ZERO);

        // 最终价格
        BigDecimal total = detail.getSubtotal().add(surgeAmount)
                .setScale(2, RoundingMode.HALF_UP);
        detail.setTotalPrice(total);

        return EstimateVO.builder()
                .distance(distMeters)
                .duration(durSeconds)
                .basePrice(detail.getSubtotal())
                .surgeFactor(surgeFactor)
                .surgeAmount(surgeAmount)
                .couponDiscount(BigDecimal.ZERO)
                .estimateTotal(total)
                .carTypeName(carType.getName())
                .priceDetail(detail)
                .build();
    }

    @Override
    public EstimateVO recalculate(Long orderId) {
        // 供动态调价后重新计算用（Week 2 周二实现）
        throw new UnsupportedOperationException("待实现");
    }

    /**
     * 阶梯计价核心算法
     * 0-3km:    起步价（含3公里）
     * 3-50km:   起步价 + (距离-3km) × 中程单价
     * 50-200km: 起步价 + 47km×中程单价 + (距离-50km) × 远程单价
     * 200km+:   起步价 + 47km×中程单价 + 150km×远程单价 + (距离-200km) × 超远程单价
     */
    private PriceDetailVO calcStepPrice(CarType carType, int distMeters, int durSeconds) {
        BigDecimal distKm = BigDecimal.valueOf(distMeters).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        BigDecimal durMin = BigDecimal.valueOf(durSeconds).divide(BigDecimal.valueOf(60), 0, RoundingMode.UP);

        PriceDetailVO detail = new PriceDetailVO();
        detail.setBasePrice(carType.getBasePrice());
        detail.setBaseDesc("起步价（含3公里）");

        BigDecimal price = BigDecimal.ZERO;
        BigDecimal remain = distKm;

        // 0-3km: 起步价已含
        if (remain.compareTo(BigDecimal.valueOf(3)) <= 0) {
            price = carType.getBasePrice();
            detail.setMidDistance(BigDecimal.ZERO);
            detail.setMidPrice(BigDecimal.ZERO);
            detail.setLongDistance(BigDecimal.ZERO);
            detail.setLongPrice(BigDecimal.ZERO);
            detail.setSuperLongDistance(BigDecimal.ZERO);
            detail.setSuperLongPrice(BigDecimal.ZERO);
        } else {
            price = carType.getBasePrice();
            remain = remain.subtract(BigDecimal.valueOf(3));

            // 3-50km
            BigDecimal midRange = BigDecimal.valueOf(47);
            if (remain.compareTo(midRange) <= 0) {
                detail.setMidDistance(remain);
                detail.setMidPrice(remain.multiply(carType.getMidPerKm()).setScale(2, RoundingMode.HALF_UP));
                detail.setLongDistance(BigDecimal.ZERO);
                detail.setLongPrice(BigDecimal.ZERO);
                detail.setSuperLongDistance(BigDecimal.ZERO);
                detail.setSuperLongPrice(BigDecimal.ZERO);
                price = price.add(detail.getMidPrice());
                remain = BigDecimal.ZERO;
            } else {
                detail.setMidDistance(midRange);
                detail.setMidPrice(midRange.multiply(carType.getMidPerKm()).setScale(2, RoundingMode.HALF_UP));
                price = price.add(detail.getMidPrice());
                remain = remain.subtract(midRange);

                // 50-200km
                BigDecimal longRange = BigDecimal.valueOf(150);
                if (remain.compareTo(longRange) <= 0) {
                    detail.setLongDistance(remain);
                    detail.setLongPrice(remain.multiply(carType.getLongPerKm()).setScale(2, RoundingMode.HALF_UP));
                    detail.setSuperLongDistance(BigDecimal.ZERO);
                    detail.setSuperLongPrice(BigDecimal.ZERO);
                    price = price.add(detail.getLongPrice());
                } else {
                    detail.setLongDistance(longRange);
                    detail.setLongPrice(longRange.multiply(carType.getLongPerKm()).setScale(2, RoundingMode.HALF_UP));
                    price = price.add(detail.getLongPrice());
                    remain = remain.subtract(longRange);

                    // 200km+
                    detail.setSuperLongDistance(remain);
                    detail.setSuperLongPrice(remain.multiply(carType.getSuperLongPerKm()).setScale(2, RoundingMode.HALF_UP));
                    price = price.add(detail.getSuperLongPrice());
                }
            }
        }

        // 时长费
        BigDecimal timeFee = durMin.multiply(carType.getPerMinPrice()).setScale(2, RoundingMode.HALF_UP);
        detail.setTimeFee(timeFee);
        price = price.add(timeFee);

        detail.setSubtotal(price.setScale(2, RoundingMode.HALF_UP));
        return detail;
    }

    /**
     * 查询可用优惠券折扣
     */
    public BigDecimal calcCouponDiscount(Long couponId, Long userId, BigDecimal orderAmount) {
        if (couponId == null) return BigDecimal.ZERO;

        UserCoupon userCoupon = userCouponMapper.selectOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getId, couponId)
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 0));
        if (userCoupon == null || userCoupon.getExpireTime().isBefore(LocalDateTime.now())) {
            return BigDecimal.ZERO;
        }

        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) return BigDecimal.ZERO;

        if (orderAmount.compareTo(coupon.getThreshold()) < 0) {
            return BigDecimal.ZERO; // 未达门槛
        }
        return coupon.getDiscount();
    }
}
