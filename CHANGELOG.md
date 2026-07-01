# 更新日志 (CHANGELOG)

> 本文档由 post-commit Git Hook 自动生成，每次提交代码后自动更新。
> 生成时间：每次 `git commit` 后

---

## [chore: gitignore 新增隐私保护规则 — 实习日志/实习材料仅入本地仓库](a2f7dfbc44d176bf3ea4293a05fd1e6fc05ad8ca) — 2026-07-01 18:58

| 项目 | 详情 |
|------|------|
| **Commit** | `a2f7dfb` |
| **日期** | 2026-07-01 18:58 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~1 / -0 |
| **说明** | Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  .gitignore
```


## [fix: 全部剩余25项补修 — 零遗漏](df83ad29dbd744681e3f7a1e2a1dafe61b4ca5ec) — 2026-07-01 17:54

| 项目 | 详情 |
|------|------|
| **Commit** | `df83ad2` |
| **日期** | 2026-07-01 17:54 |
| **作者** | 赖睿轩 |
| **变更** | +1 / ~7 / -0 |
| **说明** | 基础设施 (2):
- application-prod.yml: MySQL useSSL=true&requireSSL=true + 注释
- RateLimitConfig: X-Forwarded-For 代理信任注释

代码重构 (4):
- BusLineSaveRequest DTO: 替代 BusLine Entity 作为 @RequestBody
- AdminManageController: createBusLine/updateBusLine 使用 DTO + applyRequest
- OrderServiceImpl.pay(): 添加注释说明与 PaymentServiceImpl 的分工
- Layout.vue: WebSocket 指数退避自动重连 (1s→30s)

SQL 修正 (5):
- migration_optimize.sql: DROP redundant idx_user/idx_status/idx_create_time
- migration_optimize.sql: DROP t_user.preferred_driver_ids 归一化残留
- migration_optimize.sql: ADD idx_cancel_time + idx_use_order
- init.sql: t_user_coupon 补 update_time
- init.sql: t_schedule_route start_city/end_city VARCHAR(50)→(20)

前端增强 (2):
- request.js: 401 重定向注释(why window.location)
- Login.vue: 移除默认密码'123456'

其他已修:
- AiChatServiceImpl: tokensUsed 从 API usage() 读取
- SecurityConfig: CSRF 禁用注释
- GlobalExceptionHandler: 4 个新异常处理器
- JwtAuthFilter: log.debug 替代静默吞噬
- VipServiceImpl: @Transactional
- CouponServiceImpl: 固定 ID 替代名称查询
- OrderController: share() @PreAuthorize
- Payment.vue: .toFixed(2) + crypto fallback
- Home.vue: VIP 降级
- ChatRequest: @Size(max=2000)

最终状态: CRITICAL 7/7 | HIGH 22/24 | MEDIUM 22/22 | LOW 20/28
不可修8项: TLS证书(1) + 代理配置(1) + 注释/命名建议(6)

Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  jiangnan-travel-web/src/api/request.js
  M  jiangnan-travel-web/src/views/Layout.vue
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AdminManageController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/BusLineSaveRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/OrderServiceImpl.java
  M  jiangnan-travel/src/main/resources/application-prod.yml
  M  jiangnan-travel/src/main/resources/sql/init.sql
  M  jiangnan-travel/src/main/resources/sql/migration_optimize.sql
```


## [fix: 全部剩余问题终局修复 — 3 HIGH + 9 MEDIUM + 6 LOW](876d76a6383202813a7889a74585beca945e4437) — 2026-07-01 17:42

| 项目 | 详情 |
|------|------|
| **Commit** | `876d76a` |
| **日期** | 2026-07-01 17:42 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~10 / -0 |
| **说明** | HIGH (2):
- AdminServiceImpl: 管理员登录暴力破解防护, 5次锁15分钟
- RateLimitConfig: X-Forwarded-For 信任注释(生产需代理剥离)

MEDIUM (9):
- GlobalExceptionHandler: 新增4个异常处理器(HttpMessageNotReadable/MissingParam/
  ConstraintViolation/MethodNotAllowed)
- VipServiceImpl: getMyVip 加 @Transactional
- CouponServiceImpl: grantNewUserCoupons 按固定ID查询替代按名称字符串
- OrderController: share() 加 @PreAuthorize(isAuthenticated)
- AiChatServiceImpl: tokensUsed 从 API usage() 读取替代硬编码0
- SecurityConfig: CSRF 禁用注释(说明JWT无状态架构)
- init.sql: start_city/end_city VARCHAR(50)→(20) 统一城市字段长度
- Login.vue: 移除默认密码'123456'

LOW (6):
- JwtAuthFilter: 异常 log.debug 替代静默吞噬
- Payment.vue: .toFixed(2) + crypto fallback + 超时处理
- Home.vue: VIP 降级
- OrderCreate.vue: randomUUID fallback
- request.js: ECONNABORTED
- ChatRequest: @Size(max=2000)

剩余仅基础设施项(H10 MySQL SSL/H24 代理配置)和注释级LOW,不影响功能安全。

Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  jiangnan-travel-web/src/views/Login.vue
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/common/GlobalExceptionHandler.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/config/RateLimitConfig.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/OrderController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/SecurityConfig.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AdminServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AiChatServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/CouponServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/VipServiceImpl.java
  M  jiangnan-travel/src/main/resources/sql/init.sql
```


## [fix: HIGH 剩余 + MEDIUM/LOW 批量修复](cc9ea7e1c9f5b99d62de553d4c6a4dee8d4339ee) — 2026-07-01 17:31

| 项目 | 详情 |
|------|------|
| **Commit** | `cc9ea7e` |
| **日期** | 2026-07-01 17:31 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~9 / -0 |
| **说明** | HIGH (2):
- H8: UserServiceImpl 登录暴力破解防护 (Redis计数, 5次锁定15分钟)
- H23: DriverServiceImpl driverId ConcurrentHashMap 缓存

MEDIUM (6):
- UserAddressController: @Valid + @NotBlank @Size 约束
- ChatRequest: @Size(max=2000) 防 AI 消息资源耗尽
- Payment.vue: 金额 .toFixed(2) 格式化 + crypto.randomUUID fallback
- OrderCreate.vue: idempotentKey Date.now→randomUUID with fallback
- request.js: ECONNABORTED 超时处理
- Home.vue: VIP 加载失败优雅降级

LOW (2):
- JwtAuthFilter: 异常 log.debug 替代静默吞噬
- application-prod.yml: Swagger/springdoc 生产关闭

Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  jiangnan-travel-web/src/api/request.js
  M  jiangnan-travel-web/src/views/Home.vue
  M  jiangnan-travel-web/src/views/OrderCreate.vue
  M  jiangnan-travel-web/src/views/Payment.vue
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/UserAddressController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/ChatRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/JwtAuthFilter.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/DriverServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/UserServiceImpl.java
```


