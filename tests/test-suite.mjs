// 江南出行全量测试编排器
// 用法：node test-suite.mjs [--skip-security] [--cleanup]

import { spawn, execSync } from 'node:child_process'
import { existsSync, writeFileSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const CLEANUP_SQL = join(__dirname, 'cleanup_test_data.sql')

// 数据库连接配置 — 通过环境变量 DB_PASS 注入，禁止硬编码
const DB_HOST = process.env.DB_HOST || 'localhost'
const DB_PORT = process.env.DB_PORT || '3306'
const DB_USER = process.env.DB_USER || 'root'
const DB_PASS = process.env.DB_PASS || ''
const DB_NAME = process.env.DB_NAME || 'smart_travel'

const args = process.argv.slice(2)
const skipSecurity = args.includes('--skip-security')
const cleanup = args.includes('--cleanup')

// 测试脚本分组（按依赖顺序执行）
const groups = [
  {
    name: '核心链路回归',
    scripts: [
      'auth_boundary_test.mjs',
      'order_flow_test.mjs',
      'driver_flow_test.mjs',
      'payment_review_test.mjs',
      'comprehensive_api_test.mjs'
    ]
  },
  {
    name: '业务模块专项',
    scripts: [
      'cancel_refund_test.mjs',
      'notification_test.mjs',
      'address_test.mjs',
      'coupon_lifecycle_test.mjs',
      'vip_test.mjs',
      'bus_line_test.mjs'
    ]
  },
  {
    name: '安全与边界',
    scripts: [
      'idor_security_test.mjs',
      'jwt_security_test.mjs',
      'payment_security_test.mjs',
      'input_security_test.mjs'
    ]
  },
  {
    name: '并发、风控与实时消息',
    scripts: [
      'concurrent_test.mjs',
      'risk_rules_test.mjs',
      'websocket_test.mjs'
    ]
  }
]

if (skipSecurity) {
  const idx = groups.findIndex(g => g.name === '安全与边界')
  if (idx >= 0) groups.splice(idx, 1)
}

const results = []

function clearRedisLimits() {
  try {
    execSync('redis-cli --no-auth-warning -h 127.0.0.1 -p 6379 KEYS "rate:*" 2>nul', { encoding: 'utf8', shell: true })
      .trim().split(/\r?\n/).filter(Boolean)
      .forEach(k => {
        try { execSync(`redis-cli --no-auth-warning -h 127.0.0.1 -p 6379 DEL "${k.trim()}" 2>nul`, { stdio: 'ignore', shell: true }) } catch {}
      })
  } catch { /* redis-cli unavailable */ }
}

function runScript(script) {
  return new Promise((resolve) => {
    const start = Date.now()
    const child = spawn('node', [join(__dirname, script)], {
      stdio: ['ignore', 'pipe', 'pipe']
    })
    let stdout = ''
    let stderr = ''
    child.stdout.on('data', (data) => { stdout += data.toString() })
    child.stderr.on('data', (data) => { stderr += data.toString() })
    child.on('close', (code) => {
      const duration = Date.now() - start
      const summaryMatch = stdout.match(/总计:\s*(\d+)\s*\|\s*通过:\s*(\d+)\s*\|\s*失败:\s*(\d+)/)
      const total = summaryMatch ? parseInt(summaryMatch[1], 10) : 0
      const passed = summaryMatch ? parseInt(summaryMatch[2], 10) : 0
      const failed = summaryMatch ? parseInt(summaryMatch[3], 10) : 0
      const crashed = code !== 0 && failed === 0
      resolve({
        script,
        code,
        duration,
        total,
        passed,
        failed: failed + (crashed ? 1 : 0),
        crashed,
        stdout,
        stderr
      })
    })
  })
}

async function main() {
  console.log(`\n=== 江南出行全量测试套件 ===`)
  console.log(`模式：${skipSecurity ? '跳过安全测试' : '完整测试'} | 清理数据：${cleanup ? '是' : '否'}`)
  console.log(`开始时间：${new Date().toLocaleString()}\n`)

  if (existsSync(CLEANUP_SQL)) {
    try {
      const sql = readFileSync(CLEANUP_SQL, 'utf8')
      const mysql = spawn('mysql', ['-h', DB_HOST, '-P', DB_PORT, '-u', DB_USER, `-p${DB_PASS}`, DB_NAME], {
        stdio: ['pipe', 'ignore', 'ignore']
      })
      mysql.stdin.write(sql)
      mysql.stdin.end()
      await new Promise((resolve, reject) => {
        mysql.on('close', (code) => code === 0 ? resolve() : reject(new Error(`mysql exit ${code}`)))
        mysql.on('error', reject)
      })
      console.log('✅ 已清理历史测试数据')
    } catch (e) {
      console.log('⚠️ 历史数据清理失败，继续执行：', e.message)
    }
  }

  for (const group of groups) {
    console.log(`\n## ${group.name}`)
    for (const script of group.scripts) {
      if (!existsSync(join(__dirname, script))) {
        console.log(`⚠️ 脚本不存在，跳过：${script}`)
        continue
      }
      clearRedisLimits()
      const res = await runScript(script)
      results.push({ group: group.name, ...res })
      const icon = res.failed === 0 ? '✅' : '❌'
      console.log(`${icon} ${script} | ${res.passed}/${res.total} 通过 | ${res.failed} 失败 | ${res.duration}ms`)
      await new Promise(r => setTimeout(r, 2000))
    }
  }

  const totalTests = results.reduce((s, r) => s + r.total, 0)
  const totalPassed = results.reduce((s, r) => s + r.passed, 0)
  const totalFailed = results.reduce((s, r) => s + r.failed, 0)
  const totalDuration = results.reduce((s, r) => s + r.duration, 0)
  const failedScripts = results.filter(r => r.failed > 0 || r.crashed)

  console.log(`\n=== 全量测试汇总 ===`)
  console.log(`总用例：${totalTests}`)
  console.log(`通过：${totalPassed}`)
  console.log(`失败：${totalFailed}`)
  console.log(`耗时：${totalDuration}ms`)
  console.log(`通过率：${totalTests > 0 ? (totalPassed / totalTests * 100).toFixed(1) : 0}%`)

  if (failedScripts.length > 0) {
    console.log(`\n失败脚本详情：`)
    for (const r of failedScripts) {
      console.log(`\n--- ${r.script} ---`)
      const lines = r.stdout.split('\n').filter(l => l.includes('❌'))
      lines.forEach(l => console.log(l))
      if (r.stderr) console.log('stderr:', r.stderr.slice(0, 500))
    }
  }

  const reportPath = join(__dirname, `test-report-${Date.now()}.json`)
  writeFileSync(reportPath, JSON.stringify({
    time: new Date().toISOString(),
    skipSecurity,
    summary: { total: totalTests, passed: totalPassed, failed: totalFailed, duration: totalDuration },
    results: results.map(r => ({
      script: r.script, group: r.group, total: r.total, passed: r.passed, failed: r.failed, duration: r.duration, crashed: r.crashed
    }))
  }, null, 2))
  console.log(`\n测试报告已保存：${reportPath}`)

  if (cleanup) {
    console.log('\n⚠️ 已请求测试数据清理，请确认 SQL 脚本 cleanup_test_data.sql 内容后再执行')
    console.log('本次运行未自动执行清理，请手动运行：mysql -u root -p smart_travel < cleanup_test_data.sql')
  }

  process.exit(totalFailed > 0 ? 1 : 0)
}

main()
