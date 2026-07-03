package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.AnomalyDetectionService;
import com.jiangnan.travel.service.ConcurrentDispatchService;
import com.jiangnan.travel.service.DispatchMetricsService;
import com.jiangnan.travel.service.DriverAssignmentService;
import com.jiangnan.travel.service.BillingService;
import com.jiangnan.travel.service.SystemHealthService;
import com.jiangnan.travel.service.impl.DynamicScoringEngine;
import com.jiangnan.travel.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 派单 + 计费 + 司机心跳 API (v1.2)。
 */
@Tag(name = "派单与计费", description = "司机派单、接单、拒单、账单查询、心跳、并发调度")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final DriverAssignmentService assignmentService;
    private final ConcurrentDispatchService dispatchService;
    private final DispatchMetricsService metricsService;
    private final DynamicScoringEngine dynamicScoringEngine;
    private final AnomalyDetectionService anomalyService;
    private final SystemHealthService healthService;
    private final BillingService billingService;

    @Operation(summary = "为订单派单")
    @PostMapping("/order/{orderId}/assign")
    public Result<OrderVO> assign(@PathVariable Long orderId) {
        return Result.ok(assignmentService.assignDriver(orderId));
    }

    @Operation(summary = "司机接受派单")
    @PostMapping("/order/{orderId}/accept-assignment")
    public Result<OrderVO> acceptAssignment(@PathVariable Long orderId,
                                            @RequestParam Long driverId) {
        return Result.ok(assignmentService.acceptAssignment(orderId, driverId));
    }

    @Operation(summary = "司机拒绝派单（自动重派）")
    @PostMapping("/order/{orderId}/reject-assignment")
    public Result<OrderVO> rejectAssignment(@PathVariable Long orderId,
                                            @RequestParam Long driverId,
                                            @RequestParam(required = false) String reason) {
        return Result.ok(assignmentService.rejectAssignment(orderId, driverId, reason));
    }

    @Operation(summary = "查询订单账单")
    @GetMapping("/order/{orderId}/bill")
    public Result<?> getBill(@PathVariable Long orderId) {
        return Result.ok(billingService.getByOrderId(orderId));
    }

    // ── v1.2 并发调度 + 心跳 ──

    @Operation(summary = "并发安全派单（v1.2）")
    @PostMapping("/order/{orderId}/dispatch")
    public Result<OrderVO> concurrentDispatch(@PathVariable Long orderId) {
        return Result.ok(dispatchService.dispatchWithDriverLock(orderId));
    }

    @Operation(summary = "批量并发派单（v1.2）")
    @PostMapping("/order/batch-dispatch")
    public Result<Map<String, Object>> batchDispatch(@RequestBody Map<String, java.util.List<Long>> body) {
        int success = dispatchService.batchDispatch(body.get("orderIds"));
        return Result.ok(Map.of("success", success, "total", body.get("orderIds").size()));
    }

    @Operation(summary = "司机心跳（v1.2 — 更新GPS+活跃时间）")
    @PostMapping("/driver/{driverId}/heartbeat")
    public Result<String> heartbeat(@PathVariable Long driverId,
                                     @RequestParam BigDecimal lat,
                                     @RequestParam BigDecimal lng) {
        dispatchService.heartbeat(driverId, lat, lng);
        return Result.ok("ok");
    }

    @Operation(summary = "释放司机锁（v1.2）")
    @PostMapping("/driver/{driverId}/release")
    public Result<String> releaseDriver(@PathVariable Long driverId) {
        dispatchService.releaseDriver(driverId);
        return Result.ok("ok");
    }

    // ── v1.3 指标 + 评分监控 ──

    @Operation(summary = "调度指标统计（v1.3）")
    @GetMapping("/dispatch/metrics")
    public Result<DispatchMetricsService.DispatchSummary> getMetrics() {
        return Result.ok(metricsService.getSummary());
    }

    @Operation(summary = "司机负载分布（v1.3）")
    @GetMapping("/dispatch/driver-load")
    public Result<Map<Long, Integer>> getDriverLoad() {
        return Result.ok(metricsService.getDriverLoadDistribution());
    }

    @Operation(summary = "重置指标计数器（v1.3）")
    @PostMapping("/dispatch/metrics/reset")
    public Result<String> resetMetrics() {
        metricsService.reset();
        return Result.ok("ok");
    }

    @Operation(summary = "动态评分状态（v1.3）")
    @GetMapping("/dispatch/scoring-state")
    public Result<Map<Long, Map<String, Object>>> getScoringState() {
        var states = dynamicScoringEngine.getAllStates();
        Map<Long, Map<String, Object>> result = new java.util.LinkedHashMap<>();
        states.forEach((driverId, state) -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("modifier", Math.round(state.modifier * 100.0) / 100.0);
            m.put("acceptCount", state.acceptCount);
            m.put("rejectCount", state.rejectCount);
            m.put("cancelCount", state.cancelCount);
            result.put(driverId, m);
        });
        return Result.ok(result);
    }

    // ── v1.4 异常检测 + 健康评分 ──

    @Operation(summary = "异常行为检测（v1.4）")
    @GetMapping("/dispatch/anomalies")
    public Result<AnomalyDetectionService.AnomalyReport> getAnomalies() {
        return Result.ok(anomalyService.detectAnomalies());
    }

    @Operation(summary = "系统健康评分（v1.4）")
    @GetMapping("/dispatch/health")
    public Result<SystemHealthService.HealthResult> getHealth() {
        return Result.ok(healthService.computeHealth());
    }

    @Operation(summary = "重置所有监控状态（v1.4）")
    @PostMapping("/dispatch/reset-all")
    public Result<String> resetAll() {
        metricsService.reset();
        anomalyService.reset();
        return Result.ok("ok");
    }
}
