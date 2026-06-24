<template>
  <div class="admin-orders">
    <div class="page-header">
      <h3>订单监控</h3>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="loadData">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="user" label="用户" width="100" />
      <el-table-column prop="driver" label="司机" width="100" />
      <el-table-column label="行程" min-width="180">
        <template #default="{ row }">
          <span class="route-text">{{ row.start }} → {{ row.end }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="金额" width="80" />
      <el-table-column prop="statusText" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.statusTag" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="time" label="时间" width="160" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'

const statusFilter = ref('')
const loading = ref(false)
const statusOptions = [
  { label: '待接单', value: 0 }, { label: '已接单', value: 1 },
  { label: '已到达', value: 2 }, { label: '行程中', value: 3 },
  { label: '已完成', value: 4 }, { label: '已取消', value: 5 }
]

const tableData = ref([])
const statusMap = {
  0: ['待接单', 'warning'],
  1: ['已接单', ''],
  2: ['已到达', ''],
  3: ['行程中', 'primary'],
  4: ['已完成', 'success'],
  5: ['已取消', 'info'],
  6: ['风控拦截', 'danger']
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 20 }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await adminApi.orders(params)
    tableData.value = (res.data?.records || []).map(item => {
      const [statusText, statusTag] = statusMap[item.status] || ['未知', 'info']
      return {
        ...item,
        user: item.userId || '-',
        driver: item.driverId || '---',
        start: item.startAddress,
        end: item.endAddress,
        price: item.finalPrice != null ? `¥${item.finalPrice}` : '-',
        statusText,
        statusTag,
        time: item.createTime
      }
    })
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.route-text { font-size: 0.85rem; }
</style>
