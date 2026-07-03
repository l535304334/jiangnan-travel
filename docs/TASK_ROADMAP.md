# 江南出行 — 开发任务路线图

> 版本：v2.0 | 总任务数：30

---

## Phase 1: 技术债偿还（Task 001-005）

### ✅ Task 001 — DDL补全deleted字段（已完成）
- **目标**：所有28张表DDL与Entity对齐
- **涉及文件**：src/main/resources/sql/migration_optimize.sql
- **数据库**：16张表补ALTER TABLE ADD COLUMN deleted（含3张表补update_time、1张表补create_time）
- **验收**：所有表查询带deleted条件正常
- **完成日期**：2026-06-24

### ✅ Task 002 — JWT统一+角色鉴权（已完成）
- **目标**：三套token统一为JWT+role字段
- **涉及文件**：JwtUtil.java, JwtAuthFilter.java, SecurityConfig.java, UserServiceImpl.java, DriverServiceImpl.java, AdminServiceImpl.java
- **验收**：API调用带正确角色，错误角色返回403
- **完成日期**：2026-06-24

### ✅ Task 003 — 登录页统一（已完成）
- **目标**：删除DriverLogin.vue和AdminLogin.vue，统一使用Login.vue
- **涉及文件**：Login.vue, router/index.js（新增2个文件，删除2个文件）
- **验收**：三种角色从同一页面登录，路由重定向兼容旧访问路径
- **完成日期**：2026-06-24

### ✅ Task 004 — 前端composable提取（已完成）
- **目标**：提取useSmsCode、useFeedback公共逻辑
- **涉及文件**：新增src/composables/useSmsCode.js, useFeedback.js
- **验收**：Login和Register使用同一个composable
- **完成日期**：2026-06-24

### ✅ Task 005 — 代码清理（已完成）
- **目标**：删除未使用的依赖mysql2、bcryptjs；删除重复的Haversine计算
- **涉及文件**：jiangnan-travel-web/package.json
- **验收**：npm run build无warning
- **完成日期**：2026-06-24

---

## Phase 2: 核心功能补全（Task 006-012）

### ✅ Task 006 — 消息通知系统（已完成）
- **目标**：新增t_notification表+WebSocket推送
- **涉及文件**：Notification.java, NotificationMapper.java, NotificationService/Impl, NotificationController.java, NotificationWebSocketServer.java, OrderServiceImpl.java, MessageCenter.vue, notification.js, router/index.js, Layout.vue
- **验收**：订单状态变更时用户收到通知
- **完成日期**：2026-06-24

### ✅ Task 007 — 下单页优惠券选择（已完成）
- **目标**：下单时弹窗选择可用优惠券
- **涉及文件**：OrderCreate.vue, PricingServiceImpl.java, EstimateRequest.java
- **验收**：选择优惠券后价格实时更新
- **完成日期**：2026-06-24

### ✅ Task 008 — 司机端真实API对接（已完成）
- **目标**：DriverHome/DriverEarnings/DriverProfile对接后端
- **涉及文件**：DriverHome.vue, DriverEarnings.vue, DriverProfile.vue
- **验收**：页面显示真实数据
- **完成日期**：2026-06-24

### ✅ Task 009 — 下单页高德POI搜索（已完成）
- **目标**：替换21个预置地标为高德POI搜索
- **涉及文件**：OrderCreate.vue + useAmapPoiSearch.js + AmapView.vue
- **验收**：输入任意地址均可搜索到坐标
- **完成日期**：2026-06-24

### ✅ Task 010 — 行程追踪WebSocket对接（已完成）
- **目标**：TripTracking.vue连接WebSocket获取司机实时位置
- **涉及文件**：TripTracking.vue + DriverLocationServer.java
- **验收**：地图上司机位置实时更新
- **完成日期**：2026-06-24

### ✅ Task 011 — 风控引擎完善（已完成）
- **目标**：实现R1/R3/R5/R6规则
- **涉及文件**：RiskAlertServiceImpl.java + RiskAlertService.java + OrderServiceImpl.java
- **验收**：异常订单行为触发告警
- **完成日期**：2026-06-24

### ✅ Task 012 — 管理后台数据可视化（已完成）
- **目标**：AdminDashboard集成ECharts图表
- **涉及文件**：AdminDashboard.vue + AdminManageController.java + OrderMapper.java + admin.js + echarts
- **验收**：仪表盘显示折线图/柱状图
- **完成日期**：2026-06-24

---

## Phase 3: 功能新增（Task 013-022）

### ✅ Task 013 — 活动系统（后端）（已完成）
- **目标**：新增t_campaign + t_campaign_coupon表 + CRUD API
- **涉及文件**：Campaign.java, CampaignCoupon.java, CampaignMapper.java, CampaignCouponMapper.java, CampaignService.java, CampaignServiceImpl.java, CampaignController.java, CreateCampaignRequest.java, CampaignDetailVO.java, AdminManageController.java, init.sql
- **数据库**：2张新表
- **完成日期**：2026-06-24

### ✅ Task 014 — 活动系统（前端）（已完成）
- **目标**：活动列表页+详情页+领券
- **涉及文件**：CampaignList.vue, CampaignDetail.vue, campaign.js, router/index.js, Home.vue
- **验收**：用户可浏览活动并从活动页领券
- **完成日期**：2026-06-24

