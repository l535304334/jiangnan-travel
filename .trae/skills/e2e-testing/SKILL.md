---
name: e2e-testing
description: Playwright E2E 端到端测试。在需要验证关键用户流程、编写浏览器自动化测试、回归测试时触发。本项目已有 20 个 E2E 测试脚本，修改功能时必须同步更新。
metadata:
  triggers: E2E, Playwright, 端到端测试, 浏览器测试, 用户流程测试, 回归测试
  scope: testing
  related-skills: test-driven-development, code-review-and-quality, vue-frontend
---

# Playwright E2E 测试

端到端测试覆盖关键用户流程，确保系统从前端到后端的完整功能正确。

## 项目信息

| 项目 | 路径 | 测试数量 |
|------|------|---------|
| E2E 测试 | `tests/` | 20 个脚本 |
| 测试框架 | Playwright | Chromium 浏览器 |

## 核心工作流

1. **确定测试场景** — 识别关键用户流程
2. **编写测试脚本** — 使用 Playwright API
3. **本地执行** — `npx playwright test`
4. **分析失败** — 查看截图和 trace
5. **修复并更新** — 功能变更时同步更新测试

## 测试脚本模板

```javascript
import { test, expect } from '@playwright/test'

test.describe('用户登录流程', () => {
  test.beforeEach(async ({ page }) => {
    // 每个测试前访问首页
    await page.goto('http://localhost:5173/login')
  })

  test('使用正确的用户名和密码登录成功', async ({ page }) => {
    // Arrange: 填写登录表单
    await page.fill('input[name="username"]', 'admin')
    await page.fill('input[name="password"]', 'password123')

    // Act: 点击登录
    await page.click('button[type="submit"]')

    // Assert: 跳转到首页
    await expect(page).toHaveURL(/.*dashboard/)
    await expect(page.locator('.user-info')).toContainText('admin')
  })

  test('使用错误的密码登录失败', async ({ page }) => {
    await page.fill('input[name="username"]', 'admin')
    await page.fill('input[name="password"]', 'wrong')

    await page.click('button[type="submit"]')

    // 应显示错误提示
    await expect(page.locator('.el-message--error')).toBeVisible()
    // URL 不应跳转
    await expect(page).toHaveURL(/.*login/)
  })

  test('空表单提交显示验证错误', async ({ page }) => {
    await page.click('button[type="submit"]')

    // Element Plus 表单验证提示
    await expect(page.locator('.el-form-item__error')).toBeVisible()
  })
})
```

## 测试设计原则

### 覆盖范围

| 优先级 | 测试内容 | 示例 |
|--------|---------|------|
| P0 — 必须覆盖 | 登录/注册、核心业务流程 | 用户下单、司机接单 |
| P1 — 应该覆盖 | 管理功能、数据 CRUD | 后台管理增删改查 |
| P2 — 可以覆盖 | 边缘场景、异常流程 | 网络错误、超时处理 |

### 选择器策略

```javascript
// 优先级从高到低

// 1. data-testid (最稳定)
await page.click('[data-testid="login-btn"]')

// 2. 语义化角色 (推荐)
await page.click('button:has-text("登录")')
await page.fill('input[name="username"]', 'admin')

// 3. CSS 选择器 (稳定备选)
await page.click('.login-form button[type="submit"]')

// 4. 文本内容 (仅用于无其他选择器时)
await page.click('text=确认提交')
```

### 等待策略

```javascript
// BAD: 固定等待
await page.waitForTimeout(3000)  // 脆弱且慢

// GOOD: 确定性等待
await page.waitForSelector('.order-table')     // 等元素出现
await page.waitForURL('**/dashboard')          // 等 URL 变化
await expect(page.locator('.result')).toContainText('成功')  // 等文本出现
```

## 响应式测试

```javascript
test.describe('响应式布局', () => {
  const viewports = [
    { width: 320, height: 568, name: 'iPhone SE' },
    { width: 768, height: 1024, name: 'iPad' },
    { width: 1024, height: 768, name: '笔记本' },
    { width: 1440, height: 900, name: '桌面' },
  ]

  for (const viewport of viewports) {
    test(`首页在 ${viewport.name} (${viewport.width}px) 下正常显示`, async ({ page }) => {
      await page.setViewportSize(viewport)
      await page.goto('/')
      await expect(page.locator('h1')).toBeVisible()
      // 无水平滚动条
      const hasHorizontalScroll = await page.evaluate(() =>
        document.documentElement.scrollWidth > document.documentElement.clientWidth
      )
      expect(hasHorizontalScroll).toBe(false)
    })
  }
})
```

## 执行与调试

```bash
# 运行所有测试
npx playwright test

# 运行特定文件
npx playwright test tests/login.spec.js

# 调试模式（有 UI）
npx playwright test --ui

# 生成测试代码（录制模式）
npx playwright codegen http://localhost:5173

# 查看测试报告
npx playwright show-report
```

## 本项目测试清单

修改以下功能时，**必须同步更新**对应的 E2E 测试：

| 功能模块 | 对应测试文件 | 关键流程 |
|---------|-------------|---------|
| 用户认证 | `login.spec.js` | 登录/登出/Token 刷新 |
| 订单管理 | `order.spec.js` | 创建/查询/取消订单 |
| 司机调度 | `dispatch.spec.js` | 接单/导航/完成 |
| 后台管理 | `admin.spec.js` | 用户管理/数据统计 |

## 验证

- [ ] 核心流程测试通过
- [ ] 无基于 timeout 的脆弱断言
- [ ] 测试独立运行（不依赖其他测试的状态）
- [ ] 截图和 trace 可用于调试
- [ ] 修改功能后对应测试已更新

## 知识参考

Playwright, Chromium, 浏览器自动化, E2E 测试设计模式, data-testid 选择器
