<template>
  <div class="invoice-page app-page" v-loading="loading">
    <!-- 头部统计 -->
    <div class="invoice-header app-card">
      <div class="stat-item">
        <span class="card-stat-value">{{ invoices.length }}</span>
        <span class="card-stat-label">全部发票</span>
      </div>
      <div class="stat-item">
        <span class="card-stat-value">{{ invoices.filter(i => i.status === 1).length }}</span>
        <span class="card-stat-label">已开具</span>
      </div>
      <div class="stat-item">
        <span class="card-stat-value">{{ invoices.filter(i => i.status === 0).length }}</span>
        <span class="card-stat-label">申请中</span>
      </div>
    </div>

    <!-- 发票列表 -->
    <TransitionGroup name="list-fade" tag="div" class="invoice-list" v-if="invoices.length > 0">
      <div class="invoice-card app-card" v-for="inv in invoices" :key="inv.id">
        <div class="card-top">
          <span class="invoice-amount">¥{{ inv.amount }}</span>
          <el-tag :type="inv.status === 1 ? 'success' : inv.status === 0 ? 'warning' : 'info'" size="small">
            {{ inv.statusText }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="card-row"><span class="label">抬头</span><span class="value">{{ inv.title }}</span></div>
          <div class="card-row"><span class="label">税号</span><span class="value">{{ inv.taxNo || '-' }}</span></div>
          <div class="card-row" v-if="inv.invoiceNo"><span class="label">发票号</span><span class="value">{{ inv.invoiceNo }}</span></div>
          <div class="card-row"><span class="label">时间</span><span class="value">{{ inv.createTime?.replace('T', ' ') }}</span></div>
        </div>
        <div class="card-actions" v-if="inv.status === 0">
          <el-button size="small" type="danger" plain @click="handleCancel(inv.id)">取消申请</el-button>
        </div>
      </div>
    </TransitionGroup>

    <el-empty v-else description="暂无发票记录" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { invoiceApi } from '@/api/invoice'

const loading = ref(true)
const invoices = ref([])

onMounted(async () => {
  try {
    const res = await invoiceApi.list()
    invoices.value = res.data || []
  } catch (e) {
    ElMessage.error('加载发票列表失败')
  } finally {
    loading.value = false
  }
})

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定取消该发票申请？', '提示')
    await invoiceApi.cancel(id)
    ElMessage.success('已取消')
    const inv = invoices.value.find(i => i.id === id)
    if (inv) inv.status = 2; inv.statusText = '已取消'
  } catch (e) {}
}
</script>

<style scoped>
.invoice-page { min-height: calc(100vh - 120px); }

.invoice-header {
  display: flex; justify-content: space-around; padding: 16px; margin-bottom: 12px;
}
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }

.invoice-list { display: flex; flex-direction: column; gap: 10px; }
.invoice-card {
  padding: 14px 16px;
}
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.invoice-amount { font-size: 1.2rem; font-weight: 700; }
.card-body { font-size: 0.85rem; }
.card-row { display: flex; justify-content: space-between; padding: 4px 0; }
.card-row .label { color: var(--color-text-muted); }
.card-row .value { text-align: right; max-width: 60%; word-break: break-all; }
.card-actions { margin-top: 8px; display: flex; justify-content: flex-end; }
</style>
