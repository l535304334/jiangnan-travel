package com.jiangnan.travel.service;

/**
 * 系统健康评分服务。
 *
 * SystemHealthScore = 成功率(40%) + 稳定性(30%) - 冲突率(20%) + 延迟Score(10%)
 */
public interface SystemHealthService {

    /** 计算当前系统健康评分 (0~100) */
    HealthResult computeHealth();

    /**
     * 健康评分结果。
     */
    record HealthResult(
            double overallScore,        // 0~100
            double successRateScore,    // 成功率得分
            double stabilityScore,      // 稳定性得分
            double conflictPenalty,     // 冲突惩罚（越小越好）
            double latencyScore,        // 延迟得分
            String grade,               // S/A/B/C/D
            String recommendation       // 建议
    ) {}
}
