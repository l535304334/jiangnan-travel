package com.jiangnan.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.entity.RiskAlert;

import java.math.BigDecimal;

public interface RiskAlertService {
    Page<RiskAlert> listAlerts(Integer handled, int page, int size);

    void handleAlert(Long id, String handleRemark);

    void createAlert(RiskAlert alert);

    void createAlertWithProfileUpdate(RiskAlert alert, Long userId);

    long countUnhandledAlerts();

    /** R1: 短时高频下单（10分钟内≥3次）→ 疑似刷单 */
    void checkR1(Long userId);

    /** R3: 路线异常（起终点距离<100米）→ 疑似刷单 */
    void checkR3(BigDecimal startLat, BigDecimal startLng, BigDecimal endLat, BigDecimal endLng, Long userId);

    /** R5: 高频投诉（30天内被投诉≥3次）→ 关注 */
    void checkR5(Long userId);

    /** R6: 设备变更（30分钟内≥3个不同IP）→ 高危 */
    void checkR6(Long userId, String ip);
}
