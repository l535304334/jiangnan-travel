# 江南出行智慧服务平台

南昌大学软件工程 2307 班毕业实习项目 | 2026.7.6 - 2026.8.6

## 项目简介

江南出行运输服务有限公司宁都分公司的网约车智慧服务平台，覆盖江西省内出行服务，融合 AI 大模型与文旅元素。

### 核心技术亮点

- **5 大 AI 引擎**：智能客服（DeepSeek）、出行预测、动态调价、安全风控、智能调度
- **阶梯计价**：起步价 + 中程(3-50km) + 远程(50-200km) + 超远程(>200km)
- **文旅融合**：搜索地标标签 + 行程中文化短句轮播
- **水墨江南 UI**：碧波绿 + 暖阳杏 + 烟雨白 + 墨色

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.6 |
| 安全 | Spring Security + JWT |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 + Redisson 3.32.0 |
| AI | DeepSeek API (openai-java 0.18.0) |
| 前端 | Vue 3 + Vite + Element Plus |
| 接口文档 | Knife4j 4.5.0 |

## 项目结构

```
├── jiangnan-travel/              # 后端 Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/jiangnan/travel/
│       ├── TravelApplication.java
│       ├── config/               # 配置（CORS/Security/Cache/RateLimit）
│       ├── security/             # JWT + Spring Security
│       ├── common/               # Result/ErrorCode/异常处理
│       ├── entity/               # 21 个实体类
│       ├── mapper/               # MyBatis-Plus Mapper
│       ├── service/              # 业务逻辑（11 个 Service）
│       ├── controller/           # REST API（9 个 Controller）
│       ├── dto/                  # 请求 DTO
│       └── vo/                   # 响应 VO
│
├── jiangnan-travel-web/          # 前端 Vue 3
│   ├── package.json
│   └── src/
│       ├── api/                  # 5 个 API 模块
│       ├── views/                # 24 个页面组件
│       ├── router/               # 27 条路由
│       └── stores/               # Pinia 状态
│
└── 江南出行智慧服务平台_设计文档与开发计划.html
```

## 快速启动

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis (Windows: `winget install Redis.Redis`)
- Node.js 18+
- Maven 3.9+

### 1. 创建数据库

```bash
mysql -u root -p < jiangnan-travel/src/main/resources/sql/init.sql
# 可选：执行索引优化
mysql -u root -p smart_travel < jiangnan-travel/src/main/resources/sql/indexes.sql
```

### 2. 配置 application.yml

修改 `src/main/resources/application.yml`：
- MySQL 用户名/密码
- DeepSeek API Key
- 高德地图 API Key

### 3. 启动后端

```bash
cd jiangnan-travel
mvn spring-boot:run
# API 文档: http://localhost:8080/swagger-ui.html
```

### 4. 启动前端

```bash
cd jiangnan-travel-web
npm install
npm run dev
# 浏览器: http://localhost:5173
```

## API 概览（21 个接口）

| 模块 | 接口数 | 说明 |
|------|--------|------|
| 用户 `/api/user` | 8 | 验证码、注册、登录、个人信息、收藏地址 |
| 订单 `/api/order` | 7 | 估价、创建、列表、详情、取消、支付、评价 |
| 司机 `/api/driver` | 6 | 登录、注册、状态、位置、接单、完成 |
| AI `/api/ai` | 2 | 智能客服、出行预测 |
| 地标 `/api/landmark` | 2 | 列表、搜索 |
| 优惠券 `/api/coupon` | 3 | 列表、我的、领取 |

## 演示账号

| 角色 | 账号 | 说明 |
|------|------|------|
| 管理员 | admin | 默认密码 admin123 |
| 用户 | 13800138000 | 验证码登录 |

## 实习信息

- 学校：南昌大学软件学院
- 班级：软件工程 2307 班
- 实习单位：江南出行运输服务有限公司宁都分公司
- 岗位：软件开发实习生
- 时间：2026.7.6 - 2026.8.6
- 指导教师：校内导师 XXX / 企业导师 XXX
