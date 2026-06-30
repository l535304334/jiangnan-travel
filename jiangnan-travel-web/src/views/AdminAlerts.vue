<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h3>风控告警</h3>
    </div>
    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe :border="false" style="width: 100%">
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
      <div class="admin-pagination-wrap">
        <el-pagination
          v-model:current-page="pageInfo.current"
          v-model:page-size="pageInfo.size"
          :total="pageInfo.total"
          layout="total, prev, pager, next, jumper"
          background
          small
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const pageInfo = ref({ current: 1, size: 20, total: 0 })
const levelMap = {
  1: ['低危', 'info'],
  2: ['中危', 'warning'],
  3: ['高危', 'danger']
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.alerts({ page: pageInfo.value.current, size: pageInfo.value.size })
    pageInfo.value.total = res.data.total
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
  } catch (e) { ElMessage.error('告警加载失败') }
  finally {
    loading.value = false
  }
}

const handleAlert = async (row) => {
  try {
    await adminApi.handleAlert(row.id)
    row.handled = 1
    row.statusText = '已处理'
    ElMessage.success('告警已处理')
  } catch (e) { ElMessage.error('处理告警失败') }
}

onMounted(loadData)
</script>

<style scoped>
</style>
