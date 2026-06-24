import request from './request'

export const adminApi = {
  login(username, password) {
    return request.post('/admin/login', { username, password })
  },
  dashboard() {
    return request.get('/admin/dashboard')
  },
  users(params) {
    return request.get('/admin/users', { params })
  },
  updateUserStatus(id, status) {
    return request.put(`/admin/users/${id}/status`, { status })
  },
  drivers(params) {
    return request.get('/admin/drivers', { params })
  },
  verifyDriver(id, verifyStatus) {
    return request.put(`/admin/drivers/${id}/verify`, { verifyStatus })
  },
  orders(params) {
    return request.get('/admin/orders', { params })
  },
  alerts(params) {
    return request.get('/admin/alerts', { params })
  },
  handleAlert(id, handleRemark = '后台已处理') {
    return request.put(`/admin/alerts/${id}/handle`, { handleRemark })
  }
}
