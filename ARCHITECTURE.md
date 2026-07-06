# ARCHITECTURE.md — 项目架构文档

> 项目：江南出行智慧服务平台（Jiangnan Travel）  
> 版本：1.0.0-SNAPSHOT  
> 最后更新：2026-07-01  
> **说明：本文档是项目唯一架构文档，后续新增模块必须同步更新。**

---

## 一、项目概述

江南出行是一个 **网约车/智慧出行** 平台，采用前后端分离架构。系统分为 **乘客端**、**司机端**、**管理后台** 三端，集成 **DeepSeek AI** 智能客服与预测、**高德地图** 导航与定位、**WebSocket** 实时通信。

| 项目 | 值 |
|---|---|
| 全称 | 江南出行智慧服务平台 |
| 技术栈 | Spring Boot 3.2.6 + Java 17 + MyBatis-Plus 3.5.7 + MySQL 8.0 + Redis + Vue 3.4 + Vite 5 + Element Plus 2.7 |
| 外部集成 | DeepSeek AI、高德地图 API、WebSocket、Knife4j、Trae Image CDN |

---

## 二、项目目录结构

```
jiangnan-travel/                        # 后端 Maven 项目
├── pom.xml                             # Maven 构建（Spring Boot 3.2.6）
├── src/
│   ├── main/
│   │   ├── java/com/jiangnan/travel/
│   │   │   ├── TravelApplication.java       # 启动类
│   │   │   ├── common/
│   │   │   │   ├── BusinessException.java   # 业务异常
│   │   │   │   ├── ErrorCode.java           # 错误码枚举
│   │   │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   │   │   └── Result.java              # 统一响应封装
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java          # 跨域配置
│   │   │   │   ├── DeepSeekConfig.java      # AI 客户端配置
│   │   │   │   ├── Knife4jConfig.java       # API 文档配置
│   │   │   │   ├── MyMetaObjectHandler.java # 自动填充处理器
│   │   │   │   ├── MybatisPlusConfig.java   # MyBatis-Plus 插件
│   │   │   │   ├── RateLimitConfig.java     # Redis 限流
│   │   │   │   ├── RedisCacheConfig.java    # 缓存管理
│   │   │   │   └── TestDataInitializer.java # 测试数据初始化
│   │   │   ├── controller/          (22个)
│   │   │   ├── dto/                 (26个)
│   │   │   ├── entity/              (30个，含 BaseEntity)
│   │   │   ├── mapper/              (29个)
│   │   │   ├── security/
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── service/
│   │   │   │   ├── *.java           (22个接口)
│   │   │   │   └── impl/*.java      (22个实现)
│   │   │   ├── vo/                  (17个)
│   │   │   └── websocket/
│   │   │       ├── DriverLocationServer.java
│   │   │       ├── NotificationWebSocketServer.java
│   │   │       ├── OrderTrackingServer.java
│   │   │       └── WebSocketConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── sql/
│   │           ├── init.sql
│   │           ├── migration_optimize.sql
│   │           ├── indexes.sql
│   │           ├── seed_data.sql
│   │           ├── test_accounts.sql
│   │           ├── fix_chinese_data.sql
│   │           └── fix_password.sql
│   └── test/java/com/jiangnan/travel/
│       └── UserServiceTest.java

jiangnan-travel-web/                    # 前端 Vue 项目
├── package.json                        # npm 依赖
├── vite.config.js                      # Vite 构建配置
├── index.html
└── src/
    ├── api/                    (13个文件)
    │   ├── request.js                  # Axios 实例 + 拦截器
    │   ├── user.js                     # 用户 API
    │   ├── order.js                    # 订单 API
    │   ├── driver.js                   # 司机 API
    │   ├── admin.js                    # 管理后台 API
    │   ├── coupon.js                   # 优惠券 API
    │   ├── ai.js                       # AI/文旅 API
    │   ├── notification.js             # 消息通知 API
    │   ├── payment.js                  # 支付 API
    │   ├── invoice.js                  # 发票 API
    │   ├── campaign.js                 # 活动 API
    │   ├── vip.js                      # VIP API
    │   ├── bus.js                      # 班线 API
    ├── assets/
    │   ├── style.css                   # 全局样式（水墨江南主题）
    │   └── transitions.css             # 动画系统
    ├── components/
    │   ├── AiChatFloat.vue             # AI 聊天悬浮窗
    │   └── AmapView.vue                # 高德地图组件
    ├── composables/
    │   ├── useSmsCode.js               # 短信验证码
    │   ├── useFeedback.js              # 反馈处理
    │   └── useAmapPoiSearch.js         # 高德POI搜索
    ├── router/index.js                 # 路由配置 + 守卫
    ├── stores/user.js                  # Pinia 状态管理
    ├── views/                   (41个)

docs/
└── spec/                               # 功能规格说明
```

---

## 三、分层架构

### 3.1 后端分层

