import request from './request'

export const busApi = {
  /** 班线列表 */
  listLines(startCity, endCity) {
    const params = {}
    if (startCity) params.startCity = startCity
    if (endCity) params.endCity = endCity
    return request.get('/bus-line/list', { params })
  },

  /** 班线详情（含时刻表） */
  lineDetail(id) {
    return request.get(`/bus-line/${id}`)
  },

  /** 购票 */
  purchase(scheduleId) {
    return request.post('/bus-line/purchase', { scheduleId })
  }
}
