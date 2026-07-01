# AGENTS.md — AI 代理总入口

> 项目：江南出行（Jiangnan Travel）  
> 技术栈：Spring Boot 3.2.6 + Java 17 + MyBatis-Plus 3.5.7 + MySQL 8.0 + Redis + Vue 3.4 + Vite 5 + Element Plus 2.7  
> 外部集成：DeepSeek AI、高德地图、WebSocket、Knife4j  
> **本文件是整个项目的 AI 总入口，所有开发任务从此开始。**

---

## 一、默认行为

### 1.1 核心原则

1. **需求驱动**：所有开发从需求分析开始，不得跳过
2. **Spec 优先**：必须先写 Spec，经用户确认后再开发
3. **增量交付**：每次只完成一个 Task，不允许一次性生成整个项目
4. **测试先行**：遵循 RED → GREEN → REFACTOR 循环
5. **持续 Review**：每个 Task 完成后自动进行 Code Review
6. **有疑必问**：需求存在歧义时，必须先询问，不允许猜测

### 1.2 默认进入执行模式

**本项目默认处于 [EXECUTION_MODE.md](./EXECUTION_MODE.md) 定义的执行模式。**

- 收到"继续开发""开发 Task-NNN"等指令后，AI **自动执行**完整开发流程
- 每个 Task 完成后，AI **自动输出开发报告**，等待用户确认后再继续
- 除非用户明确要求退出，否则永久保持该模式

> 详细执行流程、Task 推进规则、开发纪律、停止条件、汇报规范 → 参见 `EXECUTION_MODE.md`

---

## 二、默认读取文件清单

每次进入项目或开始新任务时，**默认自动读取并同步**以下文件：

| 优先级 | 文件 | 用途 |
|---|---|---|
| **P0 — 必须读取** | `AGENTS.md` | AI 总入口（本文件） |
| | `EXECUTION_MODE.md` | 执行模式定义 |
| | `PROJECT_RULES.md` | 编码规范 |
| | `ARCHITECTURE.md` | 架构文档 |
| | `AI_WORKFLOW.md` | Skill + MCP 工作流 |
| **P1 — 开发时读取** | `PRODUCT_DESIGN.md` | 产品设计 |
| | `DESIGN_SYSTEM.md` | 设计系统 |
| | `UI_UX_SPEC.md` | UI/UX 规范 |
| | `PAGE_STRUCTURE.md` | 页面结构 |
| **P2 — 规划时读取** | `FEATURE_ROADMAP.md` | 功能路线图 |
| | `TASK_ROADMAP.md` | 任务路线图 |
| | `DEVELOPMENT_PLAN.md` | 开发计划 |
| | `项目开发总结报告.md` | 项目开发总结报告（含审查、能力、历程） |
| | `README.md` | 项目说明 |

> **冲突处理**：读取时如果发现文件间存在冲突，按 `constitution.md` 的优先级规则裁决。无法裁决时，立即停止并请示用户。

---

## 三、默认开发流程

