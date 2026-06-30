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

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring Profile |
| `DB_HOST` | `localhost` | 数据库地址 |
| `DB_PORT` | `3306` | 数据库端口 |
| `DB_NAME` | `smart_travel` | 数据库名 |
| `DB_USER` | `root` | 数据库用户 |
| `DB_PASS` | `Lai20050802@` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |

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
