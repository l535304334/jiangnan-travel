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
