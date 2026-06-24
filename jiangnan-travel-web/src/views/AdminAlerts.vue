<template>
  <div class="admin-alerts">
    <div class="page-header">
      <h3>风控告警</h3>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
      <el-table-column label="告警级别" width="100">
        <template #default="{ row }">
          <el-tag :type="row.levelTag" size="small">{{ row.levelText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ruleCode" label="规则编码" width="140" />
      <el-table-column prop="user" label="关联用户" width="120" />
      <el-table-column prop="orderNo" label="关联订单" width="180" />
      <el-table-column prop="time" label="告警时间" width="170" />
      <el-table-column prop="statusText" label="处理状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.handled ? 'success' : 'warning'" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="!row.handled" type="primary" size="small" @click="handleAlert(row)">处理</el-button>
          <span v-else>---</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const levelMap = {
  1: ['低危', 'info'],
  2: ['中危', 'warning'],
  3: ['高危', 'danger']
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.alerts({ page: 1, size: 20 })
    tableData.value = (res.data?.records || []).map(item => {
      const [levelText, levelTag] = levelMap[item.alertLevel] || ['提醒', 'info']
      return {
        ...item,
        levelText,
        levelTag,
        user: item.userId || '-',
        orderNo: item.orderId || '-',
        time: item.createTime,
        handled: item.handled === 1,
        statusText: item.handled === 1 ? '已处理' : '未处理'
      }
    })
  } finally {
    loading.value = false
  }
}

const handleAlert = async (row) => {
  await adminApi.handleAlert(row.id)
  row.handled = 1
  row.statusText = '已处理'
  ElMessage.success('告警已处理')
}

onMounted(loadData)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
</style>
