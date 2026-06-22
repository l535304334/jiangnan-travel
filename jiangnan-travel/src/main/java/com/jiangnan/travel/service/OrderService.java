package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderVO create(CreateOrderRequest request, Long userId);

    OrderVO getById(Long orderId, Long userId);

    List<OrderVO> listByUser(Long userId, Integer status, Integer page, Integer size);

    void cancel(Long orderId, Long userId, String reason);

    OrderVO accept(Long orderId, Long driverId);

    OrderVO arrive(Long orderId, Long driverId);

    OrderVO startTrip(Long orderId, Long driverId);

    OrderVO complete(Long orderId, Long driverId);

    void pay(Long orderId, Long userId);

    void review(Long orderId, Long userId, Integer rating, String tags, String content);
}
