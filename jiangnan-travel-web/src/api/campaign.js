import request from './request'

export const campaignApi = {
  list() {
    return request.get('/campaign/list')
  },
  detail(id) {
    return request.get(`/campaign/${id}`)
  },
  claim(id) {
    return request.post(`/campaign/${id}/claim`)
  }
}
