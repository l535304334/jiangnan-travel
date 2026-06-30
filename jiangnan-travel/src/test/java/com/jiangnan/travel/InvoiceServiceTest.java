package com.jiangnan.travel;

import com.jiangnan.travel.dto.ApplyInvoiceRequest;
import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.service.InvoiceService;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.service.PaymentService;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.InvoiceVO;
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

@SpringBootTest(properties = "jiangnan.websocket.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvoiceServiceTest {

    @Autowired
    private InvoiceService invoiceService;

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
    private static final String PHONE = String.format("13188%06d", System.currentTimeMillis() % 1_000_000);
    private static Long userId;
    private static Long orderId;
    private static Long invoiceId;
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
        req.setNickname("发票测试用户");
        LoginVO result = userService.register(req);
        assertNotNull(result.getToken());
        userId = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, PHONE)).getId();
        assertNotNull(userId);
    }

    @Test
    @Order(2)
    @DisplayName("创建已完成订单并支付")
    void testCreatePaidOrder() {
        // Create order
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
        orderId = vo.getId();

        // Update order to completed (status=4)
        com.jiangnan.travel.entity.Order orderEntity = orderMapper.selectById(orderId);
        orderEntity.setStatus(4);
        orderMapper.updateById(orderEntity);

        // Pay the order
        PaymentVO pvo = paymentService.pay(orderId, userId, "alipay", null);
        paymentService.mockCallback(pvo.getPayNo());
    }

    @Test
    @Order(3)
    @DisplayName("申请发票")
    void testApplyInvoice() {
        ApplyInvoiceRequest req = new ApplyInvoiceRequest();
        req.setOrderId(orderId);
        req.setTitle("江南大学");
        req.setTaxNo("91320200123456789X");
        InvoiceVO vo = invoiceService.apply(req, userId);
        assertNotNull(vo);
        assertNotNull(vo.getId());
        assertEquals(0, vo.getStatus()); // 0=待开具
        invoiceId = vo.getId();
    }

    @Test
    @Order(4)
    @DisplayName("查看发票详情")
    void testGetInvoice() {
        InvoiceVO vo = invoiceService.getById(invoiceId, userId);
        assertNotNull(vo);
        assertEquals("江南大学", vo.getTitle());
        assertEquals(orderId, vo.getOrderId());
    }

    @Test
    @Order(5)
    @DisplayName("发票记录列表")
    void testListInvoices() {
        List<InvoiceVO> list = invoiceService.listByUser(userId);
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(i -> i.getId().equals(invoiceId)));
    }

    @Test
    @Order(6)
    @DisplayName("取消发票")
    void testCancelInvoice() {
        assertDoesNotThrow(() -> invoiceService.cancel(invoiceId, userId));
        InvoiceVO vo = invoiceService.getById(invoiceId, userId);
        assertEquals(2, vo.getStatus()); // 2=已取消
    }

    @Test
    @Order(7)
    @DisplayName("重复申请发票应报错")
    void testDuplicateApply() {
        // Create another order for this test
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
        com.jiangnan.travel.entity.Order orderEntity2 = orderMapper.selectById(vo.getId());
        orderEntity2.setStatus(4);
        orderMapper.updateById(orderEntity2);

        // Apply for this order
        ApplyInvoiceRequest applyReq = new ApplyInvoiceRequest();
        applyReq.setOrderId(vo.getId());
        applyReq.setTitle("测试公司");
        invoiceService.apply(applyReq, userId);

        // Duplicate apply
        assertThrows(Exception.class,
                () -> invoiceService.apply(applyReq, userId));
    }
}
