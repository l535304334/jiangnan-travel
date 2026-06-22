package com.jiangnan.travel;

import com.jiangnan.travel.dto.LoginRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.LoginVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TEST_PHONE = "13800138001";
    private static String testCode;

    @Test
    @Order(1)
    @DisplayName("发送验证码")
    void testSendCode() {
        userService.sendCode(TEST_PHONE);
        testCode = redisTemplate.opsForValue().get("sms:code:" + TEST_PHONE);
        assertNotNull(testCode, "验证码不应为空");
        assertEquals(6, testCode.length(), "验证码应为6位");
    }

    @Test
    @Order(2)
    @DisplayName("用户注册")
    void testRegister() {
        RegisterRequest req = new RegisterRequest();
        req.setPhone(TEST_PHONE);
        req.setCode(testCode);
        req.setPassword("123456");
        req.setNickname("测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken(), "注册应返回Token");
        assertEquals(TEST_PHONE, result.getPhone());
    }

    @Test
    @Order(3)
    @DisplayName("重复注册应报错")
    void testDuplicateRegister() {
        // 重新发码
        userService.sendCode(TEST_PHONE);
        testCode = redisTemplate.opsForValue().get("sms:code:" + TEST_PHONE);

        RegisterRequest req = new RegisterRequest();
        req.setPhone(TEST_PHONE);
        req.setCode(testCode);
        req.setPassword("123456");
        assertThrows(Exception.class, () -> userService.register(req));
    }

    @Test
    @Order(4)
    @DisplayName("验证码登录")
    void testLogin() {
        userService.sendCode(TEST_PHONE);
        testCode = redisTemplate.opsForValue().get("sms:code:" + TEST_PHONE);

        LoginRequest req = new LoginRequest();
        req.setPhone(TEST_PHONE);
        req.setCode(testCode);
        LoginVO result = userService.login(req);
        assertNotNull(result.getToken(), "登录应返回Token");
    }

    @Test
    @Order(5)
    @DisplayName("获取个人信息")
    void testGetProfile() {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, TEST_PHONE));
        assertNotNull(user);
        User profile = userService.getProfile(user.getId());
        assertNotNull(profile);
        assertNull(profile.getPassword(), "密码不应返回");
    }
}
