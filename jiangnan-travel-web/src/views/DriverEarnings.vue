<template>
  <div class="driver-earnings">
    <div class="today-stats">
      <div class="earn-card primary">
        <div class="earn-value">¥{{ today.earnings }}</div>
        <div class="earn-label">今日收入</div>
      </div>
      <el-row :gutter="10">
        <el-col :span="12">
          <div class="earn-card">
            <div class="earn-value">{{ today.orders }}</div>
            <div class="earn-label">今日订单</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="earn-card">
            <div class="earn-value">{{ today.onlineTime }}</div>
            <div class="earn-label">在线时长</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="week-summary">
      <h4>本周汇总</h4>
      <div class="summary-row">
        <span>订单数</span><strong>{{ week.orders }}</strong>
      </div>
      <div class="summary-row">
        <span>总收入</span><strong>¥{{ week.earnings }}</strong>
      </div>
    </div>

    <div class="recent-orders">
      <h4>近期订单</h4>
      <div v-for="o in recentOrders" :key="o.id" class="order-item">
        <div class="order-left">
          <div class="order-route">{{ o.start }} → {{ o.end }}</div>
          <div class="order-time">{{ o.time }}</div>
        </div>
        <div class="order-amount">¥{{ o.amount }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const today = reactive({ earnings: '0', orders: '0', onlineTime: '0h' })
const week = reactive({ orders: '0', earnings: '0' })

const recentOrders = [
  { id: 1, start: '无锡站', end: '太湖广场', time: '10:30', amount: '28' },
  { id: 2, start: '南禅寺', end: '江南大学', time: '09:15', amount: '35' },
  { id: 3, start: '惠山古镇', end: '无锡东站', time: '昨天', amount: '42' },
]
</script>

<style scoped>
.earn-card {
  background: #fff; border-radius: var(--radius-md); padding: 16px;
  text-align: center; box-shadow: var(--shadow-sm); margin-bottom: 10px;
}
.earn-card.primary { background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)); color: #fff; }
.earn-card.primary .earn-label { color: rgba(255,255,255,0.8); }
.earn-value { font-size: 1.6rem; font-weight: 700; }
.earn-label { font-size: 0.8rem; color: var(--color-text-muted); margin-top: 4px; }
.week-summary, .recent-orders {
  background: #fff; border-radius: var(--radius-md); padding: 16px;
  margin-top: 14px; box-shadow: var(--shadow-sm);
}
.summary-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--color-border-light); }
.summary-row:last-child { border-bottom: none; }
.order-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--color-border-light); }
.order-item:last-child { border-bottom: none; }
.order-route { font-weight: 600; }
.order-time { font-size: 0.8rem; color: var(--color-text-muted); margin-top: 2px; }
.order-amount { font-size: 1.1rem; font-weight: 700; color: var(--color-primary); }
h4 { margin-bottom: 10px; color: var(--color-text); }
</style>
