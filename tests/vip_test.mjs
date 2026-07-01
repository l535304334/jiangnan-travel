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

console.log('=== VIP 等级与折扣测试 ===')

const login = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const token = login.body.data.token
const userId = login.body.data.userId

// 1. VIP 等级列表
let firstLevelId = null
await test('VIP等级列表', 'GET', '/api/vip/levels', null, null, 200, 200, b => {
  const list = b.data || []
  if (list.length > 0) firstLevelId = list[0].id
  return list.length > 0 && list[0].discount != null
})

// 2. VIP 权益
await test('VIP权益', 'GET', '/api/vip/benefits', null, null, 200, 200, b => {
  const list = b.data || []
  return list.length > 0
})

// 3. 我的 VIP（可能未开通）
let hadVipBefore = false
await test('我的VIP', 'GET', '/api/vip/my', null, token, 200, 200, b => {
  hadVipBefore = b.data && b.data.status === 1
  return b.data !== undefined
})

// 4. 购买 VIP
if (firstLevelId) {
  await test('购买VIP', 'POST', '/api/vip/purchase', { levelId: firstLevelId, feeType: 0 }, token, 200, 200)

  // 5. 购买后我的 VIP 状态更新
  await test('购买后我的VIP', 'GET', '/api/vip/my', null, token, 200, 200, b => {
    return b.data && b.data.status === 1 && b.data.vipLevel && b.data.remainingDays >= 0
  })

  // 6. 下单时 VIP 折扣应用
  await test('下单应用VIP折扣', 'POST', '/api/order/estimate', {
    startAddress: '江南大学东门',
    startLat: 31.2304,
    startLng: 121.4737,
    endAddress: '无锡火车站',
    endLat: 31.2504,
    endLng: 121.4937,
    distance: 5000,
    duration: 600,
    carTypeId: 1
  }, token, 200, 200, b => {
    return b.data && b.data.priceDetail && b.data.priceDetail.vipDiscount <= 1
  })
}

// 7. 无Token访问我的VIP返回401
await test('无Token访问我的VIP', 'GET', '/api/vip/my', null, null, 401, 401)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
