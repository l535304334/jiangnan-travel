import request from './request'

export const notificationApi = {
  list(pageNum = 1, pageSize = 20) {
    return request.get('/notification/list', { params: { pageNum, pageSize } })
  },
  unreadCount() {
    return request.get('/notification/unread-count')
  },
  markRead(id) {
    return request.put(`/notification/${id}/read`)
  },
  markAllRead() {
    return request.put('/notification/read-all')
  }
}
