package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.UserVip;
import com.jiangnan.travel.entity.VipLevel;
import com.jiangnan.travel.mapper.UserVipMapper;
import com.jiangnan.travel.mapper.VipLevelMapper;
import com.jiangnan.travel.service.VipService;
import com.jiangnan.travel.vo.UserVipVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    private final VipLevelMapper vipLevelMapper;
    private final UserVipMapper userVipMapper;

    @Override
    public List<VipLevel> listLevels() {
        return vipLevelMapper.selectList(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getStatus, 1)
                        .orderByAsc(VipLevel::getLevel));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public UserVipVO getMyVip(Long userId) {
        UserVip userVip = userVipMapper.selectOne(
                new LambdaQueryWrapper<UserVip>()
                        .eq(UserVip::getUserId, userId)
                        .eq(UserVip::getStatus, 1)
                        .orderByDesc(UserVip::getEndTime)
                        .last("LIMIT 1"));

        if (userVip == null) {
            UserVipVO vo = new UserVipVO();
            vo.setStatus(0);
            vo.setRemainingDays(0L);
            return vo;
        }

        // 检查是否过期
        if (userVip.getEndTime().isBefore(LocalDateTime.now())) {
            userVip.setStatus(0);
            userVipMapper.updateById(userVip);

            UserVipVO vo = new UserVipVO();
            vo.setStatus(0);
            vo.setRemainingDays(0L);
            return vo;
        }

        VipLevel level = vipLevelMapper.selectById(userVip.getVipLevelId());

        UserVipVO vo = new UserVipVO();
        vo.setId(userVip.getId());
        vo.setUserId(userVip.getUserId());
        vo.setVipLevel(level);
        vo.setFeeType(userVip.getFeeType());
        vo.setStartTime(userVip.getStartTime());
        vo.setEndTime(userVip.getEndTime());
        vo.setStatus(userVip.getStatus());
        vo.setRemainingDays(ChronoUnit.DAYS.between(LocalDateTime.now(), userVip.getEndTime()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purchase(Long userId, Long levelId, Integer feeType) {
        VipLevel level = vipLevelMapper.selectById(levelId);
        if (level == null || level.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 检查是否有生效中的VIP
        UserVip active = userVipMapper.selectOne(
                new LambdaQueryWrapper<UserVip>()
                        .eq(UserVip::getUserId, userId)
                        .eq(UserVip::getStatus, 1)
                        .last("LIMIT 1"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        if (active != null && active.getEndTime().isAfter(now)) {
            startTime = active.getEndTime(); // 续费，从原到期日开始
            active.setStatus(0);
            userVipMapper.updateById(active);
        } else {
            startTime = now;
        }

        int months = (feeType != null && feeType == 1) ? 12 : 1;
        LocalDateTime endTime = startTime.plusMonths(months);

        UserVip userVip = new UserVip();
        userVip.setUserId(userId);
        userVip.setVipLevelId(levelId);
        userVip.setFeeType(feeType != null ? feeType : 0);
        userVip.setStartTime(startTime);
        userVip.setEndTime(endTime);
        userVip.setStatus(1);
        userVipMapper.insert(userVip);

        log.info("用户 {} 购买VIP等级 {}（{}），有效期至 {}", userId, levelId, level.getName(), endTime);
    }

    @Override
    public BigDecimal getVipDiscount(Long userId) {
        if (userId == null) return BigDecimal.ONE;

        UserVip userVip = userVipMapper.selectOne(
                new LambdaQueryWrapper<UserVip>()
                        .eq(UserVip::getUserId, userId)
                        .eq(UserVip::getStatus, 1)
                        .orderByDesc(UserVip::getEndTime)
                        .last("LIMIT 1"));

        if (userVip == null || userVip.getEndTime().isBefore(LocalDateTime.now())) {
            return BigDecimal.ONE;
        }

        VipLevel level = vipLevelMapper.selectById(userVip.getVipLevelId());
        if (level == null || level.getDiscount() == null) {
            return BigDecimal.ONE;
        }
        return level.getDiscount();
    }
}
