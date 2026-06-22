package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.EstimateRequest;
import com.jiangnan.travel.vo.EstimateVO;

public interface PricingService {

    EstimateVO estimate(EstimateRequest request);

    EstimateVO recalculate(Long orderId);
}
