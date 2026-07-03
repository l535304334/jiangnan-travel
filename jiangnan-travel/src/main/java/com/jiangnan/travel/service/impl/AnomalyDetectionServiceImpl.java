package com.jiangnan.travel.service.impl;

import com.jiangnan.travel.service.AnomalyDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 异常检测实现 — 基于滑动窗口的规则检测。
 */
@Slf4j
@Service
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    // driverId → 连续拒单次数
    private final ConcurrentHashMap<Long, Integer> consecutiveRejects = new ConcurrentHashMap<>();
    // orderId → 累计失败次数
    private final ConcurrentHashMap<Long, Integer> orderFailures = new ConcurrentHashMap<>();
    // driverId → 最近评分变化记录 (modifier, timestamp)
    private final ConcurrentHashMap<Long, List<ScoreChange>> scoreChanges = new ConcurrentHashMap<>();
    // lockKey → 竞争计数
    private final ConcurrentHashMap<String, LongAdder> lockContention = new ConcurrentHashMap<>();

    private static final int REJECT_THRESHOLD = 5;
    private static final int FAILURE_THRESHOLD = 3;
    private static final double VOLATILITY_THRESHOLD = 30.0;
    private static final long VOLATILITY_WINDOW_MS = 60_000;
    private static final long LOCK_CONTENTION_THRESHOLD = 10;

    @Override
    public void recordDispatchEvent(Long orderId, Long driverId, boolean success, String reason) {
        if (!success && driverId != null) {
            orderFailures.merge(orderId, 1, Integer::sum);
        } else if (success) {
            orderFailures.remove(orderId); // 成功即清除
        }
    }

    @Override
    public void recordRejection(Long driverId) {
        consecutiveRejects.merge(driverId, 1, Integer::sum);
    }

    @Override
    public void recordLockContention(String lockKey) {
        lockContention.computeIfAbsent(lockKey, k -> new LongAdder()).increment();
    }

    /** 记录评分变化（供波动检测） */
    public void recordScoreChange(Long driverId, double oldModifier, double newModifier) {
        scoreChanges.computeIfAbsent(driverId, k -> new ArrayList<>())
                .add(new ScoreChange(oldModifier, newModifier, System.currentTimeMillis()));
        // 只保留最近100条
        List<ScoreChange> list = scoreChanges.get(driverId);
        if (list.size() > 100) list.subList(0, list.size() - 100).clear();
    }

    @Override
    public AnomalyReport detectAnomalies() {
        List<String> driverRejectSpikes = new ArrayList<>();
        List<String> orderRepeatedFailures = new ArrayList<>();
        List<String> scoringVolatility = new ArrayList<>();
        List<String> lockContentionSpikes = new ArrayList<>();

        // 1. 司机连续拒单检测
        consecutiveRejects.forEach((driverId, count) -> {
            if (count >= REJECT_THRESHOLD) {
                driverRejectSpikes.add(String.format(
                        "Driver[%d] 连续拒单 %d 次（阈值=%d）", driverId, count, REJECT_THRESHOLD));
            }
        });

        // 2. 订单多次失败检测
        orderFailures.forEach((orderId, count) -> {
            if (count >= FAILURE_THRESHOLD) {
                orderRepeatedFailures.add(String.format(
                        "Order[%d] 累计失败 %d 次（阈值=%d）", orderId, count, FAILURE_THRESHOLD));
            }
        });

        // 3. 评分极端波动检测
        long now = System.currentTimeMillis();
        scoreChanges.forEach((driverId, changes) -> {
            double maxChange = 0;
            long windowStart = now - VOLATILITY_WINDOW_MS;
            for (ScoreChange sc : changes) {
                if (sc.timestamp < windowStart) continue;
                double delta = Math.abs(sc.newModifier - sc.oldModifier);
                if (delta > maxChange) maxChange = delta;
            }
            if (maxChange > VOLATILITY_THRESHOLD) {
                scoringVolatility.add(String.format(
                        "Driver[%d] 评分波动 Δ=%.1f (1min内, 阈值=%.1f)", driverId, maxChange, VOLATILITY_THRESHOLD));
            }
        });

        // 4. 锁竞争热点检测
        lockContention.forEach((key, counter) -> {
            long count = counter.sum();
            if (count >= LOCK_CONTENTION_THRESHOLD) {
                lockContentionSpikes.add(String.format(
                        "Lock[%s] 竞争 %d 次（阈值=%d）", key, count, LOCK_CONTENTION_THRESHOLD));
            }
        });

        boolean hasCritical = !driverRejectSpikes.isEmpty() || !orderRepeatedFailures.isEmpty();

        return new AnomalyReport(
                driverRejectSpikes, orderRepeatedFailures,
                scoringVolatility, lockContentionSpikes, hasCritical);
    }

    @Override
    public void reset() {
        consecutiveRejects.clear();
        orderFailures.clear();
        scoreChanges.clear();
        lockContention.clear();
        log.info("AnomalyDetection: reset");
    }

    private record ScoreChange(double oldModifier, double newModifier, long timestamp) {}
}
