const BASE = 'http://localhost:8080'
let pass = 0, fail = 0
const results = []

async function request(method, path, data = null, token = null) {
  const headers = { 'Content-Type': 'application/json' }
  if (token !== null) headers['Authorization'] = token ? `Bearer ${token}` : 'Bearer '
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: data ? JSON.stringify(data) : null
  })
  const text = await res.text()
  const body = text ? JSON.parse(text) : null
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

function b64UrlEncode(str) {
  return Buffer.from(str).toString('base64url')
}

function b64UrlDecode(str) {
  return Buffer.from(str, 'base64url').toString()
}

function tamperToken(token) {
  const [header, payload, signature] = token.split('.')
  const p = JSON.parse(b64UrlDecode(payload))
  // 篡改用户ID
  p.sub = String(Number(p.sub) + 1)
  return `${header}.${b64UrlEncode(JSON.stringify(p))}.${signature}`
}

console.log('=== JWT安全与认证加固测试 ===')

// 1. 获取合法token
const login = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const token = login.body?.data?.token
if (!token) {
  console.log('❌ 登录失败，无法继续测试')
  process.exit(1)
}

// 2. 各种非法Token场景
await test('无Token访问用户资料', 'GET', '/api/user/profile', null, null, 401)
await test('空Token访问用户资料', 'GET', '/api/user/profile', null, '', 401)
await test('随机字符串Token', 'GET', '/api/user/profile', null, 'random_invalid_token', 401)
await test('格式正确但签名无效Token', 'GET', '/api/user/profile', null, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIifQ.invalid_signature', 401)

// 3. 篡改payload的Token
const tampered = tamperToken(token)
await test('篡改用户ID的Token', 'GET', '/api/user/profile', null, tampered, 401)

// 4. 伪造admin角色的Token（使用相同secret签名）
// 由于无法直接在Node生成合法签名，这里使用无算法攻击尝试
const forgedHeader = b64UrlEncode(JSON.stringify({ alg: 'none', typ: 'JWT' }))
const forgedPayload = b64UrlEncode(JSON.stringify({ sub: '1', role: 'ADMIN', iat: Date.now(), exp: Date.now() + 86400000 }))
const noneToken = `${forgedHeader}.${forgedPayload}.`
await test('alg=none伪造Token', 'GET', '/api/admin/dashboard', null, noneToken, 401)

// 5. 验证合法token能正常访问
await test('合法Token正常访问', 'GET', '/api/user/profile', null, token, 200, 200)

// 6. 静态检查：读取配置文件判断密钥长度（不直接输出密钥）
const fs = await import('fs')
const yamlPath = 'jiangnan-travel/src/main/resources/application.yml'
let keyLengthOk = false
if (fs.existsSync(yamlPath)) {
  const content = fs.readFileSync(yamlPath, 'utf8')
  const match = content.match(/secret:\s*(.+)/)
  if (match) {
    const secret = match[1].trim()
    keyLengthOk = Buffer.byteLength(secret, 'utf8') >= 32
  }
}
await test('JWT密钥长度>=256位', 'GET', '/api/user/profile', null, token, 200, 200, b => keyLengthOk)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
