package com.jiangnan.travel;

import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.Campaign;
import com.jiangnan.travel.entity.CampaignCoupon;
import com.jiangnan.travel.entity.Coupon;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.mapper.CampaignCouponMapper;
import com.jiangnan.travel.mapper.CampaignMapper;
import com.jiangnan.travel.mapper.CouponMapper;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.service.CampaignService;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.CampaignDetailVO;
import com.jiangnan.travel.vo.LoginVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CampaignServiceTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignMapper campaignMapper;

    @Autowired
    private CampaignCouponMapper campaignCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "sms:code:";
    private static final String PHONE = String.format("13488%06d", System.currentTimeMillis() % 1_000_000);
    private static Long userId;
    private static Long campaignId;
    private static Long testCouponId;
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
        req.setNickname("活动测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken());
        userId = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, PHONE)).getId();
        assertNotNull(userId);
    }

    @Test
    @Order(2)
    @DisplayName("创建测试优惠券和活动")
    void testCreateCampaignData() {
        // Insert a test coupon for the campaign
        Coupon coupon = new Coupon();
        coupon.setName("活动专享券");
        coupon.setThreshold(new BigDecimal("20"));
        coupon.setDiscount(new BigDecimal("10"));
        coupon.setValidDays(30);
        coupon.setStatus(1);
        couponMapper.insert(coupon);
        testCouponId = coupon.getId();
        assertNotNull(testCouponId);

        // Insert an active campaign
        Campaign campaign = new Campaign();
        campaign.setName("暑期特惠活动");
        campaign.setDescription("暑期乘车优惠活动");
        campaign.setStartTime(LocalDateTime.now().minusDays(1));
        campaign.setEndTime(LocalDateTime.now().plusDays(30));
        campaign.setType(1);
        campaign.setStatus(1);
        campaignMapper.insert(campaign);
        campaignId = campaign.getId();
        assertNotNull(campaignId);

        // Link coupon to campaign
        CampaignCoupon cc = new CampaignCoupon();
        cc.setCampaignId(campaignId);
        cc.setCouponId(testCouponId);
        campaignCouponMapper.insert(cc);
    }

    @Test
    @Order(3)
    @DisplayName("获取活动列表")
    void testListActive() {
        List<Campaign> list = campaignService.listActive();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(c -> c.getId().equals(campaignId)));
    }

    @Test
    @Order(4)
    @DisplayName("获取活动详情")
    void testGetDetail() {
        CampaignDetailVO detail = campaignService.getDetail(campaignId);
        assertNotNull(detail);
        assertEquals("暑期特惠活动", detail.getCampaign().getName());
        assertFalse(detail.getCoupons().isEmpty());
        assertEquals("活动专享券", detail.getCoupons().get(0).getName());
    }

    @Test
    @Order(5)
    @DisplayName("领取活动优惠券")
    void testClaimCampaignCoupons() {
        assertDoesNotThrow(() -> campaignService.claimCoupons(userId, campaignId));
    }

    @Test
    @Order(6)
    @DisplayName("重复领取活动优惠券（幂等，不抛异常）")
    void testDuplicateClaim() {
        // claimCoupons 采用幂等设计，重复领取不会抛异常
        assertDoesNotThrow(() -> campaignService.claimCoupons(userId, campaignId));
    }
}
