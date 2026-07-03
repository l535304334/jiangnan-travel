package com.jiangnan.travel.service;

import java.util.List;
import java.util.Map;

/**
 * 调度指标统计服务。
 */
public interface DispatchMetricsService {

    /** 记录一次派单成功 */
    void recordSuccess(Long orderId, Long driverId, int attemptCount, long costMs);

    /** 记录一次派单失败 */
    void recordFailure(Long orderId, String reason, int attemptCount, long costMs);

    /** 记录拒单 */
    void recordRejection(Long orderId, Long driverId);

    /** 获取统计摘要 */
    DispatchSummary getSummary();

    /** 获取司机负载分布 */
    Map<Long, Integer> getDriverLoadDistribution();

    /** 重置计数器（压测前调用） */
    void reset();

    /** 统计摘要 */
    record DispatchSummary(
            long totalAttempts,
            long successCount,
            long failureCount,
            long rejectionCount,
            double successRate,
            double avgLatencyMs,
            double avgAttempts,
            List<Map.Entry<Long, Integer>> topLoadedDrivers
    ) {}
}
