-- =====================================================
-- 江南出行智慧服务平台 - 数据库优化迁移脚本
-- 版本: 1.1.0
-- 日期: 2026-06-24
-- =====================================================

USE smart_travel;

-- =====================================================
-- 1. 补充缺失的 update_time 字段
-- =====================================================

ALTER TABLE t_coupon
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

ALTER TABLE t_admin
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_demand_hotspot
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_schedule_route
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_schedule_order
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_city_landmark
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_city_quote
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_payment
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_invoice
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_push_log
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_risk_alert
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE t_ai_chat_log
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- =====================================================
-- 2. t_review 补充 deleted 逻辑删除字段
-- =====================================================

ALTER TABLE t_review
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER content,
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

-- =====================================================
-- 3. 创建关联表 t_user_preferred_driver 替代 t_user.preferred_driver_ids
-- =====================================================
-- 说明：原 t_user.preferred_driver_ids VARCHAR(255) 存储 JSON 数组，违反第一范式
-- 新表将其规范化，每行记录一个偏好关系

DROP TABLE IF EXISTS t_user_preferred_driver;
CREATE TABLE t_user_preferred_driver (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    user_id     BIGINT          NOT NULL                    COMMENT '用户ID',
    driver_id   BIGINT          NOT NULL                    COMMENT '司机ID',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    deleted     TINYINT         DEFAULT 0                   COMMENT '逻辑删除',
    UNIQUE KEY uk_user_driver (user_id, driver_id),
    INDEX idx_user_id (user_id),
    INDEX idx_driver_id (driver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户首选司机关联表';

-- =====================================================
-- 4. 需求热点表索引优化
-- =====================================================
-- 为 t_demand_hotspot 添加针对范围查询的索引

ALTER TABLE t_demand_hotspot
    ADD INDEX idx_coord_range (lat, lng),
    ADD INDEX idx_create_time (create_time);

-- =====================================================
-- 5. t_admin 补充常规字段
-- =====================================================

ALTER TABLE t_admin
    ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER password;
