package com.jiangnan.travel.service;

import com.jiangnan.travel.vo.PaymentVO;

import java.util.List;

public interface PaymentService {

    PaymentVO pay(Long orderId, Long userId, String payMethod, String idempotentKey);

    PaymentVO getPaymentByOrder(Long orderId, Long userId);

    List<PaymentVO> listByUser(Long userId);

    PaymentVO mockCallback(String payNo);
}
