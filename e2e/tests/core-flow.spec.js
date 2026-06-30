import { test, expect } from '@playwright/test'
import { userLogin, driverLogin, createOrder, completeOrderByApi } from './helpers.js'

test.describe('江南出行核心流程 E2E', () => {
  test('乘客登录并查看首页', async ({ page }) => {
    await page.goto('/login')
    await page.getByText('乘客登录').click()
    await page.getByPlaceholder('手机号').fill('13900001111')
    await page.getByPlaceholder('密码').fill('123456')
    await page.getByRole('button', { name: '登录' }).click()

    await expect(page).toHaveURL(/\/home/)
    await expect(page.locator('text=欢迎使用江南出行')).toBeVisible()
  })

  test('司机登录并查看司机首页', async ({ page }) => {
    await page.goto('/login')
    await page.getByText('司机登录').click()
    await page.getByPlaceholder('司机手机号').fill('13810000001')
    await page.getByRole('button', { name: '司机登录' }).click()

    await expect(page).toHaveURL(/\/driver\/home/)
    await expect(page.getByRole('heading', { name: '待接订单' })).toBeVisible()
  })

  test('乘客下单 → 司机接单 → 乘客支付 → 乘客评价', async ({ page, browser }) => {
    test.setTimeout(60000)

    // 1. 通过 API 准备测试订单
    const user = await userLogin()
    const driver = await driverLogin()
    const order = await createOrder(user.token)
    expect(order).toBeTruthy()
    expect(order.id).toBeTruthy()

    // 2. 司机端登录并接单
    const driverContext = await browser.newContext()
    const driverPage = await driverContext.newPage()
    await driverPage.goto('/login')
    await driverPage.getByText('司机登录').click()
    await driverPage.getByPlaceholder('司机手机号').fill('13810000001')
    await driverPage.getByRole('button', { name: '司机登录' }).click()
    await expect(driverPage).toHaveURL(/\/driver\/home/)

    // 直接进入对应订单详情页接单（避免历史数据列表干扰）
    await driverPage.goto(`/driver/order/${order.id}`)
    await expect(driverPage.getByRole('button', { name: '接单' })).toBeVisible()
    await driverPage.getByRole('button', { name: '接单' }).click()
    await expect(driverPage.locator('text=操作成功')).toBeVisible()

    // 3. 司机完成行程（API 方式，避免 UI 多步点击）
    await completeOrderByApi(order.id, driver.token)

    // 4. 乘客端登录并支付
    await page.goto('/login')
    await page.getByText('乘客登录').click()
    await page.getByPlaceholder('手机号').fill('13900001111')
    await page.getByPlaceholder('密码').fill('123456')
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page).toHaveURL(/\/home/)

    await page.goto(`/payment/${order.id}`)
    await expect(page.locator('text=支付金额')).toBeVisible()
    await page.getByRole('button', { name: /立即支付/ }).click()
    await expect(page.locator('text=支付成功')).toBeVisible()

    // 5. 乘客评价
    await page.goto(`/order/${order.id}/review`)
    await expect(page.locator('text=评价订单')).toBeVisible()
    await page.locator('.el-rate__item').nth(4).click()
    await page.getByRole('button', { name: '提交评价' }).click()
    await expect(page.locator('text=评价成功')).toBeVisible()

    await driverContext.close()
  })
})
