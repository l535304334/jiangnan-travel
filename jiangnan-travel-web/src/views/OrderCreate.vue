<template>
  <div class="order-create">
    <div class="map-area">
      <AmapView :center="mapCenter" :markers="mapMarkers" :zoom="14" style="height:200px" />
    </div>

    <div class="address-section">
      <div class="address-row">
        <el-icon :size="20" color="#2D8A6E"><Location /></el-icon>
        <el-input v-model="startAddress" placeholder="你在哪里上车？输入地名" @focus="focusInput('start')" @blur="hideSuggestions" />
      </div>
      <div class="address-row">
        <el-icon :size="20" color="#FF4D4F"><Position /></el-icon>
        <el-input v-model="endAddress" placeholder="你要去哪里？输入地名" @focus="focusInput('end')" @blur="hideSuggestions" />
      </div>
      <div class="suggestions" v-if="showSuggestions && filteredLocations.length > 0">
        <div class="sug-item" v-for="name in filteredLocations" :key="name"
             @mousedown.prevent="selectLocation(activeField, name)">
          <span>📍</span> {{ name }}
        </div>
        <div class="sug-hint" v-if="filteredLocations.length === 0">输入地名自动匹配坐标，支持模糊搜索</div>
      </div>
    </div>

    <div class="car-type-section">
      <h4>选择车型</h4>
      <div class="car-cards">
        <div class="car-card" :class="{ active: carTypeId === 1 }" @click="selectCar(1, '快车', '经济实惠')">
          <div class="car-icon">🚗</div><div class="car-name">快车</div><div class="car-desc">经济实惠</div>
        </div>
        <div class="car-card" :class="{ active: carTypeId === 2 }" @click="selectCar(2, '专车', '舒适品质')">
          <div class="car-icon">🚙</div><div class="car-name">专车</div><div class="car-desc">舒适品质</div>
        </div>
        <div class="car-card" :class="{ active: carTypeId === 3 }" @click="selectCar(3, '商务七座', '多人出行')">
          <div class="car-icon">🚐</div><div class="car-name">商务七座</div><div class="car-desc">多人出行</div>
        </div>
      </div>
    </div>

    <div class="estimate-section" v-if="estimate">
      <div class="est-row"><span>预计距离</span><strong>{{ (estimate.distance/1000).toFixed(1) }} km</strong></div>
      <div class="est-row"><span>预计时长</span><strong>{{ Math.round(estimate.duration/60) }} 分钟</strong></div>
      <div class="est-price"><span>{{ carTypeName }}</span><strong>¥{{ estimate.estimateTotal }}</strong></div>
    </div>

    <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleCreate">
      立即下单
    </el-button>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Position } from '@element-plus/icons-vue'
import { orderApi } from '@/api/order'
import AmapView from '@/components/AmapView.vue'

const router = useRouter()
const route = useRoute()
const startAddress = ref('')
const endAddress = ref('')
const carTypeId = ref(1)
const carTypeName = ref('快车')
const estimate = ref(null)
const loading = ref(false)
const debounceTimer = ref(null)
const showSuggestions = ref(false)
const activeField = ref('') // 'start' or 'end'

const startLat = ref(28.6842); const startLng = ref(115.8759)
const endLat = ref(28.6842); const endLng = ref(115.8759)

// 常用地标坐标库
const locations = {
  '南昌火车站': { lat: 28.6977, lng: 115.8671 },
  '南昌西站': { lat: 28.6425, lng: 115.7965 },
  '南昌大学前湖校区': { lat: 28.6842, lng: 115.8759 },
  '南昌大学医学院': { lat: 28.6860, lng: 115.8500 },
  '滕王阁': { lat: 28.6833, lng: 115.8813 },
  '八一广场': { lat: 28.6783, lng: 115.9067 },
  '红谷滩万达广场': { lat: 28.6977, lng: 115.8561 },
  '秋水广场': { lat: 28.6910, lng: 115.8606 },
  '南昌昌北机场': { lat: 28.8648, lng: 115.9100 },
  '瑶湖': { lat: 28.6981, lng: 116.0312 },
  '赣州火车站': { lat: 25.8456, lng: 114.9356 },
  '赣州古城墙': { lat: 25.8522, lng: 114.9412 },
  '八境台': { lat: 25.8538, lng: 114.9445 },
  '宁都县城': { lat: 26.4825, lng: 116.0209 },
  '翠微峰': { lat: 26.4950, lng: 116.0300 },
  '宁都起义纪念馆': { lat: 26.4750, lng: 116.0150 },
  '九江火车站': { lat: 29.7178, lng: 116.0021 },
  '庐山': { lat: 29.5652, lng: 115.9786 },
  '井冈山': { lat: 26.5687, lng: 114.1483 },
  '景德镇火车站': { lat: 29.2960, lng: 117.2140 }
}

const locationNames = Object.keys(locations)

const mapCenter = computed(() => [startLng.value, startLat.value])
const mapMarkers = computed(() => [
  { lng: startLng.value, lat: startLat.value, title: startAddress.value || '起点' },
  { lng: endLng.value, lat: endLat.value, title: endAddress.value || '终点' }
])

// 根据地址自动匹配坐标
const lookupCoord = (addr) => {
  if (!addr) return null
  // 精确匹配
  for (const [name, coord] of Object.entries(locations)) {
    if (addr === name) return coord
  }
  // 模糊匹配
  for (const [name, coord] of Object.entries(locations)) {
    if (name.includes(addr) || addr.includes(name)) return coord
  }
  return null
}

