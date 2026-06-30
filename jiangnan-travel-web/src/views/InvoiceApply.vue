<template>
  <div class="invoice-apply-page app-page">
    <div class="apply-card app-card">
      <h3>申请发票</h3>
      <p class="order-info">订单号：{{ orderNo }} &nbsp; 金额：¥{{ amount }}</p>

      <el-form :model="form" label-position="top">
        <el-form-item label="发票抬头">
          <el-input v-model="form.title" placeholder="请输入发票抬头" />
        </el-form-item>
        <el-form-item label="纳税人识别号">
          <el-input v-model="form.taxNo" placeholder="请输入税号（选填）" />
        </el-form-item>
      </el-form>

      <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSubmit">
        提交申请
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api/order'
import { invoiceApi } from '@/api/invoice'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = route.query.orderId
const orderNo = ref('')
const amount = ref('0')
const submitting = ref(false)

const form = ref({
  title: '',
  taxNo: ''
})

onMounted(async () => {
  if (!orderId) {
    ElMessage.warning('缺少订单信息')
    router.replace('/invoice-center')
    return
  }
  try {
    const res = await orderApi.detail(orderId)
    orderNo.value = res.data?.orderNo || ''
    amount.value = res.data?.finalPrice || res.data?.price || '0'
  } catch (e) {
    ElMessage.error('加载订单信息失败')
  }
})

const handleSubmit = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入发票抬头')
    return
  }
  submitting.value = true
  try {
    await invoiceApi.apply({
      orderId: Number(orderId),
      title: form.value.title.trim(),
      taxNo: form.value.taxNo.trim() || undefined
    })
    ElMessage.success('发票申请已提交')
    router.push('/invoice-center')
  } catch (e) {
    ElMessage.error(e?.message || '申请失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.invoice-apply-page { min-height: calc(100vh - 120px); }
.apply-card { padding: 20px; }
.apply-card h3 { margin: 0 0 6px; }
.order-info { font-size: 0.85rem; color: var(--color-text-muted); margin: 0 0 20px; }
.submit-btn { width: 100%; margin-top: 10px; }
</style>
