package com.jiangnan.travel.service.impl;

import com.jiangnan.travel.service.AnomalyDetectionService;
import com.jiangnan.travel.service.DispatchMetricsService;
import com.jiangnan.travel.service.SystemHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统健康评分实现。
 * ponytail: simple weighted formula; externalize weights if tuning needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemHealthServiceImpl implements SystemHealthService {

    private final DispatchMetricsService metricsService;
    private final AnomalyDetectionService anomalyService;

    private static final double W_SUCCESS = 0.40;
    private static final double W_STABILITY = 0.30;
    private static final double W_CONFLICT = 0.20;
    private static final double W_LATENCY = 0.10;

    @Override
    public HealthResult computeHealth() {
        var summary = metricsService.getSummary();
        var anomalies = anomalyService.detectAnomalies();

        // 1. 成功率得分 (0~100)
        double successScore = summary.successRate();

        // 2. 稳定性得分: 异常越少越稳定
        int anomalyCount = anomalies.driverRejectSpikes().size()
                + anomalies.orderRepeatedFailures().size()
                + anomalies.scoringVolatility().size();
        double stabilityScore = Math.max(0, 100.0 - anomalyCount * 15.0);

        // 3. 冲突惩罚: 拒单率 + 锁竞争 → 扣分
        double conflictRate = summary.totalAttempts() > 0
                ? (double) summary.rejectionCount() / summary.totalAttempts()
                : 0;
        double lockContentionPenalty = Math.min(anomalies.lockContentionSpikes().size() * 10.0, 30.0);
        double conflictScore = Math.max(0, 100.0 - conflictRate * 100.0 - lockContentionPenalty);

        // 4. 延迟得分: <100ms满分, >1000ms零分
        double avgLatency = summary.avgLatencyMs();
        double latencyScore = Math.max(0, 100.0 - avgLatency / 10.0);

        // 综合
        double overall = W_SUCCESS * successScore
                + W_STABILITY * stabilityScore
                + W_CONFLICT * conflictScore
                + W_LATENCY * latencyScore;

        // 评级
        String grade;
        if (overall >= 90) grade = "S";
        else if (overall >= 75) grade = "A";
        else if (overall >= 60) grade = "B";
        else if (overall >= 40) grade = "C";
        else grade = "D";

        // 建议
        String recommendation;
        if (anomalies.hasCriticalIssues()) {
            recommendation = "发现严重异常：" + String.join("; ", anomalies.driverRejectSpikes());
        } else if (overall >= 75) {
            recommendation = "系统健康，可继续扩容";
        } else if (overall >= 60) {
            recommendation = "系统基本正常，建议关注锁竞争和拒单率";
        } else {
            recommendation = "系统不稳定，建议减少并发或增加司机资源";
        }

        return new HealthResult(
                Math.round(overall * 100.0) / 100.0,
                Math.round(successScore * 100.0) / 100.0,
                Math.round(stabilityScore * 100.0) / 100.0,
                Math.round(conflictScore * 100.0) / 100.0,
                Math.round(latencyScore * 100.0) / 100.0,
                grade, recommendation);
    }
}
