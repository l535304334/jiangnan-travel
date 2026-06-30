import request from './request'

export const paymentApi = {
  /** 创建支付 */
  create(orderId, payMethod = 'wxpay', idempotentKey) {
    return request.post('/payment/create', { orderId, payMethod, idempotentKey })
  },

  /** 查询支付信息 */
  getPayment(orderId) {
    return request.get(`/payment/${orderId}`)
  },

  /** 支付记录列表 */
  list() {
    return request.get('/payment/list')
  }
}
