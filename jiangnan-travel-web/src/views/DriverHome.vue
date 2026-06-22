<template>
  <div class="driver-home">
    <el-row :gutter="12">
      <el-col :span="8" v-for="s in stats" :key="s.label">
        <div class="stat-card">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="section-title">待接订单</div>
    <div v-if="pendingOrders.length" class="order-list">
      <div v-for="o in pendingOrders" :key="o.id" class="order-card" @click="$router.push(`/driver/order/${o.id}`)">
        <div class="order-route">{{ o.startAddress }} → {{ o.endAddress }}</div>
        <div class="order-info">¥{{ o.price }} · {{ o.distance }}</div>
      </div>
    </div>
    <el-empty v-else description="暂无待接订单" />

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
import { ref, computed } from 'vue'
import { driverApi } from '@/api/driver'
import { Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const stats = ref([
  { label: '在线时长', value: '0h' },
  { label: '完成订单', value: '0' },
  { label: '今日收入', value: '¥0' }
])

const pendingOrders = ref([])

const driverId = computed(() => JSON.parse(localStorage.getItem('driverInfo') || '{}').id || 0)

const startWork = async () => {
  try {
    await driverApi.updateStatus(driverId.value, 1)
    ElMessage.success('已开始接单')
  } catch {}
}
</script>

<style scoped>
.stat-card {
  background: #fff; border-radius: var(--radius-md);
  padding: 16px; text-align: center; box-shadow: var(--shadow-sm);
}
.stat-value { font-size: 1.4rem; font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: 0.8rem; color: var(--color-text-muted); margin-top: 4px; }
.section-title { font-size: 1rem; font-weight: 600; margin: 20px 0 12px; }
.order-card {
  background: #fff; border-radius: var(--radius-md); padding: 14px;
  margin-bottom: 10px; box-shadow: var(--shadow-sm); cursor: pointer;
}
.order-route { font-weight: 600; margin-bottom: 4px; }
.order-info { color: var(--color-text-secondary); font-size: 0.85rem; }
.action-area { margin-top: 24px; text-align: center; }
.start-btn { width: 100%; }
.location-note {
  margin-top: 12px; font-size: 0.8rem; color: var(--color-text-muted);
  display: flex; align-items: center; justify-content: center; gap: 4px;
}
</style>