```
┌─────────────────────────────────────────────────────────┐
│  Controller 层 (@RestController)                       │
│  22 个 Controller · 97 个 API 端点                       │
│  职责：URL 路由、参数校验、认证提取、调用 Service        │
├─────────────────────────────────────────────────────────┤
│  Service 层 (Interface + Impl)                          │
│  22 个接口 + 22 个实现                                  │
│  职责：业务逻辑、事务管理、风控规则、AI 调用              │
├──────────────────┬──────────────────────────────────────┤
│  Mapper 层        │  Security 层                        │
│  (BaseMapper)     │  JWT 鉴权过滤                       │
│  29 个 Mapper     │  SecurityConfig                    │
│  纯 MyBatis-Plus  │  3 层鉴权体系                       │
├──────────────────┴──────────────────────────────────────┤
│  Entity 层 (@TableName)                                 │
│  29 个业务实体 + BaseEntity（共 30 个文件）                │
│  全部继承 BaseEntity（id/deleted/createTime/updateTime）  │
├─────────────────────────────────────────────────────────┤
│  Config 层 · Common 层 · WebSocket 层                   │
└─────────────────────────────────────────────────────────┘
```

### 3.2 包依赖方向

```
controller → dto → service → mapper → entity
                ↓
               vo
```

---

## 四、数据库架构

### 4.1 全局约定

| 规则 | 说明 |
|---|---|
| 数据库名 | `smart_travel` |
| 表前缀 | 所有表以 `t_` 开头 |
| 主键策略 | `IdType.AUTO`（数据库自增 Long） |
| 逻辑删除 | `deleted` 字段，`0=正常` `1=删除`，MyBatis-Plus `@TableLogic` |
| 自动填充 | `createTime`（INSERT）、`updateTime`（INSERT+UPDATE）、`deleted`（INSERT） |
| 时间类型 | `java.time.LocalDateTime`，格式 `yyyy-MM-dd HH:mm:ss` |
| JSON 序列化 | Jackson，时区 `Asia/Shanghai` |

### 4.2 表关系全景图

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│  t_coupon   │     │   t_user     │     │  t_car_type  │
│  (优惠券模板) │     │   (用户)      │     │   (车型定价)  │
└──────┬──────┘     └──┬───┬───┬───┘     └──┬───┬───────┘
       │               │   │   │            │   │
       │    ┌──────────┘   │   └──────┐     │   │
       │    │              │          │     │   │
       ▼    ▼              ▼          ▼     ▼   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ t_user_coupon│   │  t_driver    │   │   t_order    │
│ (用户优惠券)  │   │  (司机)      │   │   (订单核心)  │
└──────────────┘   └──────────────┘   └──┬───┬───┬───┘
                                         │   │   │
                     ┌───────────────────┘   │   └──────────┐
                     ▼                       ▼              ▼
              ┌──────────────┐       ┌──────────────┐  ┌──────────┐
              │ t_order_track│       │  t_payment   │  │ t_review │
              │ (订单轨迹)   │       │  (支付)      │  │ (评价)   │
              └──────────────┘       └──────────────┘  └──────────┘
                     ▼                       ▼              ▼
              ┌─────────────────────────────────────────────────┐
              │           t_invoice (发票)                       │
              │           t_risk_alert (风控告警)                 │
              └─────────────────────────────────────────────────┘
                     
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ t_user_address│  │ t_ai_chat_log│   │ t_push_log   │
│ (常用地址)    │   │ (AI对话日志)  │   │ (推送日志)    │
└──────────────┘   └──────────────┘   └──────────────┘

┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ t_schedule_  │   │ t_schedule_  │   │ t_demand_    │
│   route      │   │   order     │   │   hotspot    │
│ (客运班线)   │   │ (客运订单)   │   │ (需求热点)    │
└──────────────┘   └──────────────┘   └──────────────┘

┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ t_city_      │   │ t_city_      │   │ t_ai_chat_log│
│   landmark   │   │   quote      │   │ (AI对话日志)  │
│ (城市地标)   │   │ (城市语录)    │   └──────────────┘
└──────────────┘   └──────────────┘

┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ t_campaign   │──→│ t_campaign_  │   │ t_vip_level  │   │ t_user_vip   │
│ (活动)       │   │   coupon     │   │ (VIP等级)    │──→│ (用户VIP)    │
└──────────────┘   └──────────────┘   └──────────────┘   └──────────────┘

┌──────────────┐   ┌──────────────┐
│ t_risk_alert │   │ t_user_risk_ │   ┌────────────────┐
│ (风控告警)   │   │   profile    │   │ t_notification  │
│              │   │ (用户风控画像)│   │ (消息通知)      │
└──────────────┘   └──────────────┘   └────────────────┘
```

### 4.3 完整表结构（28 张表）

> **表数与 Entity 文件数差异说明**：Entity 目录共 30 个文件，其中 `BaseEntity` 为抽象基类无对应表；`OperationLog` 标注了 `@TableName("t_operation_log")` 但该表尚未在 SQL 脚本中创建。反之，`migration_optimize.sql` 中的 `t_user_preferred_driver` 无对应 Entity（规划未落地）。因此有建表语句且有 Entity 的表为 28 张。

所有表均继承 `BaseEntity` 的三个字段（`id BIGINT AUTO_INCREMENT PK`、`deleted TINYINT DEFAULT 0`、`create_time DATETIME`、`update_time DATETIME`），以下不再重复列出。

#### 用户/权限体系（3 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_user` | User | phone, password, nickname, avatar, status, preferredDriverIds, lastLoginTime | 被 10 张表引用 |
| `t_admin` | Admin | username, password, realName, role(ADMIN/SUPER), status | — |
| `t_driver` | Driver | userId, realName, idCard, carPlate, carTypeId, status, lat, lng, avgRating, totalOrders, verifyStatus, onlineDuration | → t_user, t_car_type |

