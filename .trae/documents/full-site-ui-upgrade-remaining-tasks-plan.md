# 全站 UI 升级剩余任务执行计划（更新版）

> 承接此前全站 UI 升级工作，当前 Task 5（乘客端）与 Task 6（司机端）主体已完成，本计划聚焦**司机端页面精修**与**构建验证/回归测试**。
> 范围：司机端 4 个核心页面（DriverHome / DriverEarnings / DriverProfile / DriverOrder）的共享样式统一 + 全链路回归。

---

## 一、当前状态分析

| 任务 | 状态 | 说明 |
|---|---|---|
| Task 1 全局样式 | 已完成 | `style.css` 已定义 `.app-page/.app-section/.app-section-title/.app-card/.app-list-item` 及管理后台表格覆盖。 |
| Task 2 管理后台个人资料/退出 | 已完成 | `AdminProfile.vue` 已新建，`AdminLayout.vue` 下拉菜单与路由已调整。 |
| Task 3 管理后台列表页/Dashboard | 已完成 | Dashboard / Users / Orders / Drivers / Alerts / CarTypes / Campaigns / VipLevels / BusLines 已统一为 `el-card` 包裹。 |
| Task 4 认证页 | 已完成 | `Login.vue` / `Register.vue` 已统一背景、卡片、输入框风格。 |
| Task 5 乘客端 | 已完成 | `Layout.vue` / `Home.vue` / `Profile.vue` / `OrderList.vue` / `OrderCreate.vue` / `LandmarkExplore.vue` / `AiAssistant.vue` / `MessageCenter.vue` / `SecuritySettings.vue` / `AboutCompany.vue` / `QuoteWall.vue` 等已应用共享类并移除硬编码样式。 |
| Task 6 司机端 | 进行中 | `DriverLayout.vue` 已新增头像下拉菜单（切换账号/退出登录）并统一标题字号；`DriverHome.vue` / `DriverEarnings.vue` / `DriverProfile.vue` / `DriverOrder.vue` 外层已加 `.app-page`，但内部卡片/列表结构尚未完全统一。 |
| Task 7 构建验证与回归 | 未开始 | 需运行 `npm run build`、浏览器验证、API 回归。 |

关键约束：
- 不修改后端接口、数据库结构、API 路径。
- 仅做前端视觉层与交互入口的轻量规范化，不重构业务逻辑。
- 主题色沿用现有墨绿 + 金色 Design Token（`style.css`）。

---

## 二、待完成变更

### 2.1 Task 6 收尾 — 司机端页面精修

#### 2.1.1 `DriverHome.vue`

当前问题：
- 待接订单列表项使用自定义 `.card-list-item`（硬编码背景/圆角/阴影）。
- 「待接订单」标题为自定义 `.section-title`。

改造动作：
1. 将 `.section-title` 区块改为 `<div class="app-section"><h4 class="app-section-title">待接订单</h4>...`。
2. 将待接订单列表项的 `.card-list-item` 替换为 `.app-list-item`。
3. 保留 `.card-stat` 统计卡片样式（已独立定义，不冲突）。
4. 「开始接单」按钮保持 `size="large"` 与全宽。

预期文件变更：
- 模板：待接订单区块结构。
- 样式：移除 `.section-title` 与 `.card-list-item` 相关硬编码样式，必要时保留 `.order-route` / `.order-info` 文本样式。

#### 2.1.2 `DriverEarnings.vue`

当前问题：
- 「本周汇总」「近期订单」区块使用自定义 `.card`。
- 近期订单列表项使用自定义 `.order-item`（无统一卡片外观）。

改造动作：
1. 「本周汇总」容器改为 `.app-card`，标题改为 `.app-section-title`。
2. 「近期订单」容器改为 `.app-card`，标题改为 `.app-section-title`。
3. 近期订单列表项改为 `.app-list-item`。
4. 保留 `.today-stats` 内今日收入主卡片与统计卡片样式。
5. 移除 `.card` 的硬编码背景/圆角/阴影。

预期文件变更：
- 模板：两个区块容器与列表项类名。
- 样式：`.week-summary`、`.recent-orders`、`.order-item` 样式精简，仅保留布局与文本样式。

#### 2.1.3 `DriverProfile.vue`

当前问题：
- 评分行 `.rating-row` 使用 `.card`（硬编码）。
- 菜单列表 `.menu-list` 使用 `.card`（硬编码）。
- 菜单项为自定义 `.menu-item`。

改造动作：
1. 评分行 `.rating-row` 改为 `.app-card`。
2. 菜单列表 `.menu-list` 改为 `.app-card`。
3. 菜单项改为 `.app-list-item` 风格（或使用 `.app-list-item` 类并保留内部 flex 布局）。
4. 退出登录按钮保持 `type="danger"`、宽度 100%，与乘客端/管理后台风格一致。
5. 移除 `.card` 硬编码样式。

