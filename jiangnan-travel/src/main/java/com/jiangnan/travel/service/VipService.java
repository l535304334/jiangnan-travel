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

    /* ===== 管理端：VIP 等级维护 ===== */

    /** 管理端等级列表（含停用等级）。 */
    List<VipLevel> listAllLevels();

    void createLevel(VipLevel level);

    void updateLevel(Long id, VipLevel level);

    void deleteLevel(Long id);
}