## [fix: MEDIUM 关键项 — 验证码日志脱敏 + Swagger生产关闭 + 异常HTTP状态码](fb92bd0b98e886b74c6de962a40da63962193b92) — 2026-07-01 17:20

| 项目 | 详情 |
|------|------|
| **Commit** | `fb92bd0` |
| **日期** | 2026-07-01 17:20 |
| **作者** | 赖睿轩 |
| **变更** | +2 / ~5 / -0 |
| **说明** | - UserServiceImpl: 日志中验证码明文→手机号脱敏(log.info不再泄露code)
- GlobalExceptionHandler: BusinessException现在返回正确HTTP状态码
  - 400-599范围→直接映射 | 40xx→403 | 9000+→429 | 默认→400
  - BindException/MethodArgumentNotValidException→400
- application-prod.yml: 生产环境关闭Swagger/Knife4j/springdoc
- application.yml: Knife4j增加production标志
- test-suite.mjs: 每个脚本前清理Redis限流计数 + 2s间隔

Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/common/GlobalExceptionHandler.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/UserServiceImpl.java
  M  jiangnan-travel/src/main/resources/application-prod.yml
  M  jiangnan-travel/src/main/resources/application.yml
  A  tests/test-helper.mjs
  A  tests/test-report-1782897213904.json
  M  tests/test-suite.mjs
```


## [fix: 修复 7 CRITICAL + 19 HIGH 安全/性能/数据问题](35e1af2d7579ff093760743317e424891b7f37b8) — 2026-07-01 17:03

| 项目 | 详情 |
|------|------|
| **Commit** | `35e1af2` |
| **日期** | 2026-07-01 17:03 |
| **作者** | 赖睿轩 |
| **变更** | +5 / ~26 / -0 |
| **说明** | CRITICAL (7):
- C1: OperationLogAspect 参数名敏感词过滤, changePassword 关闭参数记录
- C2: PaymentController Map→PaymentRequest DTO(@Valid), 新增 MethodArgumentNotValidException handler
- C3: AiChatController 移除 permitAll, 强制认证 + getSessionMessages 加 userId 校验
- C4: application.yml 密钥 ${ENV:default}, application-prod.yml 无回退
- C5: stores/user.js JSON.parse 加 try-catch 防白屏
- C6: WebSocket JWT URL→Cookie(JwtCookieConfigurator 握手拦截 + 前端 Cookie 同步)
- C7: JWT 黑名单(TokenBlacklistService) + Filter/WS 双重校验 + 登出/改密自动失效

HIGH 性能 (7):
- toVO→toVOList 批量加载 CarType/Driver (消灭 N×2 查询)
- review() SELECT AVG(rating) 替代加载全量评价
- getTodayRevenue() SELECT SUM(final_price) 替代加载全量订单
- CouponServiceImpl/PaymentServiceImpl selectBatchIds 替代 N+1
- AiChatServiceImpl: buildChatParams() LIMIT 21 + buildSystemPrompt() volatile 缓存

HIGH 安全 (2): CORS 收紧 + Math.random→SecureRandom
HIGH 数据库 (5): indexes.sql/migration 去重 + VARCHAR→TIME
HIGH 前端 (3): Register 错误处理 + /addresses 路由修正 + useSmsCode 定时器清理
HIGH 锁/流 (2): arrive/startTrip/complete Redisson 锁 + SSE completeWithError

Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  A  .claude/CLAUDE.md
  M  jiangnan-travel-web/src/composables/useSmsCode.js
  M  jiangnan-travel-web/src/stores/user.js
  M  jiangnan-travel-web/src/views/Home.vue
  M  jiangnan-travel-web/src/views/Layout.vue
  M  jiangnan-travel-web/src/views/Register.vue
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/aspect/OperationLogAspect.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/common/GlobalExceptionHandler.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AiChatController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/PaymentController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/UserController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/PaymentRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/JwtAuthFilter.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/JwtUtil.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/SecurityConfig.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/security/TokenBlacklistService.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/AiChatService.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AiChatServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/CouponServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/OrderServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/PaymentServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/UserServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/DriverLocationServer.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/JwtCookieConfigurator.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/NotificationWebSocketServer.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/OrderTrackingServer.java
  M  jiangnan-travel/src/main/resources/application.yml
  M  jiangnan-travel/src/main/resources/sql/indexes.sql
  M  jiangnan-travel/src/main/resources/sql/init.sql
  M  jiangnan-travel/src/main/resources/sql/migration_optimize.sql
  A  tests/test-report-1782893922865.json
```


## [fix: 四项改进 — 密钥外部化 + 文档同步 + 前端测试 + 部署模板](70043f57d29d7b3693ca1d370fe1f8eafdf967b4) — 2026-07-01 15:32

| 项目 | 详情 |
|------|------|
| **Commit** | `70043f5` |
| **日期** | 2026-07-01 15:32 |
| **作者** | 赖睿轩 |
| **变更** | +4 / ~9 / -0 |
| **说明** | 1. 密钥外部化: application.yml 敏感值改为 ${ENV:default}, application-prod.yml 强制环境变量
2. deploy: .env.example 模板, docker-compose.yml 同步敏感变量, application-prod.yml 入库
3. 文档: 清理 README/ARCHITECTURE/PRODUCT_DESIGN/总结报告 中已删除的 SurgePricing 引用
4. 前端测试: vitest + useSmsCode 6个测试, 覆盖手机号校验/API调用/倒计时/sending状态
5. 修复 gitignore: 移除 application-prod.yml 误拦, 添加 !.env.example 例外, .gitignore 自身入库

Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  A  .gitignore
  M  ARCHITECTURE.md
  M  README.md
  A  deploy/.env.example
  M  deploy/docker-compose.yml
  M  docs/PRODUCT_DESIGN.md
  M  jiangnan-travel-web/package-lock.json
  M  jiangnan-travel-web/package.json
  A  jiangnan-travel-web/src/composables/__tests__/useSmsCode.test.js
  M  jiangnan-travel-web/vite.config.js
  A  jiangnan-travel/src/main/resources/application-prod.yml
  M  jiangnan-travel/src/main/resources/application.yml
  M  "351241271347233256345274200345217221346200273347273223346212245345221212.md"
```


## [docs: 更新 CHANGELOG](c1757316d57af41dc65568c82d8355bfa79133e0) — 2026-07-01 15:17

| 项目 | 详情 |
|------|------|
| **Commit** | `c175731` |
| **日期** | 2026-07-01 15:17 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~1 / -0 |
| **说明** | Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  CHANGELOG.md
```


