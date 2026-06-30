package com.jiangnan.travel;

import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.LoginVO;
import com.jiangnan.travel.vo.OrderVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "sms:code:";
    private static final String TEST_PHONE = String.format("13988%06d", (System.currentTimeMillis() + 10000) % 1_000_000);
    private static Long userId;
    private static Long orderId;

    @Test
    @Order(1)
    @DisplayName("准备测试用户 - 发送验证码")
    void testSendCode() {
        redisTemplate.delete(SMS_PREFIX + "rate:" + TEST_PHONE);
        userService.sendCode(TEST_PHONE);
        String code = redisTemplate.opsForValue().get(SMS_PREFIX + TEST_PHONE);
        assertNotNull(code, "验证码不应为空");
        assertEquals(6, code.length(), "验证码应为6位");
    }

    @Test
    @Order(2)
    @DisplayName("准备测试用户 - 注册")
    void testRegister() {
        String code = redisTemplate.opsForValue().get(SMS_PREFIX + TEST_PHONE);
        assertNotNull(code);

        RegisterRequest req = new RegisterRequest();
        req.setPhone(TEST_PHONE);
        req.setCode(code);
        req.setPassword("123456");
        req.setNickname("订单测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken(), "注册应返回Token");
        assertEquals(TEST_PHONE, result.getPhone());
        assertNotNull(result.getUserId(), "注册应返回用户ID");
        userId = result.getUserId();
    }

    @Test
    @Order(3)
    @DisplayName("创建订单")
    void testCreateOrder() {
        assertNotNull(userId, "用户ID不应为空");

        CreateOrderRequest req = new CreateOrderRequest();
        req.setStartAddress("江南大学东门");
        req.setStartLat(new BigDecimal("31.2304"));
        req.setStartLng(new BigDecimal("121.4737"));
        req.setEndAddress("无锡火车站");
        req.setEndLat(new BigDecimal("31.2504"));
        req.setEndLng(new BigDecimal("121.4937"));
        req.setDistance(5000);
        req.setDuration(600);
        req.setCarTypeId(1L);

        OrderVO vo = orderService.create(req, userId);
        assertNotNull(vo, "订单不应为空");
        assertNotNull(vo.getId(), "订单ID不应为空");
        assertNotNull(vo.getOrderNo(), "订单编号不应为空");
        assertEquals(0, vo.getStatus(), "新创建订单状态应为0（待接单）");
        assertEquals("江南大学东门", vo.getStartAddress());
        assertEquals("无锡火车站", vo.getEndAddress());
        assertEquals(userId, vo.getUserId());
        orderId = vo.getId();
    }

    @Test
    @Order(4)
    @DisplayName("查询订单列表")
    void testListOrders() {
        List<OrderVO> orders = orderService.listByUser(userId, null, 1, 50);
        assertNotNull(orders, "订单列表不应为空");
        assertFalse(orders.isEmpty(), "订单列表不应为空");
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(orderId)),
                "列表应包含刚创建的订单");
    }

    @Test
    @Order(5)
    @DisplayName("查询订单详情")
    void testGetOrderDetail() {
        assertNotNull(orderId);
        OrderVO vo = orderService.getById(orderId, userId);
        assertNotNull(vo, "订单详情不应为空");
        assertEquals(orderId, vo.getId());
        assertEquals("江南大学东门", vo.getStartAddress());
        assertEquals(0, vo.getStatus(), "订单状态应为待接单");
    }

    @Test
    @Order(6)
    @DisplayName("取消订单")
    void testCancelOrder() {
        assertNotNull(orderId);
        String reason = "行程变更";
        orderService.cancel(orderId, userId, reason);

        OrderVO vo = orderService.getById(orderId, userId);
        assertEquals(5, vo.getStatus(), "取消后订单状态应为5（已取消）");
        assertEquals(reason, vo.getCancelReason(), "取消原因应匹配");
    }

    @Test
    @Order(7)
    @DisplayName("查询行程分享")
    void testShareOrder() {
        // 验证 cancelafter flow - 已取消的订单不应出现在待接单列表中
        List<OrderVO> pendingOrders = orderService.listByUser(userId, 0, 1, 50);
        assertTrue(pendingOrders.stream().noneMatch(o -> o.getId().equals(orderId)),
                "已取消的订单不应出现在待接单列表中");
    }

    @Test
    @Order(8)
    @DisplayName("重复取消应抛出异常")
    void testDuplicateCancel() {
        assertNotNull(orderId);
        assertThrows(Exception.class, () -> orderService.cancel(orderId, userId, "再次取消"),
                "已取消的订单不可重复取消");
    }
}
