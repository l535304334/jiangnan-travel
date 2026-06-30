<template>
  <div class="order-create">
    <!-- 1. 地图区域 -->
    <div class="map-area">
      <AmapView :center="mapCenter" :markers="mapMarkers" :path="mapPath" :zoom="14" style="height:200px" @map-click="onMapClick" />
    </div>

    <!-- 2. 行程类型标识（自动检测） -->
    <div class="trip-type-badge" v-if="routePlan">
      <el-tag :type="tripType === 1 ? 'warning' : 'success'" size="small">
        {{ tripType === 1 ? '🚗 城际长途 ' + (routePlan.distance/1000).toFixed(0) + 'km' : '🏠 市内短途' }}
      </el-tag>
    </div>

    <!-- 3. 地址输入区 -->
    <div class="address-section app-card">
      <div class="address-row">
        <el-icon :size="20" color="#2D8A6E"><Location /></el-icon>
        <el-input v-model="startAddress" placeholder="你在哪里上车？输入地名" @focus="focusInput('start')" @blur="hideSuggestions" />
      </div>
      <div class="address-row">
        <el-icon :size="20" color="#FF4D4F"><Aim /></el-icon>
        <el-input v-model="endAddress" placeholder="你要去哪里？输入地名" @focus="focusInput('end')" @blur="hideSuggestions" />
      </div>
      <div class="suggestions" v-if="showSuggestions && poiSuggestions.length > 0">
        <div class="sug-item" v-for="(item, idx) in poiSuggestions" :key="idx"
             @mousedown.prevent="selectLocation(activeField, item)">
          <span>📍</span>
          <div class="sug-text">
            <div class="sug-name">{{ item.name }}</div>
            <div class="sug-addr">{{ item.address }}</div>
          </div>
        </div>
        <div class="sug-hint" v-if="poiSearching">搜索中...</div>
      </div>
      <div class="map-hint" v-if="showSuggestions">提示：点击地图可快速定位</div>
    </div>

    <!-- 4. 路线详情卡 -->
    <div class="route-detail-card app-card" v-if="routePlan">
      <div class="rd-header">
        <el-icon :size="16"><MapLocation /></el-icon>
        <span>路线详情 {{ tripType === 1 ? '(城际长途)' : '(市内短途)' }}</span>
      </div>
      <div class="rd-summary">
        <div class="rd-stat">
          <span class="rd-stat-label">总距离</span>
          <span class="rd-stat-value">{{ (routePlan.distance / 1000).toFixed(1) }} km</span>
        </div>
        <div class="rd-divider" />
        <div class="rd-stat">
          <span class="rd-stat-label">预计时长</span>
          <span class="rd-stat-value">{{ Math.round(routePlan.duration / 60) }} 分钟</span>
        </div>
        <div class="rd-divider" />
        <div class="rd-stat">
          <span class="rd-stat-label">红绿灯</span>
          <span class="rd-stat-value">{{ routePlan.trafficLightCount || 0 }} 个</span>
        </div>
        <div class="rd-divider" v-if="tripType === 1" />
        <div class="rd-stat" v-if="tripType === 1">
          <span class="rd-stat-label">收费站</span>
          <span class="rd-stat-value">{{ routePlan.tollCount || 0 }} 个</span>
        </div>
      </div>
      <div class="rd-steps" v-if="routePlan.steps && routePlan.steps.length > 0">
        <div class="rd-step" v-for="(step, idx) in routePlan.steps" :key="idx">
          <div class="rd-step-dot" :class="{ 'is-last': idx === routePlan.steps.length - 1 }" />
          <div class="rd-step-content">
            <span class="rd-step-road">{{ step.road || '道路' }}</span>
            <span class="rd-step-info">{{ step.instruction }}</span>
            <span class="rd-step-dist">{{ (step.distance / 1000).toFixed(1) }} km</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 5. 车型选择 -->
    <div class="car-type-section app-section">
      <h4 class="app-section-title">选择车型</h4>
      <div class="car-cards">
        <div class="car-card card-list-item" v-for="car in carTypes" :key="car.id"
             :class="{ active: carTypeId === car.id }" @click="selectCar(car.id, car.name)">
          <div class="car-icon">{{ car.icon }}</div>
          <div class="car-name">{{ car.name }}</div>
          <div class="car-desc">{{ car.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 6. 价格估算 -->
    <div class="estimate-section app-card" v-if="estimate">
      <div class="est-row"><span>预计距离</span><strong>{{ (estimate.distance/1000).toFixed(1) }} km</strong></div>
      <div class="est-row"><span>预计时长</span><strong>{{ Math.round(estimate.duration/60) }} 分钟</strong></div>
      <div class="est-row" v-if="estimate.baseFare"><span>起步价</span><strong>¥{{ estimate.baseFare }}</strong></div>
      <div class="est-row" v-if="estimate.distanceFee"><span>里程费</span><strong>¥{{ estimate.distanceFee }}</strong></div>
      <div class="est-row" v-if="estimate.durationFee"><span>时长费</span><strong>¥{{ estimate.durationFee }}</strong></div>
      <div class="est-row" v-if="estimate.tollFee"><span>路桥费</span><strong>¥{{ estimate.tollFee }}</strong></div>

      <!-- 优惠券选择 -->
      <div class="est-row coupon-row" @click="handleSelectCoupon">
        <span>优惠券</span>
        <strong v-if="selectedCoupon" class="coupon-discount">-¥{{ selectedCoupon.discount }}</strong>
        <strong v-else class="coupon-select">选择优惠券 ›</strong>
      </div>
      <div class="est-row" v-if="selectedCoupon">
        <span class="coupon-name-text">{{ selectedCoupon.name }}</span>
        <el-tag size="small" type="warning" @click.stop="removeCoupon" style="cursor:pointer">移除</el-tag>
      </div>

      <div class="est-price"><span>{{ carTypeName }}</span><strong>¥{{ estimate.estimateTotal }}</strong></div>
    </div>

    <!-- 优惠券选择弹窗 -->
    <el-dialog v-model="showCouponDialog" title="选择优惠券" width="90%" :close-on-click-modal="true">
      <el-empty v-if="availableCoupons.length === 0" description="暂无可用优惠券" />
      <div v-else class="coupon-list">
        <div
          class="coupon-card"
          v-for="item in availableCoupons"
          :key="item.id"
          :class="{ active: selectedCoupon?.id === item.id }"
          @click="selectCoupon(item)"
        >
          <div class="coupon-left">
            <div class="coupon-amount">¥{{ item.discount }}</div>
            <div class="coupon-condition">满¥{{ item.threshold }}可用</div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ item.name }}</div>
            <div class="coupon-expire">{{ item.validDays ? '有效期' + item.validDays + '天' : '不限有效期' }}</div>
          </div>
          <div class="coupon-check" v-if="selectedCoupon?.id === item.id">✓</div>
        </div>
      </div>
    </el-dialog>

    <!-- 7. 下单按钮 -->
    <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleCreate">
      立即下单
    </el-button>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Aim, MapLocation } from '@element-plus/icons-vue'
