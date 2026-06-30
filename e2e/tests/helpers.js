const API_BASE = 'http://localhost:8080'

export async function apiRequest(method, path, body = null, token = null) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : null,
  })
  const text = await res.text()
  return { status: res.status, body: text ? JSON.parse(text) : null }
}

export async function userLogin(phone = '13900001111', password = '123456') {
  const res = await apiRequest('POST', '/api/user/login-password', { phone, password })
  return res.body?.data
}

export async function driverLogin(phone = '13810000001') {
  const res = await apiRequest('POST', '/api/driver/login', { phone })
  return res.body?.data
}

export async function createOrder(userToken, overrides = {}) {
  const payload = {
    startAddress: '南昌八一广场',
    startLat: 28.6820,
    startLng: 115.8580,
    endAddress: '南昌西站',
    endLat: 28.6500,
    endLng: 115.9200,
    distance: 12000,
    duration: 1800,
    carTypeId: 1,
    idempotentKey: `e2e-${Date.now()}-${Math.random()}`,
    ...overrides,
  }
  const res = await apiRequest('POST', '/api/order/create', payload, userToken)
  return res.body?.data
}

export async function completeOrderByApi(orderId, driverToken) {
  // 订单已在 UI 中完成接单，此处仅通过 API 推进后续行程节点
  await apiRequest('PUT', `/api/driver/order/${orderId}/arrive`, {}, driverToken)
  await apiRequest('PUT', `/api/driver/order/${orderId}/start`, {}, driverToken)
  await apiRequest('PUT', `/api/driver/order/${orderId}/complete`, {}, driverToken)
}
