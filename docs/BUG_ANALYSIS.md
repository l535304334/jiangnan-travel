# 江南出行 — 问题分析报告

> 版本：v1.0 | 日期：2026-06-24

---

## 一、Bug清单

### 严重（P0 — 影响核心功能）

| ID | 模块 | 问题描述 | 根因 | 修复建议 |
|---|---|---|---|---|
| B-01 | 前端 | AdminDashboard.vue使用了ElMessage但未import | 编译不会报错但运行时报错 | 添加 `import { ElMessage } from 'element-plus'` |
| B-02 | 前端 | Login.vue管理员登录同时操作userStore和localStorage，与AdminLogin.vue逻辑重复 | 不一致的登录态管理 | 统一使用store或localStorage，不混用 |
| B-03 | 后端 | 约13张表的DDL中无deleted字段，但Entity继承BaseEntity包含deleted | MyBatis-Plus逻辑删除无法生效 | 执行migration_optimize.sql补全DDL |
| B-04 | 后端 | 司机端所有API无角色鉴权 — 任何持有效token的用户可以调用司机API | SecurityConfig仅验证token有效性，不校验角色 | JWT增加role字段+SecurityConfig增加hasRole |
| B-05 | 后端 | 管理员端所有API同样无角色鉴权 | 同上 | 同上 |

### 一般（P1 — 影响用户体验）

| ID | 模块 | 问题描述 | 根因 | 修复建议 |
|---|---|---|---|---|
| B-06 | 前端 | TripTracking.vue的WebSocket未实际连接，司机位置不会更新 | 代码未对接OrderTrackingServer | 实现WebSocket连接逻辑 |
| B-07 | 前端 | DriverHome.vue stats和pendingOrders为写死的值，stats={onlineDuration:0, completedOrders:0, todayEarnings:0} | 未调用后端API | 调用driverApi获取真实数据 |
| B-08 | 前端 | DriverEarnings.vue today/week/recentOrders全为硬编码假数据 | 同上 | 对接DriverEarningController |
| B-09 | 前端 | DriverProfile.vue info对象完全硬编码（张师傅/苏B·A8888） | 同上 | 对接DriverController.getProfile |
| B-10 | 后端 | 多个Controller（AIDataController）路径不RESTful — /api/api/ai/hotspots | 路径拼接错误 | 修正RequestMapping |
| B-11 | 数据库 | t_user_address DDL无update_time但Entity有 | 自动填充会失败 | 补DDL |

### 潜在（P2 — 可能出错）

| ID | 模块 | 问题描述 |
|---|---|---|
| B-12 | 后端 | 密码修改API无旧密码强度验证（无最小长度、复杂度要求） |
| B-13 | 后端 | 订单创建时未校验idempotentKey过期时间 |
| B-14 | 前端 | AmapView高德密钥+安全密钥暴露在.env.development |
| B-15 | 前端 | 前端package.json依赖了mysql2（不应在前端出现） |

---

## 二、逻辑漏洞