import { orderApi } from '@/api/order'
import { couponApi } from '@/api/coupon'
import { useAmapPoiSearch } from '@/composables/useAmapPoiSearch'
import AmapView from '@/components/AmapView.vue'

const router = useRouter()
const route = useRoute()

// 行程类型
const tripType = ref(0) // 0=短途, 1=长途

// 起终点
const startAddress = ref('')
const endAddress = ref('')
const startLat = ref(28.6842)
const startLng = ref(115.8759)
const endLat = ref(28.6842)
const endLng = ref(115.8759)

// 路线规划
const routePlan = ref(null)
const routeLoading = ref(false)

// 车型
const carTypeId = ref(1)
const carTypeName = ref('快车')

// 搜索建议
const showSuggestions = ref(false)
const activeField = ref('')

// 价格估算
const estimate = ref(null)
const estimateLoading = ref(false)
const debounceTimer = ref(null)
const loading = ref(false)

// 优惠券
const selectedCoupon = ref(null)
const showCouponDialog = ref(false)
const availableCoupons = ref([])

// POI 搜索 composable
const { suggestions: poiSuggestions, searching: poiSearching, search: poiSearch, getPoiLocation, clear: clearPoi } = useAmapPoiSearch()
let poiSearchTimer = null

// 地图点击选点
const onMapClick = (pos) => {
  if (!activeField.value) { ElMessage.info('请先在输入框中点击选择起点或终点'); return }
  if (activeField.value === 'start') {
    startLat.value = pos.lat
    startLng.value = pos.lng
  } else {
    endLat.value = pos.lat
    endLng.value = pos.lng
  }
}

