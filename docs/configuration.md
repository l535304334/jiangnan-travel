# 配置与环境变量说明

> 唯一事实源：`jiangnan-travel/src/main/resources/application.yml`（默认）与 `application-prod.yml`（生产 profile）。
> 所有凭据通过环境变量注入，代码与配置文件中不出现真实密钥。

---

## 一、后端环境变量总表

| 变量 | 默认值 | 生效范围 | 说明 |
|------|--------|---------|------|
| `DB_HOST` | `localhost` | 全部 | MySQL 主机 |
| `DB_PORT` | `3306` | 全部 | MySQL 端口 |
| `DB_NAME` | `smart_travel` | 全部 | 业务库名 |
| `DB_USER` | `root` | 全部 | 数据库用户 |
| `DB_PASS` | 空（必填） | 全部 | 数据库密码 |
| `REDIS_HOST` | `localhost` | 仅 prod profile | Redis 主机（默认 profile 固定 localhost） |
| `REDIS_PORT` | `6379` | 仅 prod profile | Redis 端口 |
| `JWT_SECRET` | 空（必填） | 全部 | JWT 签名密钥，HS256 要求 ≥256 bit（≥32 字符） |
| `DEEPSEEK_API_KEY` | 空 | 全部 | DeepSeek AI 密钥；未配置时 AI 客服自动走离线知识库兜底 |
| `AMAP_WEB_API_KEY` | 空 | 全部 | 高德 Web 服务 Key（后端路径规划） |
| `AMAP_JS_API_KEY` | 空 | 全部 | 高德 JS API Key |
| `AMAP_SECURITY_CODE` | 空 | 全部 | 高德安全密钥 |
| `SPRING_PROFILES_ACTIVE` | 默认 profile | 部署 | 生产部署设为 `prod` |

注入方式：

- **本地开发/测试**：写入 `deploy/.env`（已被 `.gitignore` 排除），后端测试用 `scripts/test-backend.ps1` 自动加载
- **Docker 部署**：`deploy/docker-compose.yml` 通过 `env_file` 注入
- **CI**：`.github/workflows/ci.yml` 的 `env` 块提供 CI 专用值

## 二、两个 profile 的差异

| 配置项 | 默认（开发） | prod |
|--------|-------------|------|
| MySQL SSL | `useSSL=false` + `allowPublicKeyRetrieval` | `useSSL=true&requireSSL=true` |
| Redis 地址 | 固定 `localhost:6379` | `${REDIS_HOST}:${REDIS_PORT}` |
| MyBatis SQL 日志 | stdout 全量打印 | 关闭 |
| Swagger / Knife4j | 开启（`/doc.html`） | 全部关闭 |
| 日志级别 | `com.jiangnan.travel: debug` | `info`，Spring 组件 `warn` |

## 三、关键固定配置（application.yml）

| 项 | 值 | 说明 |
|----|-----|------|
| 服务端口 | `8080` | 前端 dev 代理与 Nginx 反代目标 |
| JWT 过期 | `86400000` ms（24h） | 登出/改密即时失效由 Redis Token 黑名单实现 |
| Hikari 连接池 | min 5 / max 20 | |
| Spring Cache | Redis，TTL 10min，前缀 `jiangnan` | 缓存名：`carTypes` `landmarks` `cityQuotes` `hotDestinations` `dashboard` |
| MyBatis-Plus | 驼峰映射 + 逻辑删除（`deleted` 1/0） | mapper XML 位于 `resources/mapper/` |
| Jackson | `yyyy-MM-dd HH:mm:ss`，Asia/Shanghai，忽略 null 字段 | |

## 四、前端环境变量（`jiangnan-travel-web/.env`）

| 变量 | 说明 |
|------|------|
| `VITE_AMAP_KEY` | 高德 JS API Key（客户端公开性质，靠高德控制台域名白名单保护） |
| `VITE_AMAP_SECURITY_JS_CODE` | 高德安全密钥 |
| `VITE_API_BASE_URL` | API 基础路径，默认 `/api`（dev 由 Vite 代理到 `localhost:8080`） |
| `VITE_IMAGE_CDN_BASE` | 头像/图片生成 CDN 地址 |

模板：`jiangnan-travel-web/.env.example`。注意 `VITE_*` 变量会打进浏览器产物，**禁止**放入任何服务端密钥。

## 五、测试环境要求

后端 10 个测试类中 9 个为 `@SpringBootTest` 集成测试，要求：本机 MySQL（含已初始化的 `smart_travel` 库）+ Redis 运行中，且 `DB_PASS`、`JWT_SECRET` 已注入。**推荐统一入口**：

```bash
powershell -File scripts/test-backend.ps1
```

纯单元测试（无需任何外部依赖）：`OrderStateMachineTest`、`TravelInsightServiceTest`。
