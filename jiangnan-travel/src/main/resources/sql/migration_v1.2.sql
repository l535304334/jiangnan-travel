-- ============================================================
-- 迁移脚本: v1.2 — 司机运行时模型 + 并发调度
-- 日期: 2026-07-03
-- ============================================================

-- t_driver 新增字段
ALTER TABLE t_driver ADD COLUMN last_active_time DATETIME COMMENT '最后活跃时间（心跳）';
ALTER TABLE t_driver ADD COLUMN rejection_count  INT DEFAULT 0 COMMENT '累计拒单次数';
