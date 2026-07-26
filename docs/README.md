# 文档导航

> 项目文档按**工程**与**求职展示**双轴组织。修改任何文档前先读根目录 [ARCHITECTURE.md](../ARCHITECTURE.md)——它是与代码同步维护的唯一架构事实源；文档与代码冲突时一律以代码为准。

---

## 一、工程轴（开发 / 维护 / 部署）

| 文档 | 内容 | 何时读 |
|------|------|--------|
| [../README.md](../README.md) | 项目门面：亮点、技术栈、快速启动、截图 | 第一份必读 |
| [../ARCHITECTURE.md](../ARCHITECTURE.md) | **架构唯一事实源**：目录/分层/27 表/98 API/安全/缓存/测试/新增模块指引 | 接手项目、任何开发前 |
| [../CONTRIBUTING.md](../CONTRIBUTING.md) | 环境搭建、分支与提交规范、代码规范要点、PR 流程 | 提交代码前 |
| [江南出行调度系统_系统设计文档_v1.5.md](江南出行调度系统_系统设计文档_v1.5.md) | 调度系统深度设计：二阶锁/评分引擎/复杂度分析 | 理解核心设计 |
| [configuration.md](configuration.md) | 环境变量总表、两个 profile 差异、前端 env | 配置环境时 |
| [troubleshooting.md](troubleshooting.md) | FAQ：启动/测试/功能/部署四类常见问题 | 遇到问题时 |
| [../deploy/DEPLOY_README.md](../deploy/DEPLOY_README.md) | Docker Compose 生产部署 | 部署时 |
| [../tests/TEST_GUIDE.md](../tests/TEST_GUIDE.md) | 24 个 API 级 E2E 测试脚本使用说明 | 跑 E2E 时 |
| [../AGENTS.md](../AGENTS.md) | AI 代理开发入口（默认行为/读取清单/质量门禁） | 用 AI 工具开发时 |

## 二、过程与历史（时点记录，只读不改）

| 文档 | 内容 |
|------|------|
| [TASK_ROADMAP.md](TASK_ROADMAP.md) | 30 个 Task 执行记录（30/30 完成）+ 后续 Roadmap |
| [RELEASE_REVIEW_REPORT.md](RELEASE_REVIEW_REPORT.md) | Release 1.0 验收报告（2026-07-08 时点快照） |
| [UPGRADE_2026-07-26.md](UPGRADE_2026-07-26.md) | Release 后全面升级记录（缺陷修复/架构收敛/验证方式） |
| [PRODUCT_DESIGN.md](PRODUCT_DESIGN.md) | 产品设计 v2.0（含当时的差距分析，已标注时点） |
| [ai-development-log.md](ai-development-log.md) | AI 辅助开发实践：协作比例、行为约束、效率数据、Bug 案例 |
| [../项目开发总结报告.md](../项目开发总结报告.md) | 项目全景盘点（答辩/PPT 数据源） |
| [../实习日志/](../实习日志/) | 28 天每日开发记录（本地私有，不入远程仓库） |

## 三、求职展示轴（复试 / 简历 / 答辩）

**复试准备路径（约 2 小时）**：README → 讲解稿 → 精选问答 → 亮点 → 白板图 → 简历文案

| 文档 | 内容 | 场景 |
|------|------|------|
| [interview/interview_script.md](interview/interview_script.md) | 3 分钟 / 5 分钟讲解稿 + 背诵要点卡（唯一维护版本） | 面试前一天背诵 |
| [interview/interview_qna.md](interview/interview_qna.md) | 8 题精选问答 + 速查卡 | 面试前 10 分钟 |
| [面试问题.md](面试问题.md) | 30+ 题深度题库（六大主题） | 系统性准备 |
| [interview/project_highlights_interview.md](interview/project_highlights_interview.md) | 8 个亮点面试展开版 | 亮点深挖 |
| [../PROJECT_HIGHLIGHTS.md](../PROJECT_HIGHLIGHTS.md) | 6 个核心亮点（答辩版） | 答辩/PPT |
| [architecture/interview_architecture.md](architecture/interview_architecture.md) | 4 张架构图（白板练习/PPT 直用） | 画图讲解 |
| [resume/project_description.md](resume/project_description.md) | 150 字 / 100 字简历文案 + 关键词 | 写简历 |
| [项目介绍.md](项目介绍.md) | 自包含面试汇编（背景/难点/方案/收获） | 离线通读 |
| [江南出行_简历面试表达层_v1.5.md](江南出行_简历面试表达层_v1.5.md) | 📦 已归档：表达重构工作文档 | 一般无需阅读 |

## 四、文档维护约定

1. **代码即事实**：功能/接口/表结构变更时，同一 PR 内同步 `ARCHITECTURE.md` 对应章节
2. **时点文档不回改**：第二节的过程记录只追加新文件（如 `UPGRADE_YYYY-MM-DD.md`），不修改旧结论
3. **数字出处唯一**：端点数/表数/测试数以 `ARCHITECTURE.md` 为准，其余文档引用时须同步
4. **命名**：新增工程文档用小写 kebab-case 英文名；求职材料允许中文名
5. **截图**：统一存放 `screenshots/`，README 引用相对路径
