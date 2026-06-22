import request from './request'

export const aiApi = {
  chat(message, sessionId) {
    return request.post('/ai/chat', { message, sessionId })
  },
  recommendDest() {
    return request.get('/ai/recommend-dest')
  },
  getLandmarks() {
    return request.get('/landmark')
  },
  searchLandmarks(keyword) {
    return request.get('/landmark/search', { params: { keyword } })
  },
  getHotspots() {
    return request.get('/ai/hotspots')
  },
  getFrequentRoutes() {
    return request.get('/user/frequent-routes')
  },
  getCityQuotes() {
    return request.get('/common/city-quote')
  }
}