// 地图相关
const mapCenter = computed(() => [startLng.value, startLat.value])
const mapMarkers = computed(() => [
  { lng: startLng.value, lat: startLat.value, title: startAddress.value || '起点' },
  { lng: endLng.value, lat: endLat.value, title: endAddress.value || '终点' }
])
const mapPath = computed(() => routePlan.value?.path || [])

// 从首页跳转过来时带参
onMounted(() => {
  // 接收行程类型参数（首页独立入口传入）
  if (route.query.tripType !== undefined) {
    tripType.value = parseInt(route.query.tripType)
    if (tripType.value === 1) {
      carTypeId.value = 4
      carTypeName.value = '城际快车'
    }
  }
  if (route.query.endAddress) {
    endAddress.value = route.query.endAddress
    if (route.query.endLat) endLat.value = parseFloat(route.query.endLat)
    if (route.query.endLng) endLng.value = parseFloat(route.query.endLng)
  }
})

// POI 搜索 — 地址输入时自动搜索
const onAddressInput = () => {
  const val = activeField.value === 'start' ? startAddress.value : endAddress.value
  clearTimeout(poiSearchTimer)
  if (!val || val.trim().length < 1) { clearPoi(); return }
  poiSearchTimer = setTimeout(() => poiSearch(val), 300)
}

// 地址输入变化时触发 POI 搜索
watch(startAddress, () => { if (activeField.value === 'start') onAddressInput() })
watch(endAddress, () => { if (activeField.value === 'end') onAddressInput() })

// 当地址坐标变化时自动规划路线
watch([startLat, startLng, endLat, endLng], async () => {
  if (startLat.value && endLat.value && startAddress.value && endAddress.value) {
    await fetchRoutePlan()
  }
}, { deep: true })

// 调用路线规划API
const fetchRoutePlan = async () => {
  routeLoading.value = true
  try {
    const res = await orderApi.routePlan(startLng.value, startLat.value, endLng.value, endLat.value)
    if (res.code === 200) {
      routePlan.value = res.data
      // 根据实际路线距离自动判断行程类型
      tripType.value = res.data.distance > 50000 ? 1 : 0
      // 自动切换对应车型
      if (tripType.value === 1 && carTypeId.value < 4) {
        carTypeId.value = 4
        carTypeName.value = '城际快车'
      } else if (tripType.value === 0 && carTypeId.value >= 4) {
        carTypeId.value = 1
        carTypeName.value = '快车'
      }
      if (tripType.value === 1) {
        ElMessage.info(`距离 ${(res.data.distance/1000).toFixed(0)}km，已自动切换为城际长途模式`)
      }
    }
  } catch (e) {
    console.warn('路线规划失败，使用直线估算', e)
    // 直线fallback也做距离判断
    tripType.value = 5000 > 50000 ? 1 : 0
  }
  routeLoading.value = false
}

// POI 搜索建议
const focusInput = (field) => {
  activeField.value = field
  showSuggestions.value = true
  onAddressInput()
}
const hideSuggestions = () => {
  setTimeout(() => { showSuggestions.value = false }, 200)
}
const selectLocation = async (field, item) => {
  if (!item.lat || !item.lng) {
    // AutoComplete 可能没有坐标，用 PlaceSearch 补查
    const poi = await getPoiLocation(item.name)
    if (poi) { item.lat = poi.lat; item.lng = poi.lng; item.address = poi.address || item.address }
  }
  if (field === 'start') {
    startAddress.value = item.name
    if (item.lat) startLat.value = item.lat
    if (item.lng) startLng.value = item.lng
  } else {
    endAddress.value = item.name
    if (item.lat) endLat.value = item.lat
    if (item.lng) endLng.value = item.lng
  }
  showSuggestions.value = false
  clearPoi()
}

