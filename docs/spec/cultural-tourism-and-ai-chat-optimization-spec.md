# 文旅推荐 + AI 客服优化 Spec

> 日期：2026-06-24
> 项目：江南出行（Jiangnan Travel）
> 版本：v1.0

---

## 1. 文旅推荐优化

### 1.1 Home 页地标展示增强

**现状**：Home 页"江西文旅地标"仅显示地标名称和城市名，缺少图片、描述等丰富信息。

**优化方案**：
- 地标卡片改为**卡片式布局**，包含：
  - 图片（`CityLandmark.imageUrl`，若无则显示占位图）
  - 地标名称（加粗）
  - 城市名
  - 描述（截取前 30 字 + "..."）
- 点击卡片弹出 `ElDialog` 详情对话框，展示完整信息：
  - 大图（400x300）
  - 完整描述
  - 城市 + 地址标签

### 1.2 地标搜索

**现状**：后端 `GET /api/landmark/search?keyword=` 已存在，前端未接入。

**优化方案**：
- Home 页文旅区域上方添加搜索框
- 输入关键词后调用 `searchLandmarks(keyword)`
- 显示搜索结果列表，点击同现有地标卡片行为
- 支持清空搜索回到全部地标视图

### 1.3 TripTracking 引用优化

**现状**：`TripTracking.vue` 展示 CityQuote 内容，但仅显示纯文本内容。

**优化方案**：
- 引用卡片增加作者信息（`—— {author}`）
- 添加地区标签（`[city]` 标签）
- 轮播间隔调整为 8 秒
- 引用卡片背景添加渐变样式（浅色渐变背景）

---

## 2. AI 客服优化

### 2.1 多轮对话上下文

**现状**：`AiChatServiceImpl.chat()` 仅发送当前用户消息，不包含历史消息，DeepSeek 无法感知对话上下文。

**优化方案**：
- 在 `chat()` 方法中，从 `t_ai_chat_log` 查询同 `sessionId` 的最近 20 条消息
- 按时间排序后，以 `addHistoryMessage(role, content)` 形式传入 DeepSeek
- 第一条消息之后才发送用户消息
- 保持系统提示词（含文旅知识）作为第一条 system 消息

**时序**：
```
System(文旅知识) → History(msg1) → History(msg2) → ... → User(当前消息) → Assistant(回复)
```

### 2.2 SSE 流式回复

**现状**：`POST /api/ai/chat` 返回完整回复，等待时间较长。

**优化方案**：
- 新增 `POST /api/ai/chat/stream` 端点
- 后端使用 Spring `SseEmitter`（超时 60 秒）
- 调用 OpenAI Java SDK 的 `createStreaming()` 获取 `Stream<ChatCompletionChunk>`
- 每个 chunk 的 `delta.content` 通过 `SseEmitter.send()` 推送
- 推送完毕后调用 `SseEmitter.complete()`
- 异常时 `SseEmitter.completeWithError()`

**前端**：
- 使用浏览器原生 `EventSource` 或 `fetch + ReadableStream`
- 收到每个 chunk 后追加到当前 assistant 消息中
- 保持流式拼字效果

### 2.3 前端对话历史会话管理

**现状**：`AiChatFloat.vue` 仅支持单次对话，不可选择历史会话。

**优化方案**：
- 在聊天面板中添加"历史会话"侧边栏（切换按钮）
- 点击后请求 `GET /api/ai/sessions?userId=` 返回会话列表
- 选择某会话后，加载该会话的全部消息记录
- 新消息默认创建新会话

**后端新增接口**：
- `GET /api/ai/sessions` — 获取用户的 sessionId 列表（按时间倒序）
- `GET /api/ai/sessions/{sessionId}/messages` — 获取某会话的全部消息

---

## 3. 接口变更清单

### 新增接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/ai/chat/stream` | SSE 流式对话 |
| GET | `/api/ai/sessions` | 获取用户会话列表 |
| GET | `/api/ai/sessions/{sessionId}/messages` | 获取会话消息 |

### 修改接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/ai/chat` | 增加多轮上下文（内部修改，不改变请求/响应格式） |

### 前端新增/修改

| 文件 | 改动 |
|---|---|
| `Home.vue` | 地标卡片增强 + 搜索框 |
| `TripTracking.vue` | 引用卡片美化 |
| `AiChatFloat.vue` | 流式支持 + 历史会话侧边栏 |
| `api/ai.js` | 新增 `chatStream()`, `getSessions()`, `getSessionMessages()` |

---

## 4. 文件变更清单

### 后端

| 文件 | 操作 | 说明 |
|---|---|---|
| `AiChatServiceImpl.java` | 修改 | 多轮上下文 + 流式方法 |
| `AiChatService.java` | 修改 | 新增 `chatStream()` 方法签名 |
| `AiChatController.java` | 修改 | 新增 SSE 端点 + 会话查询端点 |
| `CityLandmarkController.java` | 修改 | 无需改动 |

### 前端

| 文件 | 操作 | 说明 |
|---|---|---|
| `Home.vue` | 修改 | 地标卡片增强、搜索框 |
| `TripTracking.vue` | 修改 | 引用卡片美化 |
| `AiChatFloat.vue` | 修改 | 流式支持、历史会话 |
| `api/ai.js` | 修改 | 新增 3 个 API 方法 |

---

## 5. 验收标准

### 文旅推荐
- [ ] Home 页地标显示图片 + 描述
- [ ] 点击地标卡片弹出详情对话框
- [ ] 搜索框可筛选地标
- [ ] TripTracking 引用卡片带作者 + 城市标签

### AI 客服
- [ ] 多轮对话保持上下文（连续对话有连贯性）
- [ ] /chat/stream 返回 SSE 流式数据
- [ ] 前端逐字显示回复内容
- [ ] 可查看和切换历史会话
