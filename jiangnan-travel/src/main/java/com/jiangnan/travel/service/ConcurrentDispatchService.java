package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.vo.OrderVO;

/**
 * 并发安全的多订单调度服务。
 * 核心保证：一个司机同一时间只能接一个订单，一个订单只能分配一个司机。
 */
public interface ConcurrentDispatchService {

    /**
     * 并发安全的派单。
     * 锁顺序: order lock → driver lock → 分配 → 释放。
     */
    OrderVO dispatchWithDriverLock(Long orderId);

    /**
     * 批量并发派单（测试用）。
     * @return 成功分配数
     */
    int batchDispatch(java.util.List<Long> orderIds);

    /**
     * 司机心跳更新（模拟GPS位置 + 活跃时间）。
     */
    void heartbeat(Long driverId, java.math.BigDecimal lat, java.math.BigDecimal lng);

    /**
     * 释放司机锁（订单完成/取消时调用）。
     */
    void releaseDriver(Long driverId);
}
