package com.jiangnan.travel.service.impl;

import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.service.DriverContext;
import com.jiangnan.travel.service.ScoringEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 加权评分引擎：距离40% + 评分30% + 空闲时间20% + 拒单惩罚10%。
 * ponytail: weights are constants; externalize to config if tuning frequency increases.
 */
@Slf4j
@Component
public class WeightedScoringEngine implements ScoringEngine {

    // 权重常量
    private static final double W_DISTANCE = 0.40;
    private static final double W_RATING = 0.30;
    private static final double W_IDLE = 0.20;
    private static final double W_REJECTION = 0.10;

    /** 理想最大距离(km)，用于归一化 */
    private static final double MAX_DISTANCE_KM = 20.0;
    /** 最大空闲分钟（超过此值满分） */
    private static final double MAX_IDLE_MINUTES = 60.0;
    /** 满分100 */
    private static final double MAX_SCORE = 100.0;

    @Override
    public double score(Driver driver, Order order, DriverContext context) {
        double distScore = computeDistanceScore(driver, order);
        double ratingScore = computeRatingScore(driver);
        double idleScore = computeIdleScore(context);
        double rejectionScore = computeRejectionScore(context);

        double total = W_DISTANCE * distScore
                + W_RATING * ratingScore
                + W_IDLE * idleScore
                + W_REJECTION * rejectionScore;

        log.debug("Driver[{}] score={:.1f} (dist={:.1f}, rating={:.1f}, idle={:.1f}, reject={:.1f})",
                driver.getId(), total, distScore, ratingScore, idleScore, rejectionScore);
        return total;
    }

    /** 距离分：越近分越高(0~100) */
    private double computeDistanceScore(Driver driver, Order order) {
        double dLat = safeDouble(driver.getLat()) - safeDouble(order.getStartLat());
        double dLng = safeDouble(driver.getLng()) - safeDouble(order.getStartLng());
        double distDeg = Math.sqrt(dLat * dLat + dLng * dLng);
        double distKm = distDeg * 111.0; // 1度 ≈ 111km
        double normalized = Math.min(distKm / MAX_DISTANCE_KM, 1.0);
        return (1.0 - normalized) * MAX_SCORE; // 越近分越高
    }

    /** 评分分：平均评分 * 20 (0~100) */
    private double computeRatingScore(Driver driver) {
        BigDecimal r = driver.getAvgRating() != null ? driver.getAvgRating() : BigDecimal.valueOf(4.0);
        return Math.min(r.doubleValue() * 20.0, MAX_SCORE);
    }

    /** 空闲分：空闲越久分越高(0~100) */
    private double computeIdleScore(DriverContext context) {
        if (context == null) return 50.0; // 无数据给中位分
        double normalized = Math.min((double) context.getIdleMinutes() / MAX_IDLE_MINUTES, 1.0);
        return normalized * MAX_SCORE;
    }

    /** 拒单惩罚：拒单次数越多分越低(0~100) */
    private double computeRejectionScore(DriverContext context) {
        if (context == null) return MAX_SCORE;
        int count = Math.min(context.getRejectionCount(), 10);
        return MAX_SCORE * (1.0 - count * 0.10); // 每次拒单扣10%
    }

    private double safeDouble(BigDecimal v) {
        return v != null ? v.doubleValue() : 0.0;
    }

    // ── v1.3 反馈接口（静态引擎无动态状态，空实现）──

    @Override public void recordAccept(Long driverId) { /* no-op: static engine */ }
    @Override public void recordReject(Long driverId) { /* no-op: static engine */ }
    @Override public void recordCancel(Long driverId) { /* no-op: static engine */ }
}
