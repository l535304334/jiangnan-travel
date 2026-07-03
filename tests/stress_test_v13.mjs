/**
 * v1.3 系统验证与压力测试 — 50 订单 × 10 司机高并发场景
 *
 * 场景:
 *   1. 50订单同时PAID → 10司机ONLINE_IDLE → 30%随机拒单
 *   2. 冲突极限: 多订单竞争同一司机
 *   3. Scoring 动态学习: 拒单 → penalty, 接单 → reward
 *   4. 最终一致性验证
 *
 * 前置: 后端启动 + ≥10 个 ONLINE_IDLE 司机 (status=1)
 * 运行: node tests/stress_test_v13.mjs
 */
import { request, getToken } from './test-helper.mjs'

const TOTAL_ORDERS = 50
const TOTAL_DRIVERS = 10
const REJECT_RATE = 0.30

let passed = 0, failed = 0
const log = (...args) => console.log(...args)

async function test(name, fn) {
  try { await fn(); passed++; log(`  \x1b[32m✓\x1b[0m ${name}`) }
  catch (e) { failed++; log(`  \x1b[31m✗\x1b[0m ${name}: ${e.message}`) }
}
function assert(cond, msg) { if (!cond) throw new Error(msg) }

// ── Main ──
log('\n╔══════════════════════════════════════╗')
log('║  v1.3 压力测试: 50订单 × 10司机      ║')
log('╚══════════════════════════════════════╝\n')

log('[Setup] 登录 + 重置指标...')
const userToken = await getToken('13900001111', '123456')
await request('POST', '/api/dispatch/metrics/reset', {}, userToken)
log(`  user=${!!userToken}\n`)

// ── Phase 1: Create + Pay 50 orders ──
const orderIds = []
await test('Phase 1: 创建并支付 50 个订单', async () => {
  const ts = Date.now()
  for (let i = 0; i < TOTAL_ORDERS; i++) {
    const { body } = await request('POST', '/api/order/create', {
      startAddress: `起点${i}`, startLat: 31.2 + (i % 20) * 0.005,
      startLng: 121.4 + (i % 20) * 0.005,
      endAddress: `终点${i}`, endLat: 31.3, endLng: 121.5,
      idempotentKey: `stress-v13-${ts}-${i}`
    }, userToken)
    const oid = body?.data?.id
    assert(oid > 0, `订单${i}创建失败`)
    orderIds.push(oid)
    await request('POST', '/api/payment/pay', {
      orderId: oid, payMethod: 'balance', idempotentKey: `stress-pay-${ts}-${oid}`
    }, userToken)
  }
  log(`    创建+支付: ${orderIds.length} 个订单`)
})

// ── Phase 2: Driver heartbeat (simulate online + GPS) ──
await test('Phase 2: 10 个司机心跳更新（模拟位置+在线）', async () => {
  for (let d = 1; d <= TOTAL_DRIVERS; d++) {
    await request('POST', `/api/driver/${d}/heartbeat?lat=${31.23 + d * 0.01}&lng=${121.47 + d * 0.01}`, {}, userToken)
  }
  log('    10 drivers heartbeat done')
})

// ── Phase 3: Batch dispatch 50 orders concurrent ──
const startMs = Date.now()
await test('Phase 3: 50 订单批量并发派单', async () => {
  // Split into 5 batches of 10 for controlled concurrency
  const batchSize = 10
  const results = { success: 0, failure: 0 }

  for (let b = 0; b < TOTAL_ORDERS / batchSize; b++) {
    const batch = orderIds.slice(b * batchSize, (b + 1) * batchSize)
    const { body } = await request('POST', '/api/order/batch-dispatch', { orderIds: batch }, userToken)
    results.success += (body?.data?.success ?? 0)
    results.failure += (batch.length - (body?.data?.success ?? 0))
  }

  const elapsed = Date.now() - startMs
  log(`    成功: ${results.success}/${TOTAL_ORDERS}, 失败: ${results.failure}/${TOTAL_ORDERS}`)
  log(`    总耗时: ${elapsed}ms, 平均: ${Math.round(elapsed / TOTAL_ORDERS)}ms/order`)
  assert(results.success > 0, '至少有一个成功分配')
})

