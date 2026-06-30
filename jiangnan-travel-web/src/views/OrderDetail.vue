<template>
  <div class="order-detail app-page">
    <div class="map-area">
      <AmapView :center="mapCenter" :markers="mapMarkers" :path="mapPath" :zoom="14" style="height:200px" />
    </div>

    <div class="info-card app-card" v-if="order">
      <div class="status-row">
        <span>订单状态</span>
        <el-tag :type="statusTagType">{{ order.statusText }}</el-tag>
      </div>
      <div class="route-row">
        <div class="route-point"><span class="dot start"></span><span>{{ order.startAddress }}</span></div>
        <div class="route-line"></div>
        <div class="route-point"><span class="dot end"></span><span>{{ order.endAddress }}</span></div>
      </div>
      <div class="info-row"><span>车型</span><span>{{ order.carTypeName || '快车' }}</span></div>
      <div class="info-row"><span>距离 / 时长</span><span>{{ (order.distance/1000).toFixed(1) }}km / {{ Math.round(order.duration/60) }}分钟</span></div>
      <div class="info-row price"><span>费用合计</span><span class="price-value">¥{{ order.finalPrice }}</span></div>
      <div class="info-row" v-if="order.orderNo"><span>订单号</span><span class="order-no">{{ order.orderNo }}</span></div>
    </div>

    <div class="actions">
      <el-button v-if="order && order.status === 4" type="primary" @click="handlePay">去支付</el-button>
      <el-button v-if="order && order.status === 4" plain @click="handleInvoice">申请发票</el-button>
      <el-button v-if="order && (order.status === 0 || order.status === 1)" type="danger" plain @click="handleCancel">取消订单</el-button>
      <el-button v-if="order && order.status === 4" @click="handleReview">评价</el-button>
      <el-button v-if="order && (order.status === 4 || order.status === 5)" plain @click="handleReorder">再来一单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import AmapView from '@/components/AmapView.vue'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)
const selectedReason = ref('')

const mapCenter = computed(() => {
  if (order.value?.startLng) return [order.value.startLng, order.value.startLat]
  return [115.8759, 28.6842]
})
const mapMarkers = computed(() => {
  if (!order.value) return []
  return [
    { lng: order.value.startLng, lat: order.value.startLat, title: order.value.startAddress },
    { lng: order.value.endLng, lat: order.value.endLat, title: order.value.endAddress }
  ]
})
const mapPath = computed(() => {
  if (!order.value?.startLng) return []
  return [
    { lng: order.value.startLng, lat: order.value.startLat },
    { lng: order.value.endLng, lat: order.value.endLat }
  ]
})
const statusTagType = computed(() => {
  const s = order.value?.status
  if (s === 0) return 'warning'; if (s === 1 || s === 2 || s === 3) return ''
  if (s === 4) return 'success'; if (s === 5) return 'info'; return ''
})

onMounted(async () => {
  try { const res = await orderApi.detail(route.params.id); order.value = res.data } catch (e) { ElMessage.error('加载失败') }
})

const handlePay = async () => {
  router.push(`/payment/${order.value.id}`)
}
const handleInvoice = () => {
  router.push({ path: '/invoice-apply', query: { orderId: order.value.id } })
}
const handleCancel = async () => {
  const cancelReasons = [
    '行程有变，不需要了',
    '等待时间太长',
    '误操作下单',
    '价格太贵',
    '司机长时间未接单',
    '其他原因'
  ]
  try {
    const { value } = await ElMessageBox({
      title: '取消订单',
      message: h('div', null, [
        h('div', { style: 'margin-bottom:12px;font-size:0.9rem;color:#666' }, '请选择取消原因：'),
        h('div', { style: 'display:flex;flex-direction:column;gap:8px' },
          cancelReasons.map(r => h('el-radio', {
            value: r,
            modelValue: null,
            'onUpdate:modelValue': (v) => { selectedReason.value = v }
          }, r))
        ),
        h('el-input', {
          modelValue: null,
          'onUpdate:modelValue': (v) => { if (selectedReason.value === '其他原因') selectedReason.value = v },
          placeholder: '请输入其他原因...',
          style: 'margin-top:8px',
          size: 'small'
        })
      ]),
      confirmButtonText: '确认取消',
      cancelButtonText: '暂不取消',
      showCancelButton: true,
      distinguishCancelAndClose: true,
      dangerouslyUseHTMLString: false
    })
    const reason = selectedReason.value || '用户取消'
    await orderApi.cancel(order.value.id, reason)
    ElMessage.success('订单已取消')
    order.value.status = 5
    order.value.statusText = '已取消'
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}
const handleReview = () => router.push({ path: `/order/${order.value.id}/review` })
const handleReorder = () => router.push({ path: '/order-create', query: { endAddress: order.value.endAddress } })
</script>

<style scoped>
.map-area { margin-bottom: 12px; }
.info-card { margin-bottom: 16px; }
.status-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
.route-row { padding: 4px 0; margin-bottom: 12px; }
.route-point { display: flex; align-items: center; gap: 10px; font-size: 0.9rem; }
.dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.dot.start { background: var(--color-primary); }
.dot.end { background: var(--color-danger); }
.route-line { width: 2px; height: 18px; background: var(--color-border); margin-left: 4px; }
.info-row { display: flex; justify-content: space-between; padding: 5px 0; font-size: 0.85rem; color: var(--color-text-secondary); }
.info-row.price { border-top: 1px dashed #eee; margin-top: 6px; padding-top: 10px; font-size: 1rem; font-weight: 600; color: var(--color-text); }
.price-value { color: var(--color-primary-dark); }
.order-no { font-size: 0.75rem; font-family: monospace; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
