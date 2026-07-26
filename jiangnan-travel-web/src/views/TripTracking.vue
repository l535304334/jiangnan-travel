<template>
  <div class="trip-tracking app-page" v-loading="loading">
    <div class="map-area">
      <AmapView :center="mapCenter" :markers="mapMarkers" :zoom="15" style="height:260px" />
    </div>

    <div class="driver-card app-card" v-if="driver">
      <CdnAvatar
        type="driver"
        :seed="driver.plate || driver.name"
        :size="48"
        icon="UserFilled"
      />
      <div class="driver-info">
        <div class="driver-name">{{ driver.name }}</div>
        <div class="driver-detail">{{ driver.plate }} · 评分 {{ driver.rating }}</div>
      </div>
      <div class="driver-actions">
        <el-button circle :icon="Share" @click="handleShare" />
        <el-button circle type="danger" :icon="PhoneFilled" @click="handleSafety" />
      </div>
    </div>

    <div class="status-progress app-card">
      <el-steps :active="activeStep" align-center>
        <el-step title="已接单" />
        <el-step title="已到达" />
        <el-step title="行程中" />
        <el-step title="已完成" />
      </el-steps>
    </div>

    <div class="culture-quote" v-if="quotes.length > 0">
      <div class="quote-bg">
        <span class="quote-mark">"</span>
        <p class="quote-content">{{ currentQuote.content }}</p>
        <div class="quote-footer">
          <span class="quote-author">—— {{ currentQuote.author || '佚名' }}</span>
          <el-tag size="small" effect="plain" class="quote-city">
            {{ currentQuote.city || currentQuote.dynasty || '赣鄱' }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Share, PhoneFilled } from '@element-plus/icons-vue'
import { orderApi } from '@/api/order'
import { aiApi } from '@/api/ai'
import { ElMessage } from 'element-plus'
import AmapView from '@/components/AmapView.vue'
import CdnAvatar from '@/components/CdnAvatar.vue'

const route = useRoute()
const loading = ref(true)
const driver = ref(null)
const activeStep = ref(0)
const orderData = ref({})
const quotes = ref([])
const currentQuoteIndex = ref(0)
let quoteTimer = null
let ws = null

const currentQuote = computed(() => {
  const q = quotes.value[currentQuoteIndex.value]
  return q || { content: '', author: '', city: '', dynasty: '' }
})

const mapCenter = computed(() => {
  if (driver.value && driver.value.lat) return [driver.value.lng, driver.value.lat]
  if (orderData.value.startLng) return [orderData.value.startLng, orderData.value.startLat]
  return [115.8759, 28.6842]
})

const mapMarkers = computed(() => {
  const m = []
  if (orderData.value.startLng) m.push({ lng: orderData.value.startLng, lat: orderData.value.startLat, title: '起点' })
  if (orderData.value.endLng) m.push({ lng: orderData.value.endLng, lat: orderData.value.endLat, title: '终点' })
  if (driver.value && driver.value.lat) {
    m.push({ lng: driver.value.lng, lat: driver.value.lat, title: driver.value.plate || '司机' })
  }
  return m
})

const statusStepMap = { '待接单':0, '已接单':0, '已到达':1, '行程中':2, '已完成':3, '已取消':0 }

function connectWebSocket(orderId) {
  const token = localStorage.getItem('token')
  if (!token) return
  // 鉴权走 Cookie（见后端 JwtCookieConfigurator），token 不入 URL 以免进代理日志
  const wsUrl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/order/${orderId}`
  try {
    ws = new WebSocket(wsUrl)
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'DRIVER_LOCATION' && data.lat && data.lng) {
          if (!driver.value) {
            driver.value = { name: orderData.value.driverName || '司机', plate: orderData.value.carPlate || '', rating: '4.8' }
          }
          driver.value.lat = data.lat
          driver.value.lng = data.lng
        }
      } catch { /* ignore parse errors */ }
    }
    ws.onclose = () => { ws = null }
    ws.onerror = () => { ws = null }
  } catch { /* ignore */ }
}

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await orderApi.detail(id)
    if (res.code === 200) {
      orderData.value = res.data
      if (res.data.driverName) {
        driver.value = { name: res.data.driverName, plate: res.data.carPlate, rating: '4.8' }
      }
      activeStep.value = statusStepMap[res.data.statusText] || 0
    }
  } catch (e) {
    ElMessage.error('订单详情加载失败')
  }

  // 连接 WebSocket 订阅实时位置
  if (id) connectWebSocket(id)

  try {
    const res = await aiApi.getCityQuotes()
    if (res.code === 200 && Array.isArray(res.data)) {
      quotes.value = res.data
      if (quotes.value.length > 1) {
        quoteTimer = setInterval(() => {
          currentQuoteIndex.value = (currentQuoteIndex.value + 1) % quotes.value.length
        }, 5000)
      }
    }
  } catch (e) {
    console.warn('文化名言加载失败:', e)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (ws) { ws.close(); ws = null }
  if (quoteTimer) { clearInterval(quoteTimer); quoteTimer = null }
})

const handleShare = () => {
  ElMessage.success('行程已分享给紧急联系人')
}

const handleSafety = () => {
  ElMessage.warning('已触发安全求助，客服即将联系您')
}
</script>

<style scoped>
.map-placeholder {
  height: 240px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
}
.map-car {
  font-size: 3rem;
  animation: car-bounce 1s infinite alternate;
}
.map-placeholder p {
  margin-top: 8px;
  color: var(--color-text-muted);
  font-size: 0.85rem;
}
@keyframes car-bounce {
  from { transform: translateY(0); }
  to { transform: translateY(-8px); }
}
.driver-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.driver-avatar {
  font-size: 2.5rem;
}
.driver-info {
  flex: 1;
}
.driver-name {
  font-weight: 600;
  font-size: 1rem;
}
.driver-detail {
  color: var(--color-text-muted);
  font-size: 0.8rem;
  margin-top: 2px;
}
.driver-actions {
  display: flex;
  gap: 8px;
}
.status-progress {
  padding: 20px 16px;
}
.culture-quote {
  margin-top: 16px;
  border-radius: var(--radius-md);
  overflow: hidden;
}
.quote-bg {
  position: relative;
  padding: 24px 20px 18px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: #e8d5b7;
}
.quote-mark {
  position: absolute; top: 6px; left: 14px;
  font-size: 3rem; line-height: 1;
  color: rgba(232, 213, 183, 0.15);
  font-family: Georgia, serif;
}
.quote-content {
  margin: 0; font-size: 0.95rem; line-height: 1.7;
  font-style: italic; text-align: center;
  position: relative; z-index: 1;
}
.quote-footer {
  display: flex; justify-content: center; align-items: center;
  gap: 8px; margin-top: 12px;
}
.quote-author {
  font-size: 0.78rem; color: rgba(232, 213, 183, 0.7);
}
.quote-city {
  font-size: 0.7rem;
}
</style>
