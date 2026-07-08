---
name: springboot-verification
description: Spring Boot 后端代码改动后的自动化验证流程。在后端代码修改完成后自动触发，确认服务正常启动、API 端点可用、无编译错误。适用于本项目的 jiangnan-travel 后端工程。
metadata:
  triggers: Spring Boot 验证, 后端验证, 启动检查, API 检查, 编译验证
  scope: verification
  related-skills: java-architect, code-review-and-quality, test-driven-development
---

# Spring Boot 验证流程

在修改后端代码后，执行系统化验证确保改动正确生效。

## 项目信息

| 项目 | 技术栈 | 路径 |
|------|--------|------|
| 后端工程 | Spring Boot 3.2.6 + Java 17 + MyBatis-Plus 3.5.7 | `jiangnan-travel/` |
| 数据库 | MySQL 8.0 | localhost:3306 |
| 缓存 | Redis + Redisson 3.32.0 | — |
| API 数量 | 97 个端点，22 个 Controller | — |

## 验证流程

```
代码修改完成
    │
    ├── 1. 编译检查：mvn compile (无错误)
    │
    ├── 2. 单元测试：mvn test (全部通过)
    │
    ├── 3. 启动验证：mvn spring-boot:run (启动成功)
    │
    ├── 4. API 端点检查：访问 Knife4j /doc.html 确认端点正常
    │
    ├── 5. 功能验证：调用修改的 API 确认返回正确
    │
    └── 6. 安全检查：无凭据泄露、无 SQL 注入
```

## 关键验证命令

### Maven 项目

```bash
# 1. 编译检查
cd jiangnan-travel && mvn compile -q

# 2. 运行测试
mvn test

# 3. 启动服务
mvn spring-boot:run

# 4. 验证特定 API（curl）
curl -X GET http://localhost:8080/api/v1/health
curl -X GET http://localhost:8080/doc.html  # Knife4j 文档
```

### 验证清单

每次后端改动完成后，逐项确认：

- [ ] `mvn compile` 无错误
- [ ] `mvn test` 全部通过，无跳过的测试
- [ ] 应用正常启动（无 Bean 注入失败、配置缺失等启动异常）
- [ ] 新增/修改的 API 端点返回正确的 HTTP 状态码和数据格式
- [ ] Knife4j 文档自动更新（`/doc.html` 可访问）
- [ ] Controller → Service → Mapper 调用链路完整
- [ ] 如涉及数据库变更，同步更新 `sql/init.sql`
- [ ] 如涉及 API 变更，同步更新前端 `api/` 模块
- [ ] 无硬编码凭据
- [ ] 分页查询返回正确总数
- [ ] 错误处理返回统一格式 `Result<T>`

## 常见启动问题排查

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| Bean 注入失败 | 缺少 `@Component`/`@Service` 或 Mapper 未扫描 | 检查注解和 `@MapperScan` 配置 |
| 数据库连接失败 | application.yml 配置错误或 MySQL 未启动 | 检查连接配置和 MySQL 服务状态 |
| 端口被占用 | 8080 端口已被使用 | `netstat -ano | findstr 8080` 查占用 |
| Redis 连接失败 | Redis 未启动或配置错误 | 检查 Redis 服务和连接地址 |
| JWT 解析失败 | Secret 未配置或格式错误 | 检查环境变量或配置文件 |

## 约束

### MUST DO
- 修改后端代码后必须执行编译检查
- API 变更必须同步更新前端 API 模块
- 数据库变更必须同步更新 SQL 初始化脚本
- 启动验证确认所有 Bean 正常加载
- 功能验证覆盖修改的 API 端点

### MUST NOT DO
- 跳过测试直接提交
- 忽略编译警告（尤其是弃用 API 警告）
- 修改 API 不问前端是否需要同步更新

## 验证输出

完成后输出简短的验证报告：

```
✅ 编译通过
✅ 测试通过 (X/X)
✅ 启动成功 (端口 8080)
✅ API 端点正常
⚠ 建议: [如有]
```
