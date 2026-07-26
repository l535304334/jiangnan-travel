package com.jiangnan.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.dto.CreateOrderRequest;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.vo.DailyOrderStatVO;
import com.jiangnan.travel.vo.OrderVO;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    OrderVO create(CreateOrderRequest request, Long userId);

    OrderVO getById(Long orderId, Long userId);

    List<OrderVO> listByUser(Long userId, Integer status, Integer page, Integer size);

    /** 用户历史订单总数（首页看板用）。 */
    long countByUser(Long userId);

    List<OrderVO> listByDriver(Long driverId, Integer status, Integer page, Integer size);

    void cancel(Long orderId, Long userId, String reason);

    OrderVO accept(Long orderId, Long driverId);

    OrderVO arrive(Long orderId, Long driverId);

    OrderVO startTrip(Long orderId, Long driverId);

    OrderVO complete(Long orderId, Long driverId);

    void pay(Long orderId, Long userId);

    void review(Long orderId, Long userId, Integer rating, String tags, String content);

    Page<Order> listOrders(Integer status, int page, int size);

    Order getOrderById(Long orderId);

    long countTodayOrders();

    BigDecimal getTodayRevenue();

    List<DailyOrderStatVO> getLast7DaysStats();

    List<OrderVO> findNearbyOrders(BigDecimal lat, BigDecimal lng, Integer limit);
}