预期文件变更：
- 模板：评分行、菜单列表、菜单项类名。
- 样式：`.rating-row`、`.menu-list`、`.menu-item` 样式精简。

#### 2.1.4 `DriverOrder.vue`

当前问题：
- 路线区块 `.route-block` 使用 `.card`。
- 信息网格 `.info-grid` 的 `.info-item` 硬编码背景/圆角。
- 地图占位区 `.map-placeholder` 硬编码背景/圆角。
- 操作按钮区按钮尺寸未统一。

改造动作：
1. 路线区块 `.route-block` 改为 `.app-card`。
2. 信息网格整体作为 `.app-card` 内的列表，或每个 `.info-item` 改为 `.app-card` 风格。
3. 地图占位区改为 `.app-card`（保持内部 flex 居中）。
4. 操作按钮区使用 `.app-section` 包裹，按钮统一 `size="large"`。
5. 订单头部保持 flex 布局，可放入 `.app-section` 或保持现状。
6. 移除 `#fff`、自定义 `border-radius`、自定义阴影。

预期文件变更：
- 模板：路线、信息网格、地图占位、操作按钮区结构。
- 样式：`.route-block`、`.info-grid`、`.info-item`、`.map-placeholder`、`.action-btns` 样式精简。

---

### 2.2 Task 7 — 构建验证与回归测试

1. 在前端目录 `jiangnan-travel-web` 运行 `npm run build`，修复所有构建错误（TypeScript/Vite/ESLint）。
2. 启动/确认后端（`localhost:8080`）与前端 dev server（`localhost:5173`）正常运行。
3. 浏览器验证核心链路：
   - 管理后台：登录 → Dashboard → 各列表页 → 个人资料 → 退出登录。
   - 乘客端：登录 → 首页 → 个人中心 → 订单列表 → 退出登录。
   - 司机端：登录 → 首页 → 收入 → 我的 → 订单详情 → 退出登录（验证顶部下拉菜单）。
4. 运行 `comprehensive_api_test.mjs` 全量 API 回归，确认后端接口未受影响。

---

## 三、假设与决策

1. **主题与 Design Token 不变**：沿用 `style.css` 中已定义的墨绿 + 金色 Token，不引入新主题。
2. **不新增通用 Vue 组件**：继续使用 CSS 工具类（`.app-page/.app-card/.app-list-item/.app-section-title`）降低侵入性。
3. **司机端改造为轻量规范化**：不重构业务逻辑，仅替换容器与卡片类。
4. **司机端「切换账号」与「退出登录」行为一致**：已确认两者均调用 `userStore.logout()` 并跳转 `/driver/login`。
5. **不修改后端**：所有变更集中在前端视图层与全局样式。
6. **若某页面改造后出现布局异常**，优先通过局部样式微调解决，不回滚整体方案。

---

## 四、风险与回退

| 风险 | 应对措施 |
|---|---|
| 替换 `.card` 为 `.app-card` 后内边距差异导致错位 | 逐个页面浏览器验证，必要时在 scoped 样式中覆盖内边距 |
| `.app-list-item` 上下间距导致列表过长 | 利用 `.app-list-item + .app-list-item` 的间距规则，保持自然 |
| 构建阶段出现未预期错误 | 逐项修复，必要时回滚单个文件变更 |
| API 回归失败 | 因本次不改后端，若失败则优先检查前端是否误改了请求封装 |

---

## 五、验证标准

- [ ] `DriverHome.vue` 待接订单列表项使用 `.app-list-item`，标题使用 `.app-section-title`。
- [ ] `DriverEarnings.vue`「本周汇总」「近期订单」区块使用 `.app-card`，近期订单列表项使用 `.app-list-item`。
- [ ] `DriverProfile.vue` 评分行与菜单列表使用 `.app-card`，菜单项使用 `.app-list-item` 风格。
- [ ] `DriverOrder.vue` 路线、信息网格、地图占位区使用 `.app-card`，操作按钮统一 `size="large"`。
- [ ] `npm run build` 无错误。
- [ ] 浏览器验证管理后台/乘客端/司机端核心链路正常。
- [ ] `comprehensive_api_test.mjs` 全量通过。

---

## 六、执行顺序

1. `DriverHome.vue` 精修。
2. `DriverEarnings.vue` 精修。
3. `DriverProfile.vue` 精修。
4. `DriverOrder.vue` 精修。
5. `npm run build` 修复构建错误。
6. 浏览器验证管理后台 / 乘客端 / 司机端核心链路。
7. 运行 `comprehensive_api_test.mjs` 回归。
8. 输出开发报告，等待用户确认。
