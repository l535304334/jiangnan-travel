<template>
  <div class="order-list">
    <div class="tabs">
      <span v-for="tab in tabs" :key="tab.key" class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key; fetchOrders()">{{ tab.label }}</span>
    </div>

    <div class="order-cards">
      <div class="order-card" v-for="item in filteredOrders" :key="item.id"
           @click="$router.push(`/order/${item.id}`)">
        <div class="order-top">
          <span class="order-no">{{ item.orderNo }}</span>
          <el-tag :type="statusTag(item.status)" size="small">{{ item.statusText }}</el-tag>
        </div>
        <div class="order-route">
          <span class="addr">{{ item.startAddress || '起点' }}</span>
          <el-icon><ArrowRight /></el-icon>
          <span class="addr">{{ item.endAddress || '终点' }}</span>
        </div>
        <div class="order-bottom">
          <span>{{ formatTime(item.createTime) }}</span>
          <span class="order-price">¥{{ item.finalPrice || '0.00' }}</span>
        </div>
      </div>
      <el-empty v-if="filteredOrders.length === 0 && !loading" description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { orderApi } from '@/api/order'

const activeTab = ref(0)
const orders = ref([])
const loading = ref(false)

const tabs = [
  { key: -1, label: '全部' },
  { key: 0, label: '待接单' },
  { key: 1, label: '已接单' },
  { key: 4, label: '已完成' },
  { key: 5, label: '已取消' }
]

const statusTag = (s) => {
  if (s === 0) return 'warning'
  if (s === 1 || s === 2 || s === 3) return ''
  if (s === 4) return 'success'
  if (s === 5) return 'info'
  return ''
}

const filteredOrders = computed(() => orders.value)

const formatTime = (t) => {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (activeTab.value >= 0) params.status = activeTab.value
    const res = await orderApi.list(params)
    orders.value = res.data || []
  } catch (e) { orders.value = [] }
  loading.value = false
}

onMounted(fetchOrders)
</script>

<style scoped>
.tabs { display: flex; background: #fff; border-radius: var(--radius-md); overflow-x: auto; margin-bottom: 12px; box-shadow: var(--shadow-sm); }
.tab-item { flex-shrink: 0; padding: 10px 14px; font-size: 0.82rem; color: var(--color-text-secondary); cursor: pointer; border-bottom: 2px solid transparent; white-space: nowrap; }
.tab-item.active { color: var(--color-primary); border-bottom-color: var(--color-primary); font-weight: 600; }
.order-card { background: #fff; border-radius: var(--radius-md); padding: 14px; margin-bottom: 10px; box-shadow: var(--shadow-sm); cursor: pointer; }
.order-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.order-no { font-size: 0.8rem; color: var(--color-text-muted); }
.order-route { display: flex; align-items: center; gap: 8px; font-size: 0.9rem; margin-bottom: 8px; }
.addr { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.order-bottom { display: flex; justify-content: space-between; color: var(--color-text-muted); font-size: 0.8rem; }
.order-price { color: var(--color-primary-dark); font-weight: 700; font-size: 1rem; }
</style>
