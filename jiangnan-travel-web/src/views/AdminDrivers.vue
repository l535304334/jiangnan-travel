<template>
  <div class="admin-drivers">
    <div class="page-header">
      <h3>司机审核</h3>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%">
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
          <el-tag :type="row.verifyStatus === 2 ? 'success' : row.verifyStatus === 3 ? 'danger' : 'warning'" size="small">
            {{ row.verifyText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.verifyStatus === 1" type="success" size="small" @click="audit(row, 2)">通过</el-button>
          <el-button v-if="row.verifyStatus === 1" type="danger" size="small" @click="audit(row, 3)">拒绝</el-button>
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
  { id: 1, name: '张师傅', plate: '苏B·A8888', carType: '舒适型', status: 1, statusText: '在线', verifyStatus: 2, verifyText: '已通过' },
  { id: 2, name: '李师傅', plate: '苏B·B6666', carType: '经济型', status: 0, statusText: '离线', verifyStatus: 1, verifyText: '待审核' },
  { id: 3, name: '王师傅', plate: '苏B·C1000', carType: '商务型', status: 1, statusText: '在线', verifyStatus: 3, verifyText: '已拒绝' },
  { id: 4, name: '赵师傅', plate: '苏B·D2222', carType: '舒适型', status: 0, statusText: '离线', verifyStatus: 1, verifyText: '待审核' },
])

const audit = (row, status) => {
  row.verifyStatus = status
  row.verifyText = status === 2 ? '已通过' : '已拒绝'
  ElMessage.success(status === 2 ? '已通过审核' : '已拒绝')
}
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
</style>
