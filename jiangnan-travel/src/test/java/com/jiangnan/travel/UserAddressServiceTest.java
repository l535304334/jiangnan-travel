package com.jiangnan.travel;

import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.entity.UserAddress;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.service.UserAddressService;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.LoginVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserAddressServiceTest {

    @Autowired
    private UserAddressService addressService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "sms:code:";
    private static final String PHONE = String.format("13388%06d", System.currentTimeMillis() % 1_000_000);
    private static Long userId;
    private static Long addressId;
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
        req.setNickname("地址测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken());
        userId = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, PHONE)).getId();
        assertNotNull(userId);
    }

    @Test
    @Order(2)
    @DisplayName("新增收藏地址")
    void testAddAddress() {
        UserAddress addr = addressService.add(userId, "家",
                "江南大学东门", new BigDecimal("31.2304"), new BigDecimal("121.4737"));
        assertNotNull(addr);
        assertNotNull(addr.getId());
        assertEquals("家", addr.getTag());
        addressId = addr.getId();
    }

    @Test
    @Order(3)
    @DisplayName("新增第二个地址")
    void testAddSecondAddress() {
        UserAddress addr = addressService.add(userId, "公司",
                "无锡火车站", new BigDecimal("31.2504"), new BigDecimal("121.4637"));
        assertNotNull(addr);
        assertNotNull(addr.getId());
    }

    @Test
    @Order(4)
    @DisplayName("获取地址列表")
    void testListAddresses() {
        List<UserAddress> list = addressService.listByUser(userId);
        assertEquals(2, list.size());
    }

    @Test
    @Order(5)
    @DisplayName("删除地址")
    void testDeleteAddress() {
        assertDoesNotThrow(() -> addressService.delete(userId, addressId));
        List<UserAddress> list = addressService.listByUser(userId);
        assertEquals(1, list.size());
    }

    @Test
    @Order(6)
    @DisplayName("删除不存在的地址应报错")
    void testDeleteNonExistent() {
        assertThrows(BusinessException.class,
                () -> addressService.delete(userId, 999999L));
    }
}
