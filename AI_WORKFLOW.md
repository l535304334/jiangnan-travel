# AI_WORKFLOW.md — Skill + MCP 工作流

> 本文档定义 AI 开发过程中 **何时调用哪个 Skill、何时调用哪个 MCP**。  
> 所有开发任务必须按照此工作流执行，不得跳过或颠倒顺序。

---

## 一、总览流程图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         AI 开发工作流                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐           │
│  │  需求分析  │───→│  Spec   │───→│  用户确认  │───→│ Task拆分  │           │
│  │(brainstorm│    │  (spec- │    │          │    │(planning │           │
│  │  -ing)    │    │  driven)│    │          │    │-and-task)│           │
│  └─────┬─────┘    └──────────┘    └──────────┘    └────┬─────┘           │
│        │                                                │                 │
│        ▼                                                ▼                 │
│  ┌──────────┐                                    ┌──────────┐           │
│  │Sequential│    ┌─────────────────────────────────┤ 逐个执行  │           │
│  │Thinking  │    │  是否需要数据库变更？              │  Task    │           │
│  │MCP       │    │        │          │              │          │           │
│  └──────────┘    │       是         否              └────┬─────┘           │
│                  │        ▼          │                   │                 │
│                  │  ┌──────────┐     │        ┌──────────┴──────────┐     │
│                  │  │ 数据库设计 │     │        │     Task 内循环       │     │
│                  │  │(database │     │        │                      │     │
│                  │  │ -design) │     │        │  ┌──────────┐       │     │
│                  │  │MySQL MCP │     │        │  │ 测试先行  │       │     │
│                  │  └────┬─────┘     │        │  │(test-    │       │     │
│                  │       │          │        │  │ driven)  │       │     │
│                  │       ▼          │        │  └────┬─────┘       │     │
│                  │  ┌──────────┐     │        │       ▼            │     │
│                  │  │ API设计   │     │        │  ┌──────────┐    │     │
│                  │  │(rest-api │     │        │  │ 编码实现  │    │     │
│                  │  │ -design) │     │        │  │(java-    │    │     │
│                  │  │Sequential│     │        │  │ architect │    │     │
│                  │  │Thinking  │     │        │  │ /web-dev)│    │     │
│                  │  └──────────┘     │        │  └────┬─────┘    │     │
│                  └───────────────────┘        │       ▼            │     │
│                                                │  ┌──────────┐    │     │
│                                                │  │  Code    │    │     │
│                                                │  │  Review  │    │     │
│                                                │  │(code-    │    │     │
│                                                │  │ review)  │    │     │
│                                                │  │Git MCP   │    │     │
│                                                │  └────┬─────┘    │     │
│                                                └───────┼──────────┘     │
│                                                        │                 │
│  全部 Task 完成后                                        │                 │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐          │                 │
│  │ 文档更新  │───→│ Git提交  │───→│ 完成     │◄─────────┘                 │
│  │(document │    │(git-work │    │          │                             │
│  │ -ation)  │    │ -flow)   │    │          │                             │
│  └──────────┘    └──────────┘    └──────────┘                             │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 二、Skill 调用矩阵

### 2.1 核心工作流 Skill（16 个）

| 阶段 | Skill | 触发条件 | 产出物 |
|---|---|---|---|
| **需求分析** | `brainstorming` | 收到新需求 | 需求分析记录 |
| **Spec 编写** | `spec-driven-development` | 需求已确认 | spec.md + checklist.md + tasks.md |
| **Task 拆分** | `planning-and-task-breakdown` | Spec 已确认 | 粒度 2-5min 的 Task 清单 |
| **数据库设计** | `database-design` | Task 涉及数据库变更 | 表结构设计、DDL、索引方案 |
| **API 设计** | `rest-api-design` | Task 涉及 API 变更 | API 端点定义、请求/响应结构 |
| **后端开发** | `java-architect` | Task 涉及后端代码 | Spring Boot 实现代码 |
| **前端开发** | `web-dev` | Task 涉及前端代码 | Vue 组件/页面 |
| **测试** | `test-driven-development` | 每个 Task 开始时 | 单元测试 + 集成测试 |
| **Code Review** | `code-review-and-quality` | 每个 Task 完成后 | 审查报告 |
| **重构** | `code-refactoring` | Review 提出或 REFACTOR 阶段 | 优化后的代码 |
| **安全加固** | `security-and-hardening` | 涉及认证/鉴权/用户输入 | 安全防护代码 |
| **性能优化** | `performance-optimization` | 性能瓶颈或优化需求 | 优化后的代码 |
| **调试** | `debugging-and-error-recovery` | 测试失败或构建错误 | 修复后的代码 |
| **对抗审查** | `doubt-driven-development` | 高风险决策、安全敏感逻辑 | 审查结论 |
| **文档** | `documentation-and-adrs` | Review 通过后 | 更新文档、ADR |
| **Git** | `git-workflow-and-versioning` | 所有 Task 完成后 | 规范化提交 |

