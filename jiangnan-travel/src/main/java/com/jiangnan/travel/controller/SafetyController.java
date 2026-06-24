package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.SafetyAlertRequest;
import jakarta.validation.Valid;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.RiskAlert;
import com.jiangnan.travel.entity.UserRiskProfile;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.RiskAlertMapper;
import com.jiangnan.travel.mapper.UserRiskProfileMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
@Tag(name = "安全风控", description = "用户安全预警和风险控制")
public class SafetyController {

    private final RiskAlertMapper riskAlertMapper;
    private final UserRiskProfileMapper userRiskProfileMapper;
    private final OrderMapper orderMapper;

    @PostMapping("/alert")
    @Operation(summary = "安全预警", description = "提交安全预警信息")
    public Result<?> alert(@Valid @RequestBody SafetyAlertRequest request) {
        Long orderId = request.getOrderId();

        if (orderId == null) {
            return Result.fail("orderId不能为空");
        }

        Order order = orderMapper.selectById(orderId);
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
        riskAlertMapper.insert(alert);

        if (userId != null) {
            UserRiskProfile profile = userRiskProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserRiskProfile>()
                            .eq(UserRiskProfile::getUserId, userId));
            if (profile != null) {
                profile.setCancelCount(
                        (profile.getCancelCount() != null ? profile.getCancelCount() : 0) + 1);
                profile.setComplaintCount(
                        (profile.getComplaintCount() != null ? profile.getComplaintCount() : 0) + 1);
                profile.setUpdateTime(LocalDateTime.now());
                userRiskProfileMapper.updateById(profile);
            }
        }

        return Result.ok("预警已提交");
    }
}
