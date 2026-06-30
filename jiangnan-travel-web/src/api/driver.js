import request from './request'

export const driverApi = {
  login(phone) {
    return request.post('/driver/login', { phone })
  },
  register(data) {
    return request.post('/driver/register', data)
  },
  updateStatus(status) {
    return request.put('/driver/status', { status })
  },
  updateLocation(lat, lng) {
    return request.put('/driver/location', { lat, lng })
  },
  getProfile() {
    return request.get('/driver/profile')
  },
  // 收入
  earning() {
    return request.get('/driver/earning')
  },
  weeklyEarning() {
    return request.get('/driver/earning/weekly')
  },
  // 订单
  nearbyOrders(lat, lng, limit = 20) {
    return request.get('/driver/order/nearby', { params: { lat, lng, limit } })
  },
  pendingOrders() {
    return request.get('/driver/order/pending')
  },
  orderHistory(status = 4, page = 1, pageSize = 20) {
    return request.get('/driver/order/history', { params: { status, page, pageSize } })
  },
  acceptOrder(id) {
    return request.post(`/driver/order/${id}/accept`)
  },
  arriveOrder(id) {
    return request.put(`/driver/order/${id}/arrive`)
  },
  startTrip(id) {
    return request.put(`/driver/order/${id}/start`)
  },
  completeTrip(id) {
    return request.put(`/driver/order/${id}/complete`)
  }
}
