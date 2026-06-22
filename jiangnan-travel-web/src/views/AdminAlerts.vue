<template>
  <div class="admin-alerts">
    <div class="page-header">
      <h3>风控告警</h3>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%">
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const tableData = ref([
  { id: 1, levelText: '高危', levelTag: 'danger', ruleCode: 'RISK-SPEED-001', user: '江南旅人', orderNo: 'JN20260621001', time: '2026-06-21 10:35', handled: false, statusText: '未处理' },
  { id: 2, levelText: '中危', levelTag: 'warning', ruleCode: 'RISK-ROUTE-003', user: '无锡小张', orderNo: 'JN20260621002', time: '2026-06-21 11:20', handled: false, statusText: '未处理' },
  { id: 3, levelText: '低危', levelTag: 'info', ruleCode: 'RISK-CANCEL-005', user: '太湖游客', orderNo: 'JN20260621003', time: '2026-06-21 12:05', handled: true, statusText: '已处理' },
  { id: 4, levelText: '高危', levelTag: 'danger', ruleCode: 'RISK-AREA-002', user: '江南好', orderNo: 'JN20260621004', time: '2026-06-21 14:25', handled: false, statusText: '未处理' },
])

const handleAlert = (row) => {
  row.handled = true
  row.statusText = '已处理'
  ElMessage.success('告警已处理')
}
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
</style>
