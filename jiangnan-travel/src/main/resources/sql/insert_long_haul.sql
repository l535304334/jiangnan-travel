-- ============================================
-- 长途车型种子数据（trip_type=1）
-- 短途车型保持不变（原有的快车/专车/商务七座）
-- ============================================
INSERT INTO t_car_type (name, base_price, mid_per_km, long_per_km, super_long_per_km, per_min_price, max_passengers, status) VALUES
('城际快车', 20.00, 2.00, 1.80, 1.50, 0.40, 4, 1),
('长途大巴', 50.00, 0.80, 0.60, 0.40, 0.00, 40, 1);

-- 注：t_car_type 已有快车(id=1)、专车(id=2)、商务七座(id=3)
-- 新增长的：城际快车(id=4)、长途大巴(id=5)
-- 短途（trip_type=0）推荐车型：1,2,3
-- 长途（trip_type=1）推荐车型：4,5
