<template>
  <div class="campaign-list app-page">
    <!-- Banner 轮播 -->
    <div class="banner-carousel" v-if="banners.length > 0">
      <el-carousel height="160px" indicator-position="outside" @change="onBannerChange">
        <el-carousel-item v-for="(item, idx) in banners" :key="idx">
          <div class="banner-slide" :style="{ background: getBannerBg(idx) }">
            <div class="banner-content">
              <h3>{{ item.name }}</h3>
              <p>{{ formatDateRange(item.startTime, item.endTime) }}</p>
              <el-button size="small" round @click="goDetail(item.id)">查看详情</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 活动列表标题 -->
    <div class="section-header app-section">
      <h3 class="app-section-title">全部活动</h3>
    </div>

    <!-- 活动卡片列表 -->
    <TransitionGroup name="grid-fade" tag="div" class="campaign-cards">
      <div class="campaign-card app-card" v-for="item in campaigns" :key="item.id" @click="goDetail(item.id)">
        <div class="card-banner" :style="{ background: getBannerBg(item.id) }">
          <span class="card-type-tag">{{ typeLabel(item.type) }}</span>
        </div>
        <div class="card-body">
          <h4 class="card-title">{{ item.name }}</h4>
          <p class="card-desc">{{ item.description || '暂无描述' }}</p>
          <div class="card-footer">
            <span class="card-date">{{ formatDateRange(item.startTime, item.endTime) }}</span>
            <el-tag size="small" type="success" v-if="isActive(item)">进行中</el-tag>
            <el-tag size="small" type="warning" v-else>即将开始</el-tag>
          </div>
        </div>
      </div>
    </TransitionGroup>

    <el-empty v-if="campaigns.length === 0" description="暂无活动" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { campaignApi } from '@/api/campaign'

const router = useRouter()
const campaigns = ref([])

const banners = computed(() => campaigns.value.slice(0, 5))

function isActive(item) {
  const now = Date.now()
  return now >= new Date(item.startTime).getTime() && now <= new Date(item.endTime).getTime()
}

function formatDateRange(start, end) {
  if (!start || !end) return ''
  const s = start.substring(0, 10)
  const e = end.substring(0, 10)
  return s === e ? s : `${s} ~ ${e}`
}

const bgColors = [
  'var(--gradient-brand-violet)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)'
]

function getBannerBg(idx) {
  return bgColors[idx % bgColors.length]
}

function typeLabel(type) {
  const map = { 0: '通用', 1: '新人', 2: '节日', 3: '限时' }
  return map[type] || '通用'
}

function goDetail(id) {
  router.push(`/campaign/${id}`)
}

function onBannerChange(idx) {
  // 轮播切换回调（预留）
}

onMounted(async () => {
  try {
    const res = await campaignApi.list()
    campaigns.value = res.data || []
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  }
})
</script>

<style scoped>
.campaign-list {
  padding: var(--spacing-md);
}

/* Banner 轮播 */
.banner-carousel {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
}
.banner-slide {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}
.banner-content {
  text-align: center;
  color: #fff;
}
.banner-content h3 {
  font-size: 1.2rem;
  margin: 0 0 6px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}
.banner-content p {
  font-size: 0.8rem;
  opacity: 0.9;
  margin: 0 0 10px;
}
:deep(.el-carousel__indicator--outside) {
  padding: 4px 4px 0;
}

/* 标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 活动卡片列表 */
.campaign-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.campaign-card {
  overflow: hidden;
  cursor: pointer;
}
.card-banner {
  height: 100px;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 10px;
}
.card-type-tag {
  background: rgba(255,255,255,0.25);
  backdrop-filter: blur(4px);
  color: #fff;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 0.75rem;
}
.card-body {
  padding: 12px 14px;
}
.card-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 4px;
}
.card-desc {
  font-size: 0.78rem;
  color: var(--color-text-secondary);
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-date {
  font-size: 0.72rem;
  color: var(--color-text-muted);
}
</style>