// ── Phase 4: Simulate driver rejections (30% rate) ──
const rejectedDrivers = new Set()
await test('Phase 4: 模拟司机拒单 (~30% 拒单率)', async () => {
  // Find assigned orders and randomly reject ~30%
  let rejected = 0, total = 0
  for (const oid of orderIds) {
    const { body } = await request('GET', `/api/order/${oid}`, null, userToken)
    const did = body?.data?.driverId
    const st = body?.data?.status
    if (did && st === 9) { // DRIVER_ASSIGNED
      total++
      if (Math.random() < REJECT_RATE) {
        try {
          await request('POST', `/api/order/${oid}/reject-assignment?driverId=${did}&reason=压测模拟拒单`, {}, userToken)
          rejected++
          rejectedDrivers.add(did)
          // Record rejection in metrics
          await request('POST', `/api/dispatch/metrics/rejection?orderId=${oid}&driverId=${did}`, {}, userToken).catch(() => {})
        } catch (e) {
          // reject may fail (re-assign → success or no drivers) — expected
        }
      }
    }
  }
  log(`    拒单: ${rejected}/${total} 已分配订单 (${total > 0 ? Math.round(rejected / total * 100) : 0}%)`)
})

// ── Phase 5: Verify consistency ──
await test('Phase 5: 一致性验证 — 无重复分配司机', async () => {
  const driverToOrders = new Map()
  for (const oid of orderIds) {
    const { body } = await request('GET', `/api/order/${oid}`, null, userToken)
    const did = body?.data?.driverId
    if (did) {
      if (!driverToOrders.has(did)) driverToOrders.set(did, [])
      driverToOrders.get(did).push(oid)
    }
  }
  // Check: each driver assigned to at most 1 order at a time (DRIVER_ASSIGNED status)
  let duplicates = 0
  for (const [did, orders] of driverToOrders) {
    const assignedOrders = []
    for (const oid of orders) {
      const { body } = await request('GET', `/api/order/${oid}`, null, userToken)
      if (body?.data?.status === 9) assignedOrders.push(oid)
    }
    if (assignedOrders.length > 1) {
      duplicates++
      log(`    ⚠ Driver ${did} has ${assignedOrders.length} active assignments: ${assignedOrders}`)
    }
  }
  assert(duplicates === 0, `${duplicates} drivers have duplicate assignments!`)
  log(`    唯一司机数: ${driverToOrders.size}，无重复分配 ✓`)
})

await test('Phase 5b: 订单状态一致性 — 无丢失', async () => {
  let lost = 0, completed = 0, assigned = 0, paid = 0
  for (const oid of orderIds) {
    const { body } = await request('GET', `/api/order/${oid}`, null, userToken)
    const st = body?.data?.status
    if (st === undefined || st === null) lost++
    else if (st === 4) completed++
    else if (st === 9 || st === 1) assigned++
    else if (st === 7) paid++
  }
  log(`    状态分布: completed=${completed}, assigned=${assigned}, paid=${paid}, lost=${lost}`)
  assert(lost === 0, `${lost} orders lost!`)
})

// ── Phase 6: Metrics ──
await test('Phase 6: 调度指标统计', async () => {
  const { body } = await request('GET', '/api/dispatch/metrics', null, userToken)
  const summary = body?.data
  assert(summary, 'metrics available')
  log(`    总尝试: ${summary.totalAttempts}`)
  log(`    成功: ${summary.successCount} (${summary.successRate}%)`)
  log(`    失败: ${summary.failureCount}`)
  log(`    拒单: ${summary.rejectionCount}`)
  log(`    平均延迟: ${summary.avgLatencyMs}ms`)
  log(`    平均尝试次数: ${summary.avgAttempts}`)
  log(`    Top司机负载: ${JSON.stringify(summary.topLoadedDrivers)}`)
})

