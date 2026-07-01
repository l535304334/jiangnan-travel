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

console.log('=== 地址管理模块测试 ===')

const login = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const token = login.body.data.token
const userId = login.body.data.userId

// 1. 新增地址
let addressId = null
await test('新增地址', 'POST', '/api/user/address', {
  tag: '公司',
  address: '南昌市红谷滩新区会展路999号',
  lat: 28.6900,
  lng: 115.8700
}, token, 200, 200, b => {
  addressId = b.data?.id
  return b.data && b.data.userId === userId && b.data.tag === '公司'
})

// 2. 地址列表包含新地址
await test('地址列表', 'GET', '/api/user/address', null, token, 200, 200, b => {
  const list = b.data || []
  return list.some(a => a.id === addressId)
})

// 3. 删除地址
if (addressId) {
  await test('删除地址', 'DELETE', `/api/user/address/${addressId}`, {}, token, 200, 200)

  // 4. 删除后列表不包含
  await test('删除后地址列表不包含', 'GET', '/api/user/address', null, token, 200, 200, b => {
    const list = b.data || []
    return !list.some(a => a.id === addressId)
  })

  // 5. 删除不存在的地址
  await test('删除不存在地址', 'DELETE', `/api/user/address/${addressId}`, {}, token, 200, 404)
}

// 6. 越权：用其他用户 token 删除当前用户地址（需要另一个用户）
// 简化：直接测试无 token 访问返回 401
await test('无Token访问地址列表', 'GET', '/api/user/address', null, null, 401, 401)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
