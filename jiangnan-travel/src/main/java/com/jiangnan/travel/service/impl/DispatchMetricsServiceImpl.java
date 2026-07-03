package com.jiangnan.travel.service.impl;

import com.jiangnan.travel.service.DispatchMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * 调度指标统计实现 — 线程安全。
 * ponytail: in-memory counters; flush to DB if cross-restart needed.
 */
@Slf4j
@Service
public class DispatchMetricsServiceImpl implements DispatchMetricsService {

    private final LongAdder totalAttempts = new LongAdder();
    private final LongAdder successCount = new LongAdder();
    private final LongAdder failureCount = new LongAdder();
    private final LongAdder rejectionCount = new LongAdder();
    private final LongAdder totalLatencyNanos = new LongAdder();
    private final LongAdder totalAttemptSum = new LongAdder();

    /** driverId → assigned order count */
    private final ConcurrentHashMap<Long, LongAdder> driverLoad = new ConcurrentHashMap<>();

    /** 最近的失败原因 (仅保留最近100条) */
    private final List<String> recentFailures = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void recordSuccess(Long orderId, Long driverId, int attemptCount, long costMs) {
        totalAttempts.increment();
        successCount.increment();
        totalLatencyNanos.add(costMs * 1_000_000L);
        totalAttemptSum.add(attemptCount);

        driverLoad.computeIfAbsent(driverId, k -> new LongAdder()).increment();
        log.debug("Metrics: success order={}, driver={}, attempt={}, costMs={}", orderId, driverId, attemptCount, costMs);
    }

    @Override
    public void recordFailure(Long orderId, String reason, int attemptCount, long costMs) {
        totalAttempts.increment();
        failureCount.increment();
        totalLatencyNanos.add(costMs * 1_000_000L);
        totalAttemptSum.add(attemptCount);

        String entry = String.format("order=%d reason=%s attempt=%d", orderId, reason, attemptCount);
        if (recentFailures.size() >= 100) recentFailures.remove(0);
        recentFailures.add(entry);
        log.warn("Metrics: failure order={}, reason={}", orderId, reason);
    }

    @Override
    public void recordRejection(Long orderId, Long driverId) {
        rejectionCount.increment();
        log.info("Metrics: rejection order={}, driver={}", orderId, driverId);
    }

    @Override
    public DispatchSummary getSummary() {
        long total = totalAttempts.sum();
        long success = successCount.sum();
        double rate = total > 0 ? (double) success / total * 100.0 : 0;
        double avgLatency = total > 0
                ? (double) totalLatencyNanos.sum() / total / 1_000_000.0 : 0;
        double avgAttempts = total > 0
                ? (double) totalAttemptSum.sum() / total : 0;

        // Top 5 drivers by load
        List<Map.Entry<Long, Integer>> topLoaded = driverLoad.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().intValue()))
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        return new DispatchSummary(
                total, success, failureCount.sum(), rejectionCount.sum(),
                Math.round(rate * 100.0) / 100.0,
                Math.round(avgLatency * 100.0) / 100.0,
                Math.round(avgAttempts * 100.0) / 100.0,
                topLoaded
        );
    }

    @Override
    public Map<Long, Integer> getDriverLoadDistribution() {
        Map<Long, Integer> result = new LinkedHashMap<>();
        driverLoad.forEach((k, v) -> result.put(k, v.intValue()));
        return result;
    }

    @Override
    public void reset() {
        totalAttempts.reset();
        successCount.reset();
        failureCount.reset();
        rejectionCount.reset();
        totalLatencyNanos.reset();
        totalAttemptSum.reset();
        driverLoad.clear();
        recentFailures.clear();
        log.info("Metrics: all counters reset");
    }

    /** 获取最近失败记录（供调试） */
    public List<String> getRecentFailures() {
        return new ArrayList<>(recentFailures);
    }
}
