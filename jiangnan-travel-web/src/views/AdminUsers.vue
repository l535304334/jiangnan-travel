<template>
  <div class="admin-users">
    <div class="page-header">
      <h3>用户管理</h3>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="statusText" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.statusText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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

const statusText = (status) => (status === 1 ? '启用' : status === 2 ? '冻结' : '禁用')

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.users({ page: 1, size: 20 })
    tableData.value = (res.data?.records || []).map(item => ({
      ...item,
      statusText: statusText(item.status)
    }))
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await adminApi.updateUserStatus(row.id, nextStatus)
  row.status = nextStatus
  row.statusText = statusText(nextStatus)
  ElMessage.success('状态已更新')
}

onMounted(loadData)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
</style>
