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

console.log('=== 优惠券全生命周期测试 ===')

const login = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const token = login.body.data.token
const userId = login.body.data.userId

// 1. 公开优惠券列表
let claimableCouponId = null
await test('公开优惠券列表', 'GET', '/api/coupon/list', null, null, 200, 200, b => {
  const list = b.data || []
  const avail = list.find(c => c.status === 1 && c.discount > 0)
  if (avail) claimableCouponId = avail.id
  return list.length > 0
})

// 2. 我的优惠券（可能包含已领取）
const myBefore = await request('GET', '/api/coupon/my', null, token)
const alreadyClaimedIds = (myBefore.body?.data || []).map(c => c.couponId)

// 找一个未领取的优惠券
if (claimableCouponId && alreadyClaimedIds.includes(claimableCouponId)) {
  const publicList = await request('GET', '/api/coupon/list', null, null)
  const all = publicList.body?.data || []
  const next = all.find(c => c.status === 1 && !alreadyClaimedIds.includes(c.id))
  if (next) claimableCouponId = next.id
  else claimableCouponId = null
}

if (!claimableCouponId) {
  console.log('⚠️ 没有可领取的新优惠券，跳过重领测试')
}

// 3. 领取优惠券
let claimedUserCouponId = null
if (claimableCouponId) {
  await test('领取优惠券', 'POST', '/api/coupon/claim', { couponId: claimableCouponId }, token, 200, 200)

  // 4. 我的优惠券包含新券
  await test('我的优惠券包含新券', 'GET', '/api/coupon/my', null, token, 200, 200, b => {
    const list = b.data || []
    const found = list.find(c => c.couponId === claimableCouponId)
    if (found) claimedUserCouponId = found.id
    return !!found
  })

  // 5. 重复领取应失败
  await test('重复领取优惠券', 'POST', '/api/coupon/claim', { couponId: claimableCouponId }, token, 200, 5101)
}

// 6. 使用优惠券下单（如果领取成功）
if (claimedUserCouponId) {
  await test('使用优惠券下单', 'POST', '/api/order/create', {
    startAddress: '南昌市八一广场',
    startLat: 28.6820,
    startLng: 115.8579,
    endAddress: '赣州市黄金广场',
    endLat: 25.8604,
    endLng: 114.9350,
    distance: 100000,
    duration: 7200,
    carTypeId: 4,
    couponId: claimedUserCouponId,
    idempotentKey: `coupon-life-${Date.now()}`
  }, token, 200, 200, b => {
    return b.data && b.data.couponDiscount > 0
  })
}

// 7. 无Token访问我的优惠券返回401
await test('无Token访问我的优惠券', 'GET', '/api/coupon/my', null, null, 401, 401)

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
