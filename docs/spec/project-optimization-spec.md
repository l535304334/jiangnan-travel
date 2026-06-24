# 江南出行智慧服务平台 — 项目优化规范

> **版本**: v1.1
> **日期**: 2026-06-24
> **状态**: ✅ 已完成

---

## 一、审查总结

### 整体评分

| 维度 | 评分 | 关键问题数量 |
|------|------|------------|
| 前端代码质量 | 55/100 | P1=5, P2=16, P3=8 |
| 后端代码一致性 | 65/100 | 20+ 设计不一致 |
| 数据库设计 | 80/100 | 4 个设计缺陷 |
| API RESTful 规范 | 60/100 | 8 个不符合项 |
| API 文档化 | 10/100 | 零注解 |
| 安全性 | 70/100 | 3 个安全风险 |

### P1 级严重问题（必须修复）

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 1 | 高德地图密钥硬编码在前端 | `AmapView.vue` | 密钥泄露风险 |
| 2 | 无路由鉴权守卫 | `router/index.js` | 未登录可访问任意页面 |
| 3 | 异常处理大面积静默吞除 | 多个 API 模块 | 问题难以追踪 |
| 4 | 两个页面完全硬编码假数据 | `AdminDrivers.vue`, `AdminUsers.vue` | 无法对接真实API |
| 5 | Knife4j/Swagger 零注解 | 全部 Controller/Entity/DTO | 无 API 文档可用 |

### P2 级问题（应修复）

| # | 问题 | 位置 |
|---|------|------|
| 6 | 18个Entity未继承BaseEntity | entity/ 目录 |
| 7 | Map<String,Object> 替代 DTO 传参 | 多个 Controller |
| 8 | driverId 从参数而非 JWT 获取 | DriverController, DriverOrderController |
| 9 | API 路径含动词（cancel/pay/reorder） | OrderController |
| 10 | 缺少 @Valid/@Validated 参数校验 | 多个 Controller 方法 |
| 11 | 16张表缺少 update_time | DDL |
| 12 | preferred_driver_ids 违反1NF | t_user 表 |
| 13 | `/api/coupon/list` 路径冗余 | CouponController |
| 14 | API base URL 硬编码在组件中 | 多个 Vue 组件 |
| 15 | 部分接口缺少 loading 状态 | 多个 Vue 组件 |
| 16 | Pinia store 缺少 TypeScript 类型 | stores/user.js |

---

## 二、优化优先级与执行计划

### 阶段 1: 安全与架构（P1 紧急修复）
| Task | 文件 | 工作量 | 依赖 |
|------|------|--------|------|
| 1.1 修复高德地图密钥硬编码 | `AmapView.vue` | 小 | 无 |
| 1.2 添加路由鉴权守卫 | `router/index.js` | 小 | 无 |
| 1.3 修复异常静默吞除 | 7 个 API 文件 | 小 | 无 |
| 1.4 修复硬编码假数据 | `AdminDrivers.vue`, `AdminUsers.vue` | 中 | 无 |

### 阶段 2: API 文档化
| Task | 文件 | 工作量 | 依赖 |
|------|------|--------|------|
| 2.1 添加 Knife4j 配置支持 | 配置文件 | 小 | 无 |
| 2.2 Controller 添加 @Tag/@Operation | 15 个 Controller | 大 | 无 |
| 2.3 DTO/VO 添加 @Schema | 14 个 DTO/VO | 中 | 无 |

### 阶段 3: 后端代码规范化
| Task | 文件 | 工作量 | 依赖 |
|------|------|--------|------|
| 3.1 统一 Entity 继承 BaseEntity | 18 个 Entity | 大 | 无 |
| 3.2 Map 参数替换为 DTO | 多个 Controller | 大 | 无 |
| 3.3 driverId 从 JWT 获取 | DriverController | 中 | 无 |
| 3.4 添加 @Valid 参数校验 | 多个 Controller | 中 | 无 |

### 阶段 4: API 路径规范化
| Task | 文件 | 工作量 | 依赖 |
|------|------|--------|------|
| 4.1 动词路径改为资源路径 | OrderController | 中 | 无 |
| 4.2 coupon/list 简化为 /coupon | CouponController | 小 | 无 |

### 阶段 5: 数据库优化
| Task | 文件 | 工作量 | 依赖 |
|------|------|--------|------|
| 5.1 补充缺失的 update_time 字段 | DDL | 小 | 无 |
| 5.2 修复 preferred_driver_ids 设计 | DDL + Entity | 中 | 无 |
| 5.3 t_review 补充 deleted 字段 | DDL + Entity | 小 | 无 |

### 阶段 6: 前端代码质量
| Task | 文件 | 工作量 | 依赖 |
|------|------|--------|------|
| 6.1 API base URL 抽取到环境变量 | .env + request.js | 小 | 无 |
| 6.2 添加 loading 和错误状态 | 多个组件 | 中 | 无 |
| 6.3 清理未使用导入和变量 | 多个文件 | 小 | 无 |

### 阶段 7: 测试与文档
| Task | 文件 | 工作量 | 依赖 |
|------|------|--------|------|
| 7.1 补充后端 JUnit 测试 | test/ 目录 | 大 | 阶段3 |
| 7.2 更新项目 README | README.md | 中 | 全部 |

---

## 三、技术规范

### 3.1 前端规范
- 地图密钥使用 `import.meta.env.VITE_AMAP_KEY` 读取
- 路由守卫使用 `router.beforeEach` + Pinia store 判断登录
- API 异常统一在 `request.js` 拦截器中处理
- 组件使用 Composition API + `<script setup>` 语法
- 所有 API base URL 通过 `VITE_API_BASE_URL` 配置

### 3.2 后端规范
- Entity 统一继承 BaseEntity（id, createTime, updateTime, deleted）
- 禁用在 Controller 中直接使用 `Map<String, Object>` 接收参数
- 所有请求体必须有对应的 DTO 类并添加 `@Valid` 校验
- driverId/adminId 必须从 SecurityContext 获取，不接受客户端传入
- API 路径使用资源命名，状态变更使用 `PUT /resource/{id}` + 请求体

### 3.3 数据库规范
- 所有业务表必须包含 `create_time`, `update_time`, `deleted`
- 不存储 JSON 数组在 VARCHAR 字段中，使用关联表
- 所有金额字段使用 `DECIMAL(10,2)`
- 所有状态字段使用 `TINYINT` 并加 `COMMENT`

---

## 四、验收标准

1. 高德地图密钥不在源码中出现
2. 未登录用户无法访问需要认证的页面
3. API 调用失败时用户能看到友好提示
4. 管理员页面数据来自真实 API
5. Knife4j 页面可浏览完整的 API 文档
6. 所有 Entity 继承一致
7. 所有请求参数有类型安全和校验
8. 身份信息从 JWT 获取而非客户端参数
