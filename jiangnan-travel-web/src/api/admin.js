import request from './request'

export const adminApi = {
  login(username, password) {
    return request.post('/admin/login', { username, password })
  },
  dashboard() {
    return request.get('/admin/dashboard')
  },
  chartData() {
    return request.get('/admin/dashboard/chart')
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
  },
  carTypes(params) {
    return request.get('/admin/car-types', { params })
  },
  updateCarType(id, data) {
    return request.put(`/admin/car-types/${id}`, data)
  },

  /* ===== 活动管理 ===== */
  campaigns(params) {
    return request.get('/admin/campaigns', { params })
  },
  createCampaign(data) {
    return request.post('/admin/campaigns', data)
  },
  updateCampaign(id, data) {
    return request.put(`/admin/campaigns/${id}`, data)
  },
  deleteCampaign(id) {
    return request.delete(`/admin/campaigns/${id}`)
  },

  /* ===== VIP等级管理 ===== */
  vipLevels() {
    return request.get('/admin/vip-levels')
  },
  createVipLevel(data) {
    return request.post('/admin/vip-levels/create', data)
  },
  updateVipLevel(id, data) {
    return request.put(`/admin/vip-levels/${id}`, data)
  },
  deleteVipLevel(id) {
    return request.delete(`/admin/vip-levels/${id}`)
  },

  /* ===== 班线管理 ===== */
  busLines(params) {
    return request.get('/admin/bus-lines', { params })
  },
  createBusLine(data) {
    return request.post('/admin/bus-lines/create', data)
  },
  updateBusLine(id, data) {
    return request.put(`/admin/bus-lines/${id}`, data)
  },
  deleteBusLine(id) {
    return request.delete(`/admin/bus-lines/${id}`)
  },
  busSchedules(lineId) {
    return request.get('/admin/bus/schedules', { params: { lineId } })
  },
  createBusSchedule(data) {
    return request.post('/admin/bus/schedules', data)
  },
  updateBusSchedule(id, data) {
    return request.put(`/admin/bus/schedules/${id}`, data)
  },
  deleteBusSchedule(id) {
    return request.delete(`/admin/bus/schedules/${id}`)
  }
}
