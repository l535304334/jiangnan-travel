import request from './request'

export const userApi = {
  sendCode(phone) {
    return request.post('/user/send-code', { phone })
  },
  login(data) {
    return request.post('/user/login', data)
  },
  passwordLogin(data) {
    return request.post('/user/login-password', data)
  },
  register(data) {
    return request.post('/user/register', data)
  },
  getProfile() {
    return request.get('/user/profile')
  },
  updateProfile(data) {
    return request.put('/user/profile', data)
  },
  getAddresses() {
    return request.get('/user/address')
  },
  addAddress(data) {
    return request.post('/user/address', data)
  },
  deleteAddress(id) {
    return request.delete(`/user/address/${id}`)
  }
}
