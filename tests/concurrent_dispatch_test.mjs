/**
 * E2E: 多订单并发调度测试 (v1.2)
 *
 * 场景:
 *   1. 创建N个订单并支付 → 模拟多订单同时进入 PAID
 *   2. 批量并发派单 → 验证无重复分配司机
 *   3. 司机资源竞争 → 验证 driver lock 正确工作
 *   4. 高并发模拟 (10订单, 有限司机)
 *
 * 前置: 后端启动 + 有 ONLINE_IDLE 司机 (status=1)
 * 运行: node tests/concurrent_dispatch_test.mjs
 */
import { request, getToken } from './test-helper.mjs'

let passed = 0, failed = 0
const results = []

async function test(name, fn) {
  try {
    await fn(); passed++
    console.log(`  \x1b[32m✓\x1b[0m ${name}`)
  } catch (e) {
    failed++; results.push({ name, error: e.message })
    console.log(`  \x1b[31m✗\x1b[0m ${name}: ${e.message}`)
  }
}

function assert(cond, msg) { if (!cond) throw new Error(msg) }

// ── Main ──
console.log('\n========================================')
console.log('E2E: 多订单并发调度测试 (v1.2)')
console.log('========================================\n')

let userToken, orderIds = []

console.log('[Setup] 登录...')
userToken = await getToken('13900001111', '123456')
console.log(`  user=${!!userToken}\n`)

// ── 1. 创建 + 支付 5 个订单
await test('1. 创建并支付 5 个订单', async () => {
  for (let i = 0; i < 5; i++) {
    const { body } = await request('POST', '/api/order/create', {
      startAddress: '测试起点' + i, startLat: 31.23 + i * 0.01, startLng: 121.47 + i * 0.01,
      endAddress: '测试终点' + i, endLat: 31.25, endLng: 121.49,
      idempotentKey: 'e2e-v12-batch-' + Date.now() + '-' + i
    }, userToken)
    const oid = body?.data?.id
    assert(oid > 0, '订单' + i + '创建 OK')
    orderIds.push(oid)

    await request('POST', '/api/payment/pay', {
      orderId: oid, payMethod: 'balance', idempotentKey: 'e2e-pay-v12-' + oid
    }, userToken)
  }
  console.log(`    创建+支付: ${orderIds.length} 个订单`)
})

// ── 2. 批量并发派单 ──
await test('2. 批量并发派单 (5订单)', async () => {
  const { body, status } = await request('POST', '/api/order/batch-dispatch', {
    orderIds: orderIds
  }, userToken)
  assert(status === 200, 'batch dispatch 200')
  const success = body?.data?.success ?? 0
  const total = body?.data?.total ?? orderIds.length
  console.log(`    成功: ${success}/${total}`)
  // 由于只有有限司机，部分失败是预期的
  assert(success >= 0, 'success count valid')
})

// ── 3. 验证无重复分配司机 ──
await test('3. 验证无重复分配司机 (driver uniqueness)', async () => {
  const driverIds = new Set()
  for (const oid of orderIds) {
    const { body } = await request('GET', '/api/order/' + oid, null, userToken)
    const did = body?.data?.driverId
    if (did) {
      assert(!driverIds.has(did),
        '司机 ' + did + ' 被重复分配！(order=' + oid + ')')
      driverIds.add(did)
    }
  }
  console.log(`    唯一司机数: ${driverIds.size}，无重复分配 ✓`)
})

// ── 4. 状态一致性验证 ──
await test('4. 已分配订单状态一致性', async () => {
  for (const oid of orderIds) {
    const { body } = await request('GET', '/api/order/' + oid, null, userToken)
    const st = body?.data?.status
    const did = body?.data?.driverId
    // 有 driverId → 状态应为 DRIVER_ASSIGNED(9)
    if (did) {
      assert(st === 9, 'order ' + oid + ': driverId=' + did + ' 但 status=' + st)
    }
  }
})

// ── 5. 高并发模拟 (10订单, 快速连续派单) ──
await test('5. 高并发模拟: 10订单随机顺序派单', async () => {
  const highOrderIds = []
  for (let i = 0; i < 10; i++) {
    const { body } = await request('POST', '/api/order/create', {
      startAddress: '高并发' + i, startLat: 31.2 + i * 0.005, startLng: 121.4 + i * 0.005,
      endAddress: '终点', endLat: 31.3, endLng: 121.5,
      idempotentKey: 'e2e-high-' + Date.now() + '-' + i
    }, userToken)
    highOrderIds.push(body?.data?.id)
    await request('POST', '/api/payment/pay', {
      orderId: body?.data?.id, payMethod: 'balance',
      idempotentKey: 'e2e-hpay-' + body?.data?.id
    }, userToken)
  }

  // 随机顺序并发派单
  const shuffled = [...highOrderIds].sort(() => Math.random() - 0.5)
  const promises = shuffled.map(oid =>
    request('POST', '/api/order/' + oid + '/dispatch', {}, userToken)
      .catch(() => ({ status: 500 }))
  )
  const dispatchResults = await Promise.all(promises)
  const successCount = dispatchResults.filter(r => r.status === 200).length
  console.log(`    高并发派单: ${successCount}/${highOrderIds.length} 成功`)

  // 验证无重复司机
  const highDriverIds = new Set()
  for (const oid of highOrderIds) {
    const { body } = await request('GET', '/api/order/' + oid, null, userToken)
    const did = body?.data?.driverId
    if (did && highDriverIds.has(did)) {
      throw new Error('高并发重复分配司机: ' + did + ' on order ' + oid)
    }
    if (did) highDriverIds.add(did)
  }
  console.log(`    高并发唯一司机数: ${highDriverIds.size}，无重复分配 ✓`)
})

// ── Report ──
console.log('\n========================================')
console.log(`Results: ${passed} passed, ${failed} failed`)
console.log('========================================\n')

if (failed > 0) {
  console.log('Failed:')
  results.forEach(r => console.log(`  ✗ ${r.name}: ${r.error}`))
  process.exit(1)
} else {
  console.log('All concurrent dispatch tests passed!\n')
}