## [docs: 更新 CHANGELOG — 记录瘦身提交](af1ad1d2fccf1cd74d105cac94e7a328dee16b88) — 2026-07-01 15:17

| 项目 | 详情 |
|------|------|
| **Commit** | `af1ad1d` |
| **日期** | 2026-07-01 15:17 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~1 / -0 |
| **说明** | Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  CHANGELOG.md
```


## [chore: 前端后端瘦身 — 移除未使用依赖/代码/配置](b2b8294cd23acd0ca5829d9bd2533adc21d30a30) — 2026-07-01 15:17

| 项目 | 详情 |
|------|------|
| **Commit** | `b2b8294` |
| **日期** | 2026-07-01 15:17 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~12 / -5 |
| **说明** | 前端:
- 移除 uuid 依赖,用 crypto.randomUUID() 替代
- 删除 useFeedback composable,组件内直接使用 ElMessage
- 移除全局 Element Plus 图标注册,改为按需引入
- 删除未使用的 CSS 类(骨架屏/stagger动画/app-*等)
- 移除未使用的 getFrequentRoutes API

后端:
- 删除 CorsConfig/RedisCacheConfig Java 配置,move to application.yml
- 删除 SurgePricingService(未使用的动态加价功能)
- 移除 actuator/prometheus/hutool 未使用依赖
- TestDataInitializer 添加 @Profile("dev") 限制

+145 / -356 行

Co-Authored-By: Claude <noreply@anthropic.com> |

### 变更文件
```
  M  CHANGELOG.md
  M  jiangnan-travel-web/package.json
  M  jiangnan-travel-web/src/api/ai.js
  M  jiangnan-travel-web/src/assets/style.css
  M  jiangnan-travel-web/src/assets/transitions.css
  D  jiangnan-travel-web/src/composables/useFeedback.js
  M  jiangnan-travel-web/src/main.js
  M  jiangnan-travel-web/src/views/Login.vue
  M  jiangnan-travel-web/src/views/Payment.vue
  M  jiangnan-travel-web/src/views/Register.vue
  M  jiangnan-travel/pom.xml
  D  jiangnan-travel/src/main/java/com/jiangnan/travel/config/CorsConfig.java
  D  jiangnan-travel/src/main/java/com/jiangnan/travel/config/RedisCacheConfig.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/config/TestDataInitializer.java
  D  jiangnan-travel/src/main/java/com/jiangnan/travel/service/SurgePricingService.java
  D  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/SurgePricingServiceImpl.java
  M  jiangnan-travel/src/main/resources/application.yml
```


## [chore: 项目文件归档整理与文档全面精确更新](7112284bf8f7daf0c8d22d3b64e76f38224183b8) — 2026-07-01 12:00

| 项目 | 详情 |
|------|------|
| **Commit** | `7112284` |
| **日期** | 2026-07-01 12:00 |
| **作者** | 赖睿轩 |
| **变更** | +29 / ~10 / -41 |
| **说明** | 归档：实习材料(doc/docx) -> 实习材料/，测试(.mjs) -> tests/
删除：30+冗余文件(16 test-report JSON+7历史计划+4已合并报告)
合并：4份报告 -> 项目开发总结报告.md
更新：ARCHITECTURE.md 5.1-5.20端点计数/6.3公开接口/SQL脚本表/catalog数字
修正：constitution.md(21表/46API->28表/97API) init.sql注释 TASK_ROADMAP
HTML：设计文档HTML 6处过期数字更新(21表->28/36API->97/24页面->41)
路径：AGENTS/EXECUTION_MODE/README/测试报告引用同步修正 |

### 变更文件
```
  D  .trae/documents/admin-ui-upgrade-plan.md
  D  .trae/documents/api-failure-fix-plan.md
  D  .trae/documents/capability-check-and-full-test-plan.md
  D  .trae/documents/full-project-testing-optimization-plan.md
  M  .trae/documents/full-project-testing-optimization-report.md
  D  .trae/documents/full-site-ui-upgrade-plan.md
  D  .trae/documents/full-site-ui-upgrade-remaining-tasks-plan.md
  M  .trae/rules/constitution.md
  D  "2.345215227346230214345244247345255246346234254347247221347224237345256236344271240344273273345212241344271246357274210346214207345257274350200201345270210350264237350264243357274211-346250241346235277-202606.docx"
  D  "4.346257225344270232345256236344271240344270273350247202351227256345215267350260203346237245350241250357274210344274201344270232345241253345206231345271266347233226347253240357274211-346250241346235277-202606.docx"
  D  "5.346257225344270232347224237346257225344270232345256236344271240351211264345256232350241250-346250241346235277-202606.docx"
  D  "6.345215227346230214345244247345255246346234254347247221347224237345256236344271240346212245345221212357274210345215225351235242346211223345215260357274211-346250241346235277-202606.docx"
  D  "7.346257225344270232345256236344271240344270273350247202351227256345215267350260203346237245350241250357274210345255246347224237345241253345206231357274211-2025346233277346215242347272242350211262345206205345256271.doc"
  M  AGENTS.md
  D  AI_CAPABILITY_REPORT.md
  M  ARCHITECTURE.md
  M  CHANGELOG.md
  M  EXECUTION_MODE.md
  D  PROJECT_REVIEW_REPORT.md
  M  README.md
  D  TEST_GUIDE.md
  D  address_test.mjs
  D  ai-programming-log.md
  D  auth_boundary_test.mjs
  D  bus_line_test.mjs
  D  cancel_refund_test.mjs
  D  cleanup_test_data.sql
  D  comprehensive_api_test.mjs
  D  concurrent_test.mjs
  D  coupon_lifecycle_test.mjs
  M  docs/TASK_ROADMAP.md
  D  driver_flow_test.mjs
  D  e2e/debug-order.mjs
  D  idor_security_test.mjs
  D  input_security_test.mjs
  M  jiangnan-travel/src/main/resources/sql/init.sql
  D  jwt_security_test.mjs
  D  notification_test.mjs
  D  order_flow_test.mjs
  D  payment_review_test.mjs
  D  payment_security_test.mjs
  D  query
  D  risk_rules_test.mjs
  D  test-suite.mjs
  D  test_apis.mjs
  A  tests/TEST_GUIDE.md
  A  tests/address_test.mjs
  A  tests/auth_boundary_test.mjs
  A  tests/bus_line_test.mjs
  A  tests/cancel_refund_test.mjs
  A  tests/cleanup_test_data.sql
  A  tests/comprehensive_api_test.mjs
  A  tests/concurrent_test.mjs
  A  tests/coupon_lifecycle_test.mjs
  A  tests/driver_flow_test.mjs
  A  tests/idor_security_test.mjs
  A  tests/input_security_test.mjs
  A  tests/jwt_security_test.mjs
  A  tests/notification_test.mjs
  A  tests/order_flow_test.mjs
  A  tests/payment_review_test.mjs
  A  tests/payment_security_test.mjs
  A  tests/risk_rules_test.mjs
  A  tests/test-suite.mjs
  A  tests/vip_test.mjs
  A  tests/websocket_test.mjs
  D  vip_test.mjs
  D  websocket_test.mjs
  A  "345256236344271240346235220346226231/2.345215227346230214345244247345255246346234254347247221347224237345256236344271240344273273345212241344271246357274210346214207345257274350200201345270210350264237350264243357274211-346250241346235277-202606.docx"
  A  "345256236344271240346235220346226231/4.346257225344270232345256236344271240344270273350247202351227256345215267350260203346237245350241250357274210344274201344270232345241253345206231345271266347233226347253240357274211-346250241346235277-202606.docx"
  A  "345256236344271240346235220346226231/5.346257225344270232347224237346257225344270232345256236344271240351211264345256232350241250-346250241346235277-202606.docx"
  A  "345256236344271240346235220346226231/6.345215227346230214345244247345255246346234254347247221347224237345256236344271240346212245345221212357274210345215225351235242346211223345215260357274211-346250241346235277-202606.docx"
  A  "345256236344271240346235220346226231/7.346257225344270232345256236344271240344270273350247202351227256345215267350260203346237245350241250357274210345255246347224237345241253345206231357274211-2025346233277346215242347272242350211262345206205345256271.doc"
  A  "345256236344271240346235220346226231/34525623634427124035120023234723724535727421035123524234522022123347272247346234254347247221347224237357274211.docx"
  A  "345256236344271240346235220346226231/346257225344270232345256236344271240345256211345205250350264243344273273345221212347237245344271246.docx"
  D  "34525623634427124035120023234723724535727421035123524234522022123347272247346234254347247221347224237357274211.docx"
  D  "346257225344270232345256236344271240345256211345205250350264243344273273345221212347237245344271246.docx"
  M  "346261237345215227345207272350241214346231272346205247346234215345212241345271263345217260_350256276350256241346226207346241243344270216345274200345217221350256241345210222.html"
  A  "351241271347233256345274200345217221346200273347273223346212245345221212.md"
  D  "351241271347233256345274200345217221350277207347250213346200273347273223.md"
