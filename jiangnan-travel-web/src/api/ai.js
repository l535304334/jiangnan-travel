import request from './request'

export const aiApi = {
  chat(message, sessionId) {
    return request.post('/ai/chat', { message, sessionId })
  },
  /**
   * SSE 流式对话 - 使用原生 fetch 读取流
   * @param {string} message 用户消息
   * @param {string|null} sessionId 会话ID
   * @param {function(string)} onDelta 收到每个字符块的回调
   * @param {function()} onDone 流结束回调
   * @param {function(string)} onError 错误回调
   * @returns {AbortController} 用于取消请求的控制器
   */
  chatStream(message, sessionId, onDelta, onDone, onError) {
    const controller = new AbortController()
    const token = localStorage.getItem('token')

    fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ message, sessionId }),
      signal: controller.signal
    }).then(async (response) => {
      if (!response.ok) {
        onError?.('网络请求失败')
        return
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let eventType = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            eventType = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (eventType === 'delta') {
              onDelta?.(data)
            } else if (eventType === 'done') {
              onDone?.()
            }
          }
        }
      }
      onDone?.()
    }).catch((err) => {
      if (err.name !== 'AbortError') {
        onError?.(err.message || 'SSE 连接失败')
      }
    })

    return controller
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
  },
  getSessions() {
    return request.get('/ai/sessions')
  },
  getSessionMessages(sessionId) {
    return request.get(`/ai/sessions/${sessionId}/messages`)
  }
}
