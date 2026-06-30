-- 江南出行测试数据清理脚本
-- 作用：删除由自动化测试脚本产生的业务数据，保留基础配置数据
-- 危险等级：中 — 执行前请确认当前不是生产环境
-- 用法：mysql -u root -p smart_travel < cleanup_test_data.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 定位测试用户（测试脚本固定使用的手机号）
SET @user_a = (SELECT id FROM t_user WHERE phone = '13900001111' LIMIT 1);
SET @user_b = (SELECT id FROM t_user WHERE phone = '13900002222' LIMIT 1);
SET @user_risk = (SELECT id FROM t_user WHERE phone = '13900003333' LIMIT 1);
SET @driver_user = (SELECT id FROM t_user WHERE phone = '13810000001' LIMIT 1);
SET @driver_id = (SELECT id FROM t_driver WHERE user_id = @driver_user LIMIT 1);

-- 2. 删除测试用户相关订单及子表数据
DELETE FROM t_order_track WHERE order_id IN (
    SELECT id FROM t_order WHERE user_id IN (@user_a, @user_b, @user_risk)
);

DELETE FROM t_payment WHERE user_id IN (@user_a, @user_b, @user_risk);

DELETE FROM t_invoice WHERE user_id IN (@user_a, @user_b, @user_risk);

DELETE FROM t_review WHERE order_id IN (
    SELECT id FROM t_order WHERE user_id IN (@user_a, @user_b, @user_risk)
);

DELETE FROM t_order WHERE user_id IN (@user_a, @user_b, @user_risk);

-- 3. 删除测试用户地址
DELETE FROM t_user_address WHERE user_id IN (@user_a, @user_b, @user_risk);

-- 4. 删除测试产生的优惠券领取记录
DELETE FROM t_user_coupon WHERE user_id IN (@user_a, @user_b, @user_risk);

-- 5. 删除测试通知
DELETE FROM t_notification WHERE user_id IN (@user_a, @user_b, @user_risk);

-- 6. 删除测试风控告警
DELETE FROM t_risk_alert WHERE user_id IN (@user_a, @user_b, @user_risk);

-- 7. 重置测试司机状态为在线，避免后续测试因司机忙碌失败
UPDATE t_driver SET status = 1 WHERE id = @driver_id;

-- 8. 删除测试用户风控画像（如存在）
DELETE FROM t_user_risk_profile WHERE user_id IN (@user_a, @user_b, @user_risk);

SET FOREIGN_KEY_CHECKS = 1;

-- 9. 可选：删除纯测试账号（若需要彻底清理，请取消下面注释）
-- DELETE FROM t_user WHERE phone IN ('13900003333');
