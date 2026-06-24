<template>
  <div class="admin-drivers">
    <div class="page-header">
      <h3>司机审核</h3>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="plate" label="车牌号" width="120" />
      <el-table-column prop="carType" label="车型" width="100" />
      <el-table-column prop="statusText" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.statusText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="verifyText" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.verifyStatus === 1 ? 'success' : row.verifyStatus === 2 ? 'danger' : 'warning'" size="small">
            {{ row.verifyText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.verifyStatus === 0" type="success" size="small" @click="audit(row, 1)">通过</el-button>
          <el-button v-if="row.verifyStatus === 0" type="danger" size="small" @click="audit(row, 2)">拒绝</el-button>
          <span v-else>---</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const pageInfo = ref({ current: 1, size: 20, total: 0 })

const verifyText = (status) => (status === 1 ? '已通过' : status === 2 ? '已拒绝' : '待审核')

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.drivers({ page: pageInfo.value.current, size: pageInfo.value.size })
    pageInfo.value.total = res.data.total
    tableData.value = (res.data?.records || []).map(item => ({
      ...item,
      name: item.realName,
      plate: item.carPlate,
      carType: item.carTypeId ? `车型${item.carTypeId}` : '-',
      statusText: item.status === 1 ? '在线' : '离线',
      verifyText: verifyText(item.verifyStatus)
    }))
  } finally {
    loading.value = false
  }
}

const audit = async (row, status) => {
  await adminApi.verifyDriver(row.id, status)
  row.verifyStatus = status
  row.verifyText = verifyText(status)
  ElMessage.success(status === 1 ? '已通过审核' : '已拒绝')
}

onMounted(loadData)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