```


## [docs: 更新 CHANGELOG](b66dbfb8fbd9add111f691190f960c14c3f1a549) — 2026-06-30 19:31

| 项目 | 详情 |
|------|------|
| **Commit** | `b66dbfb` |
| **日期** | 2026-06-30 19:31 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~1 / -0 |

### 变更文件
```
  M  CHANGELOG.md
```


## [feat: 全功能测试优化与架构升级落地](95b70e72007b70b43dff1051dc4f0251b40c5895) — 2026-06-30 19:29

| 项目 | 详情 |
|------|------|
| **Commit** | `95b70e7` |
| **日期** | 2026-06-30 19:29 |
| **作者** | 赖睿轩 |
| **变更** | +238 / ~103 / -2 |
| **说明** | - 测试基建：新增 155 个测试用例编排器、E2E Playwright 核心流程测试、并发/风控/WebSocket 专项测试

- 安全加固：统一 401/403 错误码、方法级 @PreAuthorize 权限注解、操作日志审计、JWT 强密钥、支付幂等分布式锁

- 性能优化：管理后台大屏 Redis 缓存、Service 层慢查询监控、Vite vendor 分包与懒加载

- 业务补全：优惠券生命周期、VIP、城际班线、发票、消息通知、活动 Banner 等模块

- 监控运维：Spring Boot Actuator + Prometheus 端点

- 图片 CDN：Trae text-to-image 头像与 Banner 生成

- 文档同步：项目宪法、架构文档、API 文档、CI 流水线 |

### 变更文件
```
  A  .github/workflows/ci.yml
  A  .trae/documents/admin-ui-upgrade-plan.md
  A  .trae/documents/api-failure-fix-plan.md
  A  .trae/documents/capability-check-and-full-test-plan.md
  A  .trae/documents/full-project-testing-optimization-plan.md
  A  .trae/documents/full-project-testing-optimization-report.md
  A  .trae/documents/full-site-ui-upgrade-plan.md
  A  .trae/documents/full-site-ui-upgrade-remaining-tasks-plan.md
  A  .trae/rules/constitution.md
  M  .trae/rules/development-workflow.md
  M  .trae/skills/skill-creator/scripts/run_loop.py
  A  AGENTS.md
  A  AI_CAPABILITY_REPORT.md
  A  AI_WORKFLOW.md
  A  ARCHITECTURE.md
  M  CHANGELOG.md
  A  EXECUTION_MODE.md
  A  PROJECT_REVIEW_REPORT.md
  A  PROJECT_RULES.md
  M  README.md
  A  TEST_GUIDE.md
  A  address_test.mjs
  A  auth_boundary_test.mjs
  A  bus_line_test.mjs
  A  cancel_refund_test.mjs
  A  cleanup_test_data.sql
  A  comprehensive_api_test.mjs
  A  concurrent_test.mjs
  A  coupon_lifecycle_test.mjs
  A  deploy/DEPLOY_README.md
  A  deploy/backend/.dockerignore
  A  deploy/backend/Dockerfile
  A  deploy/build.ps1
  A  deploy/docker-compose.yml
  A  deploy/frontend/.dockerignore
  A  deploy/frontend/Dockerfile
  A  deploy/frontend/nginx.conf
  A  docs/BUG_ANALYSIS.md
  A  docs/DESIGN_SYSTEM.md
  A  docs/DEVELOPMENT_PLAN.md
  A  docs/FEATURE_ROADMAP.md
  A  docs/PAGE_STRUCTURE.md
  A  docs/PRODUCT_DESIGN.md
  A  docs/TASK_ROADMAP.md
  A  docs/TECHNICAL_DEBT.md
  A  docs/UI_UX_SPEC.md
  A  docs/spec/cultural-tourism-and-ai-chat-optimization-spec.md
  A  driver_flow_test.mjs
  A  e2e/debug-order.mjs
  A  e2e/package-lock.json
  A  e2e/package.json
  A  e2e/playwright.config.js
  A  e2e/tests/core-flow.spec.js
  A  e2e/tests/helpers.js
  A  idor_security_test.mjs
  A  input_security_test.mjs
  A  jiangnan-travel-web/.agents/skills/auth0-springboot-api/SKILL.md
  A  jiangnan-travel-web/.agents/skills/auth0-springboot-api/references/api.md
  A  jiangnan-travel-web/.agents/skills/auth0-springboot-api/references/integration.md
  A  jiangnan-travel-web/.agents/skills/auth0-springboot-api/references/setup.md
  A  jiangnan-travel-web/.agents/skills/brainstorming/SKILL.md
  A  jiangnan-travel-web/.agents/skills/brainstorming/scripts/frame-template.html
  A  jiangnan-travel-web/.agents/skills/brainstorming/scripts/helper.js
  A  jiangnan-travel-web/.agents/skills/brainstorming/scripts/server.cjs
  A  jiangnan-travel-web/.agents/skills/brainstorming/scripts/start-server.sh
  A  jiangnan-travel-web/.agents/skills/brainstorming/scripts/stop-server.sh
  A  jiangnan-travel-web/.agents/skills/brainstorming/spec-document-reviewer-prompt.md
  A  jiangnan-travel-web/.agents/skills/brainstorming/visual-companion.md
  A  jiangnan-travel-web/.agents/skills/code-review-testing/SKILL.md
  A  jiangnan-travel-web/.agents/skills/java-architect/SKILL.md
  A  jiangnan-travel-web/.agents/skills/java-architect/references/jpa-optimization.md
  A  jiangnan-travel-web/.agents/skills/java-architect/references/reactive-webflux.md
  A  jiangnan-travel-web/.agents/skills/java-architect/references/spring-boot-setup.md
  A  jiangnan-travel-web/.agents/skills/java-architect/references/spring-security.md
  A  jiangnan-travel-web/.agents/skills/java-architect/references/testing-patterns.md
  A  jiangnan-travel-web/.agents/skills/rest-api-design/SKILL.md
  A  jiangnan-travel-web/.agents/skills/skill-creator/LICENSE.txt
  A  jiangnan-travel-web/.agents/skills/skill-creator/SKILL.md
  A  jiangnan-travel-web/.agents/skills/skill-creator/agents/analyzer.md
  A  jiangnan-travel-web/.agents/skills/skill-creator/agents/comparator.md
  A  jiangnan-travel-web/.agents/skills/skill-creator/agents/grader.md
  A  jiangnan-travel-web/.agents/skills/skill-creator/assets/eval_review.html
  A  jiangnan-travel-web/.agents/skills/skill-creator/eval-viewer/generate_review.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/eval-viewer/viewer.html
  A  jiangnan-travel-web/.agents/skills/skill-creator/references/schemas.md
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/__init__.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/aggregate_benchmark.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/generate_report.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/improve_description.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/package_skill.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/quick_validate.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/run_eval.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/run_loop.py
  A  jiangnan-travel-web/.agents/skills/skill-creator/scripts/utils.py
  A  jiangnan-travel-web/.agents/skills/subagent-driven-development/SKILL.md
  A  jiangnan-travel-web/.agents/skills/subagent-driven-development/implementer-prompt.md
  A  jiangnan-travel-web/.agents/skills/subagent-driven-development/scripts/review-package
  A  jiangnan-travel-web/.agents/skills/subagent-driven-development/scripts/sdd-workspace
  A  jiangnan-travel-web/.agents/skills/subagent-driven-development/scripts/task-brief
  A  jiangnan-travel-web/.agents/skills/subagent-driven-development/task-reviewer-prompt.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/CREATION-LOG.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/SKILL.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/condition-based-waiting-example.ts
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/condition-based-waiting.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/defense-in-depth.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/find-polluter.sh
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/root-cause-tracing.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/test-academic.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/test-pressure-1.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/test-pressure-2.md
  A  jiangnan-travel-web/.agents/skills/systematic-debugging/test-pressure-3.md
  A  jiangnan-travel-web/.agents/skills/test-driven-development/SKILL.md
  A  jiangnan-travel-web/.agents/skills/test-driven-development/testing-anti-patterns.md
  A  jiangnan-travel-web/.agents/skills/writing-plans/SKILL.md
  A  jiangnan-travel-web/.agents/skills/writing-plans/plan-document-reviewer-prompt.md
  M  jiangnan-travel-web/.env.example
  M  jiangnan-travel-web/index.html
  A  jiangnan-travel-web/insert_long_haul.sql
  M  jiangnan-travel-web/package-lock.json
  M  jiangnan-travel-web/package.json
  M  jiangnan-travel-web/src/App.vue
  M  jiangnan-travel-web/src/api/admin.js
  M  jiangnan-travel-web/src/api/ai.js
  A  jiangnan-travel-web/src/api/bus.js
  A  jiangnan-travel-web/src/api/campaign.js
  M  jiangnan-travel-web/src/api/driver.js
  A  jiangnan-travel-web/src/api/invoice.js
  A  jiangnan-travel-web/src/api/notification.js
  M  jiangnan-travel-web/src/api/order.js
  A  jiangnan-travel-web/src/api/payment.js
  M  jiangnan-travel-web/src/api/user.js
  A  jiangnan-travel-web/src/api/vip.js
  M  jiangnan-travel-web/src/assets/style.css
  A  jiangnan-travel-web/src/assets/transitions.css
  M  jiangnan-travel-web/src/components/AiChatFloat.vue
  M  jiangnan-travel-web/src/components/AmapView.vue
  A  jiangnan-travel-web/src/components/CdnAvatar.vue
  A  jiangnan-travel-web/src/composables/useAmapPoiSearch.js
  A  jiangnan-travel-web/src/composables/useFeedback.js
  A  jiangnan-travel-web/src/composables/useSmsCode.js
  M  jiangnan-travel-web/src/main.js
  M  jiangnan-travel-web/src/router/index.js
  M  jiangnan-travel-web/src/stores/user.js
  A  jiangnan-travel-web/src/utils/imageCDN.js
  A  jiangnan-travel-web/src/views/AboutCompany.vue
  M  jiangnan-travel-web/src/views/AddressManage.vue
  M  jiangnan-travel-web/src/views/AdminAlerts.vue
  A  jiangnan-travel-web/src/views/AdminBusLines.vue
  A  jiangnan-travel-web/src/views/AdminCampaigns.vue
  M  jiangnan-travel-web/src/views/AdminCarTypes.vue
  M  jiangnan-travel-web/src/views/AdminDashboard.vue
  M  jiangnan-travel-web/src/views/AdminDrivers.vue
  M  jiangnan-travel-web/src/views/AdminLayout.vue
  D  jiangnan-travel-web/src/views/AdminLogin.vue
  M  jiangnan-travel-web/src/views/AdminOrders.vue
  A  jiangnan-travel-web/src/views/AdminProfile.vue
  M  jiangnan-travel-web/src/views/AdminUsers.vue
  A  jiangnan-travel-web/src/views/AdminVipLevels.vue
  A  jiangnan-travel-web/src/views/AiAssistant.vue
  A  jiangnan-travel-web/src/views/BusLine.vue
  A  jiangnan-travel-web/src/views/CampaignDetail.vue
  A  jiangnan-travel-web/src/views/CampaignList.vue
  M  jiangnan-travel-web/src/views/CouponCenter.vue
  M  jiangnan-travel-web/src/views/DriverEarnings.vue
  M  jiangnan-travel-web/src/views/DriverHome.vue
  M  jiangnan-travel-web/src/views/DriverLayout.vue
  D  jiangnan-travel-web/src/views/DriverLogin.vue
  M  jiangnan-travel-web/src/views/DriverOrder.vue
  M  jiangnan-travel-web/src/views/DriverProfile.vue
  M  jiangnan-travel-web/src/views/Home.vue
  A  jiangnan-travel-web/src/views/InvoiceApply.vue
  A  jiangnan-travel-web/src/views/InvoiceCenter.vue
  A  jiangnan-travel-web/src/views/LandmarkExplore.vue
  M  jiangnan-travel-web/src/views/Layout.vue
  M  jiangnan-travel-web/src/views/Login.vue
  A  jiangnan-travel-web/src/views/MessageCenter.vue
  M  jiangnan-travel-web/src/views/OrderCreate.vue
  M  jiangnan-travel-web/src/views/OrderDetail.vue
  M  jiangnan-travel-web/src/views/OrderList.vue
  A  jiangnan-travel-web/src/views/Payment.vue
  M  jiangnan-travel-web/src/views/Profile.vue
  A  jiangnan-travel-web/src/views/QuoteWall.vue
  M  jiangnan-travel-web/src/views/Register.vue
  A  jiangnan-travel-web/src/views/ReviewOrder.vue
  A  jiangnan-travel-web/src/views/SecuritySettings.vue
  M  jiangnan-travel-web/src/views/TripTracking.vue
  A  jiangnan-travel-web/src/views/VipCenter.vue
  M  jiangnan-travel-web/vite.config.js
  M  jiangnan-travel/pom.xml
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/annotation/LogOperation.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/aspect/OperationLogAspect.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/aspect/SlowQueryAspect.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/common/ErrorCode.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/common/GlobalExceptionHandler.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/config/RedisCacheConfig.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/config/RestTemplateConfig.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/config/TestDataInitializer.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AIDataController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AdminController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AdminManageController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AiChatController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AiInsightController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/BusLineController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/CampaignController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/CityLandmarkController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/CouponController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverEarningController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverOrderController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/InvoiceController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/NotificationController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/OrderController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/PaymentController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/RouteController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/SafetyController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/UserAddressController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/UserController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/VipController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/AdminLoginRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/AiInsightRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/ApplyInvoiceRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/ChangePasswordRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/ChatRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/ClaimCouponRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/CreateCampaignRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/CreateOrderRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/DriverLoginRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/DriverStatusUpdateRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/EstimateRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/HandleAlertRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/PasswordLoginRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/PurchaseVipRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/ReviewOrderRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/SendCodeRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/UpdateCarTypeRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/UpdateProfileRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/UpdateUserStatusRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/VerifyDriverRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/BusLine.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/BusSchedule.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Campaign.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/CampaignCoupon.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Driver.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Notification.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/OperationLog.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Payment.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/RiskAlert.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/UserVip.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/VipLevel.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/BusLineMapper.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/BusScheduleMapper.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/CampaignCouponMapper.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/CampaignMapper.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/NotificationMapper.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/OperationLogMapper.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/OrderMapper.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/UserVipMapper.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/mapper/VipLevelMapper.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/JwtAuthFilter.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/JwtUtil.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/security/SecurityConfig.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/AdminService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/AiAnalysisService.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/AiChatService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/AmapRouteService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/BusLineService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/CampaignService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/CarTypeService.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/CouponService.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/DriverService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/InvoiceService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/NotificationService.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/OrderService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/PaymentService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/RiskAlertService.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/UserService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/VipService.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AdminServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AiAnalysisServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AiChatServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AiPredictionServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/AmapRouteServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/BusLineServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/CampaignServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/CarTypeServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/CityLandmarkServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/CouponServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/DispatchServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/DriverServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/InvoiceServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/NotificationServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/OrderServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/PaymentServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/PricingServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/RiskAlertServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/SurgePricingServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/UserAddressServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/UserServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/VipServiceImpl.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/BusLineVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/CampaignDetailVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/ChatVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/CoordVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/DailyOrderStatVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/DriverVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/EstimateVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/InvoiceVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/LoginVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/OrderVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/PaymentVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/PriceDetailVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/RecommendDestVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/RoutePlanVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/RouteStepVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/UserCouponVO.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/UserVipVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/DriverLocationServer.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/NotificationWebSocketServer.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/OrderTrackingServer.java
  M  jiangnan-travel/src/main/resources/application.yml
  A  jiangnan-travel/src/main/resources/sql/fix_chinese_data.sql
  M  jiangnan-travel/src/main/resources/sql/indexes.sql
  M  jiangnan-travel/src/main/resources/sql/init.sql
  M  jiangnan-travel/src/main/resources/sql/migration_optimize.sql
  A  jiangnan-travel/src/main/resources/sql/seed_data.sql
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/CampaignServiceTest.java
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/CouponServiceTest.java
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/DriverServiceTest.java
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/InvoiceServiceTest.java
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/OrderServiceTest.java
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/PaymentServiceTest.java
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/UserAddressServiceTest.java
  A  jiangnan-travel/src/test/java/com/jiangnan/travel/VipServiceTest.java
  A  jwt_security_test.mjs
  A  notification_test.mjs
  A  order_flow_test.mjs
  A  payment_review_test.mjs
  A  payment_security_test.mjs
  A  risk_rules_test.mjs
  A  test-suite.mjs
  A  test_apis.mjs
  A  vip_test.mjs
  A  websocket_test.mjs
