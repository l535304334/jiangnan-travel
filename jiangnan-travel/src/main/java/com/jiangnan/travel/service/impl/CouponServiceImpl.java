package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Coupon;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.entity.UserCoupon;
import com.jiangnan.travel.mapper.CouponMapper;
import com.jiangnan.travel.mapper.UserCouponMapper;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.service.CouponService;
import com.jiangnan.travel.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserMapper userMapper;

    @Override
    public List<Coupon> listAvailable() {
        return couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 1));
    }

    @Override
    public List<UserCouponVO> listUserCoupons(Long userId) {
        List<UserCoupon> list = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .orderByDesc(UserCoupon::getCreateTime));
        // ponytail: batch load coupons to avoid N+1
        List<Long> couponIds = list.stream().map(UserCoupon::getCouponId).distinct().toList();
        java.util.Map<Long, Coupon> couponMap = couponIds.isEmpty() ? java.util.Collections.emptyMap()
                : couponMapper.selectBatchIds(couponIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Coupon::getId, c -> c));
        return list.stream().map(uc -> {
            Coupon coupon = couponMap.get(uc.getCouponId());
            return UserCouponVO.builder()
                    .id(uc.getId())
                    .userId(uc.getUserId())
                    .couponId(uc.getCouponId())
                    .status(uc.getStatus())
                    .expireTime(uc.getExpireTime())
                    .useOrderId(uc.getUseOrderId())
                    .name(coupon != null ? coupon.getName() : "")
                    .threshold(coupon != null ? coupon.getThreshold() : BigDecimal.ZERO)
                    .discount(coupon != null ? coupon.getDiscount() : BigDecimal.ZERO)
                    .validDays(coupon != null ? coupon.getValidDays() : 0)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 检查是否已领取
        Long count = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_CLAIMED);
        }

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        uc.setExpireTime(LocalDateTime.now().plusDays(coupon.getValidDays()));
        userCouponMapper.insert(uc);
    }

    @Override
    public void grantNewUserCoupons(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        // 发放新用户专享券（固定 ID=1，避免按名称查询的脆断性）
        Coupon newUserCoupon = couponMapper.selectById(1L);
        if (newUserCoupon != null) {
            UserCoupon uc = new UserCoupon();
            uc.setUserId(userId);
            uc.setCouponId(newUserCoupon.getId());
            uc.setStatus(0);
            uc.setExpireTime(LocalDateTime.now().plusDays(newUserCoupon.getValidDays()));
            userCouponMapper.insert(uc);
        }
    }
}
