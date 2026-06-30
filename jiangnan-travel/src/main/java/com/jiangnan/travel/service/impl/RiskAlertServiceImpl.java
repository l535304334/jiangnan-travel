package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.RiskAlert;
import com.jiangnan.travel.entity.UserRiskProfile;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.RiskAlertMapper;
import com.jiangnan.travel.mapper.UserRiskProfileMapper;
import com.jiangnan.travel.service.RiskAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAlertServiceImpl implements RiskAlertService {

    private final RiskAlertMapper riskAlertMapper;
    private final UserRiskProfileMapper userRiskProfileMapper;
    private final OrderMapper orderMapper;

    /** R6 设备变更追踪缓存 (userId -> [(ip, timestamp), ...]) */
    private static final ConcurrentHashMap<Long, LinkedList<Object[]>> userIpCache = new ConcurrentHashMap<>();
    private static final int R6_WINDOW_MINUTES = 30;
    private static final int R6_THRESHOLD = 3;

    @Override
    public Page<RiskAlert> listAlerts(Integer handled, int page, int size) {
        Page<RiskAlert> alertPage = new Page<>(page, size);
        LambdaQueryWrapper<RiskAlert> wrapper = new LambdaQueryWrapper<RiskAlert>()
                .orderByDesc(RiskAlert::getCreateTime);
        if (handled != null) {
            wrapper.eq(RiskAlert::getHandled, handled);
        }
        return riskAlertMapper.selectPage(alertPage, wrapper);
    }

    @Override
    public void handleAlert(Long id, String handleRemark) {
        RiskAlert alert = riskAlertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        alert.setHandled(1);
        if (handleRemark != null) {
            alert.setHandleRemark(handleRemark);
        }
        riskAlertMapper.updateById(alert);
    }

    @Override
    public void createAlert(RiskAlert alert) {
        riskAlertMapper.insert(alert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAlertWithProfileUpdate(RiskAlert alert, Long userId) {
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
    }

    @Override
    public long countUnhandledAlerts() {
        return riskAlertMapper.selectCount(new LambdaQueryWrapper<RiskAlert>()
                .eq(RiskAlert::getHandled, 0));
    }

    /* ========== R1-R6 规则实现 ========== */

    /** R1: 短时高频下单（10分钟内≥3次）→ 疑似刷单 */
    @Override
    public void checkR1(Long userId) {
        if (userId == null) return;
        LocalDateTime since = LocalDateTime.now().minusMinutes(10);
        Long count = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .ge(Order::getCreateTime, since));
        if (count >= 3) {
            RiskAlert alert = new RiskAlert();
            alert.setRuleCode("R1");
            alert.setUserId(userId);
            alert.setAlertLevel(2);
            alert.setTitle("高频下单预警");
            alert.setDetail("{\"count\":" + count + ",\"windowMin\":10}");
            alert.setHandled(0);
            riskAlertMapper.insert(alert);
            log.warn("用户[{}]触发R1: 10分钟内下单{}次", userId, count);
        }
    }

    /** R3: 路线异常（起终点距离<100米）→ 疑似刷单 */
    @Override
    public void checkR3(BigDecimal startLat, BigDecimal startLng,
                        BigDecimal endLat, BigDecimal endLng, Long userId) {
        if (startLat == null || startLng == null || endLat == null || endLng == null) return;
        double dist = haversineMeters(
                startLat.doubleValue(), startLng.doubleValue(),
                endLat.doubleValue(), endLng.doubleValue());
        if (dist < 100) {
            RiskAlert alert = new RiskAlert();
            alert.setRuleCode("R3");
            alert.setUserId(userId);
            alert.setAlertLevel(2);
            alert.setTitle("路线异常预警");
            alert.setDetail(String.format("{\"distance\":\"%.1fm\"}", dist));
            alert.setHandled(0);
            riskAlertMapper.insert(alert);
            log.warn("用户[{}]触发R3: 起终点距离{:.1f}m", userId, dist);
        }
    }

    /** R5: 高频投诉（30天内被投诉≥3次）→ 关注 */
    @Override
    public void checkR5(Long userId) {
        if (userId == null) return;
        UserRiskProfile profile = userRiskProfileMapper.selectOne(
                new LambdaQueryWrapper<UserRiskProfile>()
                        .eq(UserRiskProfile::getUserId, userId));
        if (profile != null && profile.getComplaintCount() != null
                && profile.getComplaintCount() >= 3) {
            RiskAlert alert = new RiskAlert();
            alert.setRuleCode("R5");
            alert.setUserId(userId);
            alert.setAlertLevel(2);
            alert.setTitle("高频投诉预警");
            alert.setDetail("{\"complaintCount\":" + profile.getComplaintCount() + "}");
            alert.setHandled(0);
            riskAlertMapper.insert(alert);
            log.warn("用户[{}]触发R5: 投诉{}次", userId, profile.getComplaintCount());
        }
    }

    /** R6: 设备变更（30分钟内≥3个不同IP）→ 高危 */
    @Override
    public void checkR6(Long userId, String ip) {
        if (userId == null || ip == null || ip.isBlank()) return;
        LocalDateTime now = LocalDateTime.now();
        LinkedList<Object[]> records = userIpCache.computeIfAbsent(userId, k -> new LinkedList<>());
        // 清理过期记录
        records.removeIf(r -> ((LocalDateTime) r[1]).isBefore(now.minusMinutes(R6_WINDOW_MINUTES)));
        records.addLast(new Object[]{ip, now});
        // 统计不同IP数量
        long distinctIps = records.stream().map(r -> (String) r[0]).distinct().count();
        if (distinctIps >= R6_THRESHOLD) {
            RiskAlert alert = new RiskAlert();
            alert.setRuleCode("R6");
            alert.setUserId(userId);
            alert.setAlertLevel(3);
            alert.setTitle("设备变更高危预警");
            alert.setDetail("{\"distinctIps\":" + distinctIps + ",\"windowMin\":" + R6_WINDOW_MINUTES + "}");
            alert.setHandled(0);
            riskAlertMapper.insert(alert);
            log.warn("用户[{}]触发R6: 30分钟内{}个不同IP", userId, distinctIps);
        }
    }

    /* ---- 辅助 ---- */

    /** Haversine 距离计算（返回米） */
    private static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double r1 = Math.toRadians(lat1);
        double r2 = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(r1) * Math.cos(r2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000 * c;
    }
}
