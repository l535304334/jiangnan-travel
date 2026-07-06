package com.jiangnan.travel;

import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.service.PaymentService;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.LoginVO;
import com.jiangnan.travel.vo.OrderVO;
import com.jiangnan.travel.vo.PaymentVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"jiangnan.websocket.enabled=false", "payment.mock.success-rate=100"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "sms:code:";
    private static final String PHONE = String.format("13288%06d", System.currentTimeMillis() % 1_000_000);
    private static Long userId;
    private static Long orderId;
    private static String payNo;
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
        req.setNickname("支付测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken());
        userId = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, PHONE)).getId();
        assertNotNull(userId);
    }

    @Test
    @Order(2)
    @DisplayName("创建已完成状态订单")
    void testCreateCompletedOrder() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setStartAddress("江南大学东门");
        req.setStartLat(new BigDecimal("31.2304"));
        req.setStartLng(new BigDecimal("121.4737"));
        req.setEndAddress("无锡火车站");
        req.setEndLat(new BigDecimal("31.2504"));
        req.setEndLng(new BigDecimal("121.4637"));
        req.setDistance(5000);
        req.setDuration(600);
        req.setCarTypeId(1L);
        OrderVO vo = orderService.create(req, userId);
        assertNotNull(vo);
        assertNotNull(vo.getId());
        orderId = vo.getId();

        // Update order status to 4 (completed) for payment test
        com.jiangnan.travel.entity.Order orderEntity = orderMapper.selectById(orderId);
        assertNotNull(orderEntity);
        orderEntity.setStatus(4);
        orderMapper.updateById(orderEntity);
    }

    @Test
    @Order(3)
    @DisplayName("创建支付")
    void testPay() {
        PaymentVO vo = paymentService.pay(orderId, userId, "alipay", null);
        assertNotNull(vo);
        assertEquals(orderId, vo.getOrderId());
        assertEquals("alipay", vo.getPayMethod());
        assertEquals(1, vo.getStatus()); // 模拟支付直接标记成功
        payNo = vo.getPayNo();
        assertNotNull(payNo);
    }

    @Test
    @Order(4)
    @DisplayName("查询支付信息")
    void testGetPayment() {
        PaymentVO vo = paymentService.getPaymentByOrder(orderId, userId);
        assertNotNull(vo);
        assertEquals(payNo, vo.getPayNo());
    }

    @Test
    @Order(5)
    @DisplayName("支付回调模拟")
    void testCallback() {
        PaymentVO vo = paymentService.mockCallback(payNo);
        assertEquals(1, vo.getStatus()); // 1=paid
        assertNotNull(vo.getPayTime());
    }

    @Test
    @Order(6)
    @DisplayName("支付记录列表")
    void testListPayments() {
        List<PaymentVO> list = paymentService.listByUser(userId);
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(p -> p.getOrderId().equals(orderId)));
    }

    @Test
    @Order(7)
    @DisplayName("行程中订单支付应报错")
    void testPayUncompletedOrder() {
        // 创建订单（status=CREATED）
        CreateOrderRequest req = new CreateOrderRequest();
        req.setStartAddress("江南大学东门");
        req.setStartLat(new BigDecimal("31.2304"));
        req.setStartLng(new BigDecimal("121.4737"));
        req.setEndAddress("无锡火车站");
        req.setEndLat(new BigDecimal("31.2504"));
        req.setEndLng(new BigDecimal("121.4637"));
        req.setDistance(3000);
        req.setDuration(300);
        req.setCarTypeId(1L);
        OrderVO vo = orderService.create(req, userId);

        // 把订单状态改为 IN_PROGRESS（行程中）— 此状态不允许支付
        com.jiangnan.travel.entity.Order orderEntity = orderMapper.selectById(vo.getId());
        assertNotNull(orderEntity);
        orderEntity.setStatus(com.jiangnan.travel.enums.OrderStatus.IN_PROGRESS.getCode());
        orderMapper.updateById(orderEntity);

        // 行程中订单支付应报错
        assertThrows(Exception.class,
                () -> paymentService.pay(vo.getId(), userId, "alipay", null));
    }
}
