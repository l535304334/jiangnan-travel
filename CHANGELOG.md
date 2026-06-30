# 更新日志 (CHANGELOG)

> 本文档由 post-commit Git Hook 自动生成，每次提交代码后自动更新。
> 生成时间：每次 `git commit` 后

---

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
