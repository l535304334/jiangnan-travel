# 江南出行 — 生产部署指南

## 目录结构

```
deploy/
├── build.ps1                        # 构建脚本
├── docker-compose.yml                # Docker Compose 编排
├── .env                              # 环境变量
├── backend/
│   ├── Dockerfile                    # 后端 Docker 构建
│   ├── .dockerignore
│   └── jiangnan-travel-1.0.0-SNAPSHOT.jar  # (构建生成)
├── frontend/
│   ├── Dockerfile                    # 前端 Nginx Docker 构建
│   ├── nginx.conf                    # Nginx 配置
│   ├── .dockerignore
│   └── dist/                         # (构建生成)
```

## 部署方式

### 方式一：Docker Compose（推荐）

依赖: Docker + Docker Compose

```bash
cd deploy
docker compose up -d
```

| 服务 | 内部端口 | 对外端口 |
|------|---------|---------|
| MySQL | 3306 | 3307 |
| Redis | 6379 | 6380 |
| 后端 | 8080 | 8080 |
| 前端 | 80 | 80 |

访问: http://localhost

### 方式二：本地直接启动

依赖: Java 17 + MySQL 8.0 + Redis

1. 启动后端:
```bash
java -jar deploy/backend/jiangnan-travel-1.0.0-SNAPSHOT.jar
```

2. 启动前端:
```bash
cd jiangnan-travel-web && npm run dev
```

访问: http://localhost:5173

## 环境变量

> 所有敏感配置通过 `deploy/.env` 文件注入（参考 `.env.example`），**禁止在 docker-compose.yml 或文档中硬编码真实密码/API Key**。

| 变量 | 说明 |
|------|------|
| `SPRING_PROFILES_ACTIVE` | Spring Profile（默认 `prod`） |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码（从 .env 读取） |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASS` | 数据库连接参数 |
| `REDIS_HOST` / `REDIS_PORT` | Redis 连接参数 |
| `JWT_SECRET` | JWT 签名密钥（≥256 位） |
| `DEEPSEEK_API_KEY` | DeepSeek AI API Key |
| `AMAP_WEB_API_KEY` / `AMAP_JS_API_KEY` / `AMAP_SECURITY_CODE` | 高德地图配置 |

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.6 |
| Java | 17 |
| MySQL | 8.0 |
| Redis | 7.x |
| Vue | 3.4 |
| Vite | 5.x |
| Nginx | alpine |
