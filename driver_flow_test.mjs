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

console.log('=== 司机接单流程测试 ===')

// 1. 用户登录并创建待接订单
const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = userLogin.body?.data?.token
if (!userToken) {
  console.log('❌ 用户登录失败')
  process.exit(1)
}

const createRes = await request('POST', '/api/order/create', {
  startAddress: '南昌八一广场',
  startLat: 28.6820,
  startLng: 115.8580,
  endAddress: '南昌西站',
  endLat: 28.6500,
  endLng: 115.9200,
  distance: 12000,
  duration: 1800,
  carTypeId: 1,
  idempotentKey: `driver-flow-${Date.now()}`
}, userToken)
const targetOrderId = createRes.body?.data?.id
if (!targetOrderId) {
  console.log('❌ 创建测试订单失败', createRes.body)
  process.exit(1)
}
console.log(`创建测试订单 ID: ${targetOrderId}`)

// 2. 司机登录
const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' })
const driverToken = driverLogin.body.data.token

// 3. 更新司机状态为在线（前置补偿，防止其他测试中断导致司机仍处于忙碌状态）
await request('PUT', '/api/driver/status', { status: 1 }, driverToken)
await test('司机上线', 'PUT', '/api/driver/status', { status: 1 }, driverToken, 200, 200)

// 4. 更新司机位置到南昌八一广场附近
await test('更新司机位置', 'PUT', '/api/driver/location', { lat: 28.6820, lng: 115.8580 }, driverToken, 200, 200)

// 5. 查询附近订单应包含刚创建的订单（limit 放大以避免历史测试数据干扰）
await test('查询附近订单', 'GET', '/api/driver/order/nearby?lat=28.6820&lng=115.8580&limit=200', null, driverToken, 200, 200, b => {
  const list = b.data || []
  return list.some(o => o.id === targetOrderId)
})

// 5. 接单
await test('司机接单', 'POST', `/api/driver/order/${targetOrderId}/accept`, {}, driverToken, 200, 200, b => {
  return b.data && b.data.status === 1 && b.data.driverName
})

// 6. 到达
await test('司机到达', 'PUT', `/api/driver/order/${targetOrderId}/arrive`, {}, driverToken, 200, 200, b => {
  return b.data && b.data.status === 2
})

// 7. 开始行程
await test('开始行程', 'PUT', `/api/driver/order/${targetOrderId}/start`, {}, driverToken, 200, 200, b => {
  return b.data && b.data.status === 3
})

// 8. 完成行程
await test('完成行程', 'PUT', `/api/driver/order/${targetOrderId}/complete`, {}, driverToken, 200, 200, b => {
  return b.data && b.data.status === 4
})

// 9. 司机收入统计同步
await test('司机收入统计', 'GET', '/api/driver/earning', null, driverToken, 200, 200, b => {
  return b.data && b.data.totalOrders >= 1
})

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
