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

async function login(phone, password = '123456') {
  const res = await request('POST', '/api/user/login-password', { phone, password })
  return res.body?.data?.token
}

async function createOrder(token) {
  return request('POST', '/api/order/create', {
    startAddress: 'IDOR测试起点',
    startLat: 31.2304,
    startLng: 121.4737,
    endAddress: 'IDOR测试终点',
    endLat: 31.2504,
    endLng: 121.4937,
    distance: 5000,
    duration: 600,
    carTypeId: 1,
    idempotentKey: `idor-${Date.now()}-${Math.random()}`
  }, token)
}

async function completeOrder(orderId, driverToken) {
  await request('POST', `/api/driver/order/${orderId}/accept`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/arrive`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/start`, null, driverToken)
  await request('PUT', `/api/driver/order/${orderId}/complete`, null, driverToken)
}

console.log('=== 越权访问（IDOR）安全测试 ===')

// 1. 登录三个角色
const tokenA = await login('13900001111')
const tokenB = await login('13900002222')
const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' })
const driverToken = driverLogin.body?.data?.token

if (!tokenA || !tokenB || !driverToken) {
  console.log('❌ 登录失败，无法继续测试')
  process.exit(1)
}

// 确保司机在线
await request('PUT', '/api/driver/status', { status: 1 }, driverToken)

// 2. 用户A创建订单
const orderRes = await createOrder(tokenA)
const orderId = orderRes.body?.data?.id
if (!orderId) {
  console.log('❌ 用户A创建订单失败', orderRes.body)
  process.exit(1)
}

// 3. B访问A的待支付/待接单订单
await test('B访问A订单详情-待接单', 'GET', `/api/order/${orderId}`, null, tokenB, 200, 403)
await test('B取消A订单-待接单', 'PUT', `/api/order/${orderId}/cancel`, { reason: '越权取消' }, tokenB, 200, 2001)

// 4. 完成订单
await completeOrder(orderId, driverToken)

// 5. B支付A的已完成订单
await test('B支付A订单-已完成', 'PUT', `/api/order/${orderId}/pay`, null, tokenB, 200, 2001)

// 6. A支付订单
await request('PUT', `/api/order/${orderId}/pay`, null, tokenA)

// 7. B评价A的已支付订单
await test('B评价A订单-已支付', 'POST', `/api/order/${orderId}/review`, { rating: 5, content: '越权评价' }, tokenB, 200, 2001)

// 8. A评价订单
await request('POST', `/api/order/${orderId}/review`, { rating: 5, content: '服务很好' }, tokenA)

// 9. B再次评价A的已评价订单
await test('B重复评价A订单', 'POST', `/api/order/${orderId}/review`, { rating: 5, content: '再次越权评价' }, tokenB, 200, 2001)

// 10. A添加地址
const addrRes = await request('POST', '/api/user/address', {
  tag: '家',
  address: 'IDOR测试地址',
  lat: 31.2304,
  lng: 121.4737
}, tokenA)
const addressId = addrRes.body?.data?.id

// 11. B删除A的地址
await test('B删除A地址', 'DELETE', `/api/user/address/${addressId}`, null, tokenB, 200, 404)

// 12. A申请发票
const invoiceRes = await request('POST', '/api/invoice/apply', {
  orderId: orderId,
  title: 'IDOR测试公司',
  taxNo: '91330000123456789X'
}, tokenA)
const invoiceId = invoiceRes.body?.data?.id

// 13. B查看A发票详情
if (invoiceId) {
  await test('B查看A发票详情', 'GET', `/api/invoice/${invoiceId}`, null, tokenB, 200, 404)

  // 14. B取消A发票申请
  await test('B取消A发票申请', 'PUT', `/api/invoice/${invoiceId}/cancel`, null, tokenB, 200, 404)
} else {
  console.log('⚠️ 发票申请失败（可能已申请），跳过发票IDOR测试')
}

// 15. B访问A通知列表（不应看到A的通知）
await test('B访问A通知列表隔离', 'GET', '/api/notification/list?pageNum=1&pageSize=10', null, tokenB, 200, 200, b => {
  const records = b.data?.records || []
  return records.every(n => n.userId !== 2) // 用户A userId=2
})

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
