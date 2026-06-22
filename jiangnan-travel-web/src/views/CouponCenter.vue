<template>
  <div class="coupon-center">
    <div class="tabs">
      <span
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >{{ tab.label }}</span>
    </div>

    <div class="section" v-if="activeTab === 'available'">
      <h4>可领取优惠券</h4>
      <div class="coupon-card" v-for="item in availableCoupons" :key="item.id">
        <div class="coupon-left">
          <div class="coupon-amount">¥{{ item.amount }}</div>
          <div class="coupon-condition">满{{ item.minAmount }}可用</div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ item.name }}</div>
          <div class="coupon-expire">有效期至 {{ item.expireDate }}</div>
          <el-button size="small" type="primary" @click="handleClaim(item.id)">立即领取</el-button>
        </div>
      </div>
    </div>

    <div class="section" v-if="activeTab === 'used' || activeTab === 'expired'">
      <div class="my-coupon" v-for="item in filteredMyCoupons" :key="item.id">
        <span class="my-coupon-name">{{ item.name }}</span>
        <span class="my-coupon-amount">¥{{ item.amount }}</span>
      </div>
      <el-empty v-if="!filteredMyCoupons.length" description="暂无数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { couponApi } from '@/api/coupon'
import { ElMessage } from 'element-plus'

const activeTab = ref('available')
const availableCoupons = ref([])
const myCoupons = ref([])

const tabs = [
  { key: 'available', label: '可用' },
  { key: 'used', label: '已使用' },
  { key: 'expired', label: '已过期' }
]

const filteredMyCoupons = computed(() => {
  return myCoupons.value.filter(c => c.status === activeTab.value)
})

onMounted(async () => {
  try {
    const [listRes, myRes] = await Promise.all([couponApi.list(), couponApi.myCoupons()])
    availableCoupons.value = listRes.data || []
    myCoupons.value = myRes.data || []
  } catch (e) { /* ignore */ }
})

const handleClaim = async (couponId) => {
  try {
    await couponApi.claim(couponId)
    ElMessage.success('领取成功')
    availableCoupons.value = availableCoupons.value.filter(c => c.id !== couponId)
  } catch (e) { /* ignore */ }
}
</script>

<style scoped>
.tabs {
  display: flex;
  background: #fff;
  border-radius: var(--radius-md);
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-item.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}
.section h4 {
  font-size: 0.95rem;
  margin-bottom: 10px;
}
.coupon-card {
  background: #fff;
  border-radius: var(--radius-md);
  margin-bottom: 10px;
  box-shadow: var(--shadow-sm);
  display: flex;
  overflow: hidden;
}
.coupon-left {
  width: 90px;
  background: var(--color-accent);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
}
.coupon-amount {
  font-size: 1.4rem;
  font-weight: 700;
}
.coupon-condition {
  font-size: 0.7rem;
  opacity: 0.9;
}
.coupon-right {
  flex: 1;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.coupon-name {
  font-weight: 600;
  font-size: 0.9rem;
}
.coupon-expire {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
.my-coupon {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 14px;
  margin-bottom: 8px;
  box-shadow: var(--shadow-sm);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.my-coupon-name {
  font-size: 0.9rem;
}
.my-coupon-amount {
  font-weight: 600;
  color: var(--color-accent-dark);
}
</style>
