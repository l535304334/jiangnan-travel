package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.RiskAlert;
import com.jiangnan.travel.entity.UserRiskProfile;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.RiskAlertMapper;
import com.jiangnan.travel.mapper.UserRiskProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
public class SafetyController {

    private final RiskAlertMapper riskAlertMapper;
    private final UserRiskProfileMapper userRiskProfileMapper;
    private final OrderMapper orderMapper;

    @PostMapping("/alert")
    public Result<?> alert(@RequestBody Map<String, Object> body) {
        Long orderId = body.get("orderId") != null
                ? ((Number) body.get("orderId")).longValue() : null;
        BigDecimal lat = body.get("lat") != null
                ? new BigDecimal(body.get("lat").toString()) : null;
        BigDecimal lng = body.get("lng") != null
                ? new BigDecimal(body.get("lng").toString()) : null;

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
        alert.setDetail("位置 lat=" + lat + ", lng=" + lng);
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
