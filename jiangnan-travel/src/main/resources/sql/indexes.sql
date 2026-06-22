-- =====================================================
-- 江南出行智慧服务平台 - 索引优化脚本
-- 说明：在开发完成后执行，优化查询性能
-- =====================================================

USE smart_travel;

-- 1. 订单表高频查询索引
ALTER TABLE t_order ADD INDEX idx_user_status_time (user_id, status, create_time);
ALTER TABLE t_order ADD INDEX idx_driver_status (driver_id, status);
ALTER TABLE t_order ADD INDEX idx_status_create (status, create_time);
ALTER TABLE t_order ADD INDEX idx_user_create (user_id, create_time);

-- 2. 司机表地理查询索引（调度引擎核心）
ALTER TABLE t_driver ADD INDEX idx_status_location (status, lat, lng);

-- 3. 评价表司机评分查询
ALTER TABLE t_review ADD INDEX idx_driver_rating (driver_id, rating);

-- 4. 用户优惠券查询
ALTER TABLE t_user_coupon ADD INDEX idx_user_status (user_id, status);

-- 5. AI对话日志 - 按用户+时间
ALTER TABLE t_ai_chat_log ADD INDEX idx_user_time (user_id, create_time);
ALTER TABLE t_ai_chat_log ADD INDEX idx_session (session_id);

-- 6. 风控告警 - 按状态筛选
ALTER TABLE t_risk_alert ADD INDEX idx_handled_time (handled, create_time);

-- 7. 推送日志
ALTER TABLE t_push_log ADD INDEX idx_user_type (user_id, push_type);

-- 8. 支付记录
ALTER TABLE t_payment ADD INDEX idx_order (order_id);

-- 9. 发票
ALTER TABLE t_invoice ADD INDEX idx_user_order (user_id, order_id);

-- 10. 用户表手机号查询（已有UNIQUE，无需额外索引）

-- 11. 城市地标城市筛选
ALTER TABLE t_city_landmark ADD INDEX idx_city_status (city, status);
