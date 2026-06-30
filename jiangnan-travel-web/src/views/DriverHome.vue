<template>
  <div class="driver-home app-page">
    <el-row :gutter="12">
      <el-col :span="8" v-for="s in stats" :key="s.label">
        <div class="card-stat">
          <div class="card-stat-value">{{ s.value }}</div>
          <div class="card-stat-label">{{ s.label }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="app-section">
      <h4 class="app-section-title">待接订单</h4>
      <TransitionGroup name="list-fade" tag="div" v-if="pendingOrders.length" class="order-list">
        <div v-for="o in pendingOrders" :key="o.id" class="app-list-item" @click="$router.push(`/driver/order/${o.id}`)">
          <div class="order-route">{{ o.startAddress }} → {{ o.endAddress }}</div>
          <div class="order-info">¥{{ o.finalPrice ?? '0' }} · {{ o.distance ? (o.distance/1000).toFixed(1) + ' km' : '---' }}</div>
        </div>
      </TransitionGroup>
      <el-empty v-else description="暂无待接订单" />
    </div>

    <div class="action-area">
      <el-button type="primary" size="large" class="start-btn" @click="startWork">
        开始接单
      </el-button>
      <p class="location-note">
        <el-icon><Warning /></el-icon>
        接单期间将获取您的位置信息，请保持定位开启
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { driverApi } from '@/api/driver'
import { Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const stats = ref([
  { label: '在线时长', value: '0h' },
  { label: '完成订单', value: '0' },
  { label: '今日收入', value: '¥0' }
])

const pendingOrders = ref([])

const loadHomeData = async () => {
  try {
    // 加载收入统计
    const earnRes = await driverApi.earning()
    if (earnRes.code === 200) {
      const data = earnRes.data
      const mins = data.onlineMinutes || 0
      stats.value[0].value = mins >= 60 ? Math.floor(mins / 60) + 'h' + (mins % 60) + 'm' : mins + 'm'
      stats.value[1].value = (data.totalOrders || '0').toString()
      stats.value[2].value = '¥' + (data.todayEarnings || '0')
    }
    // 加载待接订单
    const orderRes = await driverApi.pendingOrders()
    if (orderRes.code === 200) {
      pendingOrders.value = orderRes.data || []
    }
  } catch {}
}

const startWork = async () => {
  try {
    await driverApi.updateStatus(1)
    ElMessage.success('已开始接单')
  } catch {}
}

onMounted(loadHomeData)
</script>

<style scoped>
.order-route { font-weight: 600; margin-bottom: 4px; }
.order-info { color: var(--color-text-secondary); font-size: 0.85rem; }
.action-area { margin-top: 24px; text-align: center; }
.start-btn { width: 100%; }
.location-note {
  margin-top: 12px; font-size: 0.8rem; color: var(--color-text-muted);
  display: flex; align-items: center; justify-content: center; gap: 4px;
}
</style>
