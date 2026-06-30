<template>
  <div class="vip-center app-page">
    <!-- 当前VIP状态 -->
    <div class="vip-status-card app-card" :class="{ 'is-vip': myVip.status === 1 }">
      <div class="vip-status-bg">
        <div class="vip-icon">{{ myVip.status === 1 ? '👑' : '⭐' }}</div>
        <div class="vip-status-text">
          <h3 v-if="myVip.status === 1">{{ myVip.vipLevel?.name || 'VIP' }}会员</h3>
          <h3 v-else>开通VIP会员</h3>
          <p v-if="myVip.status === 1">
            剩余 {{ myVip.remainingDays }} 天 ·
            {{ myVip.vipLevel?.discount ? (myVip.vipLevel.discount * 10).toFixed(1) + '折' : '' }}
          </p>
          <p v-else>享受专属折扣与特权</p>
        </div>
      </div>
      <div class="vip-expire-bar" v-if="myVip.status === 1">
        <span>{{ formatDate(myVip.endTime) }} 到期</span>
      </div>
    </div>

    <!-- 等级列表 -->
    <div class="section-header app-section">
      <h4 class="app-section-title">选择VIP等级</h4>
    </div>

    <div class="level-list" v-loading="loading">
      <div
        class="level-card app-card"
        v-for="level in levels"
        :key="level.id"
        :class="{ 'is-current': myVip.vipLevel?.id === level.id }"
      >
        <div class="level-header" :style="{ background: levelBg(level.level) }">
          <span class="level-icon">{{ levelIcon(level.level) }}</span>
          <span class="level-name">{{ level.name }}</span>
          <span class="level-discount">{{ (level.discount * 10).toFixed(1) }}折</span>
        </div>
        <div class="level-body">
          <div class="level-price">
            <div class="price-item">
              <span class="price-label">月费</span>
              <span class="price-value">¥{{ level.monthlyFee || '—' }}</span>
            </div>
            <div class="price-item">
              <span class="price-label">年费</span>
              <span class="price-value">¥{{ level.yearlyFee || '—' }}</span>
            </div>
          </div>
          <div class="level-features">
            <div class="feature-item">· 打车{{ (level.discount * 10).toFixed(1) }}折优惠</div>
            <div class="feature-item">· 优先派单</div>
            <div class="feature-item">· 专属客服</div>
          </div>
          <div class="level-actions">
            <el-button
              size="small"
              type="primary"
              plain
              @click="showPurchase(level, 0)"
              :disabled="!level.monthlyFee || level.monthlyFee <= 0"
            >月付</el-button>
            <el-button
              size="small"
              type="primary"
              @click="showPurchase(level, 1)"
              :disabled="!level.yearlyFee || level.yearlyFee <= 0"
            >年付</el-button>
          </div>
          <div class="current-badge" v-if="myVip.vipLevel?.id === level.id">当前等级</div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && levels.length === 0" description="暂无VIP等级" />

    <!-- 购买确认弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`购买${selectedLevel?.name || ''}会员`"
      width="85%"
      :close-on-click-modal="false"
    >
      <div class="purchase-confirm">
        <div class="confirm-level">
          <span>{{ selectedLevel?.name }}会员</span>
          <span class="confirm-discount">{{ selectedLevel?.discount ? (selectedLevel.discount * 10).toFixed(1) + '折' : '' }}</span>
        </div>
        <div class="confirm-fee">
          <span>{{ selectedFeeType === 0 ? '月费' : '年费' }}</span>
          <span class="confirm-price">
            ¥{{ selectedFeeType === 0 ? selectedLevel?.monthlyFee : selectedLevel?.yearlyFee }}
          </span>
        </div>
        <div class="confirm-tip">支付成功后立即生效{{ myVip.status === 1 ? '（续费自动顺延）' : '' }}</div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="purchasing" @click="handlePurchase">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { vipApi } from '@/api/vip'

const loading = ref(false)
const purchasing = ref(false)
const levels = ref([])
const myVip = ref({ status: 0, remainingDays: 0 })

