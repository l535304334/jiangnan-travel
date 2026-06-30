# 开发工作流规则（摘要）

> 本文件是 [constitution.md](./constitution.md) 的摘要版本。  
> 完整规则、优先级、更新提醒义务请查阅 **项目宪法（Project Constitution）**。

---

## 宪法文件

本项目由以下 5 个文件组成"**项目宪法（Project Constitution）**"，每次开发任务必须遵守：

| 优先级 | 文件 |
|---|---|
| **P0（最高）** | [AGENTS.md](../../AGENTS.md) — AI 代理总入口 |
| **P0** | [EXECUTION_MODE.md](../../EXECUTION_MODE.md) — 执行模式定义 |
| **P1** | [PROJECT_RULES.md](../../PROJECT_RULES.md) — 编码规范 |
| **P2** | [ARCHITECTURE.md](../../ARCHITECTURE.md) — 架构文档 |
| **P3** | [AI_WORKFLOW.md](../../AI_WORKFLOW.md) — Skill + MCP 工作流 |

优先级：`AGENTS.md == EXECUTION_MODE.md > PROJECT_RULES.md > ARCHITECTURE.md > AI_WORKFLOW.md`

---

## 默认执行模式

**本项目默认处于 [EXECUTION_MODE.md](../../EXECUTION_MODE.md) 定义的执行模式。**

收到开发指令时，AI 自动执行：
1. 加载所有项目规范与设计文档
2. 扫描项目当前状态
3. 定位当前应开发的 Task
4. 分析影响范围与依赖关系
5. 检查是否存在设计冲突
6. 执行 Task（数据库设计 → API 设计 → TDD → 编码 → 编译检查 → 测试 → Review → 重构 → 文档同步）
7. 输出开发报告
8. 等待用户确认后继续下一项

> 详细执行流程、Task 推进规则、开发纪律、停止条件、汇报规范 → 参见 `EXECUTION_MODE.md`

---

## 核心工作流（8 阶段）

```
需求分析 → Spec → 用户确认 → Task 拆分 → 逐个 Task(测试→编码→Review) → 文档 → Git 提交
```

| 阶段 | 使用的 Skill | 调用的 MCP |
|---|---|---|
| ① 需求分析 | `brainstorming` | Sequential Thinking（可选） |
| ② 编写 Spec | `spec-driven-development` | MySQL（确认表结构） |
| ③ Task 拆分 | `planning-and-task-breakdown` | — |
| ④ 数据库设计 | `database-design` | MySQL（describe_table, inspect_schema） |
| ⑤ API 设计 | `rest-api-design` | Sequential Thinking（可选） |
| ⑥ 增量开发 | `test-driven-development` + `java-architect` / `web-dev` | MySQL / Playwright |
| ⑦ Code Review | `code-review-and-quality` | Git（git_diff） |
| ⑧ 文档 + 提交 | `documentation-and-adrs` + `git-workflow-and-versioning` | Git（全流程） |

---

## 禁止行为

- ❌ 跳到需求分析直接编码
- ❌ 跳过 Spec 直接编码
- ❌ 一次性生成整个项目
- ❌ 未经 Review 提交 Git
- ❌ 需求不明确时猜测实现
- ❌ 生成 TODO / 占位符代码
- ❌ 跳过测试

---

> 详细规则、冲突裁决、更新提醒义务请参考 [constitution.md](./constitution.md)。  
> 详细 Skill + MCP 调用矩阵请参考 [AI_WORKFLOW.md](../../AI_WORKFLOW.md)。
