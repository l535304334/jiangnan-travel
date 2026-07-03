/**
 * v1.4 极端压力与收敛行为模拟测试
 *
 * 场景:
 *   100 订单 × 20 司机
 *   30% 随机拒单
 *   10% 司机瞬时掉线（BUSY→OFFLINE→ONLINE）
 *   10% 模拟网络延迟（锁超时重试）
 *
 * 观察: scoring收敛 / driver ranking稳定 / 成功率趋势 / 异常检测
 *
 * 前置: 后端启动，≥20个司机（status=1 ONLINE_IDLE）
 * 运行: node tests/extreme_stress_convergence_test.mjs
 */
import { request, getToken } from './test-helper.mjs'

const TOTAL_ORDERS = 100
const TOTAL_DRIVERS = 20
const REJECT_RATE = 0.30
const DROPOFF_RATE = 0.10
const DELAY_RATE = 0.10

let passed = 0, failed = 0
const log = (...args) => console.log(...args)

async function test(name, fn) {
  try { await fn(); passed++; log(`  \x1b[32m✓\x1b[0m ${name}`) }
  catch (e) { failed++; log(`  \x1b[31m✗\x1b[0m ${name}: ${e.message}`) }
}
function assert(cond, msg) { if (!cond) throw new Error(msg) }

// ── Main ──
log('\n╔══════════════════════════════════════════╗')
log('║  v1.4 极端压力 + 收敛分析               ║')
log('║  100 orders × 20 drivers × 30% reject   ║')
log('╚══════════════════════════════════════════╝\n')

log('[Setup] 登录 + 重置所有状态...')
const userToken = await getToken('13900001111', '123456')
await request('POST', '/api/dispatch/reset-all', {}, userToken)
log(`  user=${!!userToken}\n`)

// ── Phase 1: 创建+支付100订单 ──
const orderIds = []
await test('Phase 1: 创建并支付 100 订单', async () => {
  const ts = Date.now()
  for (let i = 0; i < TOTAL_ORDERS; i++) {
    const { body } = await request('POST', '/api/order/create', {
      startAddress: `P${i}`, startLat: 31.2 + (i % 30) * 0.005,
      startLng: 121.4 + (i % 30) * 0.005,
      endAddress: `D${i}`, endLat: 31.35, endLng: 121.55,
      idempotentKey: `extreme-${ts}-${i}`
    }, userToken)
    orderIds.push(body?.data?.id)
    await request('POST', '/api/payment/pay', {
      orderId: body?.data?.id, payMethod: 'balance',
      idempotentKey: `ext-pay-${ts}-${i}`
    }, userToken)
  }
  log(`    创建+支付: ${orderIds.length}`)
})

// ── Phase 2: 司机心跳 + 模拟掉线 ──
await test('Phase 2: 20司机心跳 + 10%随机掉线', async () => {
  let dropoffs = 0
  for (let d = 1; d <= TOTAL_DRIVERS; d++) {
    if (Math.random() < DROPOFF_RATE) {
      // 模拟掉线: 置为OFFLINE, 50ms后恢复ONLINE
      await request('POST', `/api/driver/${d}/release`, {}, userToken).catch(() => {})
      await new Promise(r => setTimeout(r, 50))
      dropoffs++
    }
    await request('POST', `/api/driver/${d}/heartbeat?lat=${31.23 + d * 0.01}&lng=${121.47 + d * 0.01}`, {}, userToken)
  }
  log(`    掉线: ${dropoffs}/${TOTAL_DRIVERS} drivers`)
})

// ── Phase 3: 5轮批量派单（模拟收敛过程）──
const convergenceHistory = []
await test('Phase 3: 5轮批量派单观测收敛', async () => {
  for (let round = 1; round <= 5; round++) {
    const roundStart = Date.now()
    const pending = orderIds.filter(async oid => {
      const { body } = await request('GET', `/api/order/${oid}`, null, userToken)
      return (body?.data?.status === 7) // PAID = re-dispatchable
    })

    // 取前20个待派单的订单
    const batch = orderIds.slice((round - 1) * 20, round * 20)
    if (batch.length === 0) continue

    // 模拟10%网络延迟
    if (Math.random() < DELAY_RATE) {
      await new Promise(r => setTimeout(r, 100 + Math.random() * 200))
    }

    const { body } = await request('POST', '/api/order/batch-dispatch', { orderIds: batch }, userToken)
    const success = body?.data?.success ?? 0
    const elapsed = Date.now() - roundStart

    // Get metrics for this round
    const { body: metricsBody } = await request('GET', '/api/dispatch/metrics', null, userToken)
    const m = metricsBody?.data ?? {}

    convergenceHistory.push({
      round, success, total: batch.length,
      elapsedMs: elapsed,
      successRate: m.successRate,
      rejectionCount: m.rejectionCount,
      avgLatencyMs: m.avgLatencyMs,
      avgAttempts: m.avgAttempts
    })

    log(`    Round ${round}: ${success}/${batch.length} success, ${elapsed}ms, rate=${m.successRate}%`)

    // 模拟部分司机拒单
    for (const oid of batch) {
      if (Math.random() < REJECT_RATE) {
        const { body: ob } = await request('GET', `/api/order/${oid}`, null, userToken)
        const did = ob?.data?.driverId
        if (did) {
          await request('POST', `/api/order/${oid}/reject-assignment?driverId=${did}&reason=压测模拟`, {}, userToken)
            .catch(() => {})
        }
      }
    }

    // Small delay between rounds
    await new Promise(r => setTimeout(r, 50))
  }
})

