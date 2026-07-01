# 江南出行智慧服务平台

> 南昌大学软件工程 2307 班毕业实习项目  
> 时间：2026.7.6 - 2026.8.6  
> 实习单位：江南出行运输服务有限公司宁都分公司

---

## 项目简介

江南出行是一个 **网约车/智慧出行** 平台，采用前后端分离架构，覆盖江西省内出行服务。系统分为 **乘客端**、**司机端**、**管理后台** 三端，融合 **AI 大模型** 与 **文旅元素**。

### 核心技术亮点

| 亮点 | 说明 |
|---|---|
| **4 大 AI 引擎** | 智能客服（DeepSeek）、出行预测、安全风控、智能调度 |
| **阶梯计价** | 起步价 + 中程(3-50km) + 远程(50-200km) + 超远程(>200km) 四段计费 |
| **智能派单** | 距离 × 0.5 + 评分 × 0.3 + 空闲时长 × 0.2 多因素加权评分 |
| **风控系统** | R2 取消频率、R4 深夜跨城、R7 疲劳驾驶 三条风控规则 |
| **文旅融合** | 城市地标 + 古诗文语录轮播 + AI 推荐目的地 |
| **水墨江南 UI** | 碧波绿 + 暖阳杏 + 烟雨白 + 墨色 CSS 变量主题 |

---

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 3.2.6 |
| JDK | Java | 17 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis + Redisson | 3.32.0 |
| 安全 | Spring Security + JWT (jjwt) | 0.12.5 |
| AI | DeepSeek API | openai-java 0.18.0 |
| 接口文档 | Knife4j | 4.5.0 |
| 前端框架 | Vue 3 + Vite 5 + Element Plus 2.7 | — |
| 状态管理 | Pinia | 2.1.x |
| 实时通信 | WebSocket | — |
| 地图 | 高德地图 JS API 2.0 | — |

---

## 项目文档索引

| 文档 | 用途 |
|---|---|
| [AGENTS.md](AGENTS.md) | AI 代理总入口 — 核心原则、默认行为、文件读取清单 |
| [EXECUTION_MODE.md](EXECUTION_MODE.md) | 企业级执行模式 — 启动流程、Task 推进、开发纪律、汇报规范 |
| [PROJECT_RULES.md](PROJECT_RULES.md) | 项目编码规范 — Controller/Service/Mapper/DTO/VO/Git 等 13 项规范 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 项目架构文档（唯一） — 完整数据库、API、安全、缓存、WebSocket 架构 |
| [AI_WORKFLOW.md](AI_WORKFLOW.md) | Skill + MCP 工作流 — 每阶段应调用哪个 Skill 和 MCP |
| [项目开发总结报告.md](项目开发总结报告.md) | 项目开发总结 — 开发历程、技术决策、AI 环境能力、完成度评估、测试成果 |
| [tests/TEST_GUIDE.md](tests/TEST_GUIDE.md) | 测试脚本使用说明 — 18 个测试脚本、全量编排器、数据清理 |

---

## 项目结构