// 车型
const shortCars = computed(() => [
  { id: 1, name: '快车', icon: '🚗', desc: '经济实惠' },
  { id: 2, name: '专车', icon: '🚙', desc: '舒适品质' },
  { id: 3, name: '商务七座', icon: '🚐', desc: '多人出行' }
])
const longCars = computed(() => [
  { id: 4, name: '城际快车', icon: '🚙', desc: '4人座·高速直达' },
  { id: 5, name: '长途大巴', icon: '🚌', desc: '40人座·团体出行' }
])
const carTypes = computed(() => tripType.value === 0 ? shortCars.value : longCars.value)

const selectCar = (id, name) => {
  carTypeId.value = id
  carTypeName.value = name
}

// 价格估算
watch([startAddress, endAddress, carTypeId, routePlan], () => {
  clearTimeout(debounceTimer.value)
  debounceTimer.value = setTimeout(() => {
    if (!startAddress.value || !endAddress.value) return
    fetchEstimate()
  }, 600)
}, { immediate: false })

const fetchEstimate = async () => {
  const dist = routePlan.value?.distance || 5000
  const dur = routePlan.value?.duration || 600
  estimateLoading.value = true
  try {
    const res = await orderApi.estimate({
      startAddress: startAddress.value,
      startLat: startLat.value,
      startLng: startLng.value,
      endAddress: endAddress.value,
      endLat: endLat.value,
      endLng: endLng.value,
      distance: dist,
      duration: dur,
      carTypeId: carTypeId.value,
      tripType: tripType.value,
      couponId: selectedCoupon.value?.id || null
    })
    if (res.code === 200) estimate.value = res.data
  } catch (e) {
    console.warn('价格估算失败')
  }
  estimateLoading.value = false
}

const debouncedEstimate = () => {
  clearTimeout(debounceTimer.value)
  debounceTimer.value = setTimeout(() => {
    if (!startAddress.value || !endAddress.value) return
    fetchEstimate()
  }, 100)
}

// 优惠券
const handleSelectCoupon = async () => {
  try {
    const res = await couponApi.myCoupons()
    // 筛选未使用且未过期的优惠券
    availableCoupons.value = (res.data || []).filter(c => c.status === 0 && c.discount > 0)
  } catch { /* ignore */ }
  showCouponDialog.value = true
}

const selectCoupon = (item) => {
  selectedCoupon.value = item
  showCouponDialog.value = false
  // 选中优惠券后重新估算价格
  debouncedEstimate()
}

const removeCoupon = () => {
  selectedCoupon.value = null
  debouncedEstimate()
}

// 下单
const handleCreate = async () => {
  if (!startAddress.value || !endAddress.value) { ElMessage.warning('请填写起终点'); return }
  loading.value = true
  try {
    const dist = routePlan.value?.distance || 5000
    const dur = routePlan.value?.duration || 600
    const res = await orderApi.create({
      startAddress: startAddress.value,
      startLat: startLat.value,
      startLng: startLng.value,
      endAddress: endAddress.value,
      endLat: endLat.value,
      endLng: endLng.value,
      distance: dist,
      duration: dur,
      carTypeId: carTypeId.value,
      tripType: tripType.value,
      couponId: selectedCoupon.value?.id || null,
      idempotentKey: Date.now().toString()
    })
    if (res.code === 200) {
      ElMessage.success('下单成功')
      router.push(`/order/${res.data.id}`)
    }
  } catch (e) {
    ElMessage.error('下单失败')
  }
  loading.value = false
}
</script>

<style scoped>
.order-create {
  padding: 0;
}

/* 地图 */
.map-area {
  margin: -12px -16px 12px;
}

