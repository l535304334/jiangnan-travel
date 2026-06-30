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
        return list.stream().map(uc -> {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
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

        // 发放新用户专享券（id=1）
        Coupon newUserCoupon = couponMapper.selectOne(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getName, "新用户专享"));
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
