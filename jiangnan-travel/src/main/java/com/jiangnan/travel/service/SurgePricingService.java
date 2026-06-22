package com.jiangnan.travel.service;

import java.math.BigDecimal;

public interface SurgePricingService {

    BigDecimal calculateSurgeFactor(BigDecimal lat, BigDecimal lng);
}
