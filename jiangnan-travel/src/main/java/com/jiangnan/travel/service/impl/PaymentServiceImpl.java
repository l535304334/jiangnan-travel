package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.Payment;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.PaymentMapper;
import com.jiangnan.travel.service.NotificationService;
import com.jiangnan.travel.service.PaymentService;
import com.jiangnan.travel.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final String PAY_LOCK_PREFIX = "order:lock:pay:";
    private static final String PAY_IDEMPOTENT_PREFIX = "pay:idempotent:";

    @Override
    public PaymentVO pay(Long orderId, Long userId, String payMethod, String idempotentKey) {
        final String finalIdempotentKey = (idempotentKey == null || idempotentKey.isEmpty())
                ? PAY_IDEMPOTENT_PREFIX + orderId + ":" + userId
                : idempotentKey;
        String lockKey = PAY_LOCK_PREFIX + finalIdempotentKey;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        }

        try {
            // 在锁内执行事务，确保事务提交后才释放锁
            return transactionTemplate.execute(status -> {
                // 1. 校验订单
                Order order = orderMapper.selectById(orderId);
                if (order == null || !order.getUserId().equals(userId)) {
                    throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
                }
                if (order.getStatus() != 4) {
                    throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
                }

                // 2. 检查是否已支付
                Payment existing = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>()
                                .eq(Payment::getOrderId, orderId)
                                .eq(Payment::getStatus, 1)
                                .last("LIMIT 1"));
                if (existing != null) {
                    throw new BusinessException(ErrorCode.PAY_FAILED, "该订单已支付");
                }

                // 3. 幂等键检查：同一幂等键仅允许一笔支付记录
                Payment idempotentPayment = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>()
                                .eq(Payment::getIdempotentKey, finalIdempotentKey)
                                .last("LIMIT 1"));
                if (idempotentPayment != null) {
                    return toVO(idempotentPayment, order);
                }

                // 4. 生成支付流水号
                String payNo = "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + String.format("%04d", new Random().nextInt(10000));

                // 5. 创建支付记录（模拟：直接标记成功）
                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setUserId(userId);
                payment.setAmount(order.getFinalPrice());
                payment.setPayMethod(payMethod != null ? payMethod : "balance");
                payment.setPayNo(payNo);
                payment.setIdempotentKey(finalIdempotentKey);
                payment.setStatus(1);
                payment.setPayTime(LocalDateTime.now());
                paymentMapper.insert(payment);

                // 5. 发送通知
                notificationService.create(userId, "PAY_SUCCESS",
                        "支付成功", "订单 " + order.getOrderNo() + " 已支付 ¥" + order.getFinalPrice(), orderId);

                log.info("用户[{}]支付订单[{}]成功，方式={}，金额={}", userId, orderId, payMethod, order.getFinalPrice());
                return toVO(payment, order);
            });
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public PaymentVO getPaymentByOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getOrderId, orderId)
                        .eq(Payment::getStatus, 1)
                        .last("LIMIT 1"));

        if (payment == null) {
            return PaymentVO.builder()
                    .orderId(orderId)
                    .orderNo(order.getOrderNo())
                    .amount(order.getFinalPrice())
                    .status(0)
                    .statusText("待支付")
                    .startAddress(order.getStartAddress())
                    .endAddress(order.getEndAddress())
                    .build();
        }
        return toVO(payment, order);
    }

    @Override
    public List<PaymentVO> listByUser(Long userId) {
        List<Payment> payments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getUserId, userId)
                        .orderByDesc(Payment::getCreateTime));

        return payments.stream().map(p -> {
            Order order = orderMapper.selectById(p.getOrderId());
            return toVO(p, order);
        }).collect(Collectors.toList());
    }

    @Override
    public PaymentVO mockCallback(String payNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getPayNo, payNo));
        if (payment == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (payment.getStatus() == 0) {
            payment.setStatus(1);
            payment.setPayTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
        }
        Order order = orderMapper.selectById(payment.getOrderId());
        return toVO(payment, order);
    }

    private PaymentVO toVO(Payment payment, Order order) {
        if (payment == null) return null;
        String methodName = switch (payment.getPayMethod()) {
            case "wxpay" -> "微信支付";
            case "alipay" -> "支付宝";
            default -> "余额支付";
        };
        return PaymentVO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .amount(payment.getAmount())
                .payMethod(payment.getPayMethod())
                .payMethodName(methodName)
                .payNo(payment.getPayNo())
                .status(payment.getStatus())
                .statusText(payment.getStatus() == 1 ? "支付成功" : payment.getStatus() == 2 ? "支付失败" : "待支付")
                .payTime(payment.getPayTime())
                .startAddress(order != null ? order.getStartAddress() : null)
                .endAddress(order != null ? order.getEndAddress() : null)
                .build();
    }
}