```
┌──────────────────────────────────────────────────────────────────────┐
│                        开发工作流（一级流程）                          │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐         │
│  │ 加载规范  │ → │ 扫描项目  │ → │ 定位Task │ → │ 执行Task │         │
│  │ &设计文档 │   │ 当前状态  │   │ &依赖分析 │   │ (完整周期)│         │
│  └─────────┘   └─────────┘   └─────────┘   └────┬────┘         │
│                                                   │                  │
│                                                   ▼                  │
│  ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐         │
│  │ 等待确认  │ ← │ 输出报告  │ ← │ 更新文档  │ ← │ 编译测试  │         │
│  │  后继续  │   │ (含建议)  │   │ &Roadmap │   │ &Review  │         │
│  └─────────┘   └─────────┘   └─────────┘   └─────────┘         │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

> **详细流程**（启动流程 5 步、单 Task 执行 9 步、冲突处理、停止恢复）→ 参见 `EXECUTION_MODE.md`

---

## 四、默认 Skill 调用原则

### 4.1 选择策略

- 根据任务类型**自动选择**最合适的 Skill
- **同一阶段只使用一个 Skill**，避免功能重复调用
- 优先使用**已安装的 Skills**，不要安装新的 Skill

### 4.2 阶段 → Skill 映射

| 阶段 | 使用的 Skill | 说明 |
|---|---|---|
| **需求分析** | `brainstorming` | Socratic 式提问澄清需求 |
| **Spec 编写** | `spec-driven-development` | 生成 spec.md + checklist.md + tasks.md |
| **Task 拆分** | `planning-and-task-breakdown` | 将大任务拆分为 2-5 分钟粒度 |
| **数据库设计** | `database-design` | ER 设计、索引策略、代码生成 |
| **API 设计** | `rest-api-design` | RESTful 规范、状态码、响应格式 |
| **后端开发** | `java-architect` | Spring Boot 3.x、Security、WebSocket |
| **前端开发** | `web-dev` | Vue 3 + Vite + Element Plus + Pinia |
| **测试** | `test-driven-development` | RED → GREEN → REFACTOR |
| **Code Review** | `code-review-and-quality` | 多维度审查 |
| **重构** | `code-refactoring` | SOLID 原则、设计模式 |
| **安全** | `security-and-hardening` | 输入验证、认证加固 |
| **性能** | `performance-optimization` | 查询优化、Core Web Vitals |
| **对抗审查** | `doubt-driven-development` | 高风险决策的对抗性审查 |
| **调试** | `debugging-and-error-recovery` | 根因分析、修复验证 |
| **文档** | `documentation-and-adrs` | API 文档、ADR |
| **Git** | `git-workflow-and-versioning` | 提交规范、分支策略 |

> 详细 Skill 调用矩阵（含排除列表、触发条件、产出物）→ 参见 `AI_WORKFLOW.md`

---

## 五、默认 MCP 调用原则

### 5.1 选择策略

- **优先使用 MCP**：能通过 MCP 完成的操作用 MCP，而不是 RunCommand
- MySQL MCP 为**只读**模式，写入必须通过后端 API 或 SQL 脚本经用户确认
- Git MCP 提交前必须先 Review
- Playwright 仅用于测试和预览

### 5.2 阶段 → MCP 映射

| 阶段 | MCP | 常用工具 |
|---|---|---|
| **需求分析** | Sequential Thinking | 复杂需求多步推演 |
| **Spec 编写** | MySQL | 确认当前数据库表结构 |
| **数据库设计** | MySQL | describe_table, inspect_schema, query |
| **API 设计** | Sequential Thinking | 复杂 API 设计决策 |
| **后端开发** | MySQL | 数据验证、SQL 调试 |
| **前端开发** | Playwright | 页面预览、交互验证 |
| **测试(E2E)** | Playwright | 浏览器自动化全工具集 |
| **Code Review** | Git | git_diff, git_log, git_status |
| **重构** | MySQL | 数据一致性确认 |
| **Git 提交** | Git | 全流程工具 |
| **调试** | MySQL + Playwright | 数据 + 页面联合调试 |

> 详细 MCP 调用矩阵（含工具名、调用条件、限制说明）→ 参见 `AI_WORKFLOW.md`

---

## 六、质量门禁

### 6.1 每个 Task 的验收标准

- [ ] 测试通过（单元测试 / 集成测试 / E2E 测试）
- [ ] Code Review 无严重问题
- [ ] 安全无漏洞（安全检查通过）
- [ ] 文档已同步更新
- [ ] 变更已在 Git 中提交

### 6.2 禁止行为

- ❌ 跳过需求分析和 Spec 直接编码
- ❌ 需求不明确时猜测实现
- ❌ 一次性生成整个项目或整个模块
- ❌ 一次完成多个 Task 不经 Review
- ❌ 生成 TODO / 占位符 / FIXME 代码
- ❌ 跳过测试直接提交
- ❌ 未经 Review 提交 Git
- ❌ 直接修改数据库数据（需经用户确认）

---

## 七、环境信息

| 配置 | 值 |
|---|---|
| IDE | Trae IDE |
| 操作系统 | Windows |
| Node.js | v22.19.0 |
| npm | 10.9.3 |
| Java | 17 |
| 包管理器（后端） | Maven |
| MySQL | 8.0.42（localhost:3306） |
| Redis | 配置在 application.yml |

---

> **文件体系：**  
> `AGENTS.md`（入口）→ `EXECUTION_MODE.md`（执行模式）→ `AI_WORKFLOW.md`（Skill/MCP 详情）→ `PROJECT_RULES.md`（编码规范）→ `ARCHITECTURE.md`（架构参考）  
> 优先级规则见 `constitution.md`。  
> 生成时间：2026-06-24
