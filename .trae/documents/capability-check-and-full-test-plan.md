# 本机配置检查与全功能测试计划

## 一、任务摘要

按用户要求完成三件事：
1. 检查并汇报当前 AI 可调用的 Skill、MCP 及其他编程能力；
2. 梳理项目相关文件，明确「江南出行」需求边界与项目内容；
3. 对项目全部功能进行系统测试（API 全量 + 前端关键路径），修复阻断性问题并输出测试报告。

## 二、当前状态分析

### 2.1 可调用的编程能力

**Skill（已安装，按阶段映射）**
- 需求分析：`brainstorming`
- 规划拆分：`planning-and-task-breakdown`
- 数据库设计：`database-design`
- API 设计：`rest-api-design`
- 后端开发：`java-architect`
- 前端开发：`web-dev`
- 测试：`test-driven-development`
- Code Review：`code-review-and-quality`
- 重构：`code-refactoring`
- 安全：`security-and-hardening`
- 性能：`performance-optimization`
- 文档：`documentation-and-adrs`
- Git 工作流：`git-workflow-and-versioning`
- 调试：`debugging-and-error-recovery`
- 专项：`TRAE-code-review`、`TRAE-debugger`、`TRAE-security-review`

**MCP（已启用）**
- `integrated_browser`：浏览器自动化（Playwright 全工具集），用于前端 E2E/预览；
- `mcp_cloudbase`：CloudBase 数据/函数/托管管理；
- `mcp_github`：GitHub 仓库/PR/Issue 操作；
- `mcp_notion`：Notion 文档协作。

**本地环境**
- Java 17、Maven、Node.js 22.19.0、npm 10.9.3；
- MySQL 8.0.42（localhost:3306，库 `smart_travel`，密码已知）；
- Redis（localhost:6379，database 0）。

### 2.2 项目内容梳理

- **项目名**：江南出行智慧服务平台（Jiangnan Travel）
- **技术栈**：Spring Boot 3.2.6 + Java 17 + MyBatis-Plus 3.5.7 + MySQL 8.0 + Redis + Vue 3.4 + Vite 5 + Element Plus 2.7
- **三端**：乘客端、司机端、管理后台
- **核心模块**：用户/司机/订单/支付/发票/优惠券/活动/VIP/班线/文旅地标/AI 助手/风控告警/WebSocket 实时通信
- **文档**：`AGENTS.md`、`EXECUTION_MODE.md`、`PROJECT_RULES.md`、`ARCHITECTURE.md`、`AI_WORKFLOW.md` 等宪法文件已建立；`docs/` 下含产品设计、路线图、UI/UX 规范等。

### 2.3 当前运行状态

- 后端 **8080** 端口当前未监听（上一次启动日志显示启动成功，但进程已结束）；
- 前端 **5173** 端口当前未监听；
- 工作区存在大量未提交修改（新增宪法文件、大量前后端文件、测试脚本等）；
- 已存在全量 API 测试脚本 `comprehensive_api_test.mjs`，上一次运行 36 个接口 100% 通过；
- 前端关键路径交互测试（下单、司机接单、管理后台）尚未完成。

## 三、拟执行步骤

### Step 1 — 环境预检
- 验证 MySQL 与 Redis 是否可用；
- 确认 `smart_travel` 库关键表（用户、司机、订单、班线等）存在且有测试数据。

### Step 2 — 启动后端
- 在 `jiangnan-travel/` 目录执行 `mvn -o spring-boot:run`（离线模式，依赖已存在）；
- 等待 8080 端口监听并验证 `/actuator` 健康端点。

### Step 3 — 启动前端
- 在 `jiangnan-travel-web/` 目录执行 `npm run dev`；
- 等待 5173 端口监听。

### Step 4 — 全量 API 测试
- 运行 `node comprehensive_api_test.mjs`；
- 记录每个接口状态码与响应；
- 若失败率 >0%，定位并修复问题后重新运行，直到 100% 通过或明确列出不可修复项。

### Step 5 — 前端关键路径交互测试
使用 `integrated_browser` MCP 验证以下黄金路径：
1. **乘客端**：登录 → 首页 → 下单页 → 预估价格 → 提交订单；
2. **司机端**：登录 → 查看附近订单 → 接单；
3. **管理后台**：登录 → 数据大屏 → VIP 等级管理 → 班线管理。

### Step 6 — 问题修复与回归
- 对 Step 4/5 发现的阻断性问题进行最小化修复；
- 修复后重新执行相关测试，确保回归通过。

### Step 7 — 输出测试报告
- 生成 `TEST_REPORT.md`，包含：
  - 环境信息
  - API 测试汇总（通过/失败/通过率）
  - 前端关键路径测试结果
  - 发现的问题与修复记录
  - 遗留风险与建议

## 四、假设与决策

- 假设 MySQL/Redis 已在本地运行且配置与 `application.yml` 一致；
- 假设测试账号（用户 `13900001111`、司机 `13810000001`、管理员 `admin/admin123`）可用；
- 不主动提交 Git，除非用户明确要求；
- 测试范围聚焦「功能可用性」，不覆盖性能压测与安全渗透测试；
- 前端测试以关键路径为主，不逐一点击所有 38 个页面。

## 五、验证标准

- [ ] 后端 8080 端口可访问；
- [ ] 前端 5173 端口可访问；
- [ ] `comprehensive_api_test.mjs` 运行通过率达到 100%；
- [ ] 乘客端能完成「登录 → 下单」完整流程；
- [ ] 司机端能完成「登录 → 查看附近订单 → 接单」完整流程；
- [ ] 管理后台能完成「登录 → VIP/班线管理」完整流程；
- [ ] `TEST_REPORT.md` 已生成并包含完整结果。
