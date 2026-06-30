<template>
  <div class="driver-order app-page">
    <div class="order-header app-section">
      <h3 class="app-section-title">订单 #{{ order.orderNo || '---' }}</h3>
      <el-tag :type="statusType">{{ statusText }}</el-tag>
    </div>

    <div class="route-block app-card">
      <div class="route-point">
        <span class="dot start"></span>
        <span>{{ order.startAddress || '---' }}</span>
      </div>
      <div class="route-line"></div>
      <div class="route-point">
        <span class="dot end"></span>
        <span>{{ order.endAddress || '---' }}</span>
      </div>
    </div>

    <div class="info-grid app-card">
      <div class="info-item" v-if="order.passengerPhone"><label>乘客电话</label><span>{{ order.passengerPhone }}</span></div>
      <div class="info-item"><label>预估价格</label><span>¥{{ order.finalPrice ?? order.price ?? '0' }}</span></div>
      <div class="info-item"><label>预估距离</label><span>{{ order.distance ? (order.distance/1000).toFixed(1) + ' km' : '---' }}</span></div>
    </div>

    <div class="map-placeholder app-card">
      <el-icon :size="36"><MapLocation /></el-icon>
      <p>地图导航区域</p>
    </div>

    <div class="action-btns app-section">
      <el-button v-if="order.status === 0" type="success" size="large" @click="doAction('accept')">接单</el-button>
      <el-button v-if="order.status === 1" type="warning" size="large" @click="doAction('arrive')">到达</el-button>
      <el-button v-if="order.status === 2" type="primary" size="large" @click="doAction('start')">开始行程</el-button>
      <el-button v-if="order.status === 3" type="success" size="large" @click="doAction('complete')">完成行程</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '@/api/order'
import { driverApi } from '@/api/driver'
import { MapLocation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const order = ref({})

const statusMap = { 0: '待接单', 1: '待到达', 2: '行程中', 3: '待完成', 4: '已完成', 5: '已取消' }
const statusTypeMap = { 0: 'warning', 1: 'info', 2: '', 3: 'primary', 4: 'success', 5: 'info' }

const statusText = computed(() => statusMap[order.value.status] || '---')
const statusType = computed(() => statusTypeMap[order.value.status] || 'info')

const loadOrder = async () => {
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data || res
  } catch (e) {
    ElMessage.error(e?.message || '加载订单失败')
  }
}

const doAction = async (action) => {
  const actions = {
    accept: () => driverApi.acceptOrder(route.params.id),
    arrive: () => driverApi.arriveOrder(route.params.id),
    start: () => driverApi.startTrip(route.params.id),
    complete: () => driverApi.completeTrip(route.params.id)
  }
  try {
    await actions[action]()
    ElMessage.success('操作成功')
    loadOrder()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(loadOrder)
</script>

<style scoped>
.driver-order { max-width: 480px; margin: 0 auto; }
.order-header { display: flex; justify-content: space-between; align-items: center; }
.route-block { margin-bottom: 12px; }
.route-point { display: flex; align-items: center; gap: 8px; }
.dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.dot.start { background: var(--color-primary); }
.dot.end { background: var(--color-danger); }
.route-line { width: 2px; height: 24px; background: var(--color-border); margin-left: 4px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px; }
.info-item label { font-size: 0.8rem; color: var(--color-text-muted); display: block; margin-bottom: 4px; }
.info-item span { font-weight: 600; }
.map-placeholder {
  height: 180px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: var(--color-text-muted);
  margin-bottom: 12px;
}
.action-btns { display: flex; gap: 10px; flex-wrap: wrap; }
.action-btns .el-button { flex: 1; }
</style>
