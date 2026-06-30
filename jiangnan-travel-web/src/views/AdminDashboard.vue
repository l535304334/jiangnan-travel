<template>
  <div class="admin-page">
    <!-- 指标卡片 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <el-card class="dash-card" :style="{ borderTopColor: c.color }" shadow="never">
          <div class="dash-card-icon" :style="{ background: c.bg }">
            <el-icon :size="24" :color="c.color"><component :is="c.icon" /></el-icon>
          </div>
          <div class="dash-card-body">
            <div class="dash-card-value">{{ c.value }}</div>
            <div class="dash-card-label">{{ c.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <div class="card-header">📊 近7日订单趋势</div>
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <div class="card-header">💰 近7日收入趋势</div>
          <div ref="revenueChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Van, Tickets, Warning } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

let echarts = null
async function loadEcharts() {
  if (!echarts) {
    echarts = await import('echarts')
  }
  return echarts
}

const stats = reactive({
  totalUsers: 0,
  todayOrders: 0,
  onlineDrivers: 0,
  todayRevenue: 0,
  alertCount: 0
})

const chartData = ref([])
const orderChartRef = ref(null)
const revenueChartRef = ref(null)
let orderChart = null
let revenueChart = null

const cards = computed(() => [
  { label: '总用户数', value: stats.totalUsers, icon: User, color: '#409EFF', bg: '#E8F4FD' },
  { label: '今日订单', value: stats.todayOrders, icon: Tickets, color: '#52C41A', bg: '#EBF8E8' },
  { label: '在线司机', value: stats.onlineDrivers, icon: Van, color: '#2D8A6E', bg: '#E8F5EE' },
  { label: '风控告警', value: stats.alertCount, icon: Warning, color: '#FAAD14', bg: '#FFF7E6' }
])

async function renderCharts(data) {
  if (!Array.isArray(data) || data.length === 0) return
  const echartsLib = await loadEcharts()
  const dates = data.map(d => d.date ? d.date.slice(5) : '')
  const orderCounts = data.map(d => d.orderCount || 0)
  const revenues = data.map(d => d.revenue || 0)

  // 订单趋势折线图
  if (orderChartRef.value) {
    if (!orderChart) orderChart = echartsLib.init(orderChartRef.value)
    orderChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        type: 'line', smooth: true, data: orderCounts,
        lineStyle: { color: '#2D8A6E', width: 3 },
        areaStyle: { color: new echartsLib.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(45,138,110,0.3)' },
          { offset: 1, color: 'rgba(45,138,110,0.02)' }
        ])},
        itemStyle: { color: '#2D8A6E' }
      }]
    })
  }

  // 收入趋势柱状图
  if (revenueChartRef.value) {
    if (!revenueChart) revenueChart = echarts.init(revenueChartRef.value)
    revenueChart.setOption({
      tooltip: { trigger: 'axis', valueFormatter: v => '¥' + v },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
      series: [{
        type: 'bar', data: revenues,
        itemStyle: {
          color: new echartsLib.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#52C41A' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }]
    })
  }
}

function resizeCharts() {
  orderChart?.resize()
  revenueChart?.resize()
}

onMounted(async () => {
  try {
    const res = await adminApi.dashboard()
    Object.assign(stats, res.data || {})
  } catch { ElMessage.error('仪表盘数据加载失败') }

  try {
    const res = await adminApi.chartData()
    chartData.value = res.data || []
    await nextTick()
    await renderCharts(chartData.value)
  } catch { /* chart data optional */ }

  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  orderChart?.dispose()
  revenueChart?.dispose()
})
</script>

<style scoped>
.dash-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  border-top: 3px solid;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.dash-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.dash-card-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dash-card-value {
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  line-height: 1.2;
}

.dash-card-label {
  font-size: var(--font-size-body-small);
  color: var(--color-text-muted);
  margin-top: 2px;
}

.chart-row {
  margin-top: var(--spacing-2xl);
}

.chart-card {
  transition: box-shadow var(--transition-fast);
}

.chart-card:hover {
  box-shadow: var(--shadow-md);
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>
