<template>
  <div class="admin-users">
    <div class="page-header">
      <h3>用户管理</h3>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%">
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const tableData = ref([
  { id: 1, phone: '13800000001', nickname: '江南旅人', status: 1, statusText: '启用', createTime: '2026-01-15 10:30' },
  { id: 2, phone: '13800000002', nickname: '无锡小张', status: 1, statusText: '启用', createTime: '2026-02-20 14:22' },
  { id: 3, phone: '13800000003', nickname: '太湖游客', status: 0, statusText: '禁用', createTime: '2026-03-10 09:15' },
  { id: 4, phone: '13800000004', nickname: '江南好', status: 1, statusText: '启用', createTime: '2026-04-05 16:48' },
])

const toggleStatus = (row) => {
  row.status = row.status === 1 ? 0 : 1
  row.statusText = row.status === 1 ? '启用' : '禁用'
  ElMessage.success('状态已更新')
}
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
</style>