#### 订单核心（4 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_order` | Order | orderNo, userId, driverId, carTypeId, startAddress, startLat/Lng, endAddress, endLat/Lng, distance, duration, basePrice, surgeFactor, couponDiscount, finalPrice, status(订单状态), cancelReason, acceptTime/arriveTime/startTime/endTime/cancelTime, isSafetyShare | → t_user, t_driver, t_car_type |
| `t_order_track` | OrderTrack | orderId, action(CREATE/ACCEPT/START/END/CANCEL), operatorId, operatorType(USER/DRIVER/SYSTEM), remark | → t_order |
| `t_payment` | Payment | orderId, userId, amount, payMethod(WECHAT/ALIPAY/BALANCE), payNo, status, payTime | → t_order, t_user |
| `t_invoice` | Invoice | userId, orderId, invoiceNo, title, taxNo, amount, status | → t_user, t_order |

#### 营销（2 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_coupon` | Coupon | name, threshold, discount, validDays, status | 被 t_user_coupon 引用 |
| `t_user_coupon` | UserCoupon | userId, couponId, status, expireTime, useOrderId | → t_user, t_coupon, t_order |

#### 评价/地址（2 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_review` | Review | orderId, userId, driverId, rating(1-5), tags, content | → t_order, t_user, t_driver |
| `t_user_address` | UserAddress | userId, tag(家/公司/学校), address, lat, lng | → t_user |

#### 车型（1 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_car_type` | CarType | name, basePrice, midPerKm, longPerKm, superLongPerKm, perMinPrice, maxPassengers, status | 被 t_driver, t_order 引用 |

#### 文旅融合（2 表）

| 表名 | 实体 | 关键字段 |
|---|---|---|
| `t_city_landmark` | CityLandmark | city, name, lat, lng, description, imageUrl, sort, status |
| `t_city_quote` | CityQuote | city, content, author, sort, status |

#### 活动/VIP（4 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_campaign` | Campaign | name, description, bannerUrl, startTime, endTime, type, status | 被 t_campaign_coupon 引用 |
| `t_campaign_coupon` | CampaignCoupon | campaignId, couponId | → t_campaign, t_coupon |
| `t_vip_level` | VipLevel | name, level(1-5), discount, minSpend, monthlyFee, yearlyFee, icon, status | — |
| `t_user_vip` | UserVip | userId, vipLevelId, feeType, startTime, endTime, status | → t_user, t_vip_level |

#### AI 服务（1 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_ai_chat_log` | AiChatLog | userId, sessionId, role(user/assistant), content, tokensUsed | → t_user |

#### 客运/班线（4 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_schedule_route` | ScheduleRoute | startCity, endCity, departTime(HH:mm), price, totalSeats, status | 被 t_schedule_order 引用 |
| `t_schedule_order` | ScheduleOrder | userId, routeId, departDate, seatCount, totalPrice, status | → t_user, t_schedule_route |
| `t_bus_line` | BusLine | lineName, startCity, endCity, busType, price, duration, distance, status | — |
| `t_bus_schedule` | BusSchedule | lineId, departTime, arriveTime, ticketCount, remaining, status | → t_bus_line |

#### 风控/推送/通知（5 表）

| 表名 | 实体 | 关键字段 | 主关联 |
|---|---|---|---|
| `t_demand_hotspot` | DemandHotspot | lat, lng, radius, demandCount, snapshotTime | — |
| `t_risk_alert` | RiskAlert | ruleCode(R2/R4/R7), userId, orderId, alertLevel(1-3), title, detail, handled, handleRemark | → t_user, t_order |
| `t_user_risk_profile` | UserRiskProfile | userId(一对一), cancelCount, orderCount, complaintCount, riskScore, riskLevel | → t_user |
| `t_push_log` | PushLog | userId, title, content, pushType, pushChannel(极光/个推/短信), status | → t_user |
| `t_notification` | Notification | userId, type(ORDER_CREATED/ORDER_ACCEPTED/...), title, content, relatedId, isRead | → t_user |

---

## 五、API 接口清单（97 个端点）

### 5.1 用户模块 — `/api/user`（7 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/user/send-code` | 发送验证码 | 公开 |
| POST | `/api/user/login` | 验证码登录 | 公开 |
| POST | `/api/user/login-password` | 密码登录 | 公开 |
| POST | `/api/user/register` | 用户注册 | 公开 |
| GET | `/api/user/profile` | 获取个人信息 | 需登录 |
| PUT | `/api/user/profile` | 修改个人信息 | 需登录 |
| PUT | `/api/user/password` | 修改密码 | 需登录 |

