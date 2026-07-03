package com.jiangnan.travel.service;

import java.util.List;
import java.util.Map;

/**
 * 异常行为检测服务。
 * ponytail: rule-based detection; upgrade to ML anomaly model if false-positive rate rises.
 */
public interface AnomalyDetectionService {

    /** 记录一次派单事件 */
    void recordDispatchEvent(Long orderId, Long driverId, boolean success, String reason);

    /** 记录一次拒单事件 */
    void recordRejection(Long driverId);

    /** 记录一次锁竞争事件 */
    void recordLockContention(String lockKey);

    /** 检测异常并返回报告 */
    AnomalyReport detectAnomalies();

    /** 重置检测器 */
    void reset();

    /** 异常报告 */
    record AnomalyReport(
            List<String> driverRejectSpikes,      // 司机连续拒单异常
            List<String> orderRepeatedFailures,    // 订单多次失败
            List<String> scoringVolatility,        // 评分极端波动
            List<String> lockContentionSpikes,     // 锁竞争热点
            boolean hasCriticalIssues
    ) {}
}
