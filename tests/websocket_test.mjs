const BASE_HTTP = 'http://localhost:8080'
const BASE_WS = 'ws://localhost:8080'
let pass = 0, fail = 0
const results = []

async function request(method, path, data = null, token = null) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(`${BASE_HTTP}${path}`, {
    method,
    headers,
    body: data ? JSON.stringify(data) : null
  })
  const text = await res.text()
  const body = text ? JSON.parse(text) : null
  return { status: res.status, body }
}

async function test(name, checkFn) {
  const ok = await checkFn()
  results.push({ name, ok })
  ok ? pass++ : fail++
  const icon = ok ? '✅' : '❌'
  console.log(`${icon} ${name}`)
}

function connectWebSocket(url, timeout = 3000) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(url)
    let opened = false
    const timer = setTimeout(() => {
      ws.close()
      if (!opened) {
        reject(new Error('WebSocket 连接超时或被拒绝'))
      }
    }, timeout)

    ws.onopen = () => {
      opened = true
      clearTimeout(timer)
      resolve(ws)
    }
    ws.onclose = (e) => {
      clearTimeout(timer)
      if (!opened) {
        reject(new Error(`WebSocket 已关闭 code=${e.code}`))
      }
    }
    ws.onerror = () => {
      clearTimeout(timer)
      if (!opened) {
        reject(new Error('WebSocket 错误'))
      }
    }
  })
}

function waitMessage(ws, timeout = 5000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      ws.close()
      reject(new Error('等待消息超时'))
    }, timeout)
    ws.onmessage = (e) => {
      clearTimeout(timer)
      resolve(e.data)
    }
    ws.onclose = () => {
      clearTimeout(timer)
      reject(new Error('连接关闭，未收到消息'))
    }
  })
}

console.log('=== WebSocket 实时消息测试 ===')

// 1. 无 Token 连接应被拒绝
await test('WS-无Token连接被拒绝', async () => {
  return new Promise((resolve) => {
    const ws = new WebSocket(`${BASE_WS}/ws/order/1`)
    let opened = false
    ws.onopen = () => { opened = true }
    ws.onclose = () => {
      // 连接在打开前或打开后立即关闭，均视为拒绝
      resolve(true)
    }
    ws.onerror = () => {
      resolve(true)
    }
    setTimeout(() => {
      ws.close()
      // 如果1秒后仍未关闭且已打开，则视为未拒绝
      resolve(!opened)
    }, 1000)
  })
})

// 2. 有效Token连接成功
const userLogin = await request('POST', '/api/user/login-password', { phone: '13900001111', password: '123456' })
const userToken = userLogin.body?.data?.token
if (!userToken) {
  console.log('❌ 用户登录失败')
  process.exit(1)
}

const driverLogin = await request('POST', '/api/driver/login', { phone: '13810000001' })
const driverToken = driverLogin.body?.data?.token
if (!driverToken) {
  console.log('❌ 司机登录失败')
  process.exit(1)
}

await test('WS-有效Token连接成功', async () => {
  const createRes = await request('POST', '/api/order/create', {
    startAddress: '南昌八一广场',
    startLat: 28.6820,
    startLng: 115.8580,
    endAddress: '南昌西站',
    endLat: 28.6500,
    endLng: 115.9200,
    distance: 12000,
    duration: 1800,
    carTypeId: 1,
    idempotentKey: `ws-${Date.now()}`
  }, userToken)
  const orderId = createRes.body?.data?.id
  if (!orderId) return false

  try {
    const ws = await connectWebSocket(`${BASE_WS}/ws/order/${orderId}?token=${userToken}`)
    ws.close()
    return true
  } catch (e) {
    return false
  }
})

// 3. 司机接单后乘客收到消息
await test('WS-司机接单后乘客收到推送', async () => {
  await request('PUT', '/api/driver/status', { status: 1 }, driverToken)

  const createRes = await request('POST', '/api/order/create', {
    startAddress: '南昌八一广场',
    startLat: 28.6820,
    startLng: 115.8580,
    endAddress: '南昌西站',
    endLat: 28.6500,
    endLng: 115.9200,
    distance: 12000,
    duration: 1800,
    carTypeId: 1,
    idempotentKey: `ws-push-${Date.now()}`
  }, userToken)
  const orderId = createRes.body?.data?.id
  if (!orderId) return false

  const ws = await connectWebSocket(`${BASE_WS}/ws/order/${orderId}?token=${userToken}`)
  const msgPromise = waitMessage(ws)

  // 司机接单
  await request('POST', `/api/driver/order/${orderId}/accept`, {}, driverToken)

  let received = false
  try {
    const msg = await msgPromise
    console.log(`   收到消息: ${msg}`)
    received = true
  } catch (e) {
    console.log(`   未收到消息: ${e.message}`)
  } finally {
    ws.close()
    // 恢复司机在线
    await request('PUT', '/api/driver/status', { status: 1 }, driverToken)
  }
  return received
})

console.log(`\n总计: ${pass + fail} | 通过: ${pass} | 失败: ${fail}`)
process.exit(fail > 0 ? 1 : 0)