const dialogVisible = ref(false)
const selectedLevel = ref(null)
const selectedFeeType = ref(0)

const levelBgColors = [
  'linear-gradient(135deg, #cd7f32, #b8860b)',
  'linear-gradient(135deg, #c0c0c0, #a8a8a8)',
  'linear-gradient(135deg, #ffd700, #daa520)',
  'linear-gradient(135deg, #e5e4e2, #bcc6cc)',
  'linear-gradient(135deg, #b9f2ff, #00bfff)'
]

function levelBg(level) {
  return levelBgColors[(level - 1) % levelBgColors.length]
}

function levelIcon(level) {
  const icons = ['🥉', '🥈', '🥇', '💎', '👑']
  return icons[(level - 1) % icons.length]
}

function formatDate(date) {
  if (!date) return ''
  return date.substring(0, 10)
}

function showPurchase(level, feeType) {
  selectedLevel.value = level
  selectedFeeType.value = feeType
  dialogVisible.value = true
}

async function handlePurchase() {
  if (!selectedLevel.value) return
  purchasing.value = true
  try {
    await vipApi.purchase(selectedLevel.value.id, selectedFeeType.value)
    ElMessage.success('购买成功')
    dialogVisible.value = false
    await loadMyVip()
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    purchasing.value = false
  }
}

async function loadLevels() {
  try {
    const res = await vipApi.levels()
    levels.value = res.data || []
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  }
}

async function loadMyVip() {
  try {
    const res = await vipApi.myVip()
    myVip.value = res.data || { status: 0, remainingDays: 0 }
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([loadLevels(), loadMyVip()])
  loading.value = false
})
</script>

<style scoped>
.vip-center {
  padding: var(--spacing-md);
}

/* VIP状态卡片 */
.vip-status-card {
  overflow: hidden;
  margin-bottom: 16px;
}
.vip-status-card.is-vip {
  background: linear-gradient(135deg, #ffd700, #f0c040);
}
.vip-status-card:not(.is-vip) {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.vip-status-bg {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #fff;
}
.vip-icon {
  font-size: 2.5rem;
}
.vip-status-text h3 {
  margin: 0;
  font-size: 1.15rem;
}
.vip-status-text p {
  margin: 4px 0 0;
  font-size: 0.8rem;
  opacity: 0.9;
}
.vip-expire-bar {
  background: rgba(0,0,0,0.1);
  padding: 8px 20px;
  font-size: 0.75rem;
  color: #fff;
}

/* 标题 */
.section-header {
  margin-bottom: 12px;
}

/* 等级卡片列表 */
.level-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.level-card {
  overflow: hidden;
  position: relative;
}
.level-card.is-current {
  box-shadow: 0 0 0 2px var(--color-primary), var(--shadow-md);
}
.level-header {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
}
.level-icon {
  font-size: 1.5rem;
}
.level-name {
  flex: 1;
  font-weight: 600;
  font-size: 1rem;
}
.level-discount {
  background: rgba(255,255,255,0.25);
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 0.8rem;
}
.level-body {
  padding: 14px 16px;
}
.level-price {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}
.price-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.price-label {
  font-size: 0.72rem;
  color: var(--color-text-muted);
}
.price-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-accent-dark);
}
.level-features {
  margin-bottom: 12px;
}
.feature-item {
  font-size: 0.82rem;
  color: var(--color-text-secondary);
  padding: 2px 0;
}
.level-actions {
  display: flex;
  gap: 10px;
}
.current-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--color-primary);
  color: #fff;
  font-size: 0.7rem;
  padding: 2px 10px;
  border-radius: 10px;
}

/* 购买弹窗 */
.purchase-confirm {
  padding: 8px 0;
}
.confirm-level {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 12px;
}
.confirm-discount {
  color: var(--color-primary);
}
.confirm-fee {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  padding: 10px 0;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
}
.confirm-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-accent-dark);
}
.confirm-tip {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  margin-top: 10px;
}
</style>
