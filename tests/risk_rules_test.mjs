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
  const icon = ok ? '✅' : '❌'
  console.log(`${icon} ${name} -> HTTP:${status} code:${body?.code ?? '-'}`)
}

import { execSync } from 'child_process'

async function userLogin(phone, password) {
  let res = await request('POST', '/api/user/login-password', { phone, password })
  if (res.body?.code === 200) return res.body.data.token

  // 用户不存在，通过发送验证码 + Redis 读取完成注册
  const sendRes = await request('POST', '/api/user/send-code', { phone })
  if (sendRes.body?.code !== 200) {
    console.log(`发送验证码失败: ${sendRes.body?.message}`)
    return null
  }
  // 等待 Redis 写入
  await new Promise(r => setTimeout(r, 200))
  let code = ''
  try {
    code = execSync(`redis-cli GET sms:code:${phone}`, { encoding: 'utf8' }).trim()
  } catch (e) {
    console.log('读取 Redis 验证码失败', e.message)
    return null
  }
  if (!code) {
    console.log('未获取到验证码')
    return null
  }

  await request('POST', '/api/user/register', { phone, password, code, nickname: '风控测试用户' })
  res = await request('POST', '/api/user/login-password', { phone, password })
  return res.body?.data?.token
}

function createOrderData(idempotentKey) {
  return {
    startAddress: '南昌八一广场',
    startLat: 28.6820,
    startLng: 115.8580,
    endAddress: '南昌西站',
    endLat: 28.6500,
    endLng: 115.9200,
    distance: 12000,
    duration: 1800,
    carTypeId: 1,
    idempotentKey
  }
}

console.log('=== 风控规则测试 ===')

// 管理员登录
const adminLogin = await request('POST', '/api/admin/login', { username: 'admin', password: '123456' })
const adminToken = adminLogin.body?.data?.token
if (!adminToken) {
  console.log('❌ 管理员登录失败')
  process.exit(1)
}

// R1: 短时高频下单（10分钟内≥3次）触发预警
const r1User = await userLogin('13900007777', '123456')
if (!r1User) {
  console.log('❌ R1 用户登录失败')
  process.exit(1)
}
for (let i = 0; i < 3; i++) {
  await request('POST', '/api/order/create', createOrderData(`risk-r1-${Date.now()}-${i}`), r1User)
}
await test('R1-高频下单触发预警', 'GET', '/api/admin/alerts', null, adminToken, 200, 200, b => {
  const list = b.data?.records || []
  return list.some(a => a.ruleCode === 'R1')
})

// R2: 1小时内取消>5次 -> 当日禁下单
const r2User = await userLogin('13900008888', '123456')
if (!r2User) {
  console.log('❌ R2 用户登录失败')
  process.exit(1)
}
for (let i = 0; i < 5; i++) {
  const res = await request('POST', '/api/order/create', createOrderData(`risk-r2-${Date.now()}-${i}`), r2User)
  const orderId = res.body?.data?.id
  if (orderId) {
    await request('PUT', `/api/order/${orderId}/cancel`, { reason: '风控测试取消' }, r2User)
  }
}
await test('R2-高频取消后第6次下单被拦截', 'POST', '/api/order/create', createOrderData(`risk-r2-block-${Date.now()}`), r2User, 200, 4001)

// R3: 起终点距离<100米 -> 疑似刷单
const r3User = await userLogin('13900009999', '123456')
if (!r3User) {
  console.log('❌ R3 用户登录失败')
  process.exit(1)
}
await request('POST', '/api/order/create', {
  startAddress: '南昌八一广场',
  startLat: 28.6820,
  startLng: 115.8580,
  endAddress: '南昌八一广场北侧',
  endLat: 28.6821,
  endLng: 115.8581,
  distance: 50,
  duration: 60,
  carTypeId: 1,
  idempotentKey: `risk-r3-${Date.now()}`
}, r3User)
await test('R3-超短距离下单触发预警', 'GET', '/api/admin/alerts', null, adminToken, 200, 200, b => {
  const list = b.data?.records || []
  return list.some(a => a.ruleCode === 'R3')
})

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
