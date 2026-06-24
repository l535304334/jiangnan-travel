# 开发工作流规则

本项目始终遵循 **Spec Driven Development** 工作流。所有开发任务必须按以下顺序执行：

## 核心流程

① **分析需求** → ② **编写 Spec** → ③ **拆分 Task** → ④ **设计数据库** → ⑤ **设计 API** → ⑥ **每次只完成一个 Task** → ⑦ **Code Review** → ⑧ **自动生成测试** → ⑨ **Refactor（如有需要）** → ⑩ **更新文档**

## 详细步骤

### 步骤 1: 分析需求
- 使用 `brainstorming` Skill 进行需求分析
- 通过 Socratic 式提问澄清模糊需求
- 记录需求分析结果

### 步骤 2: 编写 Spec
- 使用 `writing-plans` Skill 编写详细的实现计划
- 包含功能描述、输入输出、边界条件
- 保存在 `docs/spec/` 目录

### 步骤 3: 拆分 Task
- 使用 `subagent-driven-development` 将大任务拆分为小任务
- 每个 Task 的粒度控制在 2-5 分钟
- 每个 Task 包含完整代码和验证步骤

### 步骤 4: 设计数据库
- 使用 `database-design` Skill 进行数据库设计
- 遵循 MyBatis-Plus 规范
- 考虑索引和性能优化

### 步骤 5: 设计 API
- 使用 `rest-api-design` Skill 进行 API 设计
- 遵循 RESTful 规范
- 使用 `auth0-springboot-api` 确保 Spring Security 配置正确

### 步骤 6: 逐个完成 Task
- 每次只完成一个 Task
- 使用 `test-driven-development` 先写测试
- RED → GREEN → REFACTOR 循环
- 完成后立即进行 Code Review

### 步骤 7: Code Review
- 使用 `code-review-testing` 进行代码审查
- 检查安全性、性能、正确性、可维护性
- 严重问题必须修复才能继续

### 步骤 8: 测试
- 使用 `test-driven-development` 确保测试覆盖率
- Java 后端使用 JUnit + Mockito
- Vue 前端使用 Vitest

### 步骤 9: Refactor
- 使用 `code-refactoring` Skill 进行重构
- 遵循 SOLID 原则
- 重构后重新运行所有测试

### 步骤 10: 文档
- 使用 `documentation` Skill 更新项目文档
- 更新 API 文档、README、数据库设计文档

## 技术栈参考
- 后端: Spring Boot 3.2.x + MyBatis-Plus 3.5.7 + MySQL 8.0 + Redis + Redisson
- 安全: Spring Security + JWT (jjwt 0.12)
- 前端: Vue 3 + Vite 5 + Element Plus 2.x + Pinia
- AI: DeepSeek API
- 地图: 高德 JS API 2.0

## 禁止行为
- 不要在需求未确认前写代码
- 不要一次完成多个 Task 而不经过 Review
- 不要跳过测试
- 不要生成占位符或 TODO 代码
- 不要一次性生成整个项目