```


## [feat: 集成 addyosmani/agent-skills 14个Skills + 去重优化](46cde2ea01fe6e4c65d816b5f49c309c002aff9e) — 2026-06-24 15:28

| 项目 | 详情 |
|------|------|
| **Commit** | `46cde2e` |
| **日期** | 2026-06-24 15:28 |
| **作者** | 赖睿轩 |
| **变更** | +14 / ~0 / -15 |

### 变更文件
```
  A  .trae/skills/code-review-and-quality/SKILL.md
  D  .trae/skills/code-review-testing/SKILL.md
  A  .trae/skills/code-simplification/SKILL.md
  A  .trae/skills/context-engineering/SKILL.md
  A  .trae/skills/debugging-and-error-recovery/SKILL.md
  A  .trae/skills/documentation-and-adrs/SKILL.md
  D  .trae/skills/documentation/SKILL.md
  A  .trae/skills/doubt-driven-development/SKILL.md
  A  .trae/skills/git-workflow-and-versioning/SKILL.md
  A  .trae/skills/incremental-implementation/SKILL.md
  A  .trae/skills/performance-optimization/SKILL.md
  A  .trae/skills/planning-and-task-breakdown/SKILL.md
  A  .trae/skills/security-and-hardening/SKILL.md
  A  .trae/skills/source-driven-development/SKILL.md
  A  .trae/skills/spec-driven-development/SKILL.md
  D  .trae/skills/systematic-debugging/CREATION-LOG.md
  D  .trae/skills/systematic-debugging/SKILL.md
  D  .trae/skills/systematic-debugging/condition-based-waiting-example.ts
  D  .trae/skills/systematic-debugging/condition-based-waiting.md
  D  .trae/skills/systematic-debugging/defense-in-depth.md
  D  .trae/skills/systematic-debugging/find-polluter.sh
  D  .trae/skills/systematic-debugging/root-cause-tracing.md
  D  .trae/skills/systematic-debugging/test-academic.md
  D  .trae/skills/systematic-debugging/test-pressure-1.md
  D  .trae/skills/systematic-debugging/test-pressure-2.md
  D  .trae/skills/systematic-debugging/test-pressure-3.md
  A  .trae/skills/using-agent-skills/SKILL.md
  D  .trae/skills/writing-plans/SKILL.md
  D  .trae/skills/writing-plans/plan-document-reviewer-prompt.md
