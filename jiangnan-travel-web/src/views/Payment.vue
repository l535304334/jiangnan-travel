<template>
  <div class="payment-page" v-loading="loading">
    <!-- 支付金额 -->
    <div class="amount-card">
      <p class="amount-label">支付金额</p>
      <p class="amount-value">¥{{ formatAmount(payInfo?.amount) }}</p>
      <p class="order-no">订单号：{{ payInfo?.orderNo }}</p>
    </div>

    <!-- 行程信息 -->
    <div class="trip-info" v-if="payInfo">
      <div class="info-item"><span class="label">起点</span><span class="value">{{ payInfo.startAddress }}</span></div>
      <div class="info-item"><span class="label">终点</span><span class="value">{{ payInfo.endAddress }}</span></div>
    </div>

    <!-- 支付方式选择 -->
    <div class="payment-methods">
      <p class="section-title">选择支付方式</p>
      <div class="method-item" :class="{ selected: payMethod === 'wxpay' }" @click="payMethod = 'wxpay'">
        <span class="method-icon green-bg">微</span>
        <div class="method-info">
          <span class="method-name">微信支付</span>
          <span class="method-desc">推荐使用微信支付</span>
        </div>
        <el-icon v-if="payMethod === 'wxpay'" class="check-icon"><CircleCheck /></el-icon>
      </div>
      <div class="method-item" :class="{ selected: payMethod === 'alipay' }" @click="payMethod = 'alipay'">
        <span class="method-icon blue-bg">支</span>
        <div class="method-info">
          <span class="method-name">支付宝</span>
          <span class="method-desc">支持余额宝和花呗</span>
        </div>
        <el-icon v-if="payMethod === 'alipay'" class="check-icon"><CircleCheck /></el-icon>
      </div>
      <div class="method-item" :class="{ selected: payMethod === 'balance' }" @click="payMethod = 'balance'">
        <span class="method-icon orange-bg">余</span>
        <div class="method-info">
          <span class="method-name">余额支付</span>
          <span class="method-desc">使用账户余额支付</span>
        </div>
        <el-icon v-if="payMethod === 'balance'" class="check-icon"><CircleCheck /></el-icon>
      </div>
    </div>

    <!-- 支付按钮 -->
    <div class="pay-action">
      <el-button type="primary" size="large" :loading="paying" class="btn-full" @click="handlePay">
        立即支付 ¥{{ formatAmount(payInfo?.amount) }}
      </el-button>
    </div>

    <!-- 支付结果弹窗 -->
    <el-dialog v-model="resultVisible" title="支付结果" width="300px" :close-on-click-modal="false"
               :show-close="false" center>
      <div class="result-content">
        <el-icon v-if="paySuccess" class="result-icon success" :size="48"><CircleCheck /></el-icon>
        <el-icon v-else class="result-icon fail" :size="48"><CircleClose /></el-icon>
        <p class="result-text">{{ paySuccess ? '支付成功' : '支付失败' }}</p>
        <p class="result-amount text-primary">¥{{ formatAmount(payInfo?.amount) }}</p>
        <p class="result-method">{{ methodLabel }}</p>
      </div>
      <template #footer>
        <el-button v-if="paySuccess" type="primary" @click="goOrder">查看订单</el-button>
        <el-button v-else @click="resultVisible = false">重新支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { paymentApi } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const loading = ref(true)
const paying = ref(false)
const payInfo = ref(null)
const payMethod = ref('wxpay')
const resultVisible = ref(false)
const paySuccess = ref(false)

const formatAmount = (val) => (Number(val) || 0).toFixed(2)
const randomUUID = () => crypto.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`

const methodLabel = computed(() => {
  const map = { wxpay: '微信支付', alipay: '支付宝', balance: '余额支付' }
  return map[payMethod.value] || '微信支付'
})

onMounted(async () => {
  try {
    const res = await paymentApi.getPayment(orderId)
    payInfo.value = res.data
  } catch (e) {
    ElMessage.error('加载支付信息失败')
  } finally {
    loading.value = false
  }
})

const handlePay = async () => {
  paying.value = true
  try {
    const idempotentKey = randomUUID()
    const res = await paymentApi.create(orderId, payMethod.value, idempotentKey)
    payInfo.value = res.data
    paySuccess.value = true
  } catch (e) {
    paySuccess.value = false
  } finally {
    paying.value = false
    resultVisible.value = true
  }
}

const goOrder = () => {
  resultVisible.value = false
  router.push(`/order/${orderId}`)
}
</script>

<style scoped>
.payment-page { padding: 16px; background: #f5f5f5; min-height: calc(100vh - 120px); }

.amount-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; border-radius: 14px; padding: 24px; text-align: center;
  margin-bottom: 12px;
}
.amount-label { font-size: 0.82rem; opacity: 0.85; margin: 0 0 4px; }
.amount-value { font-size: 2rem; font-weight: 700; margin: 0 0 6px; }
.order-no { font-size: 0.72rem; opacity: 0.7; margin: 0; }

.trip-info {
  background: #fff; border-radius: 12px; padding: 14px 16px; margin-bottom: 12px;
}
.info-item { display: flex; justify-content: space-between; padding: 6px 0; }
.info-item .label { font-size: 0.82rem; color: var(--color-text-muted); }
.info-item .value { font-size: 0.85rem; text-align: right; max-width: 60%; word-break: break-all; }

.payment-methods { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; }
.section-title { font-size: 0.9rem; font-weight: 600; margin: 0 0 12px; }
.method-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; border-radius: 10px; margin-bottom: 8px;
  border: 1.5px solid #eee; cursor: pointer; transition: 0.2s;
}
.method-item:last-child { margin-bottom: 0; }
.method-item.selected { border-color: var(--color-primary); background: #f0f5ff; }
.method-icon {
  width: 36px; height: 36px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; font-weight: 700; color: #fff;
  flex-shrink: 0;
}
.green-bg { background: linear-gradient(135deg, #07c160, #06ad56); }
.blue-bg { background: linear-gradient(135deg, #1677ff, #0958d9); }
.orange-bg { background: linear-gradient(135deg, #fa8c16, #d46b08); }
.method-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.method-name { font-size: 0.9rem; font-weight: 500; }
.method-desc { font-size: 0.72rem; color: var(--color-text-muted); }
.check-icon { color: var(--color-primary); font-size: 1.1rem; }

.pay-action { padding: 8px 0 20px; }

.result-content { text-align: center; padding: 12px 0; }
.result-icon { margin-bottom: 12px; }
.result-icon.success { color: #52c41a; }
.result-icon.fail { color: #ff4d4f; }
.result-text { font-size: 1.1rem; font-weight: 600; margin: 0 0 8px; }
.result-amount { font-size: 1.5rem; font-weight: 700; margin: 0 0 4px; }
.result-method { font-size: 0.82rem; color: var(--color-text-muted); margin: 0; }
</style>
