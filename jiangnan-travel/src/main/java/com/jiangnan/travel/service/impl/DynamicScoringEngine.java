package com.jiangnan.travel.service.impl;

import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.service.DriverContext;
import com.jiangnan.travel.service.ScoringEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态学习型评分引擎 — 基于反馈闭环自适应调整。
 *
 * 机制:
 *   baseScore = WeightedScoringEngine.score()
 *   finalScore = baseScore + driverModifier
 *
 * 反馈:
 *   accept  → modifier += REWARD (上限 +20)
 *   reject  → modifier -= PENALTY (下限 -30)
 *   cancel  → modifier -= LIGHT_PENALTY
 *
 * 衰减: 每10分钟未活动, modifier 向0回归50%
 *
 * ponytail: in-memory ConcurrentHashMap; persist to DB if cross-restart required.
 */
@Slf4j
@Component("dynamicScoringEngine")
public class DynamicScoringEngine implements ScoringEngine {

    private final WeightedScoringEngine baseEngine;

    /** 每司机的动态修正分 (可正可负) */
    private final ConcurrentHashMap<Long, DriverScoreState> scoreState = new ConcurrentHashMap<>();

    private static final double REWARD = 5.0;
    private static final double PENALTY = -10.0;
    private static final double LIGHT_PENALTY = -3.0;
    private static final double MAX_BONUS = 20.0;
    private static final double MAX_MALUS = -30.0;
    /** 衰减半衰期(毫秒): 10分钟 */
    private static final long DECAY_HALF_LIFE_MS = 10 * 60 * 1000;

    public DynamicScoringEngine(WeightedScoringEngine baseEngine) {
        this.baseEngine = baseEngine;
    }

    @Override
    public double score(Driver driver, Order order, DriverContext context) {
        double base = baseEngine.score(driver, order, context);
        DriverScoreState state = scoreState.get(driver.getId());
        if (state == null) return base;

        double modifier = decayIfNeeded(state);
        double finalScore = base + modifier;

        log.debug("DynamicScore[{}]: base={:.1f} modifier={:.1f} final={:.1f} (a={}, r={}, c={})",
                driver.getId(), base, modifier, finalScore,
                state.acceptCount, state.rejectCount, state.cancelCount);
        return Math.max(0, Math.min(100, finalScore));
    }

    @Override
    public void recordAccept(Long driverId) {
        DriverScoreState state = scoreState.computeIfAbsent(driverId, k -> new DriverScoreState());
        state.acceptCount++;
        state.modifier = clamp(state.modifier + REWARD, MAX_MALUS, MAX_BONUS);
        state.lastFeedbackTime = System.currentTimeMillis();
        log.info("Score[{}] accept → modifier={}", driverId, state.modifier);
    }

    @Override
    public void recordReject(Long driverId) {
        DriverScoreState state = scoreState.computeIfAbsent(driverId, k -> new DriverScoreState());
        state.rejectCount++;
        state.modifier = clamp(state.modifier + PENALTY, MAX_MALUS, MAX_BONUS);
        state.lastFeedbackTime = System.currentTimeMillis();
        log.info("Score[{}] reject → modifier={}", driverId, state.modifier);
    }

    @Override
    public void recordCancel(Long driverId) {
        DriverScoreState state = scoreState.computeIfAbsent(driverId, k -> new DriverScoreState());
        state.cancelCount++;
        state.modifier = clamp(state.modifier + LIGHT_PENALTY, MAX_MALUS, MAX_BONUS);
        state.lastFeedbackTime = System.currentTimeMillis();
        log.info("Score[{}] cancel → modifier={}", driverId, state.modifier);
    }

    /** 获取所有司机的评分状态（供监控/调试） */
    public ConcurrentHashMap<Long, DriverScoreState> getAllStates() {
        return scoreState;
    }

    /** 手动重置某司机 */
    public void reset(Long driverId) {
        scoreState.remove(driverId);
    }

    // ── private ──

    private double decayIfNeeded(DriverScoreState state) {
        if (state.lastFeedbackTime == 0) return state.modifier;
        long elapsed = System.currentTimeMillis() - state.lastFeedbackTime;
        if (elapsed < DECAY_HALF_LIFE_MS) return state.modifier;

        // 每过一个半衰期, modifier 向0靠近50%
        double halves = (double) elapsed / DECAY_HALF_LIFE_MS;
        double factor = Math.pow(0.5, halves);
        double decayed = state.modifier * factor;
        if (Math.abs(decayed) < 0.1) {
            state.modifier = 0;
            state.lastFeedbackTime = 0;
            return 0;
        }
        state.modifier = decayed;
        state.lastFeedbackTime = System.currentTimeMillis();
        log.debug("Score[{}] decayed to {:.2f}", state.modifier);
        return state.modifier;
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    /** 司机评分状态 */
    public static class DriverScoreState {
        public double modifier;
        public int acceptCount;
        public int rejectCount;
        public int cancelCount;
        public long lastFeedbackTime;
    }
}
