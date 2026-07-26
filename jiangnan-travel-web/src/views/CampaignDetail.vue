<template>
  <div class="campaign-detail app-page" v-loading="loading">
    <template v-if="campaign">
      <!-- 顶部 Banner -->
      <div class="detail-banner" :style="{ background: bannerBg }">
        <div class="banner-overlay">
          <h2>{{ campaign.name }}</h2>
          <p class="banner-date">{{ formatDateRange(campaign.startTime, campaign.endTime) }}</p>
          <el-tag size="small" :type="isActive ? 'success' : 'warning'">
            {{ isActive ? '进行中' : '即将开始' }}
          </el-tag>
        </div>
      </div>

      <!-- 活动描述 -->
      <div class="detail-section app-card app-section">
        <h4 class="app-section-title">活动介绍</h4>
        <p class="detail-desc">{{ campaign.description || '暂无详细介绍' }}</p>
      </div>

      <!-- 关联优惠券 -->
      <div class="detail-section app-card app-section">
        <div class="section-header">
          <h4 class="app-section-title">活动优惠券 <span class="coupon-count">({{ coupons.length }}张)</span></h4>
          <el-button
            v-if="isActive && coupons.length > 0"
            type="primary"
            size="small"
            :loading="claiming"
            @click="handleClaim"
          >一键领取</el-button>
        </div>

        <div class="coupon-list">
          <div class="coupon-card" v-for="c in coupons" :key="c.id">
            <div class="coupon-left">
              <div class="coupon-amount">¥{{ c.discount }}</div>
              <div class="coupon-condition">满{{ c.threshold }}可用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-expire">{{ c.validDays ? `有效期${c.validDays}天` : '不限有效期' }}</div>
              <div class="coupon-type-tag">{{ typeLabel(c.type) }}</div>
            </div>
          </div>
          <el-empty v-if="coupons.length === 0" description="暂无关联优惠券" :image-size="80" />
        </div>
      </div>

      <!-- 领取成功反馈 -->
      <div class="claim-success" v-if="claimSuccess">
        <el-alert title="领取成功" type="success" show-icon :closable="false" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { campaignApi } from '@/api/campaign'
import { getCampaignBannerUrl } from '@/utils/imageCDN'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const claiming = ref(false)
const claimSuccess = ref(false)
const campaign = ref(null)
const coupons = ref([])

const isActive = computed(() => {
  if (!campaign.value) return false
  const now = Date.now()
  return now >= new Date(campaign.value.startTime).getTime() &&
         now <= new Date(campaign.value.endTime).getTime()
})

const bannerBg = computed(() => {
  const imageUrl = campaign.value?.bannerUrl || getCampaignBannerUrl(campaign.value?.name || '')
  return `url(${imageUrl}) center/cover, var(--gradient-brand-violet)`
})

function formatDateRange(start, end) {
  if (!start || !end) return ''
  const s = start.substring(0, 10)
  const e = end.substring(0, 10)
  return s === e ? s : `${s} ~ ${e}`
}

function typeLabel(type) {
  const map = { 0: '通用', 1: '新人', 2: '节日', 3: '限时' }
  return map[type] || '通用'
}

async function loadDetail() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await campaignApi.detail(id)
    if (res.data) {
      campaign.value = res.data.campaign
      coupons.value = res.data.coupons || []
    }
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function handleClaim() {
  const id = route.params.id
  claiming.value = true
  try {
    await campaignApi.claim(id)
    claimSuccess.value = true
    ElMessage.success('领取成功')
    setTimeout(() => { claimSuccess.value = false }, 3000)
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    claiming.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.campaign-detail {
  padding: var(--spacing-md);
}

/* Banner */
.detail-banner {
  height: 180px;
  border-radius: 12px;
  display: flex;
  align-items: flex-end;
  margin-bottom: 16px;
  overflow: hidden;
}
.banner-overlay {
  width: 100%;
  padding: 16px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  color: #fff;
}
.banner-overlay h2 {
  font-size: 1.3rem;
  margin: 0 0 4px;
}
.banner-date {
  font-size: 0.8rem;
  opacity: 0.9;
  margin: 0 0 8px;
}

/* 通用区块 */
.detail-section {
  padding: 14px 16px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.detail-desc {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.coupon-count {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  font-weight: 400;
}

/* 优惠券列表 */
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.coupon-card {
  background: #f8f9fa;
  border-radius: 10px;
  display: flex;
  overflow: hidden;
}
.coupon-left {
  width: 90px;
  background: var(--color-accent, #e74c3c);
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
.coupon-type-tag {
  font-size: 0.7rem;
  color: var(--color-primary);
  background: var(--color-primary-light, #e6f7ff);
  display: inline-block;
  padding: 1px 8px;
  border-radius: 8px;
  width: fit-content;
}

/* 领取反馈 */
.claim-success {
  position: fixed;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;
  width: 90%;
  max-width: 400px;
}
</style>