/* 地址输入区 */
.address-section {
  padding: 8px 16px;
  margin-bottom: 12px;
  position: relative;
}
.address-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}
.address-row + .address-row {
  border-top: 1px solid #eee;
}
.suggestions {
  background: #fff;
  border-radius: var(--radius-sm);
  margin-top: 4px;
  box-shadow: var(--shadow-md);
  max-height: 240px;
  overflow-y: auto;
}
.sug-item {
  padding: 10px 12px;
  cursor: pointer;
  font-size: 0.85rem;
  border-bottom: 1px solid #f5f5f5;
}
.sug-item:hover {
  background: var(--color-primary-bg);
}
.sug-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sug-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.sug-name {
  font-size: 0.85rem;
  font-weight: 500;
}
.sug-addr {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sug-hint {
  padding: 8px 12px;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
.map-hint {
  text-align: center;
  font-size: 0.7rem;
  color: var(--color-text-muted);
  padding: 4px 0 0;
  opacity: 0.7;
}

/* 路线详情卡 */
.route-detail-card {
  margin-bottom: 12px;
}
.rd-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.rd-summary {
  display: flex;
  align-items: center;
  gap: 0;
  margin-bottom: 10px;
}
.rd-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.rd-stat-label {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}
.rd-stat-value {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-text);
}
.rd-divider {
  width: 1px;
  height: 30px;
  background: #e8e8e8;
  flex-shrink: 0;
}
.rd-steps {
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
  max-height: 180px;
  overflow-y: auto;
}
.rd-step {
  display: flex;
  gap: 10px;
  padding: 6px 0;
  position: relative;
}
.rd-step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  flex-shrink: 0;
  margin-top: 5px;
  position: relative;
}
.rd-step-dot::after {
  content: '';
  position: absolute;
  top: 10px;
  left: 3.5px;
  width: 1px;
  height: calc(100% + 4px);
  background: #ddd;
}
.rd-step-dot.is-last::after {
  display: none;
}
.rd-step-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}
.rd-step-road {
  font-size: 0.8rem;
  font-weight: 600;
}
.rd-step-info {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rd-step-dist {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  align-self: flex-end;
}

/* 车型选择 */
.car-type-section {
  margin-bottom: 12px;
}
.car-cards {
  display: flex;
  gap: 8px;
}
.car-card {
  flex: 1;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 12px 6px;
  text-align: center;
  border: 2px solid transparent;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.2s;
}
.car-card.active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}
.car-icon {
  font-size: 1.5rem;
}
.car-name {
  font-size: 0.85rem;
  font-weight: 600;
  margin: 4px 0 2px;
}
.car-desc {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}

/* 价格估算 */
.estimate-section {
  margin-bottom: 16px;
}
.est-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  margin-bottom: 6px;
}
.est-price {
  display: flex;
  justify-content: space-between;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-primary);
  padding-top: 8px;
  border-top: 1px dashed #eee;
}

/* 下单按钮 */
.submit-btn {
  width: 100%;
  height: 48px;
}

/* 优惠券 */
.coupon-row { cursor: pointer; padding: 6px 0; border-top: 1px solid #f5f5f5; margin-top: 4px; }
.coupon-row:hover { background: var(--color-primary-bg); border-radius: 4px; }
.coupon-discount { color: #E6A23C; }
.coupon-select { color: var(--color-primary); font-weight: 500; }
.coupon-name-text { font-size: 0.8rem; color: #999; }
.coupon-list { display: flex; flex-direction: column; gap: 10px; }
.coupon-card {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; border-radius: 8px;
  border: 2px solid #f0f0f0; cursor: pointer;
  transition: all 0.2s; position: relative;
}
.coupon-card.active { border-color: var(--color-primary); background: var(--color-primary-bg); }
.coupon-card:hover { border-color: var(--color-primary); }
.coupon-left { text-align: center; min-width: 80px; }
.coupon-amount { font-size: 1.4rem; font-weight: 700; color: #E6A23C; }
.coupon-condition { font-size: 0.7rem; color: #999; margin-top: 2px; }
.coupon-right { flex: 1; }
.coupon-name { font-size: 0.9rem; font-weight: 600; }
.coupon-expire { font-size: 0.75rem; color: #999; margin-top: 2px; }
.coupon-check {
  position: absolute; top: 4px; right: 8px;
  font-size: 1.2rem; color: var(--color-primary); font-weight: 700;
}
</style>