### 2.2 不直接调用的 Skill（18 个）

以下 Skill 因功能重复/不适配/元能力，**不由工作流直接调用**，但在对应场景下可能会有 Agent 自动选用：

| Skill | 不调用的原因 |
|---|---|
| `TRAE-code-review` | 用 `code-review-and-quality` 替代 |
| `TRAE-debugger` | 用 `debugging-and-error-recovery` 替代 |
| `TRAE-security-review` | 用 `security-and-hardening` 替代 |
| `code-simplification` | 被 `code-refactoring` 覆盖 |
| `incremental-implementation` | 工作流本身保证增量交付 |
| `TRAE-generate-mini-app` | 本项目非小程序 |
| `auth0-springboot-api` | 本项目使用自定义 JWT |
| `source-driven-development` | 被 `spec-driven-development` 覆盖 |
| `subagent-driven-development` | Agent 自主管理子代理，不作独立 Skill 调用 |
| `context-engineering` | 环境配置元能力，非开发流程步骤 |
| `mem-search` | 跨会话记忆，非开发流程步骤 |
| `skill-creator` | 创建 Skill 的元能力，非开发流程步骤 |
| `using-agent-skills` | 发现 Skill 的元能力，非开发流程步骤 |
| `using-superpowers` | 对话启动元能力，非开发流程步骤 |
| `gitnexus-cli` | GitNexus CLI 工具，非开发流程步骤 |
| `humanize` | 文本润色，非开发流程步骤 |
| `humanize-chinese` | 中文文本润色，非开发流程步骤 |
| `pdf` | PDF 处理，非开发流程步骤 |

---

## 三、MCP 调用矩阵

### 3.1 调用时机

| MCP | 调用时机 | 具体用途 |
|---|---|---|
| **MySQL MCP** | 数据库设计、后端开发、数据验证 | `list_databases` / `list_tables` / `describe_table` / `query` / `inspect_schema` / `sample_rows` / `explain_query` |
| **Playwright MCP** | 前端开发、E2E 测试、页面验证 | `browser_navigate` / `browser_snapshot` / `browser_click` / `browser_type` / `browser_fill_form` / `browser_take_screenshot` / `browser_evaluate` |
| **Git MCP** | Code Review、Git 提交、状态检查 | `git_status` / `git_diff` / `git_log` / `git_add` / `git_commit` / `git_branch` |
| **Filesystem MCP** | 文档读取、非代码文件操作 | `read_file` / `write_file` / `list_directory` / `search_files` |
| **Sequential Thinking MCP** | 复杂推理场景 | `sequential_thinking`（多步推理分析） |

### 3.2 阶段 → MCP 映射