| ID | 位置 | 漏洞描述 | 风险 |
|---|---|---|---|
| L-01 | SecurityConfig | /api/ai/**公开访问，未登录用户可大量调用消耗Token配额 | API滥用 |
| L-02 | OrderServiceImpl | 取消订单未更新司机状态为在线 | 司机无法接新单 |
| L-03 | PricingServiceImpl | estimate方法的tripType由前端传入，后端未根据实际距离校验 | 用户篡改请求选择便宜计价方式 |
| L-04 | UserServiceImpl | passwordLogin无登录失败次数限制 | 暴力破解 |
| L-05 | RateLimitConfig | 限流仅在HTTP层，WebSocket无限制 | DoS攻击 |

---

## 三、安全隐患

| ID | 严重度 | 描述 | 修复建议 |
|---|---|---|---|
| S-01 | 高 | JWT密钥写在application.yml明文 — key: `jiangnan-travel-jwt-secret-key-2024...` | 使用环境变量或密钥管理器 |
| S-02 | 高 | 无角色鉴权（如前所述） | 实现RBAC |
| S-03 | 中 | 数据库密码明文写在application.yml | 使用Jasypt加密或环境变量 |
| S-04 | 中 | CorsConfig允许所有origin — `allowedOrigins("*")` | 限制为生产域名 |
| S-05 | 中 | 高德地图API Key无IP白名单限制 | 在开放平台设置Referer白名单 |
| S-06 | 低 | DeepSeek API Key写在application.yml | 同S-01 |
| S-07 | 低 | 无SQL注入防护（MyBatis-Plus已提供基础防护，但需检查XML） | 无XML文件，当前安全 |

---

## 四、性能问题

| ID | 描述 | 影响 | 修复建议 |
|---|---|---|---|
| P-01 | buildSystemPrompt()每次AI对话都查全表地标和寄语 | 每个AI请求额外2次DB查询 | 使用Redis缓存（已配置但未使用） |
| P-02 | getSessions()使用groupBy+orderBy，无分页 | 用户会话多时DB压力大 | 加LIMIT 50 |
| P-03 | AmapView多个实例各自加载一次高德JS SDK | 同一页面多个地图组件重复加载 | 全局单例加载 |
| P-04 | 订单列表无时间范围默认筛选 | 数据量大时全表扫描 | 默认筛选近30天 |

---

## 五、修复状态

> 本章节跟踪各问题的修复进度，按修复时间倒序排列。

### RC 第 9 轮（2026-07-07）

| ID | 状态 | 修复说明 |
|---|---|---|
| L-03 | ✅ 已修复 | `PricingServiceImpl.estimate` 根据实际距离（≥50km 为长途）校验并纠正前端传入的 `tripType`，防止用户篡改获取低价。显式传入且不符时记录 warn 日志，未传入时静默纠正。 |
| L-04 | ✅ 已确认修复 | `UserServiceImpl.passwordLogin` 已实现暴力破解防护：5 次失败后锁定 15 分钟（Redis 计数器 `login:attempt:{phone}`），登录成功清除计数。 |

### RC 第 10 轮（2026-07-07）

| ID | 状态 | 修复说明 |
|---|---|---|
| B-10 | ✅ 已确认无需修复 | 当前路径为 `@RequestMapping("/api")` + `@GetMapping("/ai/hotspots")` = `/api/ai/hotspots`（单层 api），前端 `request.js` baseURL 为 `/api`，路径匹配正确。原描述的双 `/api/api/` 前缀不存在。 |
| B-11 | ✅ 已确认修复 | `migration_optimize.sql` 第 153-156 行已补 `t_user_address.update_time` 字段，CI 已包含此 migration。 |
| B-02 | ✅ 部分修复 | `router/index.js` 的 `clearAllAuth()` 函数漏清 `driverInfo` 已修复（添加 `localStorage.removeItem('driverInfo')`）。双 key 设计（token/adminToken/driverToken）为有意的多角色路由守卫支持，不重构。原描述的"与 AdminLogin.vue 逻辑重复"已过时（AdminLogin.vue 不存在，管理员登录集成在 Login.vue）。 |

### RC 第 8 轮（2026-07-07）

| ID | 状态 | 修复说明 |
|---|---|---|
| B-03 | ✅ 已修复 | `migration_optimize.sql` + `migration_state_machine.sql` 补全 `t_order_event`、`t_payment_trace` 等 BaseEntity 字段（deleted/update_time）。 |
| — | ✅ 已修复 | `DynamicScoringEngine` 添加 `@Primary` 解决 ScoringEngine bean 歧义（ApplicationContext 加载失败 P0）。 |
| — | ✅ 已修复 | `PaymentServiceImpl` 模拟支付成功率改为 `@Value` 注入（`payment.mock.success-rate`），消除测试随机失败。 |
| — | ✅ 已修复 | `VipServiceTest` VIP_LEVEL 范围扩大至 1000-9999 + 防御性清理，消除数据碰撞。 |
| — | ✅ 已修复 | CI 数据库初始化补充 `migration_v1.2.sql` 和 `migration_state_machine.sql`。 |

### RC 早期轮次

| ID | 状态 | 修复说明 |
|---|---|---|
| B-04 | ✅ 已修复 | `SecurityConfig` 为司机端 API 添加 `hasRole` 角色鉴权。 |
| B-05 | ✅ 已修复 | `SecurityConfig` 为管理员端 API 添加 `hasRole` 角色鉴权。 |
| L-02 | ✅ 已修复 | `OrderServiceImpl.cancelOrder` 取消订单时恢复司机在线状态（含 ARRIVED 状态取消的司机卡死修复）。 |
| S-01 | ✅ 已修复 | JWT 密钥改为 `${JWT_SECRET:}` 环境变量注入，无默认值 fail-fast。 |
| S-03 | ✅ 已修复 | 数据库密码改为 `${DB_PASS:}` 环境变量注入，无默认值 fail-fast。 |
| S-06 | ✅ 已修复 | DeepSeek API Key 改为 `${DEEPSEEK_API_KEY:}` 环境变量注入。 |

### 待修复

| ID | 优先级 | 说明 |
|---|---|---|
| B-06 | P1 | `TripTracking.vue` WebSocket 未实际连接，司机位置不更新 |
| L-05 | P1 | WebSocket 无限流，仅 HTTP 层有限流 |
| B-07~B-09 | P1 | 司机端首页/收入/个人信息硬编码假数据 |
| B-12~B-15 | P2 | 密码强度、幂等键过期、前端密钥暴露、多余依赖 |
| L-01 | P2 | `/api/ai/**` 公开访问，未登录可调用消耗 Token |
| S-04 | 中 | `CorsConfig` 允许所有 origin |
| S-05 | 中 | 高德地图 API Key 无 IP 白名单 |
| P-01~P-04 | P2 | 性能问题（AI 缓存、分页、地图 SDK、订单筛选） |
