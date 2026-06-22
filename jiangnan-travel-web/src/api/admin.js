import request from './request'

export const adminApi = {
  login(username, password) {
    return request.post('/admin/login', { username, password })
  }
}