```
需求分析阶段：
  └─ 复杂需求推演 → Sequential Thinking MCP

Spec 编写阶段：
  └─ 确认数据库现状 → MySQL MCP (list_tables, describe_table)

数据库设计阶段：
  ├─ 查看现有表结构 → MySQL MCP (list_tables, describe_table)
  ├─ 检查字段和索引 → MySQL MCP (inspect_schema)
  ├─ 测试 SQL 语句   → MySQL MCP (query, explain_query)
  └─ 采样数据参考   → MySQL MCP (sample_rows)

API 设计阶段：
  └─ 复杂 API 决策 → Sequential Thinking MCP

后端开发阶段：
  └─ 数据验证和调试 → MySQL MCP (query)

前端开发阶段：
  ├─ 页面效果预览   → Playwright MCP (browser_navigate, browser_snapshot)
  └─ 交互行为验证   → Playwright MCP (browser_click, browser_type)

测试阶段（E2E）：
  ├─ 页面导航       → Playwright MCP (browser_navigate)
  ├─ 表单填写       → Playwright MCP (browser_type, browser_fill_form)
  ├─ 按钮点击       → Playwright MCP (browser_click)
  ├─ 内容验证       → Playwright MCP (browser_snapshot)
  ├─ 截图保存       → Playwright MCP (browser_take_screenshot)
  └─ 网络请求监控   → Playwright MCP (browser_network_requests)

Code Review 阶段：
  ├─ 查看变更差异   → Git MCP (git_diff)
  ├─ 查看提交历史   → Git MCP (git_log)
  └─ 查看当前状态   → Git MCP (git_status)

重构阶段：
  └─ 数据一致性确认 → MySQL MCP (query)

文档阶段：
  └─ 读取现有文档   → Filesystem MCP (read_file)

Git 提交阶段：
  ├─ 查看状态       → Git MCP (git_status)
  ├─ 查看变更       → Git MCP (git_diff)
  ├─ 添加到暂存区   → Git MCP (git_add)
  └─ 提交代码       → Git MCP (git_commit)

调试阶段：
  ├─ 数据库查询     → MySQL MCP (query)
  └─ 页面行为验证   → Playwright MCP (browser_snapshot, browser_evaluate)
```

---

## 四、阶段详细规范

### 阶段 1：需求分析

```
触发: 用户提出新需求

调用 Skill:
  └─ brainstorming ──→ Socratic 式提问 → 澄清需求 → 记录分析结果

可选 MCP:
  └─ Sequential Thinking ──→ 复杂多步推理场景

产出: 清晰的需求条目（边界条件、约束、验收标准）

禁止:
  ✘ 需求模糊时猜测实现
  ✘ 跳过此阶段直接编码
```

### 阶段 2：编写 Spec

```
触发: 需求分析完成并确认

前置检查:
  └─ MySQL MCP (list_tables, describe_table) ──→ 确认数据库当前状态

调用 Skill:
  └─ spec-driven-development ──→ 生成以下文件到 docs/spec/：
      ├── spec.md         (功能描述、输入输出、边界条件、错误处理)
      ├── checklist.md    (验收清单)
      └── tasks.md        (初步任务拆分)

产出: docs/spec/ 目录下三份文档

⏸ 关键节点: 等待用户确认 → 确认后进入下一阶段
```

### 阶段 3：Task 拆分

```
触发: Spec 获得用户确认

调用 Skill:
  └─ planning-and-task-breakdown ──→ 将大任务拆分为 2-5 分钟粒度

使用工具:
  └─ TodoWrite ──→ 创建 Task 列表

每个 Task 必须包含:
  ├─ 变更文件列表
  ├─ 测试方案
  └─ 验收标准

原则:
  ├─ 每个 Task 独立可验证
  ├─ 任务间依赖关系清晰
  └─ 粒度 2-5 分钟
```

### 阶段 4：数据库设计

```
触发: 当前 Task 涉及数据库变更

调用 Skill:
  └─ database-design ──→ ER 设计、索引策略、MyBatis-Plus 代码生成

调用 MCP:
  ├─ MySQL MCP (describe_table)  ──→ 查看当前表结构
  ├─ MySQL MCP (list_tables)     ──→ 查看现有表
  ├─ MySQL MCP (inspect_schema)  ──→ 检查索引和字段
  └─ MySQL MCP (query)           ──→ 验证 DDL 语法

产出:
  ├─ 表结构设计方案
  ├─ 索引策略
  └─ DDL 语句（需用户确认后执行）
```

### 阶段 5：API 设计

