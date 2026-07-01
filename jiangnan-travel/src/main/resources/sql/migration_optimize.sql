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
    -- idx_coord_range duplicates idx_coord in init.sql; removed
    ADD INDEX idx_create_time (create_time);

-- =====================================================
-- 5. t_admin 补充常规字段
-- =====================================================

-- create_time already exists in init.sql; removed duplicate
-- ALTER TABLE t_admin ADD COLUMN create_time ...

-- =====================================================
-- 6. 补充缺失的 deleted 逻辑删除字段（与所有 Entity 的 BaseEntity 对齐）
-- =====================================================
-- 说明：所有 21 个 Entity 继承 BaseEntity，包含 @TableLogic 注解的 deleted 字段
-- MyBatis-Plus 会在所有查询中自动追加 AND deleted=0
-- 已存在 deleted 的表：t_user, t_car_type, t_driver, t_user_address, t_review
-- 以下 16 张表补充 deleted 字段

ALTER TABLE t_order
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER cancel_reason;

ALTER TABLE t_order_track
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER remark,
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

ALTER TABLE t_coupon
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

ALTER TABLE t_user_coupon
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER use_order_id,
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

ALTER TABLE t_admin
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

ALTER TABLE t_schedule_route
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

ALTER TABLE t_schedule_order
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

ALTER TABLE t_ai_chat_log
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER tokens_used;

ALTER TABLE t_risk_alert
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER handle_remark;

ALTER TABLE t_user_risk_profile
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER risk_level;
-- create_time already exists in init.sql; removed duplicate ADD

ALTER TABLE t_demand_hotspot
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER snapshot_time;

ALTER TABLE t_city_landmark
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

ALTER TABLE t_city_quote
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

ALTER TABLE t_payment
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER pay_time;

ALTER TABLE t_invoice
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

ALTER TABLE t_push_log
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER status;

-- =====================================================
-- 7. 补充剩余缺失的 update_time 字段
-- =====================================================
-- t_user_address 没有 update_time 但实体继承 BaseEntity 需要

ALTER TABLE t_user_address
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

-- =====================================================
-- 8. 新增通知表 t_notification
-- =====================================================

DROP TABLE IF EXISTS t_notification;
CREATE TABLE t_notification (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '通知ID',
    user_id         BIGINT          NOT NULL                    COMMENT '接收用户ID',
    type            VARCHAR(32)     NOT NULL                    COMMENT '通知类型 ORDER_CREATED/ORDER_ACCEPTED/ORDER_STARTED/ORDER_COMPLETED/ORDER_CANCELLED/SYSTEM',
    title           VARCHAR(128)    NOT NULL                    COMMENT '通知标题',
    content         VARCHAR(512)    DEFAULT ''                  COMMENT '通知内容',
    related_id      BIGINT                                      COMMENT '关联ID(如订单ID)',
    is_read         TINYINT         DEFAULT 0                   COMMENT '0未读 1已读',
    deleted         TINYINT         DEFAULT 0                   COMMENT '逻辑删除 0未删 1已删',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_time (user_id, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- =====================================================
-- 9. 清理冗余索引（已被复合索引覆盖的单列索引）
-- =====================================================
-- t_order 复合索引 idx_user_status_time / idx_status_create 等已覆盖下列单列索引
DROP INDEX IF EXISTS idx_user ON t_order;
DROP INDEX IF EXISTS idx_status ON t_order;
DROP INDEX IF EXISTS idx_create_time ON t_order;

-- =====================================================
-- 10. 清理 t_user 归一化残留字段
-- =====================================================
-- preferred_driver_ids 已迁移到 t_user_preferred_driver 归一化表
ALTER TABLE t_user DROP COLUMN IF EXISTS preferred_driver_ids;

-- =====================================================
-- 11. 补充缺失索引
-- =====================================================
-- t_order.cancel_time — 取消分析查询
ALTER TABLE t_order ADD INDEX IF NOT EXISTS idx_cancel_time (cancel_time);
-- t_user_coupon.use_order_id — 优惠券使用关联查询
ALTER TABLE t_user_coupon ADD INDEX IF NOT EXISTS idx_use_order (use_order_id);
