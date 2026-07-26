package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.EstimateRequest;
import com.jiangnan.travel.vo.EstimateVO;

import java.math.BigDecimal;

public interface PricingService {

    EstimateVO estimate(EstimateRequest request);

    EstimateVO recalculate(Long orderId);

    /** 计算优惠券抵扣金额；couponId 为空或券不可用时返回 0。 */
    BigDecimal calcCouponDiscount(Long couponId, Long userId, BigDecimal orderAmount);
}