### 5.2 司机模块 — `/api/driver`（5 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/driver/login` | 司机登录 | 公开 |
| POST | `/api/driver/register` | 司机注册 | 公开 |
| PUT | `/api/driver/status` | 更新在线状态 | 司机身份 |
| PUT | `/api/driver/location` | 更新位置坐标 | 司机身份 |
| GET | `/api/driver/profile` | 获取司机信息 | 司机身份 |

### 5.3 司机订单 — `/api/driver/order`（8 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/driver/order/nearby` | 附近订单 | 公开 |
| POST | `/api/driver/order/{id}/accept` | 接单 | 司机身份 |
| PUT | `/api/driver/order/{id}/arrive` | 到达上车点 | 司机身份 |
| PUT | `/api/driver/order/{id}/start` | 开始行程 | 司机身份 |
| PUT | `/api/driver/order/{id}/complete` | 完成行程 | 司机身份 |
| GET | `/api/driver/order/best/{orderId}` | 最佳司机推荐 | 公开 |
| GET | `/api/driver/order/pending` | 进行中订单 | 司机身份 |
| GET | `/api/driver/order/history` | 历史订单 | 司机身份 |

### 5.4 司机收入 — `/api/driver`（2 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/driver/earning` | 收入统计 | 司机身份 |
| GET | `/api/driver/earning/weekly` | 周收入统计 | 司机身份 |

### 5.5 订单管理 — `/api/order`（9 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/order/estimate` | 预估价格 | 公开 |
| POST | `/api/order/create` | 创建订单 | 需登录 |
| GET | `/api/order/list` | 订单列表 | 需登录 |
| GET | `/api/order/{id}` | 订单详情 | 需登录 |
| PUT | `/api/order/{id}/cancel` | 取消订单 | 需登录 |
| GET | `/api/order/{id}/share` | 行程分享 | 公开 |
| POST | `/api/order/{id}/reorder` | 重新下单 | 需登录 |
| PUT | `/api/order/{id}/pay` | 支付 | 需登录 |
| POST | `/api/order/{id}/review` | 评价订单 | 需登录 |

### 5.6 优惠券 — `/api/coupon`（3 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/coupon/list` | 优惠券列表 | 公开 |
| GET | `/api/coupon/my` | 我的优惠券 | 需登录 |
| POST | `/api/coupon/claim` | 领取优惠券 | 需登录 |

### 5.7 地址 — `/api/user/address`（3 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/user/address` | 地址列表 | 需登录 |
| POST | `/api/user/address` | 新增地址 | 需登录 |
| DELETE | `/api/user/address/{id}` | 删除地址 | 需登录 |

### 5.8 管理后台 — `/api/admin`（24 个）

> 由 `AdminController`（1 个登录端点）与 `AdminManageController`（23 个管理端点，类级 `@PreAuthorize("hasRole('ADMIN')")`）共同组成。

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/admin/login` | 管理员登录 | 公开 |
| GET | `/api/admin/dashboard` | 数据大屏 | 管理员 |
| GET | `/api/admin/dashboard/chart` | 图表数据 | 管理员 |
| GET | `/api/admin/users` | 用户列表 | 管理员 |
| PUT | `/api/admin/users/{id}/status` | 更新用户状态 | 管理员 |
| GET | `/api/admin/drivers` | 司机列表 | 管理员 |
| PUT | `/api/admin/drivers/{id}/verify` | 审核司机 | 管理员 |
| GET | `/api/admin/orders` | 订单列表 | 管理员 |
| GET | `/api/admin/alerts` | 告警列表 | 管理员 |
| PUT | `/api/admin/alerts/{id}/handle` | 处理告警 | 管理员 |
| GET | `/api/admin/car-types` | 车型列表 | 管理员 |
| PUT | `/api/admin/car-types/{id}` | 更新车型 | 管理员 |
| GET | `/api/admin/campaigns` | 活动管理列表 | 管理员 |
| POST | `/api/admin/campaigns` | 新建活动 | 管理员 |
| PUT | `/api/admin/campaigns/{id}` | 编辑活动 | 管理员 |
| DELETE | `/api/admin/campaigns/{id}` | 删除活动 | 管理员 |
| GET | `/api/admin/vip-levels` | VIP 等级管理列表 | 管理员 |
| POST | `/api/admin/vip-levels/create` | 新建 VIP 等级 | 管理员 |
| PUT | `/api/admin/vip-levels/{id}` | 编辑 VIP 等级 | 管理员 |
| DELETE | `/api/admin/vip-levels/{id}` | 删除 VIP 等级 | 管理员 |
| GET | `/api/admin/bus-lines` | 班线管理列表 | 管理员 |
| POST | `/api/admin/bus-lines/create` | 新建班线 | 管理员 |
| PUT | `/api/admin/bus-lines/{id}` | 编辑班线 | 管理员 |
| DELETE | `/api/admin/bus-lines/{id}` | 删除班线 | 管理员 |

### 5.9 文旅 — `/api/landmark`（2 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/landmark` | 地标列表 | 公开 |
| GET | `/api/landmark/search` | 搜索地标 | 公开 |

