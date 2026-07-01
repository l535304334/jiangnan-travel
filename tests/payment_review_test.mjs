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
    startAddress: '南昌八一广场',
    startLat: 28.6820,
    startLng: 115.8580,
    endAddress: '南昌西站',
    endLat: 28.6500,
    endLng: 115.9200,
    distance: 12000,
    duration: 1800,
    carTypeId: 1,
    idempotentKey: `pay-review-${Date.now()}-${Math.random()}`
  }, token)
}

async function completeOrder(orderId, driverToken) {
  await request('POST', `/api/driver/order/${orderId}/accept`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/arrive`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/start`, null, driverToken)
  return request('PUT', `/api/driver/order/${orderId}/complete`, null, driverToken)
}

console.log('=== 支付评价流程测试 ===')

// 1. 乘客登录
const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = userLogin.body?.data?.token
const userId = userLogin.body?.data?.userId
if (!userToken) {
  console.log('❌ 用户登录失败')
  process.exit(1)
}

// 2. 司机登录并确保在线
const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' })
const driverToken = driverLogin.body?.data?.token
if (!driverToken) {
  console.log('❌ 司机登录失败')
  process.exit(1)
}
await request('PUT', '/api/driver/status', { status: 1 }, driverToken)

// 3. 创建订单并走完全程
const orderRes = await createOrder(userToken)
const orderId = orderRes.body?.data?.id
if (!orderId) {
  console.log('❌ 创建订单失败', orderRes.body)
  process.exit(1)
}
console.log(`使用测试订单 ID: ${orderId}`)

const completeRes = await completeOrder(orderId, driverToken)
if (completeRes.body?.code !== 200) {
  console.log('❌ 完成订单失败', completeRes.body)
  process.exit(1)
}

// 4. 创建支付
await test('创建支付', 'POST', '/api/payment/create', { orderId, payMethod: 'balance' }, userToken, 200, 200, b => {
  return b.data && b.data.status === 1 && b.data.amount > 0
})

// 5. 支付查询
await test('支付查询', 'GET', `/api/payment/${orderId}`, null, userToken, 200, 200, b => {
  return b.data && b.data.status === 1
})

// 6. 申请发票
let invoiceId = null
await test('申请发票', 'POST', '/api/invoice/apply', {
  orderId,
  title: '江南科技有限公司',
  taxNo: '91360100309683294U'
}, userToken, 200, 200, b => {
  invoiceId = b.data?.id
  return b.data && b.data.status === 0 && b.data.amount > 0
})

// 7. 发票列表包含新发票
if (invoiceId) {
  await test('发票列表包含新发票', 'GET', '/api/invoice/list', null, userToken, 200, 200, b => {
    const list = b.data || []
    return list.some(inv => inv.id === invoiceId)
  })
}

// 8. 评价订单
await test('评价订单', 'POST', `/api/order/${orderId}/review`, {
  rating: 5,
  tags: '准时,服务好',
  content: '司机服务很好，车辆干净整洁'
}, userToken, 200, 200)

// 9. 司机评分更新
const driverProfile = await request('GET', '/api/driver/profile', null, driverToken)
await test('司机评分已更新', 'GET', '/api/driver/profile', null, driverToken, 200, 200, b => {
  return b.data && b.data.avgRating > 0
})

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
