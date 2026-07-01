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

async function test(name, checkFn) {
  const ok = await checkFn()
  results.push({ name, ok })
  ok ? pass++ : fail++
  const icon = ok ? '✅' : '❌'
  console.log(`${icon} ${name}`)
}

function driverLogin(phone) {
  return request('POST', '/api/driver/login', { phone })
}

function userLogin(phone, password) {
  return request('POST', '/api/user/login-password', { phone, password })
}

async function createOrder(token) {
  const res = await request('POST', '/api/order/create', {
    startAddress: '南昌八一广场',
    startLat: 28.6820,
    startLng: 115.8580,
    endAddress: '南昌西站',
    endLat: 28.6500,
    endLng: 115.9200,
    distance: 12000,
    duration: 1800,
    carTypeId: 1,
    idempotentKey: `concurrent-${Date.now()}-${Math.random()}`
  }, token)
  return res.body?.data?.id
}

async function completeOrder(orderId, driverToken) {
  await request('POST', `/api/driver/order/${orderId}/accept`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/arrive`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/start`, null, driverToken)
  return request('PUT', `/api/driver/order/${orderId}/complete`, null, driverToken)
}

console.log('=== 并发场景测试 ===')

// 1. 并发抢单测试
await test('并发抢单-仅一个司机成功', async () => {
  const userToken = (await userLogin('13900001111', '123456')).body?.data?.token
  if (!userToken) return false
  const orderId = await createOrder(userToken)
  if (!orderId) return false

  const drivers = await Promise.all([
    driverLogin('13810000001'),
    driverLogin('13810000002'),
    driverLogin('13810000003')
  ])
  const tokens = drivers.map(r => r.body?.data?.token).filter(Boolean)
  if (tokens.length !== 3) return false

  // 确保所有司机在线
  await Promise.all(tokens.map(t => request('PUT', '/api/driver/status', { status: 1 }, t)))

  // 同时发起接单请求
  const responses = await Promise.all(
    tokens.map(t => request('POST', `/api/driver/order/${orderId}/accept`, {}, t))
  )

  const successCount = responses.filter(r => r.body?.code === 200).length
  console.log(`   并发抢单成功数: ${successCount}/3`)

  // 清理：恢复司机在线状态
  await Promise.all(tokens.map(t => request('PUT', `/api/driver/status`, { status: 1 }, t)))

  return successCount === 1
})

// 2. 并发支付测试
await test('并发支付-仅一次支付成功', async () => {
  const userToken = (await userLogin('13900002222', '123456')).body?.data?.token
  if (!userToken) {
    // 用户不存在则注册
    await request('POST', '/api/user/register', { phone: '13900002222', password: '123456', nickname: '并发支付用户' })
  }
  const loginRes = await userLogin('13900002222', '123456')
  const token = loginRes.body?.data?.token
  if (!token) return false

  const driverToken = (await driverLogin('13810000001')).body?.data?.token
  if (!driverToken) return false
  await request('PUT', '/api/driver/status', { status: 1 }, driverToken)

  const orderId = await createOrder(token)
  if (!orderId) return false

  await completeOrder(orderId, driverToken)

  // 同时发起 5 次支付请求
  const responses = await Promise.all(
    Array.from({ length: 5 }).map(() =>
      request('POST', '/api/payment/create', { orderId, payMethod: 'balance' }, token)
    )
  )

  const successCount = responses.filter(r => r.body?.code === 200).length
  const alreadyPaidCount = responses.filter(r => r.body?.code === 5003 || (r.body?.message && r.body.message.includes('已支付'))).length
  console.log(`   并发支付成功数: ${successCount}, 已支付拦截数: ${alreadyPaidCount}`)

  // 恢复司机在线
  await request('PUT', '/api/driver/status', { status: 1 }, driverToken)

  return successCount === 1
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
