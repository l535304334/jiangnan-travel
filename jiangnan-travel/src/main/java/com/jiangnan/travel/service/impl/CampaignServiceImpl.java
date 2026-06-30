package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Campaign;
import com.jiangnan.travel.entity.CampaignCoupon;
import com.jiangnan.travel.entity.Coupon;
import com.jiangnan.travel.entity.UserCoupon;
import com.jiangnan.travel.mapper.CampaignCouponMapper;
import com.jiangnan.travel.mapper.CampaignMapper;
import com.jiangnan.travel.mapper.CouponMapper;
import com.jiangnan.travel.mapper.UserCouponMapper;
import com.jiangnan.travel.service.CampaignService;
import com.jiangnan.travel.vo.CampaignDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignMapper campaignMapper;
    private final CampaignCouponMapper campaignCouponMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<Campaign> listActive() {
        LocalDateTime now = LocalDateTime.now();
        return campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStatus, 1)
                        .le(Campaign::getStartTime, now)
                        .ge(Campaign::getEndTime, now)
                        .orderByDesc(Campaign::getCreateTime));
    }

    @Override
    public CampaignDetailVO getDetail(Long campaignId) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) throw new BusinessException(ErrorCode.NOT_FOUND);

        List<CampaignCoupon> relations = campaignCouponMapper.selectList(
                new LambdaQueryWrapper<CampaignCoupon>()
                        .eq(CampaignCoupon::getCampaignId, campaignId));
        List<Long> couponIds = relations.stream()
                .map(CampaignCoupon::getCouponId).collect(Collectors.toList());
        List<Coupon> coupons = couponIds.isEmpty() ? List.of()
                : couponMapper.selectBatchIds(couponIds);

        CampaignDetailVO vo = new CampaignDetailVO();
        vo.setCampaign(campaign);
        vo.setCoupons(coupons);
        return vo;
    }

    @Override
    public Page<Campaign> listAdmin(String keyword, int page, int size) {
        Page<Campaign> campaignPage = new Page<>(page, size);
        LambdaQueryWrapper<Campaign> wrapper = new LambdaQueryWrapper<Campaign>()
                .orderByDesc(Campaign::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Campaign::getName, keyword);
        }
        return campaignMapper.selectPage(campaignPage, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Campaign campaign, List<Long> couponIds) {
        campaignMapper.insert(campaign);
        if (couponIds != null && !couponIds.isEmpty()) {
            for (Long couponId : couponIds) {
                CampaignCoupon cc = new CampaignCoupon();
                cc.setCampaignId(campaign.getId());
                cc.setCouponId(couponId);
                campaignCouponMapper.insert(cc);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Campaign campaign, List<Long> couponIds) {
        Campaign existing = campaignMapper.selectById(campaign.getId());
        if (existing == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        campaignMapper.updateById(campaign);

        // 重新设置关联优惠券
        campaignCouponMapper.delete(
                new LambdaQueryWrapper<CampaignCoupon>()
                        .eq(CampaignCoupon::getCampaignId, campaign.getId()));
        if (couponIds != null && !couponIds.isEmpty()) {
            for (Long couponId : couponIds) {
                CampaignCoupon cc = new CampaignCoupon();
                cc.setCampaignId(campaign.getId());
                cc.setCouponId(couponId);
                campaignCouponMapper.insert(cc);
            }
        }
    }

    @Override
    public void delete(Long id) {
        campaignMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimCoupons(Long userId, Long campaignId) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null || campaign.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(campaign.getStartTime()) || now.isAfter(campaign.getEndTime())) {
            throw new BusinessException(4001, "活动不在进行中");
        }

        List<CampaignCoupon> relations = campaignCouponMapper.selectList(
                new LambdaQueryWrapper<CampaignCoupon>()
                        .eq(CampaignCoupon::getCampaignId, campaignId));

        for (CampaignCoupon cc : relations) {
            // 检查是否已领取
            Long count = userCouponMapper.selectCount(
                    new LambdaQueryWrapper<UserCoupon>()
                            .eq(UserCoupon::getUserId, userId)
                            .eq(UserCoupon::getCouponId, cc.getCouponId()));
            if (count > 0) continue;

            Coupon coupon = couponMapper.selectById(cc.getCouponId());
            if (coupon == null || coupon.getStatus() == 0) continue;

            UserCoupon uc = new UserCoupon();
            uc.setUserId(userId);
            uc.setCouponId(cc.getCouponId());
            uc.setStatus(0);
            uc.setExpireTime(now.plusDays(coupon.getValidDays() != null ? coupon.getValidDays() : 30));
            userCouponMapper.insert(uc);
        }
    }
}
