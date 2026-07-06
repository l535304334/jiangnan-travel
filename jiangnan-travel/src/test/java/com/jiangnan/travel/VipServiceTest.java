package com.jiangnan.travel;

import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.entity.VipLevel;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.mapper.VipLevelMapper;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.service.VipService;
import com.jiangnan.travel.vo.LoginVO;
import com.jiangnan.travel.vo.UserVipVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VipServiceTest {

    @Autowired
    private VipService vipService;

    @Autowired
    private VipLevelMapper vipLevelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "sms:code:";
    private static final String PHONE = String.format("13688%06d", System.currentTimeMillis() % 1_000_000);
    private static final int VIP_LEVEL = (int)(System.currentTimeMillis() % 9000 + 1000);
    private static Long userId;
    private static Long levelId;
    private static String testCode;

    private String issueCode(String phone) {
        redisTemplate.delete(SMS_PREFIX + "rate:" + phone);
        userService.sendCode(phone);
        return redisTemplate.opsForValue().get(SMS_PREFIX + phone);
    }

    @Test
    @Order(1)
    @DisplayName("创建测试VIP等级")
    void testCreateVipLevel() {
        // 防御性清理：若同 level 残留记录则先删除（避免 UNIQUE 约束冲突）
        vipLevelMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VipLevel>()
                .eq(VipLevel::getLevel, VIP_LEVEL));

        VipLevel level = new VipLevel();
        level.setName("黄金会员");
        level.setLevel(VIP_LEVEL);
        level.setDiscount(new BigDecimal("0.85"));
        level.setMonthlyFee(new BigDecimal("29.9"));
        level.setYearlyFee(new BigDecimal("299"));
        level.setStatus(1);
        vipLevelMapper.insert(level);
        levelId = level.getId();
        assertNotNull(levelId);
    }

    @Test
    @Order(2)
    @DisplayName("获取VIP等级列表")
    void testListLevels() {
        List<VipLevel> levels = vipService.listLevels();
        assertFalse(levels.isEmpty());
        assertTrue(levels.stream().anyMatch(l -> l.getId().equals(levelId)));
    }

    @Test
    @Order(3)
    @DisplayName("用户注册")
    void testRegister() {
        testCode = issueCode(PHONE);
        assertNotNull(testCode);

        RegisterRequest req = new RegisterRequest();
        req.setPhone(PHONE);
        req.setCode(testCode);
        req.setPassword("123456");
        req.setNickname("VIP测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken());
        userId = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, PHONE)).getId();
        assertNotNull(userId);
    }

    @Test
    @Order(4)
    @DisplayName("购买VIP（月费）")
    void testPurchaseVip() {
        assertDoesNotThrow(() -> vipService.purchase(userId, levelId, 0));
    }

    @Test
    @Order(5)
    @DisplayName("获取我的VIP信息")
    void testGetMyVip() {
        UserVipVO vo = vipService.getMyVip(userId);
        assertNotNull(vo);
        assertNotNull(vo.getVipLevel());
        assertEquals("黄金会员", vo.getVipLevel().getName());
        assertEquals(0, vo.getFeeType());
        assertTrue(vo.getRemainingDays() > 0);
    }

    @Test
    @Order(6)
    @DisplayName("获取VIP折扣")
    void testGetVipDiscount() {
        BigDecimal discount = vipService.getVipDiscount(userId);
        assertEquals(0, new BigDecimal("0.85").compareTo(discount));
    }

    @Test
    @Order(7)
    @DisplayName("续费VIP（年费）")
    void testRenewVip() {
        assertDoesNotThrow(() -> vipService.purchase(userId, levelId, 1));
        UserVipVO vo = vipService.getMyVip(userId);
        assertEquals(1, vo.getFeeType());
    }

    @Test
    @Order(8)
    @DisplayName("非VIP用户折扣应为1.0")
    void testNoVipDiscount() {
        BigDecimal discount = vipService.getVipDiscount(999999L);
        assertEquals(0, BigDecimal.ONE.compareTo(discount));
    }
}
