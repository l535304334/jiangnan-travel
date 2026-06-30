import request from './request'

export const vipApi = {
  levels() {
    return request.get('/vip/levels')
  },
  myVip() {
    return request.get('/vip/my')
  },
  purchase(levelId, feeType) {
    return request.post('/vip/purchase', { levelId, feeType })
  }
}
