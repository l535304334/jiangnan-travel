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
  const body = await res.json().catch(() => null)
  return { status: res.status, body }
}

async function test(name, method, path, data, token, expectStatus, expectCode) {
  const { status, body } = await request(method, path, data, token)
  let ok = status === expectStatus
  if (expectCode !== undefined && body) {
    ok = ok && body.code === expectCode
  }
  results.push({ name, status, expected: expectStatus, code: body?.code, ok })
  ok ? pass++ : fail++
}

console.log('=== 认证边界测试 ===')

// 1. 正常登录获取 token
const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = userLogin.body.data.token

const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' })
const driverToken = driverLogin.body.data.token

const adminLogin = await request('POST', '/api/admin/login', { username: 'admin', password: '123456' })
const adminToken = adminLogin.body.data.token

// 2. 错误密码 - 业务错误，HTTP 200，code 非 200
await test('乘客登录-错误密码', 'POST', '/api/user/login-password', { phone: '13900001111', password: 'wrong' }, null, 200, 401)
await test('管理员登录-错误密码', 'POST', '/api/admin/login', { username: 'admin', password: 'wrong' }, null, 200, 401)

// 3. 缺少参数 - 业务错误
await test('乘客登录-缺少密码', 'POST', '/api/user/login-password', { phone: '13900001111' }, null, 200, 400)
await test('管理员登录-缺少用户名', 'POST', '/api/admin/login', { password: '123456' }, null, 200, 400)

// 4. 无 Token 访问受保护接口
await test('无Token访问用户资料', 'GET', '/api/user/profile', null, null, 401)
await test('无Token访问司机资料', 'GET', '/api/driver/profile', null, null, 401)
await test('无Token访问管理后台', 'GET', '/api/admin/dashboard', null, null, 401)

// 5. 错误/过期 Token
await test('错误Token访问用户资料', 'GET', '/api/user/profile', null, 'invalid_token', 401)

// 6. 跨角色访问
await test('用户Token访问司机资料', 'GET', '/api/driver/profile', null, userToken, 403)
await test('用户Token访问管理后台', 'GET', '/api/admin/dashboard', null, userToken, 403)
await test('司机Token访问管理后台', 'GET', '/api/admin/dashboard', null, driverToken, 403)
await test('管理员Token访问用户资料', 'GET', '/api/user/profile', null, adminToken, 403)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
