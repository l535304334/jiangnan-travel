package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.Coupon;
import com.jiangnan.travel.entity.UserCoupon;

import java.util.List;

public interface CouponService {

    List<Coupon> listAvailable();

    List<UserCoupon> listUserCoupons(Long userId);

    void claim(Long userId, Long couponId);

    void grantNewUserCoupons(Long userId);
}
