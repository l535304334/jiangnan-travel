<template>
  <div class="driver-earnings app-page">
    <div class="today-stats">
      <div class="card-primary">
        <div class="earn-value">¥{{ today.earnings }}</div>
        <div class="card-stat-label">今日收入</div>
      </div>
      <el-row :gutter="10">
        <el-col :span="12">
          <div class="card-stat">
            <div class="earn-value">{{ today.orders }}</div>
            <div class="card-stat-label">今日订单</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="card-stat">
            <div class="earn-value">{{ today.onlineTime }}</div>
            <div class="card-stat-label">在线时长</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="week-summary app-card">
      <h4 class="app-section-title">本周汇总</h4>
      <div class="summary-row">
        <span>订单数</span><strong>{{ week.orders }}</strong>
      </div>
      <div class="summary-row">
        <span>总收入</span><strong>¥{{ week.earnings }}</strong>
      </div>
    </div>

    <div class="recent-orders app-card">
      <h4 class="app-section-title">近期订单</h4>
      <div v-for="o in recentOrders" :key="o.id" class="order-item app-list-item">
        <div class="order-left">
          <div class="order-route">{{ o.startAddress }} → {{ o.endAddress }}</div>
          <div class="order-time">{{ o.createTime }}</div>
        </div>
        <div class="order-amount">¥{{ o.finalPrice || o.price || '0' }}</div>
      </div>
      <el-empty v-if="!recentOrders.length" description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { driverApi } from '@/api/driver'

const today = reactive({ earnings: '0', orders: '0', onlineTime: '0h' })
const week = reactive({ orders: '0', earnings: '0' })
const recentOrders = ref([])

const loadData = async () => {
  try {
    // 今日收入统计
    const earnRes = await driverApi.earning()
    if (earnRes.code === 200) {
      const data = earnRes.data
      today.earnings = (data.todayEarnings || '0').toString()
      today.orders = (data.todayOrders || '0').toString()
      const mins = data.onlineMinutes || 0
      today.onlineTime = mins >= 60 ? Math.floor(mins / 60) + 'h' + (mins % 60) + 'm' : mins + 'm'
    }

    // 本周收入汇总
    const weekRes = await driverApi.weeklyEarning()
    if (weekRes.code === 200) {
      const data = weekRes.data
      week.earnings = (data.weekTotal || '0').toString()
      week.orders = (data.weekOrders || '0').toString()
    }

    // 近期已完成订单
    const orderRes = await driverApi.orderHistory(4, 1, 10)
    if (orderRes.code === 200) {
      recentOrders.value = orderRes.data || []
    }
  } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.earn-value { font-size: 1.6rem; font-weight: 700; }
.week-summary, .recent-orders {
  margin-top: 14px;
}
.summary-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--color-border-light); }
.summary-row:last-child { border-bottom: none; }
.order-item { display: flex; justify-content: space-between; align-items: center; }
.order-route { font-weight: 600; }
.order-time { font-size: 0.8rem; color: var(--color-text-muted); margin-top: 2px; }
.order-amount { font-size: 1.1rem; font-weight: 700; color: var(--color-primary); }
</style>
