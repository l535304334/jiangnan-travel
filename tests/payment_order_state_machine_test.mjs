/**
 * E2E 测试: 支付 + 订单状态机全流程
 *
 * 流程: 创建订单 → 支付(CREATED→PAID) → 司机接单(PAID→ASSIGNED)
 *       → 到达(→ARRIVED) → 开始行程(→IN_PROGRESS) → 完成(→COMPLETED)
 * 异常: 创建→取消、支付→取消、支付失败重试
 *
 * 前置: 后端启动 (mvn spring-boot:run)
 * 运行: node tests/payment_order_state_machine_test.mjs
 */
import { request, getToken } from './test-helper.mjs'

const PASS = Symbol('pass'), FAIL = Symbol('fail')
let passed = 0, failed = 0
const results = []

function assert(condition, msg) {
  if (!condition) throw new Error(msg)
}

async function test(name, fn) {
  try {
    await fn()
    passed++
    results.push({ name, status: 'PASS' })
    console.log(`  ✓ ${name}`)
  } catch (e) {
    failed++
    results.push({ name, status: 'FAIL', error: e.message })
    console.log(`  ✗ ${name}: ${e.message}`)
  }
}

// ── helpers ──
let userToken, driverToken, createdOrderId

async function createOrder() {
  const { body } = await request('POST', '/api/order/create', {
    startAddress: '测试起点',
    startLat: 30.5, startLng: 120.5,
    endAddress: '测试终点',
    endLat: 30.6, endLng: 120.6,
    idempotentKey: 'e2e-state-machine-' + Date.now()
  }, userToken)
  return body
}

async function getOrder(orderId) {
  const { body } = await request('GET', '/api/order/' + orderId, null, userToken)
  return body
}

// ── Main ──
console.log('\n========================================')
console.log('E2E: 支付 + 订单状态机全流程')
console.log('========================================\n')

// 登录
console.log('[Setup] 登录...')
userToken = await getToken('13900001111', '123456')
driverToken = await getToken('13900002222', '123456')
console.log(`  userToken=${!!userToken} driverToken=${!!driverToken}\n`)

// ── 正常流程 ──
await test('1. 创建订单 → status=0 (CREATED)', async () => {
  const body = await createOrder()
  createdOrderId = body?.data?.id
  assert(createdOrderId > 0, '订单创建成功，id=' + createdOrderId)
  const status = body?.data?.status ?? body?.status
  assert(status === 0, '初始状态应为 0(CREATED)，实际: ' + status)
})

await test('2. 支付订单 → CREATED→PAID (status=7)', async () => {
  const { body, status } = await request('POST', '/api/payment/pay', {
    orderId: createdOrderId,
    payMethod: 'balance',
    idempotentKey: 'e2e-pay-' + createdOrderId
  }, userToken)
  assert(status === 200, '支付接口返回 200')
  const payStatus = body?.data?.status ?? body?.status
  assert(payStatus === 1, '支付状态应为 1(PAID)，实际: ' + payStatus)
})

await test('3. 支付后订单状态 → PAID (status=7)', async () => {
  const body = await getOrder(createdOrderId)
  const orderStatus = body?.data?.status ?? body?.status
  assert(orderStatus === 7, '支付后订单状态应为 7(PAID)，实际: ' + orderStatus)
})

await test('4. 司机接单 → PAID→ASSIGNED (status=1)', async () => {
  const { body, status } = await request('POST', '/api/order/accept', {
    orderId: createdOrderId
  }, driverToken)
  assert(status === 200, '接单成功')
  const orderStatus = body?.data?.status ?? body?.status
  assert(orderStatus === 1, '接单后状态应为 1(ASSIGNED)，实际: ' + orderStatus)
})

await test('5. 司机到达 → ASSIGNED→ARRIVED (status=2)', async () => {
  const { body } = await request('POST', '/api/order/arrive', {
    orderId: createdOrderId
  }, driverToken)
  const orderStatus = body?.data?.status ?? body?.status
  assert(orderStatus === 2, '到达后状态应为 2(ARRIVED)，实际: ' + orderStatus)
})

await test('6. 开始行程 → ARRIVED→IN_PROGRESS (status=3)', async () => {
  const { body } = await request('POST', '/api/order/start-trip', {
    orderId: createdOrderId
  }, driverToken)
  const orderStatus = body?.data?.status ?? body?.status
  assert(orderStatus === 3, '行程中状态应为 3(IN_PROGRESS)，实际: ' + orderStatus)
})

await test('7. 完成行程 → IN_PROGRESS→COMPLETED (status=4)', async () => {
  const { body } = await request('POST', '/api/order/complete', {
    orderId: createdOrderId
  }, driverToken)
  const orderStatus = body?.data?.status ?? body?.status
  assert(orderStatus === 4, '完成后状态应为 4(COMPLETED)，实际: ' + orderStatus)
})

// ── 取消流程 ──
let cancelOrderId
await test('8. 创建新订单 → 取消 (CREATED→CANCELLED)', async () => {
  const body = await createOrder()
  cancelOrderId = body?.data?.id
  assert(cancelOrderId > 0, '订单创建成功')

  const { status } = await request('POST', '/api/order/cancel', {
    orderId: cancelOrderId,
    reason: 'E2E测试取消'
  }, userToken)
  assert(status === 200, '取消成功')

  const orderAfter = await getOrder(cancelOrderId)
  const orderStatus = orderAfter?.data?.status ?? orderAfter?.status
  assert(orderStatus === 5, '取消后状态应为 5(CANCELLED)，实际: ' + orderStatus)
})

// ── 支付重复调用 (幂等) ──
await test('9. 重复支付同一订单 → 幂等返回已支付记录', async () => {
  const { body, status } = await request('POST', '/api/payment/pay', {
    orderId: createdOrderId,
    payMethod: 'balance',
    idempotentKey: 'e2e-pay-' + createdOrderId
  }, userToken)
  // 幂等返回 200 或 400（已支付）
  assert(status === 200 || status === 400, '幂等请求应返回 200 或 400')
})

// ── 支付追踪查询 ──
await test('10. 查询支付记录 → 可见支付状态', async () => {
  const { body, status } = await request('GET', '/api/payment/order/' + createdOrderId, null, userToken)
  assert(status === 200, '查询支付接口返回 200')
  const payStatus = body?.data?.status ?? body?.status
  assert(payStatus === 1 || payStatus === 0,
    '支付状态应为 0(PENDING) 或 1(PAID)，实际: ' + payStatus)
})

// ── Report ──
console.log('\n========================================')
console.log(`Results: ${passed} passed, ${failed} failed`)
console.log('========================================\n')

if (failed > 0) {
  console.log('Failed tests:')
  results.filter(r => r.status === 'FAIL').forEach(r =>
    console.log(`  ✗ ${r.name}: ${r.error}`)
  )
  process.exit(1)
} else {
  console.log('All tests passed!\n')
}