```
触发: 当前 Task 涉及新增/修改 API

调用 Skill:
  └─ rest-api-design ──→ 遵循 RESTful 规范的 API 设计

可选调用:
  ├─ security-and-hardening ──→ 安全敏感的 API
  └─ Sequential Thinking MCP ──→ 复杂 API 设计决策

产出:
  ├─ API 端点定义（URL、方法、参数、响应）
  └─ 请求/响应数据结构
```

### 阶段 6：增量开发（每个 Task）

```
┌─────────────────────────────────────────┐
│          每个 Task 的内部循环              │
│                                         │
│  ① 测试先行                              │
│     ├─ test-driven-development Skill     │
│     └─ 写测试（RED 阶段）                 │
│                                         │
│  ② 编码实现                              │
│     ├─ java-architect Skill（后端）      │
│     │    或 web-dev Skill（前端）         │
│     ├─ MySQL MCP（数据验证）              │
│     └─ Playwright MCP（前端预览）         │
│                                         │
│  ③ 测试通过                              │
│     └─ 运行测试确认通过（GREEN 阶段）      │
│                                         │
│  ④ 安全审查（如需）                       │
│     ├─ security-and-hardening Skill      │
│     └─ doubt-driven-development Skill    │
│                                         │
│  ⑤ 重构                                  │
│     ├─ code-refactoring Skill            │
│     └─ 运行测试确认重构后仍通过             │
│                                         │
│  ⑥ Code Review                           │
│     ├─ Git MCP (git_diff)               │
│     └─ code-review-and-quality Skill     │
│                                         │
│  ⏸ 严重问题 → 退回 ②                     │
└─────────────────────────────────────────┘

MCP 调用场景:
  后端 Task:
    └─ MySQL MCP (query) ──→ 验证 SQL、检查数据
  前端 Task:
    ├─ Playwright (browser_navigate) ──→ 页面导航
    ├─ Playwright (browser_snapshot)  ──→ 页面结构验证
    └─ Playwright (browser_click/type) ──→ 交互验证
  调试 Task:
    └─ debugging-and-error-recovery Skill
```

### 阶段 7：文档更新

```
触发: 当前 Task 的 Code Review 通过

调用 Skill:
  └─ documentation-and-adrs ──→ 更新相关文档

调用 MCP:
  └─ Filesystem MCP (read_file) ──→ 读取现有文档

更新内容:
  ├─ API 文档（影响 Swagger 则自动更新）
  ├─ 数据库设计文档（表结构变更时）
  ├─ README（影响项目使用方式时）
  └─ ADR（架构决策记录，架构变更时）
```

### 阶段 8：Git 提交

```
触发: 文档更新完成

调用 MCP:
  ├─ Git MCP (git_status)   ──→ 确认变更
  ├─ Git MCP (git_diff)     ──→ 审查变更内容
  ├─ Git MCP (git_add)      ──→ 添加文件
  └─ Git MCP (git_commit)   ──→ 提交（message 使用中文）

调用 Skill:
  └─ git-workflow-and-versioning ──→ 确保提交规范

提交信息格式:
  type: 简短描述

  type 类型: feat / fix / refactor / perf / style / docs / test / chore
```

---

## 五、异常处理流程

### 5.1 测试失败 / 构建错误

```
┌──────────────────────────────┐
│    测试失败 / 构建错误          │
└──────────┬───────────────────┘
           ▼
┌──────────────────────────────┐
│  debugging-and-error-recovery │
│  Skill 介入                   │
├──────────────────────────────┤
│  ① 分析错误日志               │
│  ② 定位根因                   │
│  ③ 修复代码                   │
│  ④ 重新运行测试               │
│  ⑤ 3 次修复不成功 → 报告用户  │
└──────────────────────────────┘
```

### 5.2 需求变更

```
① 返回 阶段 1：需求分析
② 重新分析变更影响
③ 更新 Spec → 用户确认
④ 重新拆分 Task
⑤ 继续执行
```

### 5.3 MCP 连接异常

```
① 检查 MCP 状态: claude mcp list
② 查看详情: claude mcp get <name>
③ 如显示 Error → 尝试重新连接或报告用户
④ Skill 调用失败 → 降级使用直接工具调用
```