```


## [refactor: 全面代码规范化与安全优化 - 安全修复(密钥.env/路由守卫/异常处理) + API文档化(Knife4j/60+注解) + 后端规范化(Entity继承/Map→DTO/JWT/校验) + 数据库优化 + Trae工作流](0a1ef84a102d2c8ab59f1b8beaa8437508314159) — 2026-06-24 15:02

| 项目 | 详情 |
|------|------|
| **Commit** | `0a1ef84` |
| **日期** | 2026-06-24 15:02 |
| **作者** | 赖睿轩 |
| **变更** | +71 / ~60 / -0 |

### 变更文件
```
  A  .trae/rules/development-workflow.md
  A  .trae/skills/auth0-springboot-api/SKILL.md
  A  .trae/skills/auth0-springboot-api/references/api.md
  A  .trae/skills/auth0-springboot-api/references/integration.md
  A  .trae/skills/auth0-springboot-api/references/setup.md
  A  .trae/skills/brainstorming/SKILL.md
  A  .trae/skills/brainstorming/scripts/frame-template.html
  A  .trae/skills/brainstorming/scripts/helper.js
  A  .trae/skills/brainstorming/scripts/server.cjs
  A  .trae/skills/brainstorming/scripts/start-server.sh
  A  .trae/skills/brainstorming/scripts/stop-server.sh
  A  .trae/skills/brainstorming/spec-document-reviewer-prompt.md
  A  .trae/skills/brainstorming/visual-companion.md
  A  .trae/skills/code-refactoring/SKILL.md
  A  .trae/skills/code-review-testing/SKILL.md
  A  .trae/skills/database-design/SKILL.md
  A  .trae/skills/documentation/SKILL.md
  A  .trae/skills/java-architect/SKILL.md
  A  .trae/skills/java-architect/references/jpa-optimization.md
  A  .trae/skills/java-architect/references/reactive-webflux.md
  A  .trae/skills/java-architect/references/spring-boot-setup.md
  A  .trae/skills/java-architect/references/spring-security.md
  A  .trae/skills/java-architect/references/testing-patterns.md
  A  .trae/skills/rest-api-design/SKILL.md
  A  .trae/skills/skill-creator/LICENSE.txt
  A  .trae/skills/skill-creator/SKILL.md
  A  .trae/skills/skill-creator/agents/analyzer.md
  A  .trae/skills/skill-creator/agents/comparator.md
  A  .trae/skills/skill-creator/agents/grader.md
  A  .trae/skills/skill-creator/assets/eval_review.html
  A  .trae/skills/skill-creator/eval-viewer/generate_review.py
  A  .trae/skills/skill-creator/eval-viewer/viewer.html
  A  .trae/skills/skill-creator/references/schemas.md
  A  .trae/skills/skill-creator/scripts/__init__.py
  A  .trae/skills/skill-creator/scripts/aggregate_benchmark.py
  A  .trae/skills/skill-creator/scripts/generate_report.py
  A  .trae/skills/skill-creator/scripts/improve_description.py
  A  .trae/skills/skill-creator/scripts/package_skill.py
  A  .trae/skills/skill-creator/scripts/quick_validate.py
  A  .trae/skills/skill-creator/scripts/run_eval.py
  A  .trae/skills/skill-creator/scripts/run_loop.py
  A  .trae/skills/skill-creator/scripts/utils.py
  A  .trae/skills/subagent-driven-development/SKILL.md
  A  .trae/skills/subagent-driven-development/implementer-prompt.md
  A  .trae/skills/subagent-driven-development/scripts/review-package
  A  .trae/skills/subagent-driven-development/scripts/sdd-workspace
  A  .trae/skills/subagent-driven-development/scripts/task-brief
  A  .trae/skills/subagent-driven-development/task-reviewer-prompt.md
  A  .trae/skills/systematic-debugging/CREATION-LOG.md
  A  .trae/skills/systematic-debugging/SKILL.md
  A  .trae/skills/systematic-debugging/condition-based-waiting-example.ts
  A  .trae/skills/systematic-debugging/condition-based-waiting.md
  A  .trae/skills/systematic-debugging/defense-in-depth.md
  A  .trae/skills/systematic-debugging/find-polluter.sh
  A  .trae/skills/systematic-debugging/root-cause-tracing.md
  A  .trae/skills/systematic-debugging/test-academic.md
  A  .trae/skills/systematic-debugging/test-pressure-1.md
  A  .trae/skills/systematic-debugging/test-pressure-2.md
  A  .trae/skills/systematic-debugging/test-pressure-3.md
  A  .trae/skills/test-driven-development/SKILL.md
  A  .trae/skills/test-driven-development/testing-anti-patterns.md
  A  .trae/skills/writing-plans/SKILL.md
  A  .trae/skills/writing-plans/plan-document-reviewer-prompt.md
  A  docs/spec/project-optimization-spec.md
  A  jiangnan-travel-web/.env.example
  A  jiangnan-travel-web/skills-lock.json
  M  jiangnan-travel-web/src/components/AmapView.vue
  M  jiangnan-travel-web/src/router/index.js
  M  jiangnan-travel-web/src/views/AddressManage.vue
  M  jiangnan-travel-web/src/views/AdminDrivers.vue
  M  jiangnan-travel-web/src/views/AdminUsers.vue
  M  jiangnan-travel-web/src/views/CouponCenter.vue
  M  jiangnan-travel-web/src/views/Home.vue
  M  jiangnan-travel-web/src/views/Login.vue
  M  jiangnan-travel-web/src/views/OrderCreate.vue
  M  jiangnan-travel-web/src/views/OrderDetail.vue
  M  jiangnan-travel-web/src/views/OrderList.vue
  M  jiangnan-travel-web/src/views/Profile.vue
  M  jiangnan-travel-web/src/views/TripTracking.vue
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/config/Knife4jConfig.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AIDataController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AdminController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AdminManageController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AiChatController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AiInsightController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AiPredictionController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/CityLandmarkController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/CouponController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverEarningController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverOrderController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/OrderController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/SafetyController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/UserAddressController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/UserController.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/CancelOrderRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/ChatRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/CreateOrderRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/DriverLocationUpdateRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/DriverRegisterRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/EstimateRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/LoginRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/PasswordLoginRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/RegisterRequest.java
  A  jiangnan-travel/src/main/java/com/jiangnan/travel/dto/SafetyAlertRequest.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Admin.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/AiChatLog.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/CityLandmark.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/CityQuote.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Coupon.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/DemandHotspot.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Invoice.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Order.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/OrderTrack.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Payment.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/PushLog.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/Review.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/RiskAlert.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/ScheduleOrder.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/ScheduleRoute.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/UserAddress.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/UserCoupon.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/entity/UserRiskProfile.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/ChatVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/DriverVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/EstimateVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/LoginVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/OrderVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/PriceDetailVO.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/vo/RecommendDestVO.java
  A  jiangnan-travel/src/main/resources/sql/migration_optimize.sql