// ── Phase 4: 收敛分析 ──
await test('Phase 4: 收敛行为分析', async () => {
  log('')
  log('    Round │ Success │ Rate(%) │ Rejects │ Latency(ms) │ Attempts')
  log('    ──────┼─────────┼─────────┼─────────┼─────────────┼──────────')
  for (const r of convergenceHistory) {
    log(`    ${String(r.round).padStart(5)} │ ${String(r.success).padStart(7)} │ ${String(r.successRate).padStart(7)} │ ${String(r.rejectionCount).padStart(7)} │ ${String(r.avgLatencyMs).padStart(11)} │ ${String(r.avgAttempts).padStart(8)}`)
  }

  // Check convergence: successRate should stabilize
  if (convergenceHistory.length >= 3) {
    const rates = convergenceHistory.map(r => r.successRate)
    const variance = rates.length > 1
      ? rates.reduce((sum, r, i, arr) => {
          const mean = arr.reduce((a, b) => a + b, 0) / arr.length
          return sum + (r - mean) ** 2
        }, 0) / rates.length
      : 0
    const isStable = variance < 100 // variance < 100%² = relatively stable
    log(`\n    成功率方差: ${Math.round(variance)} (${isStable ? '稳定 ✓' : '震荡 ⚠'})`)
  }
})

// ── Phase 5: 一致性验证 ──
await test('Phase 5: 最终一致性 — 无重复司机/无丢失订单', async () => {
  const driverToOrders = new Map()
  let lost = 0
  for (const oid of orderIds) {
    const { body } = await request('GET', `/api/order/${oid}`, null, userToken)
    const st = body?.data?.status
    if (st === undefined || st === null) { lost++; continue }
    const did = body?.data?.driverId
    if (did && st === 9) {
      if (!driverToOrders.has(did)) driverToOrders.set(did, [])
      driverToOrders.get(did).push(oid)
    }
  }

  let duplicates = 0
  driverToOrders.forEach((orders, did) => {
    if (orders.length > 1) { duplicates++; log(`    ⚠ Driver ${did}: ${orders.length} active`) }
  })

  assert(duplicates === 0, `${duplicates} drivers with duplicate assignments`)
  assert(lost === 0, `${lost} lost orders`)
  log(`    ${orderIds.length} orders OK, ${driverToOrders.size} unique drivers, 0 duplicates`)
})

// ── Phase 6: 异常检测 ──
await test('Phase 6: 异常行为检测', async () => {
  const { body } = await request('GET', '/api/dispatch/anomalies', null, userToken)
  const a = body?.data
  assert(a, 'anomaly report available')
  log(`    严重问题: ${a.hasCriticalIssues}`)
  if (a.driverRejectSpikes?.length > 0) {
    log(`    司机拒单异常: ${a.driverRejectSpikes.length} drivers`)
    a.driverRejectSpikes.slice(0, 3).forEach(s => log(`      ${s}`))
  }
  if (a.orderRepeatedFailures?.length > 0) {
    log(`    订单重复失败: ${a.orderRepeatedFailures.length} orders`)
    a.orderRepeatedFailures.slice(0, 3).forEach(s => log(`      ${s}`))
  }
  if (a.lockContentionSpikes?.length > 0) {
    log(`    锁竞争热点: ${a.lockContentionSpikes.length}`)
    a.lockContentionSpikes.slice(0, 3).forEach(s => log(`      ${s}`))
  }
  if (a.scoringVolatility?.length > 0) {
    log(`    评分波动: ${a.scoringVolatility.length} drivers`)
  }
})

// ── Phase 7: 健康评分 ──
await test('Phase 7: 系统健康评分', async () => {
  const { body } = await request('GET', '/api/dispatch/health', null, userToken)
  const h = body?.data
  assert(h, 'health report available')
  log(`    综合评分: ${h.overallScore}/100 (${h.grade})`)
  log(`    成功率: ${h.successRateScore} | 稳定性: ${h.stabilityScore} | 冲突: ${h.conflictPenalty} | 延迟: ${h.latencyScore}`)
  log(`    建议: ${h.recommendation}`)
})

// ── Phase 8: Scoring收敛分析 ──
await test('Phase 8: 评分收敛分析', async () => {
  const { body } = await request('GET', '/api/dispatch/scoring-state', null, userToken)
  const states = body?.data ?? {}
  const entries = Object.entries(states)
  if (entries.length > 0) {
    const modifiers = entries.map(([, s]) => s.modifier).filter(m => m !== 0)
    if (modifiers.length > 0) {
      const avg = modifiers.reduce((a, b) => a + b, 0) / modifiers.length
      const maxAbs = Math.max(...modifiers.map(Math.abs))
      log(`    活跃评分状态: ${entries.length} drivers`)
      log(`    modifier平均: ${avg.toFixed(2)}, 最大绝对值: ${maxAbs.toFixed(2)}`)
      log(`    状态: ${maxAbs < 20 ? '已收敛 ✓' : '仍在震荡 ⚠'}`)
    } else {
      log('    (所有modifier=0 — 已完全收敛)')
    }
    // Top 5 by absolute modifier
    entries.sort((a, b) => Math.abs(b[1].modifier) - Math.abs(a[1].modifier))
    entries.slice(0, 5).forEach(([did, s]) => {
      log(`      Driver ${did}: modifier=${s.modifier}, accept=${s.acceptCount}, reject=${s.rejectCount}`)
    })
  }
})

// ── Report ──
log('\n╔══════════════════════════════════════════╗')
log(`║  Results: ${String(passed).padStart(2)} passed, ${String(failed).padStart(2)} failed              ║`)
log('╚══════════════════════════════════════════╝\n')

if (failed > 0) {
  log('❌ Some tests failed!')
  process.exit(1)
} else {
  log('✅ All extreme stress tests passed!')
}
