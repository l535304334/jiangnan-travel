package com.jiangnan.travel;

import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.Coupon;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.entity.UserCoupon;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.service.CouponService;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.LoginVO;
import com.jiangnan.travel.vo.UserCouponVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "sms:code:";
    private static final String PHONE = String.format("13588%06d", System.currentTimeMillis() % 1_000_000);
    private static Long userId;
    private static Long couponId;
    private static String testCode;

    private String issueCode(String phone) {
        redisTemplate.delete(SMS_PREFIX + "rate:" + phone);
        userService.sendCode(phone);
        return redisTemplate.opsForValue().get(SMS_PREFIX + phone);
    }

    @Test
    @Order(1)
    @DisplayName("用户注册")
    void testRegister() {
        testCode = issueCode(PHONE);
        RegisterRequest req = new RegisterRequest();
        req.setPhone(PHONE);
        req.setCode(testCode);
        req.setPassword("123456");
        req.setNickname("优惠券测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken());
        userId = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, PHONE)).getId();
        assertNotNull(userId);
    }

    @Test
    @Order(2)
    @DisplayName("获取可领取优惠券列表")
    void testListAvailable() {
        List<Coupon> list = couponService.listAvailable();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(c -> c.getStatus() == 1));
        // 跳过第1张（注册时自动发放的"新用户专享"），使用第2张
        couponId = list.size() > 1 ? list.get(1).getId() : list.get(0).getId();
    }

    @Test
    @Order(3)
    @DisplayName("领取优惠券")
    void testClaim() {
        assertDoesNotThrow(() -> couponService.claim(userId, couponId));
    }

    @Test
    @Order(4)
    @DisplayName("重复领取应报错")
    void testDuplicateClaim() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.claim(userId, couponId));
        assertEquals(ErrorCode.COUPON_ALREADY_CLAIMED.getCode(), ex.getCode());
    }

    @Test
    @Order(5)
    @DisplayName("获取我的优惠券")
    void testMyCoupons() {
        List<UserCouponVO> list = couponService.listUserCoupons(userId);
        assertFalse(list.isEmpty());
        // 用户应有至少2张券（注册自动发放+手动领取）
        assertTrue(list.size() >= 2);
        assertTrue(list.stream().anyMatch(uc -> uc.getCouponId().equals(couponId)));
    }

    @Test
    @Order(6)
    @DisplayName("新用户自动领券")
    void testGrantNewUserCoupons() {
        assertDoesNotThrow(() -> couponService.grantNewUserCoupons(userId));
    }
}
