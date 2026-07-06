package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.Payment;
import com.jiangnan.travel.entity.PaymentTrace;
import com.jiangnan.travel.enums.PaymentStatus;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.PaymentMapper;
import com.jiangnan.travel.mapper.PaymentTraceMapper;
import com.jiangnan.travel.service.NotificationService;
import com.jiangnan.travel.service.PaymentService;
import com.jiangnan.travel.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final PaymentTraceMapper paymentTraceMapper;
    private final NotificationService notificationService;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final String PAY_LOCK_PREFIX = "order:lock:pay:";
    private static final String PAY_IDEMPOTENT_PREFIX = "pay:idempotent:";
    /** 最大重试次数 */
    private static final int MAX_RETRY = 3;

    /** 模拟支付成功率（0-100），默认 90%；测试环境可配置为 100 以消除随机失败 */
    @Value("${payment.mock.success-rate:90}")
    private int mockSuccessRate;

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
            // ── 支付重试 + 追踪 ──
            return transactionTemplate.execute(status -> {
                // 1. 校验订单
                Order order = orderMapper.selectById(orderId);
                if (order == null || !order.getUserId().equals(userId)) {
                    traceAndThrow(null, orderId, userId, 1, payMethod, null, null,
                            finalIdempotentKey, "订单不存在");
                }
                // 允许 CREATED → 预支付 或 COMPLETED → 后付费
                if (order.getStatus() != com.jiangnan.travel.enums.OrderStatus.CREATED.getCode()
                        && order.getStatus() != com.jiangnan.travel.enums.OrderStatus.COMPLETED.getCode()) {
                    traceAndThrow(null, orderId, userId, 1, payMethod, order.getFinalPrice(), null,
                            finalIdempotentKey, "订单状态不允许支付: " + order.getStatus());
                }

                // 2. 检查是否已支付
                Payment existing = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>()
                                .eq(Payment::getOrderId, orderId)
                                .eq(Payment::getStatus, PaymentStatus.PAID.getCode())
                                .last("LIMIT 1"));
                if (existing != null) {
                    throw new BusinessException(ErrorCode.PAY_FAILED, "该订单已支付");
                }

                // 3. 幂等键检查
                Payment idempotentPayment = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>()
                                .eq(Payment::getIdempotentKey, finalIdempotentKey)
                                .last("LIMIT 1"));
                if (idempotentPayment != null) {
                    return toVO(idempotentPayment, order);
                }

                // 4. 查已有失败记录，决定重试
                int attemptNo = 1;
                Payment failedPayment = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>()
                                .eq(Payment::getOrderId, orderId)
                                .eq(Payment::getStatus, PaymentStatus.FAILED.getCode())
                                .last("LIMIT 1"));
                if (failedPayment != null) {
                    attemptNo = (failedPayment.getRetryCount() != null ? failedPayment.getRetryCount() : 0) + 1;
                    if (attemptNo > MAX_RETRY) {
                        traceAndThrow(failedPayment.getId(), orderId, userId, attemptNo, payMethod,
                                order.getFinalPrice(), null, finalIdempotentKey,
                                "超过最大重试次数 " + MAX_RETRY);
                    }
                }

                // 5. 执行支付（模拟：90% 成功率；真实环境接入微信/支付宝 SDK）
                long startMs = System.currentTimeMillis();
                String payNo = "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + String.format("%04d", new Random().nextInt(10000));
                boolean paySuccess = new Random().nextInt(100) < mockSuccessRate; // 可配置的模拟成功率

                if (!paySuccess) {
                    int costMs = (int) (System.currentTimeMillis() - startMs);
                    Payment failRecord = (failedPayment != null) ? failedPayment : new Payment();
                    if (failedPayment == null) {
                        failRecord.setOrderId(orderId);
                        failRecord.setUserId(userId);
                        failRecord.setAmount(order.getFinalPrice());
                        failRecord.setPayMethod(payMethod != null ? payMethod : "balance");
                        failRecord.setIdempotentKey(finalIdempotentKey);
                    }
                    failRecord.setPayNo(payNo);
                    failRecord.setStatus(PaymentStatus.FAILED.getCode());
                    failRecord.setRetryCount(attemptNo);
                    failRecord.setFailReason("模拟支付失败 (第" + attemptNo + "次尝试)");
                    if (failedPayment != null) {
                        paymentMapper.updateById(failRecord);
                    } else {
                        paymentMapper.insert(failRecord);
                    }
                    // trace
                    tracePayment(failRecord.getId(), orderId, userId, attemptNo,
                            PaymentStatus.FAILED.getCode(), payMethod, payNo, finalIdempotentKey,
                            order.getFinalPrice(), "模拟支付失败", costMs);
                    if (attemptNo >= MAX_RETRY) {
                        throw new BusinessException(ErrorCode.PAY_FAILED,
                                "支付失败，已重试 " + attemptNo + " 次，请稍后再试");
                    }
                    throw new BusinessException(ErrorCode.PAY_FAILED,
                            "支付失败(第" + attemptNo + "次)，将自动重试");
                }

                int costMs = (int) (System.currentTimeMillis() - startMs);

                // 6. 创建/更新支付记录为成功
                Payment payment = (failedPayment != null) ? failedPayment : new Payment();
                if (failedPayment == null) {
                    payment.setOrderId(orderId);
                    payment.setUserId(userId);
                    payment.setAmount(order.getFinalPrice());
                    payment.setPayMethod(payMethod != null ? payMethod : "balance");
                    payment.setIdempotentKey(finalIdempotentKey);
                }
                payment.setPayNo(payNo);
                payment.setStatus(PaymentStatus.PAID.getCode());
                payment.setRetryCount(attemptNo);
                payment.setPayTime(LocalDateTime.now());
                payment.setFailReason(null);
                if (failedPayment != null) {
                    paymentMapper.updateById(payment);
                } else {
                    paymentMapper.insert(payment);
                }

                // 7. 追踪成功
                tracePayment(payment.getId(), orderId, userId, attemptNo,
                        PaymentStatus.PAID.getCode(), payMethod, payNo, finalIdempotentKey,
                        order.getFinalPrice(), null, costMs);

                // 8. 发送通知
                notificationService.create(userId, "PAY_SUCCESS",
                        "支付成功", "订单 " + order.getOrderNo() + " 已支付 ¥" + order.getFinalPrice(), orderId);

                log.info("用户[{}]支付订单[{}]成功，方式={}，金额={}，尝试次数={}",
                        userId, orderId, payMethod, order.getFinalPrice(), attemptNo);
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
                        .eq(Payment::getStatus, PaymentStatus.PAID.getCode())
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

        // ponytail: batch load orders to avoid N+1
        List<Long> orderIds = payments.stream().map(Payment::getOrderId).distinct().toList();
        Map<Long, Order> orderMap = orderIds.isEmpty() ? java.util.Collections.emptyMap()
                : orderMapper.selectBatchIds(orderIds).stream()
                        .collect(Collectors.toMap(Order::getId, o -> o));
        return payments.stream().map(p -> toVO(p, orderMap.get(p.getOrderId()))).collect(Collectors.toList());
    }

    @Override
    public PaymentVO mockCallback(String payNo) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getPayNo, payNo));
        if (payment == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (payment.getStatus() == PaymentStatus.PENDING.getCode()) {
            payment.setStatus(PaymentStatus.PAID.getCode());
            payment.setPayTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
        }
        Order order = orderMapper.selectById(payment.getOrderId());
        return toVO(payment, order);
    }

    // ── 支付追踪辅助 ──

    /** 记录一条支付追踪日志 */
    private void tracePayment(Long paymentId, Long orderId, Long userId, int attemptNo,
                              int status, String payMethod, String payNo, String idempotentKey,
                              java.math.BigDecimal amount, String failReason, int costMs) {
        PaymentTrace trace = new PaymentTrace();
        trace.setPaymentId(paymentId);
        trace.setOrderId(orderId);
        trace.setUserId(userId);
        trace.setAttemptNo(attemptNo);
        trace.setStatus(status);
        trace.setPayMethod(payMethod);
        trace.setPayNo(payNo);
        trace.setIdempotentKey(idempotentKey);
        trace.setAmount(amount);
        trace.setFailReason(failReason);
        trace.setCostMs(costMs);
        trace.setTraceTime(LocalDateTime.now());
        paymentTraceMapper.insert(trace);
    }

    /** 记录追踪日志后抛出业务异常 */
    private void traceAndThrow(Long paymentId, Long orderId, Long userId, int attemptNo,
                               String payMethod, java.math.BigDecimal amount, String payNo,
                               String idempotentKey, String reason) {
        tracePayment(paymentId, orderId, userId, attemptNo, PaymentStatus.FAILED.getCode(),
                payMethod, payNo, idempotentKey, amount, reason, 0);
        throw new BusinessException(ErrorCode.PAY_FAILED, reason);
    }

    private PaymentVO toVO(Payment payment, Order order) {
        if (payment == null) return null;
        String methodName = switch (payment.getPayMethod()) {
            case "wxpay" -> "微信支付";
            case "alipay" -> "支付宝";
            default -> "余额支付";
        };
        String statusText;
        try {
            statusText = PaymentStatus.fromCode(payment.getStatus()).getLabel();
        } catch (IllegalArgumentException e) {
            statusText = "未知";
        }
        return PaymentVO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .amount(payment.getAmount())
                .payMethod(payment.getPayMethod())
                .payMethodName(methodName)
                .payNo(payment.getPayNo())
                .status(payment.getStatus())
                .statusText(statusText)
                .payTime(payment.getPayTime())
                .startAddress(order != null ? order.getStartAddress() : null)
                .endAddress(order != null ? order.getEndAddress() : null)
                .build();
    }
}
