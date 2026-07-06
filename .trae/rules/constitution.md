# 项目宪法（Project Constitution）

> 本文件为项目最高规则，优先级高于其他所有 workspace rules。  
> 以下 5 个文件合称"**项目宪法（Project Constitution）**"，每次进入项目或开始新任务时必须自动加载并遵守。

---

## 1. 宪法文件清单

| 优先级 | 文件 | 路径 | 核心内容 |
|---|---|---|---|
| **最高** (P0) | **AGENTS.md** | `./AGENTS.md` | AI 代理总入口 — 核心原则、默认行为、文件读取清单、Skill/MCP 调用原则、质量门禁 |
| **P0** | **EXECUTION_MODE.md** | `./EXECUTION_MODE.md` | 企业级执行模式 — 启动流程、Task 推进规则、文档维护、开发纪律、停止条件、汇报规范 |
| **P1** | **PROJECT_RULES.md** | `./PROJECT_RULES.md` | 编码规范 — 包结构、Controller/Service/Mapper/DTO/VO/Result/异常/日志/Git/AI 规范 |
| **P1** | **privacy-and-security.md** | `./.trae/rules/privacy-and-security.md` | 隐私保护 + 安全规范 — 个人隐私不上云端、密钥管理、OWASP 防护、Spring Security 要点 |
| **P1** | **coding-standards.md** | `./.trae/rules/coding-standards.md` | 编码规范 — Java/Spring Boot 后端规范 + Vue 3 前端规范 + 命名/包结构/代码质量 |
| **P1** | **testing-standards.md** | `./.trae/rules/testing-standards.md` | 测试规范 — 80% 覆盖率最低要求、TDD 工作流、AAA 模式、Playwright E2E |
| **P1** | **git-safety.md** | `./.trae/rules/git-safety.md` | Git 安全操作 — Commit 格式、破坏性操作防护、分支策略、提交前检查 |
| **P1** | **environment-rules.md** | `./.trae/rules/environment-rules.md` | 环境维护原则 — 最小修改、检查点机制、会话管理、禁止行为 |
| **P2** | **ARCHITECTURE.md** | `./ARCHITECTURE.md` | 架构文档（唯一） — 数据库 28 张表、22 个 Controller、97 个 API 端点、安全/缓存/WebSocket 架构 |
| **P3** | **AI_WORKFLOW.md** | `./AI_WORKFLOW.md` | Skill + MCP 工作流 — 8 阶段流程、每阶段调用哪个 Skill 和 MCP |

---

## 2. 优先级规则

当文件之间存在冲突时，按以下优先级裁决：

```
AGENTS.md (P0) == EXECUTION_MODE.md (P0)
  > PROJECT_RULES.md == privacy-and-security.md == coding-standards.md
    == testing-standards.md == git-safety.md == environment-rules.md (P1)
  > ARCHITECTURE.md (P2)
  > AI_WORKFLOW.md (P3)
```

具体规则：

- **AGENTS.md** 的"一、默认行为"中的 6 条核心原则不可违反
- **EXECUTION_MODE.md** 的执行流程、开发纪律和停止条件不可违反
- AGENTS.md 与 EXECUTION_MODE.md 同为 P0 级别，内容互补不冲突。如果二者存在冲突，以 AGENTS.md 为准
- **PROJECT_RULES.md** 的具体编码规范不可违反，除非与 P0 文件冲突
- **ARCHITECTURE.md** 的架构描述如需更新，必须同步修改该文件
- **AI_WORKFLOW.md** 的工作流步骤在执行时可按实际情况微调，但必须记录偏离

---

## 3. 自动遵守条款

1. **启动自动加载**：每次进入本项目时，AI 必须自动读取上述 5 个宪法文件作为上下文。
2. **默认遵守**：每次开始新的开发任务时，默认遵循这些文件，用户不需要再次提醒。
3. **不可忽略**：除非用户明确说"忽略宪法规则"或逐条指定例外，否则 AI 不得主动忽略这些规则。
4. **全自动执行**：用户只需描述需求，AI 负责按照 EXECUTION_MODE.md 定义的执行流程自动完成整个开发流程。
5. **默认执行模式**：项目默认处于执行模式，除非用户明确要求退出。

---

## 4. 更新提醒义务

当以下情况发生时，AI **必须主动提醒**用户是否需要更新相关宪法文件：

| 触发条件 | 需要提醒更新的文件 |
|---|---|
| 新增数据库表或修改表结构 | `ARCHITECTURE.md`（第四章数据库架构） |
| 新增/修改/删除 API 端点 | `ARCHITECTURE.md`（第五章 API 接口清单） |
| 新增 Service/Controller 包或模块 | `ARCHITECTURE.md`（第三章分层架构） |
| 新增前端页面或路由 | `ARCHITECTURE.md`（第九章前端架构） |
| 新增 Maven/npm 依赖 | `ARCHITECTURE.md`（第八章或第九章） |
| 新增外部服务集成 | `ARCHITECTURE.md`（第十二章配置清单） |
| 编码规范无法覆盖的新场景 | `PROJECT_RULES.md` 或 `coding-standards.md` |
| 新增安全策略或发现安全漏洞 | `privacy-and-security.md` |
| 测试策略调整或覆盖率目标变更 | `testing-standards.md` |
| Git 工作流或分支策略变更 | `git-safety.md` |
| 环境配置或开发原则调整 | `environment-rules.md` |
| 工作流需要调整 | `AI_WORKFLOW.md` |
| 执行模式需要调整 | `EXECUTION_MODE.md` 或 `AGENTS.md` |

提醒格式：

```
⚠️ 项目结构变更提醒：
项目结构发生变化，建议更新以下宪法文件：
- [文件路径] — [需要更新的内容说明]
是否现在更新？
```

---

## 5. 违反后果

- 未遵守宪法规则生成的代码，将被标记为"不合规"并退回重做
- 连续 3 次违反相同规则，需用户明确确认后方可继续
- 用户可随时通过"豁免"指令临时绕过特定规则

---

## 6. 与其他规则文件的关系

| 文件 | 关系 |
|---|---|
| `./.trae/rules/development-workflow.md` | 本宪法的简版摘要，内容以宪法为准 |
| 其他 `.trae/rules/*.md` | 与本宪法不冲突时可同时生效，冲突时本宪法优先 |