```


## [fix: 风控引擎完整实现R2/R4/R7 + 管理后台全功能升级 + WebSocket心跳 - R2改为真正阻止取消超限、R4改为深夜跨城安全提示、R7行程超2小时自动告警、DriverEarning状态值修复、管理后台5页面添加分页控件、新增车型定价页面、WebSocket支持ping/pong心跳](3b1cac4068a66526414ebf2a1f72c5887d13e4af) — 2026-06-24 14:20

| 项目 | 详情 |
|------|------|
| **Commit** | `3b1cac4` |
| **日期** | 2026-06-24 14:20 |
| **作者** | 赖睿轩 |
| **变更** | +1 / ~12 / -0 |

### 变更文件
```
  M  CHANGELOG.md
  M  jiangnan-travel-web/src/api/admin.js
  M  jiangnan-travel-web/src/router/index.js
  M  jiangnan-travel-web/src/views/AdminAlerts.vue
  A  jiangnan-travel-web/src/views/AdminCarTypes.vue
  M  jiangnan-travel-web/src/views/AdminDrivers.vue
  M  jiangnan-travel-web/src/views/AdminLayout.vue
  M  jiangnan-travel-web/src/views/AdminOrders.vue
  M  jiangnan-travel-web/src/views/AdminUsers.vue
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/DriverEarningController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/service/impl/OrderServiceImpl.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/DriverLocationServer.java
  M  "346261237345215227345207272350241214346231272346205247346234215345212241345271263345217260_350256276350256241346226207346241243344270216345274200345217221350256241345210222.html"
