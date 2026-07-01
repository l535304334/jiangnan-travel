const BASE = 'http://localhost:8080'
let pass = 0, fail = 0
const results = []
const testOrders = []

async function request(method, path, data = null, token = null) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: data ? JSON.stringify(data) : null
  })
  const text = await res.text()
  const body = text ? JSON.parse(text) : null
  return { status: res.status, body }
}

async function test(name, method, path, data, token, expectStatus, expectCode, checkFn) {
  const { status, body } = await request(method, path, data, token)
  let ok = status === expectStatus
  if (expectCode !== undefined && body) {
    ok = ok && body.code === expectCode
  }
  if (checkFn && body) {
    ok = ok && checkFn(body)
  }
  results.push({ name, status, expected: expectStatus, code: body?.code, ok, data: body?.data })
  ok ? pass++ : fail++
}

async function createOrder(token) {
  const res = await request('POST', '/api/order/create', {
    startAddress: '测试起点',
    startLat: 31.2304,
    startLng: 121.4737,
    endAddress: '测试终点',
    endLat: 31.2504,
    endLng: 121.4937,
    distance: 5000,
    duration: 600,
    carTypeId: 1,
    idempotentKey: `cancel-test-${Date.now()}-${Math.random()}`
  }, token)
  if (res.body?.data?.id) testOrders.push(res.body.data.id)
  return res
}

console.log('=== 取消退款流程测试 ===')

// 1. 登录
const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = userLogin.body?.data?.token
const userId = userLogin.body?.data?.userId

const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' })
const driverToken = driverLogin.body?.data?.token
const driverId = driverLogin.body?.data?.driverId

if (!userToken || !driverToken) {
  console.log('❌ 登录失败，无法继续测试')
  process.exit(1)
}

// 2. 创建并取消未支付订单
let order1 = await createOrder(userToken)
let order1Id = order1.body?.data?.id
await test('取消未支付订单', 'PUT', `/api/order/${order1Id}/cancel`, { reason: '用户主动取消' }, userToken, 200, 200)
await test('重复取消已取消订单失败', 'PUT', `/api/order/${order1Id}/cancel`, { reason: '再次取消' }, userToken, 200, 2003)

// 3. 司机接单后仍可取消（status=1）
let order2 = await createOrder(userToken)
let order2Id = order2.body?.data?.id
await test('司机接单', 'POST', `/api/driver/order/${order2Id}/accept`, null, driverToken, 200, 200)
await test('接单后取消订单', 'PUT', `/api/order/${order2Id}/cancel`, { reason: '司机未到达' }, userToken, 200, 200)

// 4. 行程开始后不能取消
let order3 = await createOrder(userToken)
let order3Id = order3.body?.data?.id
await test('司机接单-订单3', 'POST', `/api/driver/order/${order3Id}/accept`, null, driverToken, 200, 200)
await test('司机到达-订单3', 'PUT', `/api/driver/order/${order3Id}/arrive`, null, driverToken, 200, 200)
await test('开始行程-订单3', 'PUT', `/api/driver/order/${order3Id}/start`, null, driverToken, 200, 200)
await test('行程开始后取消失败', 'PUT', `/api/order/${order3Id}/cancel`, { reason: '想下车' }, userToken, 200, 2003)

// 5. 完成行程后订单不能取消
// 先完成订单3使司机恢复在线，再用于测试完成态不可取消
await test('完成行程-订单3', 'PUT', `/api/driver/order/${order3Id}/complete`, null, driverToken, 200, 200)
await test('已完成订单取消失败', 'PUT', `/api/order/${order3Id}/cancel`, { reason: '反悔' }, userToken, 200, 2003)

// 6. 取消率风控：1小时内取消5次后第6次下单失败
// 使用一个独立用户避免影响其他测试
let riskUserLogin = await request('POST', '/api/user/login-password', { phone: '13900003333', password: '123456' })
if (riskUserLogin.status === 200 && riskUserLogin.body?.code === 1001) {
  // 用户不存在则注册
  await request('POST', '/api/user/register', { phone: '13900003333', password: '123456', nickname: '风控测试用户' })
  riskUserLogin = await request('POST', '/api/user/login-password', { phone: '13900003333', password: '123456' })
}
const riskToken = riskUserLogin.body?.data?.token
if (riskToken) {
  for (let i = 0; i < 5; i++) {
    const o = await createOrder(riskToken)
    const oid = o.body?.data?.id
    if (oid) {
      await request('PUT', `/api/order/${oid}/cancel`, { reason: '风控测试取消' }, riskToken)
    }
  }
  await test('取消率风控-第6次下单被限制', 'POST', '/api/order/create', {
    startAddress: '风控测试起点',
    startLat: 31.2304,
    startLng: 121.4737,
    endAddress: '风控测试终点',
    endLat: 31.2504,
    endLng: 121.4937,
    distance: 5000,
    duration: 600,
    carTypeId: 1,
    idempotentKey: `risk-${Date.now()}`
  }, riskToken, 200, 4001)
}

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
