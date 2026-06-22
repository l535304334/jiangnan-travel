import request from './request'

export const driverApi = {
  login(phone) {
    return request.post('/driver/login', { phone })
  },
  register(data) {
    return request.post('/driver/register', data)
  },
  updateStatus(driverId, status) {
    return request.put('/driver/status', { driverId, status })
  },
  updateLocation(driverId, lat, lng) {
    return request.put('/driver/location', { driverId, lat, lng })
  },
  getProfile(driverId) {
    return request.get('/driver/profile', { params: { driverId } })
  },
  nearbyOrders(orderId, limit = 5) {
    return request.get('/driver/order/nearby', { params: { orderId, limit } })
  },
  acceptOrder(id, driverId) {
    return request.post(`/driver/order/${id}/accept`, { driverId })
  },
  arriveOrder(id, driverId) {
    return request.put(`/driver/order/${id}/arrive`, { driverId })
  },
  startTrip(id, driverId) {
    return request.put(`/driver/order/${id}/start`, { driverId })
  },
  completeTrip(id, driverId) {
    return request.put(`/driver/order/${id}/complete`, { driverId })
  }
}
