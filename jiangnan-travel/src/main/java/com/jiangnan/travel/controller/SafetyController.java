package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.SafetyAlertRequest;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.RiskAlert;
import com.jiangnan.travel.service.OrderService;
import com.jiangnan.travel.service.RiskAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
@Tag(name = "安全风控", description = "用户安全预警和风险控制")
public class SafetyController {

    private final RiskAlertService riskAlertService;
    private final OrderService orderService;

    @PostMapping("/alert")
    @Operation(summary = "安全预警", description = "提交安全预警信息")
    public Result<?> alert(@Valid @RequestBody SafetyAlertRequest request) {
        Long orderId = request.getOrderId();

        if (orderId == null) {
            return Result.fail("orderId不能为空");
        }

        Order order = orderService.getOrderById(orderId);
        Long userId = order != null ? order.getUserId() : null;

        RiskAlert alert = new RiskAlert();
        alert.setRuleCode("SAFETY_ALERT");
        alert.setUserId(userId);
        alert.setOrderId(orderId);
        alert.setAlertLevel(1);
        alert.setTitle("安全预警");
        alert.setDetail("位置 lat=" + request.getLat() + ", lng=" + request.getLng());
        alert.setHandled(0);
        alert.setCreateTime(LocalDateTime.now());

        riskAlertService.createAlertWithProfileUpdate(alert, userId);

        return Result.ok("预警已提交");
    }
}
