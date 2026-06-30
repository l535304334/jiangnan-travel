package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.Coupon;
import com.jiangnan.travel.vo.UserCouponVO;

import java.util.List;

public interface CouponService {

    List<Coupon> listAvailable();

    List<UserCouponVO> listUserCoupons(Long userId);

    void claim(Long userId, Long couponId);

    void grantNewUserCoupons(Long userId);
}
