<template>
  <div class="admin-dashboard">
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="dash-card" :style="{ borderTopColor: c.color }">
          <div class="dash-card-icon">
            <el-icon :size="28" :color="c.color"><component :is="c.icon" /></el-icon>
          </div>
          <div class="dash-card-body">
            <div class="dash-card-value">{{ c.value }}</div>
            <div class="dash-card-label">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="dash-section">
      <h3>数据概览</h3>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="平台注册用户">{{ stats.totalUsers }}</el-descriptions-item>
        <el-descriptions-item label="今日订单">{{ stats.todayOrders }}</el-descriptions-item>
        <el-descriptions-item label="在线司机">{{ stats.onlineDrivers }}</el-descriptions-item>
        <el-descriptions-item label="今日收入">¥{{ stats.todayRevenue }}</el-descriptions-item>
        <el-descriptions-item label="未处理风控">{{ stats.alertCount }}</el-descriptions-item>
        <el-descriptions-item label="数据来源">实时接口</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { User, Van, Tickets, Warning } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

const stats = reactive({
  totalUsers: 0,
  todayOrders: 0,
  onlineDrivers: 0,
  todayRevenue: 0,
  alertCount: 0
})

const cards = computed(() => [
  { label: '总用户数', value: stats.totalUsers, icon: User, color: '#1890FF' },
  { label: '今日订单', value: stats.todayOrders, icon: Tickets, color: '#52C41A' },
  { label: '在线司机', value: stats.onlineDrivers, icon: Van, color: '#2D8A6E' },
  { label: '风控告警', value: stats.alertCount, icon: Warning, color: '#FAAD14' }
])

onMounted(async () => {
  const res = await adminApi.dashboard()
  Object.assign(stats, res.data || {})
})
</script>

<style scoped>
.dash-card {
  background: #fff; border-radius: var(--radius-md); padding: 20px;
  display: flex; align-items: center; gap: 16px;
  border-top: 3px solid; box-shadow: var(--shadow-sm);
}
.dash-card-value { font-size: 1.6rem; font-weight: 700; }
.dash-card-label { font-size: 0.85rem; color: var(--color-text-muted); }
.dash-section { margin-top: 24px; }
.dash-section h3 { margin-bottom: 12px; }
</style>
