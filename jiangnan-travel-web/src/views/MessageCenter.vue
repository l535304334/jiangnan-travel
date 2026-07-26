<template>
  <div class="message-center app-page" v-loading="loading" element-loading-text="加载中...">
    <div class="mc-header app-section">
      <h3 class="app-section-title">消息通知</h3>
      <el-button v-if="unreadCount > 0" type="primary" size="small" plain @click="handleMarkAllRead">
        全部已读
      </el-button>
    </div>

    <div v-if="!loading && notifications.length === 0">
      <el-empty description="暂无通知" />
    </div>

    <el-timeline v-if="!loading && notifications.length > 0">
      <el-timeline-item
        v-for="item in notifications"
        :key="item.id"
        :timestamp="item.createTime?.replace('T', ' ')"
        :color="item.isRead ? '#e4e7ed' : '#2D8A6E'"
      >
        <div class="mc-item" :class="{ unread: !item.isRead }" @click="handleClick(item)">
          <div class="mc-item-title">{{ item.title }}</div>
          <div class="mc-item-content">{{ item.content }}</div>
        </div>
      </el-timeline-item>
    </el-timeline>

    <div v-if="total > pageSize" class="mc-pagination">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        small
        @current-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { notificationApi } from '@/api/notification'
import { useRouter } from 'vue-router'

const router = useRouter()
const notifications = ref([])
const loading = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const unreadCount = ref(0)

// WebSocket 连接
let ws = null
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const token = localStorage.getItem('token')

function connectWebSocket() {
  if (!token || !userInfo.id) return
  // 鉴权走 Cookie（见后端 JwtCookieConfigurator），token 不入 URL 以免进代理日志
  const wsUrl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/notification/${userInfo.id}`
  try {
    ws = new WebSocket(wsUrl)
    ws.onmessage = (event) => {
      // 新通知到达，刷新列表和未读数
      loadList()
      loadUnreadCount()
    }
    ws.onclose = () => { ws = null }
    ws.onerror = () => { ws = null }
  } catch { /* ignore */ }
}

async function loadList() {
  try {
    const res = await notificationApi.list(pageNum.value, pageSize.value)
    notifications.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { /* ignore */ }
  finally { loading.value = false }
}

async function loadUnreadCount() {
  try {
    const res = await notificationApi.unreadCount()
    unreadCount.value = res.data || 0
  } catch { /* ignore */ }
}

async function handleMarkAllRead() {
  try {
    await notificationApi.markAllRead()
    ElMessage.success('已全部标为已读')
    unreadCount.value = 0
    notifications.value.forEach(n => { n.isRead = 1 })
  } catch { ElMessage.error('操作失败') }
}

async function handleClick(item) {
  if (!item.isRead) {
    try {
      await notificationApi.markRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch { /* ignore */ }
  }
  // 如果有关联订单，跳转到订单详情
  if (item.relatedId && item.type !== 'SYSTEM') {
    router.push(`/order/${item.relatedId}`)
  }
}

onMounted(() => {
  loadList()
  loadUnreadCount()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) { ws.close(); ws = null }
})
</script>

<style scoped>
.mc-header {
  display: flex; align-items: center; justify-content: space-between;
}
.mc-item { cursor: pointer; padding: 4px 0; }
.mc-item.unread { font-weight: 600; }
.mc-item-title { font-size: 0.95rem; color: var(--color-text); margin-bottom: 4px; }
.mc-item-content { font-size: 0.85rem; color: #666; line-height: 1.4; }
.mc-pagination { display: flex; justify-content: center; margin-top: 16px; }
</style>
