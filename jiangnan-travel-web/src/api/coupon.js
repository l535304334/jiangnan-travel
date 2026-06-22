import request from './request'

export const couponApi = {
  list() {
    return request.get('/coupon/list')
  },
  myCoupons() {
    return request.get('/coupon/my')
  },
  claim(couponId) {
    return request.post('/coupon/claim', { couponId })
  }
}