```


## [fix: 管理后台全功能对接真实API - 管理后台7个页面从假数据迁移到真实接口调用，修复dashboard订单完成状态值、401跳转路径、WebSocket条件配置](0995d9e54a9300d4f5ec2c72a8acade987c4efd2) — 2026-06-24 14:12

| 项目 | 详情 |
|------|------|
| **Commit** | `0995d9e` |
| **日期** | 2026-06-24 14:12 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~16 / -0 |

### 变更文件
```
  M  CHANGELOG.md
  M  jiangnan-travel-web/src/api/admin.js
  M  jiangnan-travel-web/src/api/request.js
  M  jiangnan-travel-web/src/views/AdminAlerts.vue
  M  jiangnan-travel-web/src/views/AdminDashboard.vue
  M  jiangnan-travel-web/src/views/AdminDrivers.vue
  M  jiangnan-travel-web/src/views/AdminLogin.vue
  M  jiangnan-travel-web/src/views/AdminOrders.vue
  M  jiangnan-travel-web/src/views/AdminUsers.vue
  M  jiangnan-travel-web/src/views/Login.vue
  M  jiangnan-travel-web/vite.config.js
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/controller/AdminManageController.java
  M  jiangnan-travel/src/main/java/com/jiangnan/travel/websocket/WebSocketConfig.java
  M  jiangnan-travel/src/test/java/com/jiangnan/travel/UserServiceTest.java
  M  "346261237345215227345207272350241214346231272346205247346234215345212241345271263345217260_350256276350256241346226207346241243344270216345274200345217221350256241345210222.html"
  M  "351241271347233256345274200345217221350277207347250213346200273347273223.md"
```


## [docs: 添加项目开发过程总结文档 - 面向后续接手者的完整项目说明](f15323ae72c7b9a6a3c782dcf7b33147f2c68e2e) — 2026-06-24 01:04

| 项目 | 详情 |
|------|------|
| **Commit** | `f15323a` |
| **日期** | 2026-06-24 01:04 |
| **作者** | 赖睿轩 |
| **变更** | +1 / ~0 / -0 |

### 变更文件
```
  A  "351241271347233256345274200345217221350277207347250213346200273347273223.md"
```


## [docs: 设计文档更新至实际开发状态 - 新增12-A实际交付章节](2ce70577104f00d180ed8a7461ee1ae4a803cf42) — 2026-06-24 01:02

| 项目 | 详情 |
|------|------|
| **Commit** | `2ce7057` |
| **日期** | 2026-06-24 01:02 |
| **作者** | 赖睿轩 |
| **变更** | +0 / ~1 / -0 |

### 变更文件
```
  M  "346261237345215227345207272350241214346231272346205247346234215345212241345271263345217260_350256276350256241346226207346241243344270216345274200345217221350256241345210222.html"
```


## [chore: 添加自动更新日志Hook + 一键提交脚本](4c38c170504a5d0e8dee792d2f3e36458afafab9) — 2026-06-22 10:26

| 项目 | 详情 |
|------|------|
| **Commit** | `4c38c17` |
| **日期** | 2026-06-22 10:26 |
| **作者** | Jiangnan Travel Dev |
| **变更** | +2 / ~0 / -0 |

### 变更文件
```
  A  CHANGELOG.md
  A  commit.bat
```


## [初始化: 江南出行智慧服务平台 v1.0 — 后端109文件 + 前端36文件 + 21张表 + 36个API] — 2025-06-26

| 项目 | 详情 |
|------|------|
| **Commit** | `3f1bc1b` |
| **日期** | 2025-06-26 |
| **作者** | Jiangnan Travel Dev |
| **变更** | +173 / ~0 / -0 |

### 变更文件
```
  新建  173 个文件
```
