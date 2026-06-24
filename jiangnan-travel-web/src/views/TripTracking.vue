<template>
  <div class="trip-tracking">
    <div class="map-area">
      <AmapView :center="mapCenter" :markers="mapMarkers" :zoom="15" style="height:260px" />
    </div>

    <div class="driver-card" v-if="driver">
      <div class="driver-avatar">🧑‍✈️</div>
      <div class="driver-info">
        <div class="driver-name">{{ driver.name }}</div>
        <div class="driver-detail">{{ driver.plate }} · 评分 {{ driver.rating }}</div>
      </div>
      <div class="driver-actions">
        <el-button circle :icon="Share" @click="handleShare" />
        <el-button circle type="danger" :icon="PhoneFilled" @click="handleSafety" />
      </div>
    </div>

    <div class="status-progress">
      <el-steps :active="activeStep" align-center>
        <el-step title="已接单" />
        <el-step title="已到达" />
        <el-step title="行程中" />
        <el-step title="已完成" />
      </el-steps>
    </div>

    <div class="culture-quote" v-if="quotes.length > 0">
      <p class="quote-text">「{{ quotes[currentQuoteIndex] }}」</p>
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

const route = useRoute()
const driver = ref(null)
const activeStep = ref(0)
const orderData = ref({})
const quotes = ref([])
const currentQuoteIndex = ref(0)
let quoteTimer = null

const mapCenter = computed(() => {
  if (orderData.value.startLng) return [orderData.value.startLng, orderData.value.startLat]
  return [115.8759, 28.6842]
})
const mapMarkers = computed(() => {
  const m = []
  if (orderData.value.startLng) m.push({ lng: orderData.value.startLng, lat: orderData.value.startLat, title: '起点' })
  if (orderData.value.endLng) m.push({ lng: orderData.value.endLng, lat: orderData.value.endLat, title: '终点' })
  if (driver.value && driver.value.lat) m.push({ lng: driver.value.lng, lat: driver.value.lat, title: '司机' })
  return m
})

const statusStepMap = { '待接单':0, '已接单':0, '已到达':1, '行程中':2, '已完成':3, '已取消':0 }

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
  }
})

onUnmounted(() => {
  if (quoteTimer) {
    clearInterval(quoteTimer)
    quoteTimer = null
  }
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
  background: #fff;
  border-radius: var(--radius-md);
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: var(--shadow-sm);
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
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px 16px;
  box-shadow: var(--shadow-sm);
}
.culture-quote {
  margin-top: 16px;
  padding: 16px 20px;
  background: #f5f3ef;
  border-radius: var(--radius-md);
  text-align: center;
}
.quote-text {
  margin: 0;
  font-style: italic;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}
</style>