---

## 六、质量门禁

### 6.1 每个 Task 的验收标准

```
□ 测试通过（单元测试 / 集成测试 / E2E 测试）
□ Code Review 无严重问题（code-review-and-quality 审查通过）
□ 安全无漏洞（security-and-hardening 检查通过）
□ 文档已更新（documentation-and-adrs 执行）
□ Git 已提交（git-workflow-and-versioning + Git MCP）
```

### 6.2 强制检查点

```
阶段 1 (需求分析) → 必须调用 brainstorming
阶段 2 (Spec)     → 必须用户确认后才能继续
阶段 3 (Task)     → 每个 Task 必须包含测试方案
阶段 6-① (测试)   → 每个 Task 必须写测试
阶段 6-⑥ (Review) → 每个 Task 完成后必须 Review
阶段 7 (文档)      → 必须更新受影响的文档
阶段 8 (Git)       → 提交前必须 Review 通过
```

### 6.3 禁止行为

```
❌ 跳过需求分析直接编码
❌ 跳过 Spec 直接编码
❌ 一次性生成整个项目
❌ 一次完成多个 Task 未经 Review
❌ 需求不明确时猜测实现
❌ 生成 TODO / 占位符代码
❌ 跳过测试
❌ 没有 Review 直接提交 Git
```

---

## 七、速查卡

### 7.1 一句话记工作流

```
分析 → Spec → 确认 → 拆分 → 逐个(TDD→编码→Review→文档→提交)
```

### 7.2 Skill 速查表

| 阶段 | Skill | 一句话 |
|---|---|---|
| 需求分析 | `brainstorming` | 问清楚再动手 |
| Spec | `spec-driven-development` | 先写文档再编码 |
| Task 拆分 | `planning-and-task-breakdown` | 大任务切小块 |
| 数据库 | `database-design` | 表结构先设计 |
| API | `rest-api-design` | RESTful 规范 |
| 后端代码 | `java-architect` | Spring Boot 专家 |
| 前端代码 | `web-dev` | Vue 专家 |
| 测试 | `test-driven-development` | RED→GREEN→REFACTOR |
| Review | `code-review-and-quality` | 多维度审查 |
| 重构 | `code-refactoring` | 优化不改变行为 |
| 安全 | `security-and-hardening` | 防漏洞 |
| 性能 | `performance-optimization` | 优化不拖慢 |
| 调试 | `debugging-and-error-recovery` | 找根因修复 |
| 对抗审查 | `doubt-driven-development` | 高风险反复查 |
| 文档 | `documentation-and-adrs` | 记录决策 |
| Git | `git-workflow-and-versioning` | 规范提交 |

### 7.3 MCP 速查表

| MCP | 何时调用 | 常用工具 |
|---|---|---|
| **MySQL** | 数据库/后端/数据验证 | `query`, `describe_table`, `list_tables`, `inspect_schema` |
| **Playwright** | 前端/E2E/页面验证 | `browser_navigate`, `browser_snapshot`, `browser_click`, `browser_type` |
| **Git** | Review/提交/状态 | `git_diff`, `git_status`, `git_log`, `git_add`, `git_commit` |
| **Filesystem** | 文档/非代码文件 | `read_file`, `write_file`, `list_directory` |
| **Sequential Thinking** | 复杂推理 | `sequential_thinking` |

### 7.4 阶段 → MCP 速查

```
需求分析  → Sequential Thinking（复杂推理）
Spec      → MySQL（确认表结构）
数据库设计 → MySQL（多工具组合）
API 设计  → Sequential Thinking（复杂决策）
后端开发  → MySQL（数据验证）
前端开发  → Playwright（页面预览）
测试(E2E) → Playwright（全工具集）
Review    → Git（差异对比）
重构      → MySQL（数据一致性）
文档      → Filesystem（读取现有文件）
Git 提交  → Git（全流程）
调试      → MySQL + Playwright（数据+页面）
```

---

> 本文档定义完整的 Skill + MCP 工作流，后续所有开发默认按此流程执行。  
> 生成时间：2026-06-24