// 根据两点坐标计算距离（简化 Haversine）
const calcDistance = (lat1, lng1, lat2, lng2) => {
  const R = 6371000
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat/2) ** 2 + Math.cos(lat1 * Math.PI/180) * Math.cos(lat2 * Math.PI/180) * Math.sin(dLng/2) ** 2
  return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)))
}

const selectCar = (id, name) => { carTypeId.value = id; carTypeName.value = name }

const selectLocation = (field, name) => {
  const coord = locations[name]
  if (!coord) return
  if (field === 'start') {
    startAddress.value = name; startLat.value = coord.lat; startLng.value = coord.lng
  } else {
    endAddress.value = name; endLat.value = coord.lat; endLng.value = coord.lng
  }
  showSuggestions.value = false
}

const focusInput = (field) => {
  activeField.value = field; showSuggestions.value = true
}
const hideSuggestions = () => {
  setTimeout(() => { showSuggestions.value = false }, 200)
}

const filteredLocations = computed(() => {
  const query = activeField.value === 'start' ? startAddress.value : endAddress.value
  if (!query || query.length < 1) return locationNames.slice(0, 8)
  return locationNames.filter(n => n.includes(query)).slice(0, 8)
})

// 地址变化时尝试匹配坐标
watch(startAddress, (v) => {
  const c = lookupCoord(v); if (c) { startLat.value = c.lat; startLng.value = c.lng }
})
watch(endAddress, (v) => {
  const c = lookupCoord(v); if (c) { endLat.value = c.lat; endLng.value = c.lng }
})

watch([startAddress, endAddress, carTypeId], () => {
  clearTimeout(debounceTimer.value)
  debounceTimer.value = setTimeout(() => {
    if (!startAddress.value || !endAddress.value) return
    const dist = calcDistance(startLat.value, startLng.value, endLat.value, endLng.value)
    const dur = Math.round(dist / 6.67) // ~24km/h → 6.67m/s
    orderApi.estimate({
      startAddress: startAddress.value, startLat: startLat.value, startLng: startLng.value,
      endAddress: endAddress.value, endLat: endLat.value, endLng: endLng.value,
      distance: dist, duration: dur, carTypeId: carTypeId.value
    }).then(res => { if (res.code === 200) estimate.value = res.data }).catch(() => {
      console.warn('价格估算失败')
    })
  }, 600)
}, { immediate: false })

const handleCreate = async () => {
  if (!startAddress.value || !endAddress.value) { ElMessage.warning('请填写起终点'); return }
  loading.value = true
  try {
    const dist = calcDistance(startLat.value, startLng.value, endLat.value, endLng.value)
    const dur = Math.round(dist / 6.67)
    const res = await orderApi.create({
      startAddress: startAddress.value, startLat: startLat.value, startLng: startLng.value,
      endAddress: endAddress.value, endLat: endLat.value, endLng: endLng.value,
      distance: dist, duration: dur, carTypeId: carTypeId.value,
      idempotentKey: Date.now().toString()
    })
    if (res.code === 200) { ElMessage.success('下单成功'); router.push(`/order/${res.data.id}`) }
  } catch (e) { ElMessage.error('下单失败') }
  loading.value = false
}
</script>

<style scoped>
.order-create { padding: 0; }
.map-area { margin: -12px -16px 12px; }
.address-section { background: #fff; border-radius: var(--radius-md); padding: 8px 16px; box-shadow: var(--shadow-sm); margin-bottom: 12px; }
.address-row { display: flex; align-items: center; gap: 10px; padding: 6px 0; }
.address-row + .address-row { border-top: 1px solid #eee; }
.suggestions {
  background: #fff; border-radius: var(--radius-sm); margin-top: 4px;
  box-shadow: var(--shadow-md); max-height: 240px; overflow-y: auto;
}
.sug-item { padding: 10px 12px; cursor: pointer; font-size: 0.85rem; border-bottom: 1px solid #f5f5f5; }
.sug-item:hover { background: var(--color-primary-bg); }
.sug-hint { padding: 8px 12px; font-size: 0.75rem; color: var(--color-text-muted); }
.car-type-section { margin-bottom: 12px; }
.car-type-section h4 { font-size: 0.95rem; margin-bottom: 8px; }
.car-cards { display: flex; gap: 8px; }
.car-card { flex: 1; background: #fff; border-radius: var(--radius-md); padding: 12px 6px; text-align: center; border: 2px solid transparent; box-shadow: var(--shadow-sm); cursor: pointer; }
.car-card.active { border-color: var(--color-primary); background: var(--color-primary-bg); }
.car-icon { font-size: 1.5rem; }
.car-name { font-size: 0.85rem; font-weight: 600; margin: 4px 0 2px; }
.car-desc { font-size: 0.7rem; color: var(--color-text-muted); }
.estimate-section { background: #fff; border-radius: var(--radius-md); padding: 14px 16px; box-shadow: var(--shadow-sm); margin-bottom: 16px; }
.est-row { display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 6px; }
.est-price { display: flex; justify-content: space-between; font-size: 1.1rem; font-weight: 700; color: var(--color-primary); padding-top: 8px; border-top: 1px dashed #eee; }
.submit-btn { width: 100%; height: 48px; }
</style>
