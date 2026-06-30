# 江南出行 — 页面结构

> v2.0 重新规划

---

## 一、完整页面树

### 1.1 用户端（16页）

```
├── /                          → 自动跳转 /home（需登录）
├── /login                     → 统一登录页（角色选择）
├── /register                  → 用户注册页
│
├── /home                      → 首页（L0）
│
├── /trip                      → 出行Tab（合并）
│   ├── /trip/order            → 下单页（L1）
│   ├── /trip/list             → 订单列表（L1）
│   ├── /trip/:id              → 订单详情（L2）
│   └── /trip/:id/track        → 行程追踪（L3）
│
├── /discover                  → 发现Tab（NEW）
│   ├── /discover/landmarks    → 文旅地标（L1 — 从Home移出）
│   ├── /discover/campaign     → 活动中心（L1 — NEW）
│   │   └── /discover/campaign/:id → 活动详情（L2 — NEW）
│   └── /discover/quotes       → 城市寄语集合（L1 — NEW）
│
├── /profile                   → 我的Tab
│   ├── /profile/address       → 收藏地址（L1）
│   ├── /profile/coupons       → 优惠券（L1）
│   ├── /profile/security      → 安全设置（L1）
│   ├── /profile/about         → 关于我们（L1）
│   ├── /profile/vip           → 会员中心（L1 — NEW）
│   └── /profile/messages      → 消息中心（L1 — NEW）
│
└── /chat                      → AI助手独立页（L1 — NEW）
```

### 1.2 司机端（8页）

```
├── /driver/login              → 司机登录
├── /driver                    → 司机首页（接单大厅）
├── /driver/order/:id          → 订单详情
├── /driver/order/:id/navigate → 导航页（NEW）
├── /driver/trips              → 当前行程（NEW）
├── /driver/earnings           → 收入统计
├── /driver/profile            → 个人中心
└── /driver/wallet             → 钱包（NEW）
```

### 1.3 管理端（12页）

```
├── /admin/login               → 管理员登录
├── /admin                     → 数据大屏
├── /admin/users               → 用户管理
├── /admin/drivers             → 司机审核
├── /admin/orders              → 订单监控
├── /admin/alerts              → 风控告警
├── /admin/car-types           → 定价管理
├── /admin/campaigns           → 活动管理（NEW）
├── /admin/vip                 → 会员管理（NEW）
├── /admin/settings            → 系统设置（NEW）
├── /admin/analytics           → 数据分析（NEW）
└── /admin/notifications       → 推送管理（NEW）
```

---

## 二、页面变更说明

### 2.1 需拆分/重构的页面

| 页面 | 状态 | 说明 |
|---|---|---|
| Home.vue | 重构 | 文旅地标模块移至/discover，首页聚焦出行功能 |
| Layout.vue | 重构 | Tab从4项改为4项（下单+订单合并为"出行"，新增"发现"） |
| OrderCreate.vue | 重构 | 新增优惠券选择弹窗、高德POI搜索 |
| TripTracking.vue | 重构 | 增加WebSocket实时位置更新 |
| DriverHome.vue | 重构 | 从假数据改为对接真实API |
| DriverEarnings.vue | 重构 | 从假数据改为对接真实API |
| DriverProfile.vue | 重构 | 从假数据改为对接真实API |

### 2.2 需新增的页面

| 页面 | 路径 | 说明 |
|---|---|---|
| CampaignList.vue | /discover/campaign | 活动列表页 |
| CampaignDetail.vue | /discover/campaign/:id | 活动详情+领券 |
| VipCenter.vue | /profile/vip | 会员中心（等级/权益/购买） |
| MessageCenter.vue | /profile/messages | 消息中心（订单/优惠/系统通知） |
| ChatPage.vue | /chat | AI助手全屏独立页 |
| LandmarkExplore.vue | /discover/landmarks | 文旅地标独立页 |
| QuoteWall.vue | /discover/quotes | 城市寄语集合 |
| DriverWallet.vue | /driver/wallet | 司机钱包 |
| DriverNavigate.vue | /driver/order/:id/navigate | 司机导航页 |
| AdminCampaigns.vue | /admin/campaigns | 活动管理 |
| AdminVip.vue | /admin/vip | 会员管理 |
| AdminAnalytics.vue | /admin/analytics | 数据分析 |
| AdminSettings.vue | /admin/settings | 系统设置 |
