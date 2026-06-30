<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h3>定价管理</h3>
    </div>
    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe :border="false" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="车型名称" width="140" />
        <el-table-column prop="basePrice" label="起步价" width="100">
          <template #default="{ row }">
            ¥{{ row.basePrice }}
          </template>
        </el-table-column>
        <el-table-column prop="midPerKm" label="每公里单价" width="120">
          <template #default="{ row }">
            ¥{{ row.midPerKm }}
          </template>
        </el-table-column>
        <el-table-column prop="perMinPrice" label="每分钟单价" width="120">
          <template #default="{ row }">
            ¥{{ row.perMinPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              :loading="row.toggleLoading"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
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

const statusText = (status) => (status === 1 ? '启用' : '停用')

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.carTypes({ page: pageInfo.value.current, size: pageInfo.value.size })
    const raw = res.data
    const records = Array.isArray(raw) ? raw : (raw?.records || [])
    pageInfo.value.total = Array.isArray(raw) ? raw.length : (raw?.total || 0)
    tableData.value = records.map(item => ({
      ...item,
      basePrice: item.basePrice,
      midPerKm: item.midPerKm,
      perMinPrice: item.perMinPrice,
      statusText: statusText(item.status),
      toggleLoading: false
    }))
  } catch (e) { ElMessage.error('车型数据加载失败') }
  finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  row.toggleLoading = true
  try {
    const nextStatus = row.status === 1 ? 0 : 1
    await adminApi.updateCarType(row.id, { status: nextStatus })
    row.status = nextStatus
    row.statusText = statusText(nextStatus)
    ElMessage.success('状态已更新')
  } catch (e) { ElMessage.error('状态更新失败') }
  finally {
    row.toggleLoading = false
  }
}

onMounted(loadData)
</script>

<style scoped>
</style>
