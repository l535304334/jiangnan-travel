# 全站 UI 升级执行计划

> 范围：管理后台（/admin/*）+ 认证页 + 乘客端 + 司机端
> 目标：统一水墨江南设计 Token，规范卡片/表格/按钮/页面头部，补齐管理后台退出登录与个人资料，提升整体视觉一致性。

---

## 一、现状分析

| 模块 | 现状 | 主要问题 |
|---|---|---|
| 设计 Token | `style.css` 已定义墨绿色系、金色强调、圆角、阴影、间距等 Token | 部分页面仍写死 `#fff`、固定 `12px` 圆角、自定义渐变，未完全复用 Token |
| 管理后台布局 | `AdminLayout.vue` 已改为墨绿渐变侧边栏、顶部面包屑、管理员下拉菜单 + 侧边栏底部退出登录 | 基本可用，但缺少真正的「个人资料」页面；各子页面表格/分页/卡片风格不统一 |
| 管理后台页面 | Dashboard 指标卡片为自定义 div；Users/Orders/Drivers/Alerts/CarTypes 使用 `border`+`stripe` 表格；Campaigns/VipLevels/BusLines 使用 `size="small"` 表格且无统一卡片容器 | 表格边框、分页容器、loading 覆盖、按钮尺寸不一致 |
| 认证页 | `Login.vue` 已使用部分 Token；`Register.vue` 待确认 | 输入框、tab 切换、卡片阴影可进一步统一 |
| 乘客端 | `Layout.vue` 顶部栏、底部 tab 已可用；`Home.vue`/`Profile.vue` 等页面各自实现卡片 | 圆角、阴影、颜色不统一；部分页面直接写死样式；底部 tab 未与安全区完全适配 |
| 司机端 | `DriverLayout.vue` 只有在线开关，无用户菜单；页面复用部分 Token 类 | 缺少退出/切换账号入口；头部信息层级可优化 |

关键依赖：
- `style.css` 是唯一的全局样式源。
- `useUserStore().logout()` 已清除 `token/userInfo/adminToken/adminInfo/driverToken/driverInfo`。
- 路由守卫 `router/index.js` 已区分 admin/driver/user 三态鉴权。
- 请求拦截器 `request.js` 目前只自动附加 `token`，管理员接口实际依赖后端以 `token` 同时承载管理员身份（已验证 API 测试通过）。

---

## 二、升级策略

1. **先基础后页面**：先在 `style.css` 补充移动端/管理端共享类，再改造各页面。
2. **先管理后台后 C 端**：管理后台是用户直接痛点，优先完成并验证；再扩展登录页、乘客端、司机端。
3. **组件化但不新增过度抽象**：不创建大量新组件，优先使用 `.card`、`.app-page`、`.app-section`、`.app-list-item` 等 CSS 工具类，保持改动轻量、可回滚。
4. **每改完一个模块即测试**：按 Task 拆分，每个 Task 完成后运行对应页面验证 + `npm run build` 前置检查。

---

## 三、具体任务清单

### Task 1 — 全局样式与共享类补齐

**目标**：让所有页面有统一可复用的容器/卡片/列表/头部类。

**修改文件**：`jiangnan-travel-web/src/assets/style.css`

**内容**：
1. 在「十二、管理后台共享样式」之后新增「十三、移动端共享样式」：
   - `.app-page`：统一内边距 `padding: var(--spacing-md)`，避免各页面自己写 `padding`。
   - `.app-section`：统一上下间距 `margin-bottom: var(--spacing-lg)`。
   - `.app-section-title`：复用 `.section-title` 但适配移动端，字号 `var(--font-size-h3)`，字重 600。
   - `.app-card`：复用 `.card` 但移除 hover 上移（移动端不友好）。
   - `.app-list-item`：与 `.card-list-item` 一致，但去掉 cursor:pointer 默认。
2. 统一覆盖 `el-tab-bar` / `el-tabs__active-bar` 颜色为主色。
3. 确保 `body.admin-layout-open` 已存在（已存在）。
4. 修复移动端底部固定 tab 遮挡内容：检查 `.app-layout` 的 `padding-bottom: 60px` 是否足够（已存在）。

**验收**：新增类能在 `Home.vue`、`Profile.vue`、`DriverHome.vue` 中直接替换原有硬编码样式。

---

### Task 2 — 管理后台：个人资料页 + 退出登录完善

**目标**：补齐顶部下拉菜单「个人资料」的真实页面，并确保退出登录后三态 token 全清。

**修改文件**：
- `jiangnan-travel-web/src/views/AdminProfile.vue`（新建）
- `jiangnan-travel-web/src/views/AdminLayout.vue`
- `jiangnan-travel-web/src/router/index.js`

**内容**：
1. 新建 `AdminProfile.vue`：
   - 页面头部：`admin-page-header` + `<h3>个人资料</h3>`。
   - 主体使用 `el-card` 包裹只读表单：
     - 管理员昵称（从 `adminInfo.name` 读取）
     - 用户名/账号（从 `adminInfo.username` 读取，若无则显示 `-`）
     - 角色标签（`el-tag type="danger"` 显示「超级管理员」）
     - 登录时间（取 `adminInfo.loginTime` 或本地 `Date`）
   - 底部提供「退出登录」按钮，调用 `userStore.logout()` 并跳转 `/login`。
2. `AdminLayout.vue`：将下拉菜单 `command="profile"` 的处理从 `ElMessage.info('个人资料功能开发中')` 改为 `router.push('/admin/profile')`。
3. `router/index.js`：在 `/admin` children 中新增 `{ path: 'profile', name: 'AdminProfile', component: () => import('@/views/AdminProfile.vue'), meta: { title: '个人资料' } }`，并加入 `adminAuthRoutes` 列表。

**验收**：管理员登录后，顶部下拉菜单点击「个人资料」可进入新页面；点击退出登录后 `localStorage` 中 `adminToken/adminInfo` 被清空并回到登录页。

---

### Task 3 — 管理后台：统一所有列表页与 Dashboard

**目标**：让所有 admin 子页面遵循「页面头部 + el-card 包裹表格 + 统一分页 + 统一按钮尺寸」的规范。

**修改文件**：
- `jiangnan-travel-web/src/views/AdminDashboard.vue`
- `jiangnan-travel-web/src/views/AdminUsers.vue`
- `jiangnan-travel-web/src/views/AdminOrders.vue`
- `jiangnan-travel-web/src/views/AdminDrivers.vue`
- `jiangnan-travel-web/src/views/AdminAlerts.vue`
- `jiangnan-travel-web/src/views/AdminCarTypes.vue`
- `jiangnan-travel-web/src/views/AdminCampaigns.vue`
- `jiangnan-travel-web/src/views/AdminVipLevels.vue`
- `jiangnan-travel-web/src/views/AdminBusLines.vue`

**内容**：
1. **AdminDashboard.vue**
   - 4 个指标卡片由自定义 `dash-card` 改为 `el-card`，保留顶部彩色边框（使用 `:style` 动态设置 `border-top`），图标背景色改用 Token 变量或保留原色。
   - 图表区两个图表由自定义 `div.card` 改为 `el-card`。
   - 指标数字字号使用 `var(--font-size-display)`，标签使用 `var(--font-size-body-small)`。
2. **Users / Orders / Drivers / Alerts / CarTypes**
   - 页面标题已存在 `admin-page-header`，保持不变。
   - 将 `el-table` 整体用 `el-card` 包裹。
   - 表格属性统一为 `stripe :border="false" size="default"`（去掉原 `border`）。
   - 操作列按钮统一 `size="small"`。
   - 分页容器统一使用 `admin-pagination-wrap`（已存在 CSS）。
   - Orders 的状态筛选器放到 `admin-page-toolbar`（若未使用）或直接保留在头部右侧。
3. **Campaigns / VipLevels / BusLines**
   - 将 `v-loading` 从外层 div 移到 `el-card` 上，保证 loading 覆盖卡片。
   - 表格同样改为 `el-card` 包裹、`stripe :border="false"`。
   - 分页统一为 `admin-pagination-wrap` + `layout="total, prev, pager, next, jumper"`。
   - 弹窗表单按钮尺寸统一 `size="default"`。
4. **BusLines.vue 班次弹窗**：内部表格同样改为无 border + stripe。

**验收**：所有 admin 子页面在 1920/1440/768 宽度下表格不溢出；分页容器位置一致；无红色报错。

---

### Task 4 — 认证页视觉统一

**目标**：让登录/注册页风格与管理后台水墨江南主题一致。

**修改文件**：
- `jiangnan-travel-web/src/views/Login.vue`
- `jiangnan-travel-web/src/views/Register.vue`

**内容**：
1. `Login.vue`
   - 背景渐变改用 `var(--color-primary-bg)`、`var(--color-bg)`、`var(--color-accent-light)` 组合，降低饱和度。
   - 输入框统一使用 `size="large"` 或保持默认高度 40px。
   - tab 切换 `.login-tabs` 使用 Token 边框色与主色。
   - 卡片标题颜色使用 `var(--color-primary-dark)`。
   - 测试账号区域改用 `.card` 类（背景白色、圆角、阴影）。
2. `Register.vue`
   - 复用 Login 的卡片结构与背景色。
   - 统一按钮、输入框、表单间距。

**验收**：登录页、注册页渲染正常；管理员账号可正常登录并跳转 `/admin/dashboard`。

---

### Task 5 — 乘客端布局与核心页面统一

**目标**：让乘客端头部、底部 tab、首页、个人中心等核心页面风格统一。

**修改文件**：
- `jiangnan-travel-web/src/views/Layout.vue`
- `jiangnan-travel-web/src/views/Home.vue`
- `jiangnan-travel-web/src/views/Profile.vue`
- 其他乘客页面：`OrderList.vue`、`OrderCreate.vue`、`OrderDetail.vue`、`AddressManage.vue`、`CouponCenter.vue`、`InvoiceCenter.vue`、`InvoiceApply.vue`、`MessageCenter.vue`、`CampaignList.vue`、`CampaignDetail.vue`、`VipCenter.vue`、`BusLine.vue`、`AiAssistant.vue`、`TripTracking.vue`、`SecuritySettings.vue`、`AboutCompany.vue`、`LandmarkExplore.vue`、`QuoteWall.vue`

**内容**：
1. **Layout.vue**
   - 顶部栏右侧下拉菜单增加头像图标（与管理员一致），使用 `UserFilled`。
   - 底部 tab active 颜色已使用 `var(--color-primary)`，保持不变。
   - 调整 `app-header` 高度与管理员顶部栏一致（56px）或保持 50px；选择保持 50px 以不破坏移动端习惯。
2. **Home.vue**
   - 用户问候区、数据看板、快捷功能卡片统一使用 `.app-card`。
   - 移除写死的 `background: #fff` 与 `border-radius: 12px`，改用 Token。
   - 活动中心/VIP/班线入口的渐变保持当前语义色，但文字与阴影改用 Token。
3. **Profile.vue**
   - 头部已使用墨绿渐变，保持。
   - 菜单列表容器改用 `.app-card`/`card` 工具类，统一圆角与阴影。
   - 退出登录按钮使用 `.btn-full` 或保持宽度 100%。
4. **其他乘客页面**
   - 对每个页面做轻量改造：
     - 外层包裹 `<div class="app-page">`（若还没有统一内边距）。
     - 卡片/列表项尽量替换为 `.app-card` / `.app-list-item`。
     - 按钮统一使用 Element Plus 默认尺寸或 `size="large"`（保持现有主要 CTA）。
     - 标题统一为 `.app-section-title`。
   - 不重构业务逻辑，仅调整视觉层 class 与少量内联样式。

**验收**：乘客端首页、个人中心、订单列表、下单页在浏览器 375/414/768 宽度下无错位；颜色风格一致。

---

### Task 6 — 司机端布局与页面统一

**目标**：给司机端增加退出/切换账号入口，并统一页面视觉。

**修改文件**：
- `jiangnan-travel-web/src/views/DriverLayout.vue`
- `jiangnan-travel-web/src/views/DriverHome.vue`
- `jiangnan-travel-web/src/views/DriverEarnings.vue`
- `jiangnan-travel-web/src/views/DriverProfile.vue`
- `jiangnan-travel-web/src/views/DriverOrder.vue`

**内容**：
1. **DriverLayout.vue**
   - 在头部右侧增加司机头像下拉菜单，提供「切换账号」「退出登录」。
   - 在线开关保持现状。
   - 头部标题与乘客端/管理后台对齐，使用 `var(--font-size-h3)` 与粗体。
2. **DriverHome.vue / DriverEarnings.vue / DriverProfile.vue / DriverOrder.vue**
   - 外层使用 `.app-page`。
   - 统计卡片复用 `.card-stat`。
   - 订单列表项复用 `.card-list-item`。
   - 主要操作按钮统一 `size="large"` 与主色。

**验收**：司机端可正常切换在线状态；顶部下拉菜单可退出登录并回到登录页；页面无样式异常。

---

### Task 7 — 构建验证与回归测试

**目标**：确保升级后项目能正常构建，且核心链路可用。

**步骤**：
1. 在前端目录运行 `npm run build`，修复所有 TypeScript/Vite/ESLint 报错。
2. 启动后端（已在运行则跳过）与前端的 dev server。
3. 使用浏览器验证：
   - 管理后台：登录 → 数据大屏 → 用户管理 → 订单监控 → 司机审核 → 风控告警 → 定价管理 → 活动管理 → VIP 等级 → 班线管理 → 个人资料 → 退出登录。
   - 乘客端：登录 → 首页 → 个人中心 → 订单列表 → 退出登录。
   - 司机端：登录 → 首页 → 我的 → 退出登录。
4. 运行全量 API 回归脚本 `comprehensive_api_test.mjs`，确认后端接口未因前端改动受影响。

---

## 四、假设与决策

1. **不修改后端接口**：本次只做前端样式与交互，不调整 API 路径、字段、权限。
2. **主题色沿用现有墨绿 + 金色**：用户未提出换主题，仅在现有 Design System v2.0 基础上做规范化。
3. **不新增通用 Vue 组件**：优先用 CSS 工具类降低侵入性；若后续页面增多再考虑抽组件。
4. **管理后台个人资料为只读**：不开放修改管理员信息，避免涉及后端管理员更新接口。
5. **乘客端/司机端改造为轻量规范化**：不做大布局重构，保留底部 tab 导航与顶部栏结构。
6. **若某页面业务逻辑复杂导致样式改造风险高**，将先向用户确认再改动。

---

## 五、风险与回退

| 风险 | 应对措施 |
|---|---|
| 全站改动面大，容易影响现有功能 | 按 Task 拆分，每 Task 完成后即测试；保留 Git 工作区，便于回退 |
| 乘客端页面众多，逐一检查耗时 | 先改核心页面，再批量替换通用 class；遇到复杂页面单独确认 |
| 管理后台表格去掉 border 后辨识度下降 | 通过表头底色、斑马纹、hover 色保证可读性 |
| 司机端新增下拉菜单与在线开关位置冲突 | 小屏时采用图标按钮，保留足够点击区域 |

---

## 六、预期成果

- 管理后台：每个页面都有统一页面头部、卡片容器、无框表格、统一分页；顶部/侧边栏均可退出登录；新增个人资料页。
- 认证页：视觉与主站一致，输入/按钮/卡片统一。
- 乘客端：首页、个人中心等核心页面卡片/列表/按钮风格统一，视觉层级清晰。
- 司机端：头部具备退出/切换账号入口，页面复用统一卡片类。
- 项目通过 `npm run build`，核心功能经浏览器验证无异常。
