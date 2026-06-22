import request from './request'

export const orderApi = {
  estimate(data) {
    return request.post('/order/estimate', data)
  },
  create(data) {
    return request.post('/order/create', data)
  },
  list(params) {
    return request.get('/order/list', { params })
  },
  detail(id) {
    return request.get(`/order/${id}`)
  },
  cancel(id, reason) {
    return request.put(`/order/${id}/cancel`, { reason })
  },
  pay(id) {
    return request.put(`/order/${id}/pay`)
  },
  review(id, data) {
    return request.post(`/order/${id}/review`, data)
  }
}
