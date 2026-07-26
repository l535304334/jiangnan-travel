# FAQ 与故障排查

> 覆盖本地启动、测试、部署三个场景的常见问题。环境变量详情见 [configuration.md](configuration.md)。

---

## 一、启动类问题

### 后端启动失败：`CannotCreateTransaction` / JDBC 连接失败

最常见原因是环境变量未注入。检查：

1. MySQL 是否在 3306 监听：`netstat -ano | findstr 3306`
2. `smart_travel` 库是否已初始化：`mysql -u root -p -e "USE smart_travel; SHOW TABLES;"`（应有 27 张表）
3. `DB_PASS` 是否已注入当前进程（IDE 运行配置 / `deploy/.env`）——**系统全局环境变量默认没有配置这些值**

### 后端启动失败：JWT 相关 Bean 报错 / `WeakKeyException`

`JWT_SECRET` 未注入或长度不足。HS256 要求密钥 ≥256 bit（≥32 字符）。

### Bean 注入失败（`NoSuchBeanDefinitionException`）

检查实现类是否有 `@Service`/`@Component`，Mapper 接口是否位于 `com.jiangnan.travel.mapper`（启动类 `@MapperScan` 覆盖范围）。

### 端口被占用

```bash
netstat -ano | findstr "8080 5173"
taskkill /PID <pid> /F
```

### 前端页面空白 / 接口全部 404

Vite dev 代理目标是 `http://localhost:8080`（见 `vite.config.js`）——确认后端已启动。生产环境由 Nginx 将 `/api` 反代到后端容器。

---

## 二、测试类问题

### 裸跑 `mvn test` 挂掉约 60 个测试

不是代码问题。9 个 `@SpringBootTest` 集成测试类需要活的 MySQL/Redis 与 `DB_PASS`/`JWT_SECRET` 环境变量。用统一入口：

```bash
powershell -File scripts/test-backend.ps1
```

只想快速验证逻辑时跑纯单元测试（零外部依赖）：

```bash
cd jiangnan-travel && mvn test -Dtest="OrderStateMachineTest,TravelInsightServiceTest"
```

### `node tests/test-suite.mjs` 全部失败

API 级 E2E 套件要求**前后端都已启动**（后端 8080、前端 5173），且数据库已含种子数据与测试账号。详见 [tests/TEST_GUIDE.md](../tests/TEST_GUIDE.md)。

---

## 三、功能类问题

### AI 客服没有智能回复

`DEEPSEEK_API_KEY` 未配置或调用失败时，系统自动降级为离线文旅知识库兜底——这是设计行为，不是故障。配置有效 Key 后恢复流式 AI 回复。

### 地图不显示 / POI 搜索无结果

前端 `VITE_AMAP_KEY` 与 `VITE_AMAP_SECURITY_JS_CODE` 未配置，或高德控制台的域名白名单不含当前访问域名。

### WebSocket 连不上（订单追踪 / 消息中心不实时）

- WebSocket 鉴权走 **Cookie**（登录时前端会写 `token` cookie，供握手时 `JwtCookieConfigurator` 读取）——直接用 IP+端口跨域访问时 Cookie 不会随握手发送，需通过同源代理（Vite dev 代理或 Nginx）访问
- 全局连接数上限 1000，超出会被拒绝

### 登录提示"登录已过期"但刚登录

三端 token 分别存储（`token` / `driverToken` / `adminToken`），跨端访问路由会按对应端校验。清空 localStorage 重新登录对应端即可。

### 支付/接单偶发"系统繁忙"

Redisson 分布式锁竞争超时（下单幂等锁 3s / 司机接单锁）——高并发下的保护行为，重试即可。若持续出现，检查 Redis 是否可用。

---

## 四、部署类问题

### docker-compose 起来后端起不来

按依赖顺序排查：MySQL 容器健康检查通过 → `deploy/.env` 已从模板复制并填写 → 后端容器日志 `docker logs <container>`。

### 生产环境访问 `/doc.html` 404

设计行为：prod profile 关闭了 Swagger/Knife4j（`knife4j.production=true`）。接口文档仅开发环境可用。

---

## 五、还没解决？

1. 后端日志（开发模式 `com.jiangnan.travel: debug` 会打印全量 SQL）
2. 浏览器控制台 + Network 面板（前端所有请求错误会经 axios 拦截器统一弹出提示）
3. 提交 issue 时附：复现步骤、后端日志片段、浏览器控制台截图
