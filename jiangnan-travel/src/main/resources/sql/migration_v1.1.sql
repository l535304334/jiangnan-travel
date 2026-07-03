-- ============================================================
-- 迁移脚本: v1.1 — 派单 + 计费 + 评价增强
-- 日期: 2026-07-03
-- ============================================================

-- 1. 账单表
DROP TABLE IF EXISTS t_bill;
CREATE TABLE t_bill (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '账单ID',
    order_id        BIGINT          NOT NULL                    COMMENT '订单ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    driver_id       BIGINT                                      COMMENT '司机ID',
    distance_fee    DECIMAL(10,2)   DEFAULT 0.00                COMMENT '里程费',
    duration_fee    DECIMAL(10,2)   DEFAULT 0.00                COMMENT '时长费',
    peak_surcharge  DECIMAL(10,2)   DEFAULT 0.00                COMMENT '高峰加价',
    coupon_discount DECIMAL(10,2)   DEFAULT 0.00                COMMENT '优惠券抵扣',
    toll_fee        DECIMAL(8,2)    DEFAULT 0.00                COMMENT '过路费',
    total_amount    DECIMAL(10,2)   NOT NULL                    COMMENT '合计金额',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    INDEX idx_bill_order (order_id),
    INDEX idx_bill_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单';

-- 2. t_driver 补充字段（如不存在）
-- ALTER TABLE t_driver ADD COLUMN lat DECIMAL(10,7) DEFAULT 0 COMMENT '纬度';
-- ALTER TABLE t_driver ADD COLUMN lng DECIMAL(10,7) DEFAULT 0 COMMENT '经度';
