<template>
  <div class="driver-order">
    <div class="order-header">
      <h3>订单 #{{ order.orderNo || '---' }}</h3>
      <el-tag :type="statusType">{{ statusText }}</el-tag>
    </div>

    <div class="route-block">
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

    <div class="info-grid">
      <div class="info-item"><label>乘客电话</label><span>{{ order.passengerPhone || '---' }}</span></div>
      <div class="info-item"><label>预估价格</label><span>¥{{ order.price || '0' }}</span></div>
      <div class="info-item"><label>预估距离</label><span>{{ order.distance || '---' }}</span></div>
    </div>

    <div class="map-placeholder">
      <el-icon :size="36"><MapLocation /></el-icon>
      <p>地图导航区域</p>
    </div>

    <div class="action-btns">
      <el-button v-if="order.status === 1" type="success" @click="doAction('accept')">接单</el-button>
      <el-button v-if="order.status === 2" type="warning" @click="doAction('arrive')">到达</el-button>
      <el-button v-if="order.status === 3" type="primary" @click="doAction('start')">开始行程</el-button>
      <el-button v-if="order.status === 4" type="success" @click="doAction('complete')">完成行程</el-button>
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

const driverInfo = JSON.parse(localStorage.getItem('driverInfo') || '{}')

const statusMap = { 1: '待接单', 2: '待到达', 3: '行程中', 4: '待完成', 5: '已完成' }
const statusTypeMap = { 1: 'warning', 2: 'info', 3: '', 4: 'primary', 5: 'success' }

const statusText = computed(() => statusMap[order.value.status] || '---')
const statusType = computed(() => statusTypeMap[order.value.status] || 'info')

const loadOrder = async () => {
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data || res
  } catch {}
}

const doAction = async (action) => {
  const actions = {
    accept: () => driverApi.acceptOrder(route.params.id, driverInfo.id),
    arrive: () => driverApi.arriveOrder(route.params.id, driverInfo.id),
    start: () => driverApi.startTrip(route.params.id, driverInfo.id),
    complete: () => driverApi.completeTrip(route.params.id, driverInfo.id)
  }
  try {
    await actions[action]()
    ElMessage.success('操作成功')
    loadOrder()
  } catch {}
}

onMounted(loadOrder)
</script>

<style scoped>
.driver-order { max-width: 480px; margin: 0 auto; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.route-block { background: #fff; border-radius: var(--radius-md); padding: 20px; margin-bottom: 12px; }
.route-point { display: flex; align-items: center; gap: 8px; }
.dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.dot.start { background: var(--color-primary); }
.dot.end { background: var(--color-danger); }
.route-line { width: 2px; height: 24px; background: var(--color-border); margin-left: 4px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px; }
.info-item { background: #fff; border-radius: var(--radius-sm); padding: 12px; }
.info-item label { font-size: 0.8rem; color: var(--color-text-muted); display: block; margin-bottom: 4px; }
.info-item span { font-weight: 600; }
.map-placeholder {
  background: var(--color-bg-secondary); border-radius: var(--radius-md);
  height: 180px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: var(--color-text-muted);
}
.action-btns { display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
.action-btns .el-button { flex: 1; }
</style>