// ── Phase 7: Scoring feedback ──
await test('Phase 7: 动态评分效果', async () => {
  const { body } = await request('GET', '/api/dispatch/scoring-state', null, userToken)
  const states = body?.data ?? {}
  const entries = Object.entries(states)
  if (entries.length > 0) {
    log(`    活跃评分状态: ${entries.length} 个司机`)
    entries.slice(0, 5).forEach(([did, s]) => {
      log(`      Driver ${did}: modifier=${s.modifier}, accept=${s.acceptCount}, reject=${s.rejectCount}`)
    })
  } else {
    log('    (无评分状态 — 可能无driver被分配/拒单)')
  }
})

// ── Phase 8: Conflict edge cases ──
await test('Phase 8: 冲突极限测试 (driver瞬时状态变化)', async () => {
  // 快速将某司机从 BUSY→IDLE→BUSY，验证不会死锁
  const testDriverId = 1
  await request('POST', `/api/driver/${testDriverId}/release`, {}, userToken)
  await request('POST', `/api/driver/${testDriverId}/heartbeat?lat=31.23&lng=121.47`, {}, userToken)
  // 同时派单
  const dispatchPromise = request('POST', `/api/order/${orderIds[0]}/dispatch`, {}, userToken).catch(() => ({ status: 500 }))
  const releasePromise = request('POST', `/api/driver/${testDriverId}/release`, {}, userToken).catch(() => ({ status: 500 }))
  const [r1, r2] = await Promise.all([dispatchPromise, releasePromise])
  log(`    并发派单+释放: dispatch=${r1.status}, release=${r2.status}`)
  // 无死锁 = 两个都返回了
})

// ── Phase 9: Scoring tie test ──
await test('Phase 9: 评分完全相同场景 (scoring tie)', async () => {
  // 重置所有评分状态，所有司机回到相同 baseline
  const { body: resetBody } = await request('POST', '/api/dispatch/metrics/reset', {}, userToken)
  assert(resetBody?.code === 200 || resetBody?.success, 'metrics reset OK')

  // All drivers have same location → same distance score → same total score (tie)
  for (let d = 1; d <= TOTAL_DRIVERS; d++) {
    await request('POST', `/api/driver/${d}/heartbeat?lat=31.23&lng=121.47`, {}, userToken)
  }

  // Create 3 fresh orders + pay + dispatch concurrently
  const tieOrderIds = []
  const ts = Date.now()
  for (let i = 0; i < 3; i++) {
    const { body } = await request('POST', '/api/order/create', {
      startAddress: 'TieTest', startLat: 31.23, startLng: 121.47,
      endAddress: 'End', endLat: 31.3, endLng: 121.5,
      idempotentKey: `tie-${ts}-${i}`
    }, userToken)
    tieOrderIds.push(body?.data?.id)
    await request('POST', '/api/payment/pay', {
      orderId: body?.data?.id, payMethod: 'balance',
      idempotentKey: `tie-pay-${ts}-${i}`
    }, userToken)
  }

  // Concurrent dispatch (tie)
  const tieResults = await Promise.all(tieOrderIds.map(oid =>
    request('POST', `/api/order/${oid}/dispatch`, {}, userToken).catch(() => ({ status: 500 }))
  ))
  const tieSuccess = tieResults.filter(r => r.status === 200).length
  // Verify unique drivers
  const tieDrivers = new Set()
  for (const oid of tieOrderIds) {
    const { body } = await request('GET', `/api/order/${oid}`, null, userToken)
    if (body?.data?.driverId) tieDrivers.add(body.data.driverId)
  }
  log(`    Scoring tie dispatch: ${tieSuccess}/${tieOrderIds.length} success, ${tieDrivers.size} unique drivers`)
  assert(tieDrivers.size <= tieOrderIds.length, 'unique drivers ≤ orders')
  if (tieSuccess >= 2) {
    assert(tieDrivers.size >= Math.min(2, tieSuccess), 'Scoring tie should distribute to different drivers')
  }
})

// ── Report ──
log('\n╔══════════════════════════════════════╗')
log(`║  Results: ${String(passed).padStart(2)} passed, ${String(failed).padStart(2)} failed        ║`)
log('╚══════════════════════════════════════╝\n')

if (failed > 0) {
  log('❌ Some tests failed!')
  process.exit(1)
} else {
  log('✅ All stress tests passed!')
}
