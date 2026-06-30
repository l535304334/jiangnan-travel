# 管理后台 UI 升级计划

## 背景与目标

当前管理后台（`AdminLayout.vue`）存在以下问题：
- **没有退出登录入口**，管理员无法主动退出
- 深色侧边栏（`#1A1A2E`）与消费者端/司机端的墨绿渐变风格不一致
- 顶部 header 信息层级弱，右侧只有静态 tag
- 管理页面之间视觉风格不够统一（表格、卡片、按钮等细节参差）

本次升级目标：
1. 增加安全、明显的**退出登录**按钮
2. 将管理后台视觉语言统一到项目已有的「水墨江南 + 现代极简」设计体系
3. 改善侧边栏、顶部栏、内容区的信息层级与组件质感
4. 统一各管理页面的卡片、表格、按钮、分页等样式

---

## 升级范围

| 优先级 | 内容 | 说明 |
|---|---|---|
| P0 | `AdminLayout.vue` 重构 | 新侧边栏、新顶部栏、加入退出登录、响应式适配 |
| P0 | `style.css` 设计 Token 更新 | 管理后台颜色、表格、卡片等共享样式 |
| P0 | `stores/user.js` logout 增强 | 统一清除所有身份态（user/admin/driver） |
| P1 | `AdminDashboard.vue` 样式统一 | 指标卡片、图表卡片风格统一 |
| P1 | `AdminUsers.vue` / `AdminOrders.vue` | 作为列表页模板标准化 |
| P2 | 其余 admin 列表页 | 按相同模板微调（Drivers/Alerts/CarTypes/Campaigns/VipLevels/BusLines）|
| 不改动 | `Layout.vue` / `DriverLayout.vue` | 二者已使用墨绿渐变与现有设计 Token，保持现状 |

---

## 具体方案

### 1. AdminLayout.vue 重构

#### 结构
- 保持「左侧 sidebar + 右侧内容区」两栏布局
- 顶部 header：左侧为「页面标题 + 面包屑」，右侧为管理员下拉菜单（含退出登录）
- 侧边栏底部：保留管理员信息，并增加退出图标按钮（折叠态可用）
- 折叠按钮保留，样式与新侧边栏协调

#### 脚本
- 引入 `useRouter`、`useUserStore`、`ArrowDown` 图标
- `onMounted` 给 `document.body` 添加 `admin-layout-open`
- `onUnmounted` 移除 `admin-layout-open`
- 新增 `handleLogout`：调用 `userStore.logout()`，跳转 `/login`，并提示「已安全退出」

#### 样式
- 侧边栏背景改为墨绿渐变：`linear-gradient(180deg, var(--color-primary-dark), var(--color-primary))`
- 菜单项 hover 使用半透明白色背景
- active 菜单使用金色左边框 + 半透明金色背景
- header 增加细微阴影，背景白色
- 主内容区背景使用 `--color-bg`

### 2. style.css 更新

- 管理后台 Token 从深蓝系改为墨绿系
- 新增/完善 `.admin-page-header`、`.admin-page-toolbar`、`.admin-pagination-wrap`
- 增加 `el-table` 全局轻量覆盖：表头底色、斑马纹行背景
- 为 `body.admin-layout-open` 补充背景色，确保后台铺满全屏

### 3. stores/user.js 增强

```js
const logout = () => {
  token.value = ''
  userInfo.value = null
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminInfo')
  localStorage.removeItem('driverToken')
  localStorage.removeItem('driverInfo')
}
```

### 4. 管理页面统一

- 所有列表页补齐 `.admin-page-header`（左标题，右操作/筛选）
- 表格外层用 `el-card` 包裹，使用 `stripe`、去掉 `border`
- 分页统一使用 `.admin-pagination-wrap`
- Dashboard 指标卡片统一使用项目主色/功能色，图表卡片使用 `.card`/`el-card`

---

## 退出登录行为

- **位置 1**：顶部 header 右侧管理员下拉菜单
- **位置 2**：侧边栏底部管理员信息区（折叠态显示为图标）
- **行为**：清除所有 localStorage 身份态 → 提示「已安全退出」→ 跳转 `/login`
- **兼容**：路由守卫已使用 `clearAllAuth()`，退出后访问 admin 路由会正确重定向

---

## 跨端风格统一

- **色彩**：管理后台侧边栏、消费者/司机端 header 均使用同一墨绿渐变体系
- **强调色**：active 状态、重要徽章统一使用暖金 `--color-accent`
- **圆角与阴影**：所有端复用 `--radius-*` 与 `--shadow-*`
- **字体与间距**：管理后台标题、正文全部使用现有字号/间距 Token
- **动画**：页面切换复用 `page-fade`，hover/折叠使用 `--transition-fast/normal`

---

## 验证计划

1. 启动前端 dev server
2. 访问 `/admin/dashboard`：确认新侧边栏、header 样式
3. 切换各 admin 菜单：确认 active 状态、页面切换正常
4. 折叠/展开侧边栏：确认图标不偏移、功能可用
5. 点击退出登录：确认 localStorage 清空并跳转到登录页
6. 退出后访问 `/admin/users`：确认路由守卫拦截并重定向
7. 管理员重新登录：确认回到 dashboard
8. 检查消费者端 `/home` 与司机端 `/driver/home`：布局不受 `admin-layout-open` 影响
9. 检查各 admin 列表页：表格表头底色、斑马纹、卡片容器
10. 运行 `npm run build` 确认无构建错误

---

## 关键修改文件

- `jiangnan-travel-web/src/views/AdminLayout.vue`
- `jiangnan-travel-web/src/assets/style.css`
- `jiangnan-travel-web/src/stores/user.js`
- `jiangnan-travel-web/src/views/AdminDashboard.vue`
- `jiangnan-travel-web/src/views/AdminUsers.vue`
- `jiangnan-travel-web/src/views/AdminOrders.vue`