```
├── jiangnan-travel/                  # 后端 Spring Boot 项目
│   ├── pom.xml                       # Maven 构建（19 条依赖）
│   └── src/main/java/com/jiangnan/travel/
│       ├── TravelApplication.java    # @SpringBootApplication 启动类
│       ├── common/                   # Result、ErrorCode、BusinessException、GlobalExceptionHandler
│       ├── config/                   # 9 个配置类（CORS、Security、Cache、RateLimit、Knife4j 等）
│       ├── security/                 # JwtUtil、JwtAuthFilter、SecurityConfig
│       ├── controller/               # 22 个 Controller → 97 个 API 端点
│       ├── service/ + impl/          # 22 个 Service 接口 + 22 个实现
│       ├── entity/                   # 29 个业务实体 + BaseEntity
│       ├── mapper/                   # 29 个 MyBatis-Plus Mapper
│       ├── dto/                      # 26 个请求 DTO
│       ├── vo/                       # 17 个响应 VO
│       └── websocket/                # 3 个实时通信端点（司机位置/订单追踪/通知）
│
├── jiangnan-travel-web/              # 前端 Vue 3 项目
│   ├── package.json                  # 8 个运行依赖 + 2 个构建依赖
│   ├── vite.config.js                # Vite 5 配置（代理 /api → 8080）
│   └── src/
│       ├── api/                      # 13 个 API 模块（封装 Axios + 拦截器）
│       ├── components/               # 3 个公共组件（CdnAvatar / AiChatFloat / AmapView）
│       ├── composables/              # 3 个组合式函数（POI搜索/反馈/验证码）
│       ├── views/                    # 41 个页面视图（乘客/司机/管理 三端）
│       ├── router/                   # 路由配置 + 三层鉴权守卫
│       ├── stores/                   # Pinia 状态管理
│       └── assets/                   # 全局样式（style.css + transitions.css）
│
├── docs/                             # 设计文档（产品设计/设计系统/页面结构/路线图 等）
├── docs/spec/                        # 功能规格说明
│
├── jiangnan-travel/.../resources/sql/  # SQL 脚本（init.sql、indexes.sql 等 7 个）
│
├── tests/                            # 测试回归网（18 个 + 编排器 + 数据清理）
│   ├── test-suite.mjs                # 全量测试编排器（155 用例 100% 通过）
│   ├── *_test.mjs                    # 18 个分模块测试脚本
│   ├── TEST_GUIDE.md                 # 测试使用说明
│   └── cleanup_test_data.sql         # 测试数据清理脚本
│
├── 实习材料/                          # 实习相关文档（doc/docx，不参与开发）
│
├── AGENTS.md                         # AI 代理总入口
├── EXECUTION_MODE.md                 # 企业级执行模式
├── PROJECT_RULES.md                  # 编码规范
├── ARCHITECTURE.md                   # 架构文档
└── AI_WORKFLOW.md                    # Skill + MCP 工作流
```

---

## API 概览（97 端点）

| 模块 | 路径 | 端点数 | 说明 |
|---|---|---|---|
| 管理后台 | `/api/admin` + `/api/admin/ai` | 25 | 大屏、用户/司机/订单/告警/车型/活动/VIP/班线管理、AI 洞察 |
| 订单管理 | `/api/order` | 9 | 估价、创建、列表、详情、取消、支付、评价、分享、重下 |
| 司机订单 | `/api/driver/order` | 8 | 接单、到达、出发、完成、附近订单、推荐等 |
| 用户管理 | `/api/user` + `/api/user/address` | 10 | 验证码、注册、登录、个人信息、收藏地址 CRUD |
| 司机管理 | `/api/driver` | 7 | 登录、注册、状态、位置、收入统计 |
| AI 服务 | `/api/ai` | 5 | 对话、流式对话、会话、推荐目的地 |
| 发票 | `/api/invoice` | 5 | 申请、列表、详情、取消 |
| VIP | `/api/vip` | 4 | 等级、购买、续费、我的会员 |
| 消息通知 | `/api/notification` | 4 | 列表、未读数、标记已读 |
| 支付 | `/api/payment` | 4 | 创建支付、查询状态、回调、幂等校验 |
| 活动 | `/api/campaign` | 3 | 列表、详情、领券 |
| 优惠券 | `/api/coupon` | 3 | 列表、我的、领取 |
| 班线 | `/api/bus-line` | 3 | 线路、时刻表、购票 |
| 文旅/AI 数据 | `/api`（根） | 3 | 热点、城市语录、常走路线 |
| 城市地标 | `/api/landmark` | 2 | 列表、搜索 |
| 路线 | `/api/route` | 1 | 路线规划 |
| 安全风控 | `/api/safety` | 1 | 安全预警 |

> 完整 API 文档见 [ARCHITECTURE.md](ARCHITECTURE.md) 第五章。

---

## 数据库（28 张表）

