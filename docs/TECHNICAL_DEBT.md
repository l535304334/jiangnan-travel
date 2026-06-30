# 江南出行 — 技术债清单

> 版本：v1.0 | 日期：2026-06-24

---

## 一、DDL与Entity不一致（13张表）

| 表名 | DDL缺失字段 | Entity有 | 影响 |
|---|---|---|---|
| t_order | deleted | ✅ (BaseEntity) | 逻辑删除不生效 |
| t_order_track | deleted, update_time | ✅ | 同上 |
| t_user_address | update_time | ✅ | 自动填充异常 |
| t_user_coupon | deleted, update_time | ✅ | 逻辑删除不生效 |
| t_coupon | deleted | ✅ | 同上 |
| t_admin | deleted | ✅ | 同上 |
| t_review | deleted | ✅ | 同上 |
| t_payment | deleted | ✅ | 同上 |
| t_invoice | deleted | ✅ | 同上 |
| t_push_log | deleted | ✅ | 同上 |
| t_risk_alert | deleted | ✅ | 同上 |
| t_user_risk_profile | deleted, create_time | ✅ | 同上 |
| t_schedule_route/order | deleted | ✅ | 同上 |
| t_ai_chat_log | deleted | ✅ | 同上 |
| t_demand_hotspot | deleted | ✅ | 同上 |
| t_city_landmark/quote | deleted | ✅ | 同上 |

**修复方案**：执行 migration_optimize.sql 补全所有缺失字段。

---

## 二、代码重复

| 重复内容 | 位置 | 建议 |
|---|---|---|
| sendCode倒计时逻辑 | Login.vue + Register.vue | 提取为composable `useSmsCode()` |
| Haversine距离计算公式 | OrderCreate.vue + AmapRouteServiceImpl.java | 仅保留后端实现 |
| 登录状态管理 | Login.vue直接操作localStorage + AdminLogin.vue使用adminApi | 统一使用store |
| ElMessage/ElMessageBox用法不一致 | 多处 | 提取为composable `useFeedback()` |

---

## 三、未实现的功能代码

| 文件 | 未实现内容 |
|---|---|
| PricingServiceImpl.recalculate() | throw UnsupportedOperationException |
| t_schedule_route/order表 | 完整的表结构但无任何业务代码 |
| t_payment表 | 有表但支付接口都是模拟的 |
| t_invoice表 | 有表但无发票业务代码 |
| DemandHotspot实体 | 有表但无需求热力图生成服务 |
| UserRiskProfile实体 | 有表但风控引擎实际未读写此表 |
| PushLog实体 | 有表但无推送发送服务 |

---

## 四、架构问题

1. **三套token体系** — user/driver/admin各自独立，应统一为JWT + role字段
2. **WebSocket未使用** — DriverLocationServer和OrderTrackingServer已实现但前端未对接
3. **Redis缓存未充分利用** — 已经配置了缓存策略但AI提示词构建未使用
4. **Controller职责混乱** — AIDataController同时处理hotspots、city-quote、frequent-routes

---

## 五、建议偿还顺序

| 优先级 | 技术债 | 预估工作量 |
|---|---|---|
| P0 | DDL补全deleted字段 | 0.5天 |
| P0 | JWT增加role字段+角色鉴权 | 1天 |
| P1 | 前端提取composable去重复 | 1天 |
| P1 | 司机端假数据替换为真实API | 2天 |
| P2 | WebSocket对接 | 2天 |
| P2 | Redis缓存实际使用 | 1天 |
| P3 | 未实现功能开发 | 按需 |
