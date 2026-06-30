import { chromium } from '@playwright/test'

const browser = await chromium.launch({ executablePath: 'c:\\Users\\lai\\Desktop\\软件工程2307班实习材料\\e2e\\.playwright-browsers\\chromium-1228\\chrome-win64\\chrome.exe' })
const page = await browser.newPage()
await page.goto('http://localhost:5173/login')
await page.getByText('司机登录').click()
await page.getByPlaceholder('司机手机号').fill('13810000001')
await page.getByRole('button', { name: '司机登录' }).click()
await page.waitForURL(/\/driver\/home/)

// 1. 用户（乘客）登录，用于创建订单
const userRes = await fetch('http://localhost:8080/api/user/login-password', {
  method: 'POST',
  headers: {'Content-Type':'application/json'},
  body: JSON.stringify({phone:'13900001111', password:'123456'})
})
const userBody = await userRes.json()
console.log('user login', userBody.code, userBody.message)
const userToken = userBody.data.token

// 2. 司机登录，用于页面操作
await page.goto('http://localhost:5173/login')
await page.getByText('司机登录').click()
await page.getByPlaceholder('司机手机号').fill('13810000001')
await page.getByRole('button', { name: '司机登录' }).click()
await page.waitForURL(/\/driver\/home/)

// 3. 创建测试订单
const orderRes = await fetch('http://localhost:8080/api/order/create', {
  method: 'POST',
  headers: {'Content-Type':'application/json', 'Authorization': 'Bearer ' + userToken},
  body: JSON.stringify({startAddress:'南昌八一广场', startLat:28.6820, startLng:115.8580, endAddress:'南昌西站', endLat:28.6500, endLng:115.9200, distance:12000, duration:1800, carTypeId:1, idempotentKey:'debug-'+Date.now()})
})
const orderBody = await orderRes.json()
console.log('create order response', JSON.stringify(orderBody))
const orderId = orderBody.data?.id
if (!orderId) {
  console.error('Failed to create order')
  await browser.close()
  process.exit(1)
}
console.log('order id', orderId, 'status', orderBody.data.status)

await page.goto('http://localhost:5173/driver/order/' + orderId)
await page.waitForTimeout(2000)

// 打印页面中“接单”相关按钮的 HTML
const acceptButtons = await page.locator('button:has-text("接单")').evaluateAll(els =>
  els.map(e => ({ tag: e.tagName, outerHTML: e.outerHTML, text: e.textContent.trim() }))
)
console.log('accept buttons found:', acceptButtons.length)
console.log(JSON.stringify(acceptButtons, null, 2))

// 同时打印 action-btns 区域 HTML
const actionHtml = await page.locator('.action-btns').evaluate(el => el.innerHTML).catch(() => null)
console.log('action-btns innerHTML:', actionHtml)

await browser.close()
