package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.VipLevel;
import com.jiangnan.travel.vo.UserVipVO;

import java.math.BigDecimal;
import java.util.List;

public interface VipService {

    List<VipLevel> listLevels();

    UserVipVO getMyVip(Long userId);

    void purchase(Long userId, Long levelId, Integer feeType);

    BigDecimal getVipDiscount(Long userId);
}
