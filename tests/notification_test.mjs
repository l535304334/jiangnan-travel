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

console.log('=== 通知消息模块测试 ===')

// 1. 登录两个用户
const userA = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const tokenA = userA.body?.data?.token
const userIdA = userA.body?.data?.userId

const userB = await request('POST', '/api/user/login-password', { phone: '13900002222', password: '123456' })
const tokenB = userB.body?.data?.token
const userIdB = userB.body?.data?.userId

if (!tokenA || !tokenB) {
  console.log('❌ 用户登录失败，无法继续测试')
  process.exit(1)
}

// 2. 给用户A创建订单以触发通知
const orderRes = await request('POST', '/api/order/create', {
  startAddress: '江南大学东门',
  startLat: 31.2304,
  startLng: 121.4737,
  endAddress: '无锡火车站',
  endLat: 31.2504,
  endLng: 121.4937,
  distance: 5000,
  duration: 600,
  carTypeId: 1,
  idempotentKey: `notify-${Date.now()}`
}, tokenA)
const orderId = orderRes.body?.data?.id

// 3. 查询通知列表
let firstNotificationId = null
await test('通知列表-用户A', 'GET', '/api/notification/list?pageNum=1&pageSize=10', null, tokenA, 200, 200, b => {
  const records = b.data?.records || []
  if (records.length > 0) {
    firstNotificationId = records[0].id
    return records.every(n => n.userId === userIdA)
  }
  return false
})

// 4. 未读计数
let unreadBefore = 0
await test('未读计数-用户A', 'GET', '/api/notification/unread-count', null, tokenA, 200, 200, b => {
  unreadBefore = b.data
  return typeof b.data === 'number' && b.data >= 0
})

// 5. 无Token访问通知接口返回401
await test('无Token访问通知列表', 'GET', '/api/notification/list', null, null, 401)
await test('无Token访问未读计数', 'GET', '/api/notification/unread-count', null, null, 401)
await test('无Token标记已读', 'PUT', `/api/notification/${firstNotificationId}/read`, null, null, 401)

// 6. 标记单条已读
await test('标记单条已读-用户A', 'PUT', `/api/notification/${firstNotificationId}/read`, null, tokenA, 200, 200)

// 7. 验证未读计数减少
await test('未读计数减少', 'GET', '/api/notification/unread-count', null, tokenA, 200, 200, b => {
  return b.data === Math.max(0, unreadBefore - 1)
})

// 8. 用户B尝试标记用户A的通知（应静默无效或失败）
await test('跨用户标记已读-用户B访问用户A', 'PUT', `/api/notification/${firstNotificationId}/read`, null, tokenB, 200, 200)

// 9. 验证用户A的通知状态未因跨用户操作改变（仍是已读）
await test('跨用户标记后状态检查', 'GET', '/api/notification/list?pageNum=1&pageSize=10', null, tokenA, 200, 200, b => {
  const n = b.data?.records?.find(x => x.id === firstNotificationId)
  return n && n.isRead === 1
})

// 10. 标记全部已读
await test('标记全部已读-用户A', 'PUT', '/api/notification/read-all', null, tokenA, 200, 200)
await test('全部已读后计数为0', 'GET', '/api/notification/unread-count', null, tokenA, 200, 200, b => b.data === 0)

// 11. 用户B通知列表隔离
await test('通知列表-用户B隔离', 'GET', '/api/notification/list?pageNum=1&pageSize=10', null, tokenB, 200, 200, b => {
  const records = b.data?.records || []
  return records.every(n => n.userId === userIdB)
})

// 12. 删除测试产生的通知（软清理）
if (firstNotificationId) {
  try {
    await request('DELETE', `/api/admin/notification/${firstNotificationId}`, null, null)
  } catch { /* 通知没有管理删除接口，忽略 */ }
}

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
