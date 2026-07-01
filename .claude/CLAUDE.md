# 江南出行智慧服务平台 — 项目级配置

> 全局 CLAUDE.md 规则 + 本项目专属自动绑定，同时生效。

## 技术栈自动识别

| 层级 | 技术 | 自动绑定 |
|------|------|---------|
| 后端框架 | Spring Boot 3.2.6 / Java 17 | S8, S9, S10, S15 |
| ORM | MyBatis-Plus 3.5.7 | S11 |
| 数据库 | MySQL 8.0 | S6, S7, M3 |
| 缓存 | Redis + Redisson 3.32.0 | — |
| 前端 | Vue 3.4 + Vite 5 | S12, S13, S14 |
| UI 库 | Element Plus 2.7 | — |
| AI | DeepSeek API | S20 |
| 地图 | 高德地图 | — |
| 安全 | Spring Security + JWT | A4 |
| 文档 | Knife4j (Swagger) | — |

## 项目目录结构

```
软件工程2307班实习材料/
├── jiangnan-travel/          # Spring Boot 后端
│   └── src/main/java/com/jiangnan/travel/
│       ├── controller/       # 22 个 Controller
│       ├── service/          # 21 个 Service 接口
│       ├── mapper/           # 29 个 Mapper
│       ├── entity/           # 30 个 Entity
│       └── config/           # 7 个 Config
├── jiangnan-travel-web/      # Vue 3 前端
│   └── src/
│       ├── views/            # 41 个页面
│       ├── api/              # 13 个 API 模块
│       └── composables/      # 2 个 Composable
├── tests/                    # 20 个 E2E 测试脚本
└── deploy/                   # Docker + Nginx 部署
```

## 项目专属规则

1. **后端改了 API** → 同步更新前端 `api/` 模块 + Knife4j 文档
2. **数据库改了表** → 同步更新 `sql/init.sql` + Entity + Mapper
3. **前端新增页面** → 在 `router/` 注册路由 + 三端鉴权守卫
4. **任何改动** → 3 步以上的任务每步汇报（规则 #10）
5. **实习材料（doc/docx）** → 只读，不修改
