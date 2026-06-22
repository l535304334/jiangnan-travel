<template>
  <div class="admin-orders">
    <div class="page-header">
      <h3>订单监控</h3>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="filterData">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%">
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
import { ref, computed } from 'vue'

const statusFilter = ref('')
const statusOptions = [
  { label: '待接单', value: 1 }, { label: '待到达', value: 2 },
  { label: '行程中', value: 3 }, { label: '待完成', value: 4 }, { label: '已完成', value: 5 }
]

const allData = [
  { orderNo: 'JN20260621001', user: '江南旅人', driver: '张师傅', start: '无锡站', end: '太湖广场', price: '¥28', status: 5, statusText: '已完成', statusTag: 'success', time: '2026-06-21 10:30' },
  { orderNo: 'JN20260621002', user: '无锡小张', driver: '李师傅', start: '南禅寺', end: '江南大学', price: '¥35', status: 4, statusText: '待完成', statusTag: '', time: '2026-06-21 11:15' },
  { orderNo: 'JN20260621003', user: '太湖游客', driver: '王师傅', start: '惠山古镇', end: '无锡东站', price: '¥42', status: 3, statusText: '行程中', statusTag: '', time: '2026-06-21 12:00' },
  { orderNo: 'JN20260621004', user: '江南好', driver: '---', start: '蠡园', end: '灵山大佛', price: '¥56', status: 1, statusText: '待接单', statusTag: 'warning', time: '2026-06-21 14:20' },
]

const tableData = ref([...allData])

const filterData = () => {
  if (!statusFilter.value) {
    tableData.value = [...allData]
  } else {
    tableData.value = allData.filter(o => o.status === statusFilter.value)
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.route-text { font-size: 0.85rem; }
</style>
