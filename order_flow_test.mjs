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

function assert(cond, msg) {
  if (!cond) throw new Error(msg)
}

console.log('=== 下单流程测试 ===')

// 1. 登录获取 token
const login = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = login.body.data.token
const userId = login.body.data.userId

// 2. 获取可用车型和优惠券
const carTypesRes = await request('GET', '/api/admin/car-types', null, userToken)
const carTypes = carTypesRes.body?.data?.records || carTypesRes.body?.data || []
const firstCarType = Array.isArray(carTypes) && carTypes.length > 0 ? carTypes[0] : null

const couponsRes = await request('GET', '/api/coupon/my', null, userToken)
const myCoupons = couponsRes.body?.data || []
// 选择一张未使用且可匹配的优惠券
const firstCoupon = Array.isArray(myCoupons)
  ? myCoupons.find(c => c.status === 0)
  : null

const sampleOrder = {
  startAddress: '江南大学东门',
  startLat: 31.2304,
  startLng: 121.4737,
  endAddress: '无锡火车站',
  endLat: 31.2504,
  endLng: 121.4937,
  distance: 5000,
  duration: 600,
  carTypeId: firstCarType?.id || 1
}

// 3. 预估价格（不带优惠券）
await test('预估价格-基础', 'POST', '/api/order/estimate', sampleOrder, userToken, 200, 200, b => {
  return b.data && b.data.estimateTotal > 0 && b.data.carTypeName
})

// 4. 预估价格（带优惠券）
if (firstCoupon) {
  await test('预估价格-使用优惠券', 'POST', '/api/order/estimate', {
    startAddress: '南昌市八一广场',
    startLat: 28.6820,
    startLng: 115.8579,
    endAddress: '赣州市黄金广场',
    endLat: 25.8604,
    endLng: 114.9350,
    distance: 100000,
    duration: 7200,
    carTypeId: 4,
    couponId: firstCoupon.id
  }, userToken, 200, 200, b => {
    return b.data && b.data.couponDiscount > 0
  })
}

// 5. 创建订单（基础流程）
let createdOrderId = null
await test('创建订单-基础', 'POST', '/api/order/create', {
  ...sampleOrder,
  idempotentKey: `test-${Date.now()}`
}, userToken, 200, 200, b => {
  createdOrderId = b.data?.id
  return b.data && b.data.id && b.data.orderNo && b.data.status === 0
})

// 6. 创建订单（使用优惠券）
if (firstCoupon) {
  // 使用长途订单确保金额超过优惠券门槛
  await test('创建订单-使用优惠券', 'POST', '/api/order/create', {
    startAddress: '南昌市八一广场',
    startLat: 28.6820,
    startLng: 115.8579,
    endAddress: '赣州市黄金广场',
    endLat: 25.8604,
    endLng: 114.9350,
    distance: 100000,
    duration: 7200,
    carTypeId: 4,
    couponId: firstCoupon.id,
    idempotentKey: `test-coupon-${Date.now()}`
  }, userToken, 200, 200, b => {
    return b.data && b.data.couponDiscount > 0
  })
}

// 7. 创建订单（无效车型）
await test('创建订单-无效车型', 'POST', '/api/order/create', {
  ...sampleOrder,
  carTypeId: 99999,
  idempotentKey: `test-invalid-car-${Date.now()}`
}, userToken, 200, 2005)

// 8. 创建订单（缺少必填参数）
await test('创建订单-缺少起点地址', 'POST', '/api/order/create', {
  ...sampleOrder,
  startAddress: undefined
}, userToken, 200, 400)

// 9. 订单列表能查到刚创建的订单
if (createdOrderId) {
  await test('订单列表包含新订单', 'GET', '/api/order/list', null, userToken, 200, 200, b => {
    const list = b.data || []
    return list.some(o => o.id === createdOrderId)
  })

  // 10. 取消刚创建的订单，避免测试数据残留
  await test('取消测试订单', 'PUT', `/api/order/${createdOrderId}/cancel`, { reason: '测试取消' }, userToken, 200, 200)
}

results.forEach(r => {
  const icon = r.ok ? '✅' : '❌'
  console.log(`${icon} ${r.name} -> HTTP:${r.status} (expected:${r.expected}) code:${r.code ?? '-'}`)
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
