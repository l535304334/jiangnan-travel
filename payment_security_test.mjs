const BASE = 'http://localhost:8080'
let pass = 0, fail = 0
const results = []

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
  return request('POST', '/api/order/create', {
    startAddress: '支付测试起点',
    startLat: 31.2304,
    startLng: 121.4737,
    endAddress: '支付测试终点',
    endLat: 31.2504,
    endLng: 121.4937,
    distance: 5000,
    duration: 600,
    carTypeId: 1,
    idempotentKey: `pay-${Date.now()}-${Math.random()}`
  }, token)
}

async function completeOrder(orderId, driverToken) {
  await request('POST', `/api/driver/order/${orderId}/accept`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/arrive`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/start`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/complete`, null, driverToken)
}

console.log('=== 支付幂等性与异常安全测试 ===')

// 1. 登录
const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = userLogin.body?.data?.token
const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' })
const driverToken = driverLogin.body?.data?.token

if (!userToken || !driverToken) {
  console.log('❌ 登录失败')
  process.exit(1)
}

// 确保司机在线
await request('PUT', '/api/driver/status', { status: 1 }, driverToken)

// 2. 未完成订单支付应失败
const pendingOrder = await createOrder(userToken)
const pendingOrderId = pendingOrder.body?.data?.id
await test('未完成订单支付失败', 'POST', '/api/payment/create', { orderId: pendingOrderId, payMethod: 'balance' }, userToken, 200, 2002)

// 3. 完成订单并支付
const orderRes = await createOrder(userToken)
const orderId = orderRes.body?.data?.id
await completeOrder(orderId, driverToken)
const payRes = await request('POST', '/api/payment/create', { orderId: orderId, payMethod: 'balance' }, userToken)
let payNo = null
if (payRes.body?.code === 200) {
  payNo = payRes.body?.data?.payNo
  pass++
  results.push({ name: '已完成订单首次支付成功', status: payRes.status, expected: 200, code: payRes.body?.code, ok: true, data: payRes.body?.data })
} else {
  fail++
  results.push({ name: '已完成订单首次支付成功', status: payRes.status, expected: 200, code: payRes.body?.code, ok: false, data: payRes.body?.data })
}

// 4. 重复支付应失败
await test('重复支付应失败-payment', 'POST', '/api/payment/create', { orderId: orderId, payMethod: 'balance' }, userToken, 200, 6001)
await test('重复支付应失败-order', 'PUT', `/api/order/${orderId}/pay`, null, userToken, 200, 6001)

// 5. 支付回调幂等（回调公开，不需要token）
if (payNo) {
  await test('支付回调首次成功', 'POST', '/api/payment/callback', { payNo: payNo }, null, 200, 200)
  await test('支付回调重复幂等', 'POST', '/api/payment/callback', { payNo: payNo }, null, 200, 200)
}

// 6. 支付金额不可篡改（接口未接受金额字段，重复支付应失败）
await test('支付接口忽略金额篡改字段', 'POST', '/api/payment/create', { orderId: orderId, payMethod: 'balance', amount: 0.01 }, userToken, 200, 6001)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
