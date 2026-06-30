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

console.log('=== 城际班线购票测试 ===')

const login = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const token = login.body.data.token
const userId = login.body.data.userId

// 1. 班线列表
let lineId = null
await test('班线列表', 'GET', '/api/bus-line/list', null, null, 200, 200, b => {
  const list = b.data || []
  if (list.length > 0) lineId = list[0].id
  return list.length > 0
})

// 2. 班线详情
let scheduleId = null
let originalRemaining = null
if (lineId) {
  await test('班线详情', 'GET', `/api/bus-line/${lineId}`, null, null, 200, 200, b => {
    const schedules = b.data?.schedules || []
    const avail = schedules.find(s => s.remaining > 0)
    if (avail) {
      scheduleId = avail.id
      originalRemaining = avail.remaining
    }
    return b.data && b.data.id === lineId
  })
}

// 3. 购票
if (scheduleId) {
  await test('班线购票', 'POST', '/api/bus-line/purchase', { scheduleId }, token, 200, 200, b => {
    return b.data && b.data.id === scheduleId && b.data.remaining === originalRemaining - 1
  })

  // 4. 再次查询详情，余票减少
  await test('购票后余票减少', 'GET', `/api/bus-line/${lineId}`, null, null, 200, 200, b => {
    const schedules = b.data?.schedules || []
    const s = schedules.find(x => x.id === scheduleId)
    return s && s.remaining === originalRemaining - 1
  })
}

// 5. 无余票购票失败（如果找到余票为0的班次）
const detail = await request('GET', `/api/bus-line/${lineId}`, null, null)
const soldOut = (detail.body?.data?.schedules || []).find(s => s.remaining <= 0)
if (soldOut) {
  await test('无余票购票失败', 'POST', '/api/bus-line/purchase', { scheduleId: soldOut.id }, token, 200, 400)
}

// 6. 无Token购票返回401
await test('无Token购票', 'POST', '/api/bus-line/purchase', { scheduleId: scheduleId || 1 }, null, 401, 401)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