### 5.10 AI 服务 — `/api/ai`（6 个）

> 由 `AiChatController`（4 个）、`AiPredictionController`（1 个）与 `AIDataController.hotspots`（1 个）共同组成。

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/ai/chat` | AI 对话 | 公开/可选登录 |
| POST | `/api/ai/chat/stream` | AI 流式对话（SSE） | 公开/可选登录 |
| GET | `/api/ai/sessions` | 会话历史列表 | 需登录 |
| GET | `/api/ai/sessions/{sessionId}/messages` | 会话消息详情 | 需登录 |
| GET | `/api/ai/recommend-dest` | 推荐目的地 | 需登录 |
| GET | `/api/ai/hotspots` | 需求热点 | 公开 |

### 5.11 安全风控 — `/api/safety`（1 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/safety/alert` | 安全预警 | 公开 |

### 5.12 文旅数据 — 多路径（2 个）

> 由 `AIDataController` 提供（类级 `@RequestMapping("/api")`，方法分布在不同业务前缀下）。

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/common/city-quote` | 城市文化短句 | 公开 |
| GET | `/api/user/frequent-routes` | 常走路线 | 需登录 |

### 5.13 AI 分析 — `/api/admin/ai`（1 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/admin/ai/insight` | 数据分析 | 公开 |

### 5.14 消息通知 — `/api/notification`（4 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/notification/list` | 通知列表(分页) | 需登录 |
| GET | `/api/notification/unread-count` | 未读通知数量 | 需登录 |
| PUT | `/api/notification/{id}/read` | 标记单条已读 | 需登录 |
| PUT | `/api/notification/read-all` | 标记全部已读 | 需登录 |

### 5.15 活动管理 — `/api/campaign`（3 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/campaign/list` | 活动列表(分页) | 公开 |
| GET | `/api/campaign/{id}` | 活动详情 | 公开 |
| POST | `/api/campaign/{id}/claim` | 活动领券 | 需登录 |

### 5.16 VIP 会员 — `/api/vip`（4 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/vip/levels` | VIP等级列表 | 公开 |
| GET | `/api/vip/benefits` | VIP权益说明 | 公开 |
| GET | `/api/vip/my` | 我的VIP信息 | 需登录 |
| POST | `/api/vip/purchase` | 开通/续费VIP | 需登录 |

### 5.17 班线 — `/api/bus-line`（3 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/bus-line/list` | 班线列表 | 公开 |
| GET | `/api/bus-line/{id}` | 班线详情 | 公开 |
| POST | `/api/bus-line/purchase` | 购买班线车票 | 需登录 |

### 5.18 支付 — `/api/payment`（4 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/payment/create` | 创建支付单 | 需登录 |
| GET | `/api/payment/{orderId}` | 查询支付状态 | 需登录 |
| GET | `/api/payment/list` | 支付记录列表 | 需登录 |
| POST | `/api/payment/callback` | 支付回调（第三方/模拟） | 公开 |

### 5.19 发票 — `/api/invoice`（5 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/invoice/apply` | 申请发票 | 需登录 |
| GET | `/api/invoice/list` | 发票列表 | 需登录 |
| GET | `/api/invoice/{id}` | 发票详情 | 需登录 |
| PUT | `/api/invoice/{id}/cancel` | 取消发票申请 | 需登录 |
| PUT | `/api/invoice/{id}/issue` | 开票 | 管理员 |

