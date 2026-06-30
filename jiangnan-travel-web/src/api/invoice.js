import request from './request'

export const invoiceApi = {
  /** 申请发票 */
  apply(data) {
    return request.post('/invoice/apply', data)
  },

  /** 发票详情 */
  detail(id) {
    return request.get(`/invoice/${id}`)
  },

  /** 发票列表 */
  list() {
    return request.get('/invoice/list')
  },

  /** 取消申请 */
  cancel(id) {
    return request.put(`/invoice/${id}/cancel`)
  }
}
