# 工具装配 — 实习材料 / jiangnan-travel-web

> jiangnan-travel-web/：Vue 3 + Element Plus + Pinia + ECharts + Vitest（Vite 端口 5173，启动配置 .claude/launch.json 的 web-dev）
> 实习材料/：南昌大学实习 docx 模板（含个人信息）
> 编码规则见 .claude/CLAUDE.md（sync-ai 生成，勿手改）

## Agent（改完代码后主动调用）

| 场景 | Agent |
|------|-------|
| 改了 .vue / Pinia / 路由 | vue-reviewer（必用）+ typescript-reviewer |
| 登录 / 表单 / 接口改动 | security-reviewer |
| SQL / 建表 / 慢查询 | database-reviewer |
| 关键用户流程回归 | e2e-runner |
| 新功能拆解 | planner |

## Skill 知识库（需要时读 ~/.claude/skills/ecc/<名称>/SKILL.md）

vue-patterns · frontend-patterns · mysql-patterns · e2e-testing · error-handling
填实习表格 / 处理文档 → 直接说"帮我填 xx.docx"（docx / pdf / xlsx skill 会接管）

## 常用命令

/vue-review · /code-review · /security-review · /test-coverage · /plan

## MCP（已在 ~/.claude.json 本项目作用域配置，无需 .mcp.json）

filesystem · git · sequential-thinking · playwright · mysql（readonly）
建议：mysql 密码改用环境变量注入，不要留在配置文件里。

## P0 红线

实习材料/ 下的 docx 含姓名、学号等个人信息 —— 绝对禁止 git push（全局隐私规则明文点名实习材料）。
