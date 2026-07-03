package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.Bill;
import com.jiangnan.travel.entity.Order;

/**
 * 计费服务 — 订单完成时自动生成账单。
 */
public interface BillingService {

    /**
     * 根据订单生成账单。
     * ponytail: distance × unitPrice + peakSurcharge - couponDiscount + tollFee
     */
    Bill generateBill(Order order);

    Bill getByOrderId(Long orderId);
}
