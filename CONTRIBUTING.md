# 贡献指南

感谢关注江南出行智慧服务平台。本文说明如何搭建开发环境、遵循项目规范并提交变更。

---

## 一、开发环境

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端（Spring Boot 3.2.6） |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+（建议 22） | 前端（Vite 5） |
| MySQL | 8.0+ | 业务数据库，默认库名 `smart_travel` |
| Redis | 6+ | 缓存 + Redisson 分布式锁 |

### 首次搭建

```bash
# 1. 初始化数据库（含建表 + 种子数据 + 测试账号）
mysql -u root -p < jiangnan-travel/src/main/resources/sql/init.sql

# 2. 配置环境变量（复制模板后按本机填写，绝不提交真实密钥）
cp deploy/.env.example deploy/.env

# 3. 启动后端（需先注入 deploy/.env 中的环境变量）
cd jiangnan-travel && mvn spring-boot:run

# 4. 启动前端
cd jiangnan-travel-web && npm install && npm run dev
```

完整环境变量说明见 [docs/configuration.md](docs/configuration.md)。

---

## 二、分支与提交规范

### 分支模型

```
main            # 保护分支，只接受合并，禁止直接 push
  └── feature/xxx   # 功能开发
  └── fix/xxx       # Bug 修复
```

### Commit 消息格式

```
<type>: <description>
```

| type | 用途 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `refactor` | 重构（不改行为） |
| `docs` | 文档 |
| `test` | 测试 |
| `chore` | 构建/工具/依赖 |
| `perf` | 性能优化 |
| `ci` | CI/CD |

---

## 三、代码规范（要点）

**后端**（完整分层示例见 [ARCHITECTURE.md](ARCHITECTURE.md)）：

- 分层：`Controller（薄路由）→ Service 接口 → ServiceImpl → Mapper`，Controller 禁止直连 Mapper、禁止写业务逻辑
- 注入：一律构造器注入（`@RequiredArgsConstructor`），注入接口而非实现类
- 事务：`@Transactional(rollbackFor = Exception.class)` 加在 ServiceImpl 方法上
- 异常：业务错误抛 `BusinessException`（由 `GlobalExceptionHandler` 统一转 HTTP 响应），禁止吞异常
- 状态流转：订单状态变更必须走 `OrderStatus` 枚举状态机 + `guardTransition()` 防御

**前端**：

- Vue 3 `<script setup>` Composition API，组件 PascalCase，组合式函数 `useXxx`
- 请求一律走 `src/api/` 模块（axios 拦截器统一注入 token / 弹出错误提示，业务代码中 `catch {}` 即可）
- 颜色/间距/圆角/阴影使用 `src/assets/style.css` 的设计 token（CSS 变量），禁止新增硬编码色值
- 路由鉴权由父路由 `meta.requiresAuth` 声明（`'user' | 'driver' | 'admin'`），子路由自动继承

**安全红线**：

- 任何密钥/密码只能通过环境变量注入（`${ENV_VAR:}` 占位），禁止出现在可提交文件中
- 含个人身份信息的文件禁止入库（`.gitignore` 已覆盖 `实习日志/`、`实习材料/`、`.env*`）

---

## 四、测试与验证

提交前必须全绿：

```bash
# 后端全量测试（自动加载 deploy/.env；需本机 MySQL/Redis 已启动）
powershell -File scripts/test-backend.ps1

# 前端测试 + 构建
cd jiangnan-travel-web && npm test && npm run build
```

- 纯单元测试（无需数据库）：`mvn test -Dtest="OrderStateMachineTest,TravelInsightServiceTest"`
- API 级 E2E 测试（需前后端已启动）：`node tests/test-suite.mjs`，说明见 [tests/TEST_GUIDE.md](tests/TEST_GUIDE.md)
- 新功能必须附带有意义的测试（验证值/结构/副作用/错误类型，而不是只测"有返回"）

CI（GitHub Actions）会在 push/PR 时自动运行后端测试（含 MySQL/Redis 服务容器）与前端测试+构建，见 [.github/workflows/ci.yml](.github/workflows/ci.yml)。

---

## 五、Pull Request

1. 从 `main` 拉出 `feature/xxx` 或 `fix/xxx` 分支
2. 完成开发并确保第四节的验证全绿
3. PR 描述写清：动机、改动点、测试方式；关联相关 issue
4. 数据库结构变更必须同步 `jiangnan-travel/src/main/resources/sql/init.sql`
5. API 变更必须同步前端 `src/api/` 模块与 [ARCHITECTURE.md](ARCHITECTURE.md) 的接口清单

---

## 六、AI 辅助开发

本项目支持并鼓励 AI 辅助开发（项目本身即为人机协作产物，实践记录见 [docs/ai-development-log.md](docs/ai-development-log.md)）。AI 代理的行为约束与项目上下文入口见 [AGENTS.md](AGENTS.md)——使用 Claude Code、Cursor 等工具开发时请让代理先读取该文件。