### 5.20 路径规划 — `/api/route`（1 个）

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/api/route/plan` | 路径规划 | 需登录 |

---

## 六、安全架构

### 6.1 三层鉴权体系

```
┌──────────────────────────────────────────────────┐
│  SecurityConfig                                    │
│  ├─ 公开接口（permitAll）                           │
│  │   登录/注册、地标、swagger、/ws/**               │
│  ├─ 需认证接口（authenticated）                     │
│  │   订单 CRUD、地址、个人信息、优惠券操作            │
│  └─ CSRF 禁用                                       │
├──────────────────────────────────────────────────┤
│  JwtAuthFilter                                     │
│  ├─ 从 Authorization: Bearer <token> 提取           │
│  ├─ JwtUtil.parse() → userId + phone               │
│  ├─ 放入 SecurityContext                            │
│  └─ Authentication.getPrincipal() = userId (Long)   │
├──────────────────────────────────────────────────┤
│  Controller 层                                     │
│  ├─ Authentication authentication 参数注入           │
│  ├─ (Long) authentication.getPrincipal() = userId  │
│  └─ 司机端：getDriverIdFromAuth() 转换 userId→driverId │
└──────────────────────────────────────────────────┘
```

### 6.2 JWT 配置

| 配置 | 值 |
|---|---|
| 算法 | HMAC-SHA256（jjwt 0.12.5） |
| 有效期 | 86400000ms（24 小时） |
| 密钥 | 通过环境变量 `JWT_SECRET` 注入（≥256 位，禁止硬编码） |
| Token 内容 | sub=userId, claim("phone")=phone, claim("role")=ROLE |
| 密码加密 | BCryptPasswordEncoder |

### 6.3 公开接口清单（permitAll）

> 严格对应 `SecurityConfig` 中的 `permitAll()` 配置。注意：`/api/ai/**` 全部放行，但其中 `sessions`、`recommend-dest` 等端点在业务层依赖 `SecurityContext` 中的 `userId`，未登录调用将无法获取用户身份。

```
# 用户登录/注册
POST /api/user/register
POST /api/user/login
POST /api/user/login-password
POST /api/user/send-code

# 司机登录/注册
POST /api/driver/login
POST /api/driver/register

# 管理员登录
POST /api/admin/login

# 文旅与通用数据
GET  /api/landmark
GET  /api/landmark/search
GET  /api/common/city-quote

# AI 服务（/api/ai/** 全部放行，方法级 @PreAuthorize 可能进一步限制）
POST /api/ai/chat
POST /api/ai/chat/stream
GET  /api/ai/sessions
GET  /api/ai/sessions/{sessionId}/messages
GET  /api/ai/recommend-dest
GET  /api/ai/hotspots

# 优惠券/活动/VIP/订单/班线 公开查询
GET  /api/coupon/list
GET  /api/campaign/list
GET  /api/campaign/{id}
GET  /api/vip/levels
GET  /api/vip/benefits
POST /api/order/estimate
GET  /api/bus-line/list
GET  /api/bus-line/{id}

# 司机订单查询
GET  /api/driver/order/nearby

# 支付回调
POST /api/payment/callback

# WebSocket
/ws/**

# API 文档
/swagger-ui/**, /swagger-ui.html, /v3/api-docs/**, /doc.html, /webjars/**

# 监控端点
/actuator/health, /actuator/info, /actuator/prometheus, /actuator/metrics
```

---

## 七、核心业务引擎

### 7.1 阶梯计价引擎（PricingService）

```
3km 以内        → basePrice（起步价）
3km - 50km     → basePrice + (distance-3) × midPerKm
50km - 200km   → 3km内起步价 + 47km×midPerKm + (distance-50)×longPerKm
200km+         → 3km内起步价 + 47km×midPerKm + 150km×longPerKm + (distance-200)×superLongPerKm

总价 = 距离费 + 时长费(duration × perMinPrice)
最终价 = 总价 × surgeFactor - couponDiscount
```

### 7.2 智能派单引擎（DispatchService）

```
派单评分 = 距离得分 × 0.5 + 评分得分 × 0.3 + 空闲时长得分 × 0.2
派给评分最高的司机
```

### 7.3 风控系统（OrderService）

| 规则 | 编码 | 逻辑 |
|---|---|---|
| 取消频率限制 | R2 | 1 小时内取消 > 5 次 → 禁止下单并写入风控告警 |
| 深夜跨城检查 | R4 | 23:00-05:00 跨城订单 → 记录风控告警 |
| 疲劳驾驶提醒 | R7 | 行程时长 > 2 小时 → 写入风控告警 |

### 7.4 AI 客服（AiChatService）

```
┌─ 用户消息 ──→ 保存到 t_ai_chat_log ──→ 加载历史20条 ──→ 构建对话上下文
├─ SSE 流式模式 ──→ CompletableFuture ──→ createStreaming() ──→ 逐 chunk 发送 SSE event(delta/done)
├─ 普通对话模式 ──→ create() ──→ 返回完整回复
├─ 成功 → 保存 AI 回复日志
└─ 失败 → 返回本地兜底回复
系统提示词：包含江西/无锡文旅知识库
```

| 方法 | 说明 |
|---|---|
| `chat()` | 普通 POST 对话 |
| `chatStream()` | SSE 流式对话，返回 `SseEmitter`（60s 超时）|
| `getSessions()` | 获取用户会话列表 |
| `getSessionMessages()` | 获取指定会话全部消息 |
| `buildChatParams()` | 加载历史消息（最近 20 条）构建上下文参数 |

会话管理：前端通过 `sessionId` 关联同一主题，`AiChatFloat` 组件支持侧边栏历史切换、新建对话。

---

## 八、Maven 依赖

| 依赖 | 版本 | 用途 |
|---|---|---|
| spring-boot-starter-web | 3.2.6(managed) | Web 容器 |
| spring-boot-starter-security | 3.2.6(managed) | 安全认证 |
| spring-boot-starter-validation | 3.2.6(managed) | 参数校验 |
| spring-boot-starter-websocket | 3.2.6(managed) | WebSocket 实时通信 |
| spring-boot-starter-data-redis | 3.2.6(managed) | Redis 缓存 |
| mybatis-plus-spring-boot3-starter | 3.5.7 | ORM |
| mysql-connector-j | managed | MySQL 驱动 |
| redisson-spring-boot-starter | 3.32.0 | 分布式锁 |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.5 | JWT 令牌 |
| knife4j-openapi3-jakarta-spring-boot-starter | 4.5.0 | API 文档 |
| openai-java | 0.18.0 | DeepSeek AI SDK |
| hutool-all | 5.8.25 | 工具类（已移除）|
| spring-boot-starter-actuator | 3.2.6(managed) | 健康检查与监控端点（已移除）|
| micrometer-registry-prometheus | 1.12.6(managed) | Prometheus Metrics 暴露（已移除）|
| lombok | managed | 编译期注解 |

---

## 九、前端架构

### 9.1 技术栈

| 依赖 | 版本 | 用途 |
|---|---|---|
| Vue | 3.4.x | 核心框架 |
| Vue Router | 4.3.x | 路由 |
| Pinia | 2.1.x | 状态管理 |
| Axios | 1.7.x | HTTP 请求 |
| Element Plus | 2.7.x | UI 组件库 |
| Vite | 5.2.x | 构建工具 |

### 9.2 路由与鉴权

```
乘客端（Layout）
├── /home
├── /order-create
├── /order-list
├── /orders → /order-list（重定向）
├── /order/:id
├── /order/:id/review
├── /trip/:id                 — 行程追踪
├── /address                  — 常用地址
├── /coupon                   — 优惠券
├── /profile                  — 个人中心
├── /security-settings        — 安全设置
├── /about-company            — 关于公司
├── /message-center           — 消息中心
├── /campaign-list            — 活动中心
├── /campaign/:id             — 活动详情
├── /vip-center               — 会员中心
├── /landmark-explore         — 文旅发现
├── /ai-assistant             — AI 助手
├── /invoice-center           — 发票中心
├── /invoice-apply            — 申请发票
├── /payment/:id              — 支付页
└── /bus-line                 — 城际班线

司机端（DriverLayout）
├── /driver/home
├── /driver/order/:id
├── /driver/earnings
└── /driver/profile

管理端（AdminLayout）
├── /admin/dashboard
├── /admin/users
├── /admin/drivers
├── /admin/orders
├── /admin/alerts
├── /admin/car-types
├── /admin/campaigns
├── /admin/vip-levels
├── /admin/bus-lines
└── /admin/profile

公开路由
├── /login
├── /register
├── /driver/login  → /login
├── /admin/login   → /login
└── /orders        → /order-list
```

**路由守卫三层鉴权：**
1. **乘客认证** — 检查 `localStorage.token`，无则跳 `/login`
2. **司机认证** — 检查 `localStorage.driverToken`，无则跳 `/driver/login`
3. **管理员认证** — 检查 `localStorage.adminToken`，无则跳 `/admin/login`

### 9.3 API 请求拦截

```
request.js (Axios)
├── 请求拦截器：自动附加 Bearer token
└── 响应拦截器：
    ├── code !== 200 → ElMessage.error + Promise.reject
    ├── 401 → 清除存储 + 跳转登录页
    ├── 403 → 权限不足提示
    ├── 500 → 服务器繁忙提示
    └── 网络错误 → 网络请求失败提示
```

### 9.4 主题规范

CSS 变量体系（水墨江南风格）：

```css
--color-primary: #2D8A6E   主色（绿）
--color-accent: #F5C26B    辅色（金）
--color-bg: #FAF8F5        背景（米白）
--color-text: #2C2C2C      文本
```

移动端适配：768px 以下缩放字体，768px 以上模拟手机容器（max-width: 540px）。

### 9.5 构建与性能优化

Vite 构建配置（`vite.config.js`）：

```js
build: {
  chunkSizeWarningLimit: 1500,
  rollupOptions: {
    output: {
      manualChunks: {
        'echarts-vendor': ['echarts'],
        'element-plus-vendor': ['element-plus', '@element-plus/icons-vue']
      }
    }
  }
}
```

- **Vendor 分包**：将 ECharts 和 Element Plus 单独打包，避免主 chunk 过大。
- **按需加载**：`AdminDashboard.vue` 通过 `import('echarts')` 动态加载图表库，首屏不阻塞。
- **路由懒加载**：所有页面组件使用 `() => import('@/views/xxx.vue')`。

### 9.6 图片 CDN

前端使用 **Trae text-to-image** 服务作为图片 CDN，通过环境变量 `VITE_IMAGE_CDN_BASE` 配置基础 URL，默认地址为：

```
https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image
```

封装工具：

| 文件 | 说明 |
|---|---|
| `src/utils/imageCDN.js` | CDN URL 构建、用户/司机/管理员/AI 头像生成、活动 Banner 生成 |
| `src/components/CdnAvatar.vue` | 通用头像组件，支持自定义头像、CDN 默认头像、加载失败 fallback |

使用场景：

- **乘客头像**：`Profile.vue`、`Home.vue` 问候区
- **司机头像**：`DriverProfile.vue`、`DriverLayout.vue`、`TripTracking.vue`
- **管理员头像**：`AdminProfile.vue`、`AdminLayout.vue`
- **AI 助手头像**：`AiAssistant.vue` 头部与聊天消息
- **活动 Banner**：`CampaignDetail.vue` 无 bannerUrl 时自动生成；`AdminCampaigns.vue` 支持“生成默认 Banner”

配置示例（`.env`）：

```env
VITE_IMAGE_CDN_BASE=https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image
```

---

## 十、WebSocket 架构

| 端点 | 路径 | 用途 | 鉴权 |
|---|---|---|---|
| DriverLocationServer | `/ws/driver-location` | 司机位置心跳（ping/pong + 坐标更新） | Token 鉴权 |
| OrderTrackingServer | `/ws/order/{orderId}` | 用户订阅订单实时状态推送 | Token 鉴权 |
| NotificationWebSocketServer | `/ws/notification/{userId}` | 用户实时接收新通知推送 | Token 鉴权 |
| WebSocketConfig | — | WebSocket 注册配置（注册以上 3 个端点） | — |

---

## 十一、缓存架构

| 缓存 | TTL | 存储位置 |
|---|---|---|
| 验证码 | 5 分钟 | Redis（String） |
| 车型列表 | 24 小时 | Redis Cache（`carTypes`） |
| 城市地标 | 24 小时 | Redis Cache（`landmarks`） |
| 用户限流 | 1 分钟 | Redis（RateLimitInterceptor） |
| 管理后台大屏统计 | 2 分钟 | Redis Cache（`dashboard`） |
| JWT | 24 小时 | Token 本身（无状态） |

---

## 十二、配置清单

### 12.1 MySQL 配置

```yaml
url: jdbc:mysql://localhost:3306/smart_travel?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
username: root
pool: HikariCP (min-idle=5, max-pool-size=20)
```

### 12.2 Redis 配置

```yaml
host: localhost:6379
database: 0
pool: Lettuce (max-active=16, max-idle=8, min-idle=4)
```

### 12.3 外部服务

| 服务 | 配置项 |
|---|---|
| DeepSeek AI | api-key, base-url: https://api.deepseek.com/v1, model: deepseek-chat |
| 高德地图 | web-api-key, js-api-key, security-code |

### 12.4 监控配置

| 端点 | 说明 | 权限 |
|---|---|---|
| `/actuator/health` | 健康检查（含 db、diskSpace） | 公开 |
| `/actuator/info` | 应用信息 | 公开 |
| `/actuator/metrics` | 指标名称列表 | 公开 |
| `/actuator/prometheus` | Prometheus 拉取端点 | 公开 |

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
```

### 12.5 SQL 脚本（`resources/sql/`）

| 脚本 | 用途 |
|---|---|
| `init.sql` | 完整建表（27 张）+ 管理员/车型/地标等种子数据 |
| `migration_optimize.sql` | 补充 update_time 字段；新增 `t_notification`（有 Entity）与 `t_user_preferred_driver`（无 Entity，规划未落地）两张表；需求热点索引优化 |
| `indexes.sql` | 订单高频查询联合索引优化 |
| `seed_data.sql` | 长途车型种子数据（城际快车、长途大巴） |
| `test_accounts.sql` | 6 个测试账号（3 司机 + 3 乘客，默认密码 `123456`） |
| `fix_chinese_data.sql` | 修复中文乱码数据（城市地标、城市语录等） |
| `fix_password.sql` | 重置用户/管理员密码 |

### 12.6 CI/CD 配置

| 文件 | 说明 |
|---|---|
| `.github/workflows/ci.yml` | GitHub Actions 流水线：后端 Maven 编译/测试 + 前端 npm 构建 |

触发条件：`push` / `pull_request` 至 `main` / `master` / `develop` 分支。

---

## 十三、测试

当前有 3 个测试文件：

| 文件 | 测试内容 |
|---|---|
| `UserServiceTest.java` | 发送验证码 → 注册 → 重复注册异常 → 验证码登录 → 获取个人信息 |
| `OrderServiceTest.java` | 发送验证码 → 注册 → 创建订单 → 列表查询 → 订单详情 → 取消订单 → 行程分享 → 重复取消异常 |
| `DriverServiceTest.java` | 用户注册 → 司机注册 → 未审核登录异常 → 审核通过 → 司机登录 → 个人信息 → 状态与位置更新 → 收入统计 → 线上数量统计 → 重复注册异常 |

---

## 十四、新增模块指引

新增模块时需同步更新的内容：

1. **实体** → `entity/` 包，继承 `BaseEntity`，标注 `@TableName` + `@TableId`
2. **Mapper** → `mapper/` 包，继承 `BaseMapper<T>`
3. **接口** → `service/` 包 + `service/impl/` 包
4. **请求 DTO** → `dto/` 包，`XxxRequest` 命名，加 `@Schema` + 校验注解
5. **响应 VO** → `vo/` 包，`XxxVO` 命名，`@Builder` 模式
6. **Controller** → `controller/` 包，`@Tag` + `@Operation` + `@RequiredArgsConstructor`
7. **安全配置** → 如接口需要公开访问，在 `SecurityConfig` 中添加白名单
8. **前端 API** → `api/` 目录新增 JS 文件
9. **前端页面** → `views/` 目录新增 Vue 文件
10. **前端路由** → `router/index.js` 添加路由 + 鉴权守卫
11. **本文档** → 各章节同步更新

---

> 本文档为项目唯一架构文档，由 AI 开发环境全量扫描自动生成。  
> 后续每次新增模块必须同步更新本文档。  
> 生成时间：2026-06-24