### Task 015 — VIP会员（后端）
- **目标**：新增t_vip_level + t_user_vip表 + 购买/权益API
- **涉及文件**：新建Vip实体/Service/Controller
- **数据库**：2张新表

### ✅ Task 016 — VIP会员（前端）（已完成）
- **目标**：会员中心页+下单时会员折扣
- **涉及文件**：VipCenter.vue, vip.js, router/index.js, Home.vue
- **验收**：VIP用户下单显示折扣价
- **完成日期**：2026-06-24

### ✅ Task 017 — 文旅地标独立页（已完成）
- **目标**：将Home中的文旅模块移至发现Tab
- **涉及文件**：LandmarkExplore.vue, QuoteWall.vue, Layout.vue, router/index.js, Home.vue
- **验收**：独立文旅页功能完整，支持城市筛选+搜索+打卡下单
- **完成日期**：2026-06-24

### ✅ Task 018 — AI助手独立页（已完成）
- **目标**：将AI助手从Home中提取为独立Tab
- **涉及文件**：AiAssistant.vue, Layout.vue, router/index.js, Home.vue
- **验收**：AI助手功能完整（智能问答、推荐目的地、热门景点、城市寄语）
- **完成日期**：2026-06-24

### ✅ Task 019 — 支付模块（已完成）
- **目标**：集成微信/支付宝支付SDK
- **涉及文件**：PaymentService, PaymentController + Payment.vue支付页
- **验收**：支持微信/支付宝/余额三种支付方式，支付结果弹窗
- **完成日期**：2026-06-24

### ✅ Task 020 — 电子发票（已完成）
- **目标**：发票申请+开具流程
- **涉及文件**：InvoiceService, InvoiceController + InvoiceCenter.vue
- **验收**：支持申请/查询/取消/管理员开具，发票列表三态展示
- **完成日期**：2026-06-24

### ✅ Task 021 — 班线系统（已完成）
- **目标**：城际班线查询+购票
- **涉及文件**：BusLine/BusSchedule Entity/Mapper/Service/Controller + BusLine.vue
- **验收**：支持城市筛选、时刻表查询、一键购票（减余票）
- **完成日期**：2026-06-24

### ✅ Task 022 — 后台活动/VIP/系统设置（已完成）
- **目标**：管理后台新增活动、VIP、班线管理Tab
- **涉及文件**：AdminCampaigns.vue, AdminVipLevels.vue, AdminBusLines.vue + admin.js + AdminLayout.vue
- **验收**：完整管理后台活动/VIP等级/班线CRUD + 子班次管理
- **完成日期**：2026-06-24

---

## Phase 4: UI/UX全面升级（Task 023-027）

### ✅ Task 023 — 全局CSS变量+主题系统（已完成）
- **目标**：统一颜色/字体/间距/圆角/阴影
- **涉及文件**：assets/style.css
- **验收**：所有页面视觉统一
- **完成日期**：2026-06-24

### ✅ Task 024 — 统一组件规范（已完成）
- **目标**：按钮/输入框/卡片/弹窗/Loading/空状态统一
- **涉及文件**：全局样式+各页面
- **验收**：全站组件风格一致
- **完成日期**：2026-06-24

### ✅ Task 025 — 动画系统（已完成）
- **目标**：页面过渡/卡片悬停/列表进入动画
- **涉及文件**：assets/transitions.css
- **验收**：流畅的交互动画
- **完成日期**：2026-06-24

### ✅ Task 026 — 司机端UI重构（已完成）
- **目标**：司机端统一视觉风格
- **涉及文件**：DriverLayout + 4个司机页面
- **验收**：与用户端统一设计语言
- **完成日期**：2026-06-24

### ✅ Task 027 — 管理后台UI升级（已完成）
- **目标**：侧边栏优化+数据可视化+深色主题
- **涉及文件**：AdminLayout + 9个管理页面
- **验收**：专业级后台体验
- **完成日期**：2026-06-24

---

## Phase 5: 文档+测试+部署（Task 028-030）

### ✅ Task 028 — 文档更新（已完成）
- **目标**：更新README+ARCHITECTURE+PROJECT_HIGHLIGHTS
- **涉及文件**：4个核心文档
- **验收**：文档与代码一致
- **完成日期**：2026-06-24

### ✅ Task 029 — 全链路测试（已完成）
- **目标**：端到端测试主要业务流程
- **涉及文件**：6个新增测试文件（VipServiceTest, CouponServiceTest, CampaignServiceTest, UserAddressServiceTest, PaymentServiceTest, InvoiceServiceTest）
- **验收**：63个测试用例全部通过
- **完成日期**：2026-06-24

### ✅ Task 030 — 生产部署（已完成）
- **目标**：打包+部署到服务器
- **涉及文件**：Dockerfile, nginx.conf, docker-compose.yml, application-prod.yml, build.ps1
- **验收**：Maven jar 构建成功(3.1s) + npm dist 构建成功(10.4s) + Docker Compose 完整部署方案就绪
- **完成日期**：2026-06-24
