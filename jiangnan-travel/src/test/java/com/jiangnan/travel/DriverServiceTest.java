package com.jiangnan.travel;

import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.service.DriverService;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.DriverVO;
import com.jiangnan.travel.vo.LoginVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DriverServiceTest {

    @Autowired
    private DriverService driverService;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "sms:code:";
    private static final String TEST_PHONE = String.format("13788%06d", (System.currentTimeMillis() + 20000) % 1_000_000);
    private static Long userId;
    private static Long driverId;

    @Test
    @Order(1)
    @DisplayName("准备测试用户 - 发送验证码并注册")
    void testPrepareUser() {
        redisTemplate.delete(SMS_PREFIX + "rate:" + TEST_PHONE);
        userService.sendCode(TEST_PHONE);
        String code = redisTemplate.opsForValue().get(SMS_PREFIX + TEST_PHONE);
        assertNotNull(code, "验证码不应为空");

        RegisterRequest req = new RegisterRequest();
        req.setPhone(TEST_PHONE);
        req.setCode(code);
        req.setPassword("123456");
        req.setNickname("司机测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken(), "注册应返回Token");
        assertNotNull(result.getUserId(), "注册应返回用户ID");
        userId = result.getUserId();
    }

    @Test
    @Order(2)
    @DisplayName("注册司机")
    void testRegisterDriver() {
        assertNotNull(userId);

        // register 已消耗验证码，需重新发送
        redisTemplate.delete(SMS_PREFIX + "rate:" + TEST_PHONE);
        userService.sendCode(TEST_PHONE);
        String code = redisTemplate.opsForValue().get(SMS_PREFIX + TEST_PHONE);
        assertNotNull(code, "验证码不应为空");

        DriverRegisterRequest req = new DriverRegisterRequest();
        req.setPhone(TEST_PHONE);
        req.setCode(code);
        req.setRealName("张三");
        req.setIdCard("360101199001011234");
        req.setDriverLicense("360101199001011234");
        req.setCarPlate("赣A·88888");

        driverService.register(req);

        driverId = driverService.getDriverIdByUserId(userId);
        assertNotNull(driverId, "司机ID不应为空");
    }

    @Test
    @Order(3)
    @DisplayName("司机未审核时登录应抛出异常")
    void testLoginBeforeVerify() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> driverService.login(TEST_PHONE),
                "未审核司机登录应抛出异常");
        assertEquals(3002, ex.getCode(), "错误码应为3002(司机未通过审核)");
    }

    @Test
    @Order(4)
    @DisplayName("审核通过司机")
    void testVerifyDriver() {
        assertNotNull(driverId);
        driverService.verifyDriver(driverId, 1);

        DriverVO vo = driverService.getProfile(driverId);
        assertEquals(1, vo.getVerifyStatus(), "审核状态应为1(已通过)");
    }

    @Test
    @Order(5)
    @DisplayName("司机登录")
    void testLogin() {
        LoginVO result = driverService.login(TEST_PHONE);
        assertNotNull(result.getToken(), "登录应返回Token");
        assertEquals(TEST_PHONE, result.getPhone());
        assertEquals("张三", result.getNickname());
    }

    @Test
    @Order(6)
    @DisplayName("获取司机个人信息")
    void testGetProfile() {
        assertNotNull(driverId);
        DriverVO vo = driverService.getProfile(driverId);
        assertNotNull(vo, "司机信息不应为空");
        assertEquals(driverId, vo.getId());
        assertEquals("张三", vo.getRealName());
        assertEquals("赣A·88888", vo.getCarPlate());
        assertNotNull(vo.getStatusText(), "状态文本不应为空");
    }

    @Test
    @Order(7)
    @DisplayName("更新司机状态和位置")
    void testUpdateStatusAndLocation() {
        assertNotNull(driverId);

        // 更新为在线状态
        driverService.updateStatus(driverId, 1);
        DriverVO vo = driverService.getProfile(driverId);
        assertEquals(1, vo.getStatus(), "司机状态应为1(在线)");

        // 更新位置
        driverService.updateLocation(driverId, new BigDecimal("31.2304"), new BigDecimal("121.4737"));
        DriverVO vo2 = driverService.getProfile(driverId);
        assertEquals(0, new BigDecimal("31.2304").compareTo(vo2.getLat()), "纬度应匹配");
        assertEquals(0, new BigDecimal("121.4737").compareTo(vo2.getLng()), "经度应匹配");
    }

    @Test
    @Order(8)
    @DisplayName("获取司机收入统计")
    void testGetEarningStats() {
        assertNotNull(driverId);
        Map<String, Object> stats = driverService.getEarningStats(driverId);
        assertNotNull(stats, "收入统计不应为空");
        assertTrue(stats.containsKey("todayOrders"), "应包含今日订单数");
        assertTrue(stats.containsKey("todayEarnings"), "应包含今日收入");
        assertTrue(stats.containsKey("onlineMinutes"), "应包含在线时长");
        assertTrue(stats.containsKey("totalOrders"), "应包含总订单数");
        assertTrue(stats.containsKey("avgRating"), "应包含平均评分");
        assertEquals(0L, stats.get("todayOrders"), "新司机今日订单数应为0");
    }

    @Test
    @Order(9)
    @DisplayName("线上司机数量统计")
    void testCountOnlineDrivers() {
        long count = driverService.countOnlineDrivers();
        assertTrue(count >= 0, "线上司机数量不应为负数");
    }

    @Test
    @Order(10)
    @DisplayName("重复注册司机应抛出异常")
    void testDuplicateRegister() {
        String code = redisTemplate.opsForValue().get(SMS_PREFIX + TEST_PHONE);
        assertNotNull(code);

        DriverRegisterRequest req = new DriverRegisterRequest();
        req.setPhone(TEST_PHONE);
        req.setCode(code);
        req.setRealName("李四");
        req.setIdCard("360101199001011235");
        req.setDriverLicense("360101199001011235");
        req.setCarPlate("赣B·66666");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> driverService.register(req),
                "重复注册应抛出异常");
        assertEquals(3004, ex.getCode(), "错误码应为3004(已是司机)");
    }
}
