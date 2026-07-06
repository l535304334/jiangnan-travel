-- ============================================================
-- 迁移脚本: 订单状态机 + 支付增强
-- 日期: 2026-07-03
-- 说明: 新增 order_event / payment_trace 表，扩展 t_payment 字段
-- 回滚: 见文件末尾
-- ============================================================

-- 1. 订单事件表（替代 t_order_track 的严格事件记录）
DROP TABLE IF EXISTS t_order_event;
CREATE TABLE t_order_event (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '事件ID',
    order_id        BIGINT          NOT NULL                    COMMENT '订单ID',
    order_no        VARCHAR(32)     NOT NULL                    COMMENT '订单编号',
    from_status     TINYINT         NOT NULL                    COMMENT '变更前状态码',
    to_status       TINYINT         NOT NULL                    COMMENT '变更后状态码',
    operator_id     BIGINT          NOT NULL                    COMMENT '操作人ID',
    operator_type   VARCHAR(20)     NOT NULL                    COMMENT 'user/driver/system',
    remark          VARCHAR(255)    DEFAULT ''                  COMMENT '备注',
    event_time      DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '事件时间',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '记录创建时间',
    INDEX idx_order_event_order (order_id),
    INDEX idx_order_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更事件';

-- 2. 支付追踪表
DROP TABLE IF EXISTS t_payment_trace;
CREATE TABLE t_payment_trace (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '追踪ID',
    payment_id      BIGINT                                      COMMENT '关联支付记录ID（首次可能为空）',
    order_id        BIGINT          NOT NULL                    COMMENT '订单ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    attempt_no      TINYINT         NOT NULL DEFAULT 1          COMMENT '尝试序号',
    status          TINYINT         NOT NULL DEFAULT 0          COMMENT '0待支付 1已支付 2失败 3已退款',
    pay_method      VARCHAR(20)     DEFAULT 'balance'           COMMENT '支付方式',
    pay_no          VARCHAR(64)                                 COMMENT '流水号',
    idempotent_key  VARCHAR(128)                                COMMENT '幂等键',
    amount          DECIMAL(10,2)   NOT NULL                    COMMENT '金额',
    fail_reason     VARCHAR(500)    DEFAULT ''                  COMMENT '失败原因',
    cost_ms         INT             DEFAULT 0                   COMMENT '耗时(ms)',
    trace_time      DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '追踪时间',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '记录创建时间',
    INDEX idx_trace_order (order_id),
    INDEX idx_trace_payment (payment_id),
    INDEX idx_trace_time (trace_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付追踪日志';

-- 3. t_payment 补充字段（幂等键已存在于 entity 中但表中缺）
-- MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，拆分为单独 ALTER 语句，用 --force 容错执行
ALTER TABLE t_payment ADD COLUMN idempotent_key VARCHAR(128) DEFAULT '' COMMENT '幂等键' AFTER pay_no;
ALTER TABLE t_payment ADD COLUMN retry_count    TINYINT      DEFAULT 0  COMMENT '重试次数' AFTER status;
ALTER TABLE t_payment ADD COLUMN fail_reason    VARCHAR(500) DEFAULT '' COMMENT '失败原因' AFTER retry_count;

-- 3.1 t_order_event / t_payment_trace 补充 BaseEntity 所需的 deleted / update_time 字段
ALTER TABLE t_order_event ADD COLUMN deleted     TINYINT    DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER event_time;
ALTER TABLE t_order_event ADD COLUMN update_time DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;
ALTER TABLE t_payment_trace ADD COLUMN deleted     TINYINT    DEFAULT 0 COMMENT '逻辑删除 0未删 1已删' AFTER create_time;
ALTER TABLE t_payment_trace ADD COLUMN update_time DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER deleted;

-- ============================================================
-- 回滚脚本（如需撤销）
-- ============================================================
-- DROP TABLE IF EXISTS t_order_event;
-- DROP TABLE IF EXISTS t_payment_trace;
-- ALTER TABLE t_payment DROP COLUMN idempotent_key;
-- ALTER TABLE t_payment DROP COLUMN retry_count;
-- ALTER TABLE t_payment DROP COLUMN fail_reason;