| 模块 | 表 |
|---|---|
| 用户/权限 | `t_user`, `t_driver`, `t_admin` |
| 订单核心 | `t_order`, `t_order_track`, `t_payment`, `t_invoice` |
| 营销 | `t_coupon`, `t_user_coupon`, `t_campaign`, `t_campaign_coupon` |
| 评价/地址 | `t_review`, `t_user_address` |
| 车型 | `t_car_type` |
| 文旅 | `t_city_landmark`, `t_city_quote` |
| AI | `t_ai_chat_log` |
| 客运班线 | `t_bus_line`, `t_bus_schedule`, `t_schedule_route`, `t_schedule_order` |
| VIP | `t_vip_level`, `t_user_vip` |
| 消息 | `t_notification` |
| 风控/推送 | `t_demand_hotspot`, `t_risk_alert`, `t_user_risk_profile`, `t_push_log` |

> 详见 [ARCHITECTURE.md](ARCHITECTURE.md#四数据库架构) 第四章。

---

## 快速启动

### 环境要求

- JDK 17+ / Maven 3.9+
- MySQL 8.0+ / Redis
- Node.js 18+

### 1. 初始化数据库

```bash
mysql -u root -p < jiangnan-travel/src/main/resources/sql/init.sql
# 可选索引优化
mysql -u root -p smart_travel < jiangnan-travel/src/main/resources/sql/indexes.sql
# 可选测试数据
mysql -u root -p smart_travel < jiangnan-travel/src/main/resources/sql/test_accounts.sql
```

### 2. 配置应用

修改 `jiangnan-travel/src/main/resources/application.yml`：

- `spring.datasource.username/password` — MySQL 连接信息
- `deepseek.api-key` — DeepSeek API Key
- `amap.web-api-key` / `amap.js-api-key` — 高德地图 Key

### 3. 启动后端

```bash
cd jiangnan-travel
mvn spring-boot:run
# API 文档: http://localhost:8080/doc.html （Knife4j）
```

### 4. 启动前端

```bash
cd jiangnan-travel-web
npm install
npm run dev
# 浏览器: http://localhost:5173
```

---

## 核心业务引擎

| 引擎 | 位置 | 说明 |
|---|---|---|
| 阶梯计价 | `PricingServiceImpl` | 4 段距离 + 时长费阶梯计算 |

| 智能派单 | `DispatchServiceImpl` | 距离×0.5 + 评分×0.3 + 空闲×0.2 加权 |
| 风控系统 | `OrderServiceImpl` | R2 取消限制、R4 深夜跨城、R7 疲劳提醒 |
| AI 客服 | `AiChatServiceImpl` | DeepSeek API + 文旅知识库 + 离线兜底 |
| AI 推荐 | `AiPredictionServiceImpl` | 历史订单 → 热门目的地推荐 |

---

## 演示账号

| 角色 | 说明 |
|---|---|
| 管理员 | `admin` 默认密码 `123456` |
| 乘客 | 测试手机号见 `jiangnan-travel/src/main/resources/sql/test_accounts.sql`，密码统一 `123456` |
| 司机 | `13810000001` 等，密码统一 `123456` |

---

## AI 开发环境

本项目已配置完整的 AI 辅助开发环境：

| 资源 | 数量 | 用途 |
|---|---|---|
| Skills | 34 个（选用 16 个核心） | 工作流各阶段 Skill 支持 |
| MCP | 5 个（MySQL / Git / Playwright / Filesystem / Sequential Thinking） | 数据库、Git、浏览器、文件、推理 |
| 工作流 | 8 阶段 Spec-Driven 流程 | 需求→Spec→Task→开发→Review→文档→提交 |

---

## 实习信息

| 项目 | 内容 |
|---|---|
| 学校 | 南昌大学软件学院 |
| 班级 | 软件工程 2307 班 |
| 实习单位 | 江南出行运输服务有限公司宁都分公司 |
| 岗位 | 软件开发实习生 |
| 时间 | 2026.7.6 - 2026.8.6 |
