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
  results.push({ name, status, expected: expectStatus, code: body?.code, ok })
  ok ? pass++ : fail++
}

const xssPayload = "<script>alert('xss')</script>"
const sqlPayload = "' OR '1'='1"
const unionPayload = "' UNION SELECT * FROM t_user -- "

console.log('=== 输入安全扫描（SQL/XSS）测试 ===')

const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = userLogin.body?.data?.token

// 1. 登录SQL注入尝试
await test('登录-phone单引号注入', 'POST', '/api/user/login-password', { phone: "139' OR '1'='1", password: '123456' }, null, 200, 400)
await test('登录-password单引号注入', 'POST', '/api/user/login-password', { phone: '13900001111', password: "' OR '1'='1" }, null, 200, 401)

// 2. 注册XSS/特殊字符
await test('注册-phone含XSS', 'POST', '/api/user/register', { phone: '13900009999', code: '123456', password: '123456', nickname: xssPayload }, null, 200, undefined, b => b.code !== 500)

// 3. 订单地址XSS
const orderRes = await request('POST', '/api/order/create', {
  startAddress: `起点${xssPayload}`,
  startLat: 31.2304,
  startLng: 121.4737,
  endAddress: `终点${sqlPayload}`,
  endLat: 31.2504,
  endLng: 121.4937,
  distance: 5000,
  duration: 600,
  carTypeId: 1,
  idempotentKey: `input-${Date.now()}`
}, userToken)
const orderId = orderRes.body?.data?.id
await test('订单地址含XSS/SQL不触发500', 'POST', '/api/order/create', {
  startAddress: `起点${xssPayload}`,
  startLat: 31.2304,
  startLng: 121.4737,
  endAddress: `终点${sqlPayload}`,
  endLat: 31.2504,
  endLng: 121.4937,
  distance: 5000,
  duration: 600,
  carTypeId: 1,
  idempotentKey: `input2-${Date.now()}`
}, userToken, 200, undefined, b => b.code !== 500)

// 4. 文旅地标搜索SQL注入
await test('文旅搜索SQL注入尝试', 'GET', `/api/landmark/search?city=${encodeURIComponent(sqlPayload)}&keyword=${encodeURIComponent(unionPayload)}`, null, null, 200, undefined, b => b.code !== 500 && Array.isArray(b.data))

// 5. 优惠券列表不受注入影响
await test('优惠券列表无异常', 'GET', '/api/coupon/list', null, userToken, 200, undefined, b => b.code !== 500)

// 6. 地址标签XSS
const addrRes = await request('POST', '/api/user/address', {
  tag: xssPayload,
  address: `地址${sqlPayload}`,
  lat: 31.2304,
  lng: 121.4737
}, userToken)
const addressId = addrRes.body?.data?.id
await test('地址含XSS不触发500', 'GET', '/api/user/address', null, userToken, 200, undefined, b => b.code !== 500)
if (addressId) {
  await request('DELETE', `/api/user/address/${addressId}`, null, userToken)
}

// 7. 评价内容XSS（需要已完成订单，这里仅验证接口不500）
await test('评价内容XSS不触发500', 'POST', `/api/order/${orderId}/review`, { rating: 5, content: xssPayload, tags: sqlPayload }, userToken, 200, undefined, b => b.code !== 500)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
