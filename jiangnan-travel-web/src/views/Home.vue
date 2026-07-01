<template>
  <div class="home-page app-page">
    <!-- 用户问候区 -->
    <div class="app-section">
      <div class="user-greeting app-card">
        <div class="greeting-avatar" @click="$router.push('/profile')">
          <CdnAvatar
            :src="userStore.userInfo?.avatar"
            :seed="userStore.userInfo?.phone || String(userStore.userInfo?.id)"
            type="user"
            :size="40"
            :icon="UserFilled"
          />
        </div>
        <div>
          <h3>{{ userStore.userInfo?.nickname || '用户' }}</h3>
          <p>欢迎使用江南出行</p>
        </div>
      </div>
    </div>

    <!-- 数据看板 -->
    <div class="app-section">
      <div class="stats-cards">
        <div class="app-card stat-card" v-for="s in statsList" :key="s.label">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷功能 -->
    <div class="app-section">
      <el-row :gutter="12">
        <el-col :span="12" v-for="item in quickActionsShort" :key="item.label">
          <div class="quick-action-card app-card" @click="item.action">
            <div class="qa-icon" :style="{ background: item.bg }">{{ item.icon }}</div>
            <div class="qa-label">{{ item.label }}</div>
            <div class="qa-desc">{{ item.desc }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 活动中心入口 -->
    <div class="app-section">
      <div class="campaign-entry" @click="$router.push('/campaign-list')">
        <div class="campaign-entry-icon">🎉</div>
        <div class="campaign-entry-text">
          <div class="campaign-entry-title">活动中心</div>
          <div class="campaign-entry-desc">限时优惠 · 新人专享 · 节日特惠</div>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- VIP会员入口 -->
    <div class="app-section">
      <div class="vip-entry" @click="$router.push('/vip-center')">
        <div class="vip-entry-icon">👑</div>
        <div class="vip-entry-text">
          <div class="vip-entry-title">VIP会员</div>
          <div class="vip-entry-desc">开通会员享 {{ vipDiscountText }} 折扣</div>
        </div>
        <el-tag size="small" :type="myVipStatus === 1 ? 'warning' : 'info'">
          {{ myVipStatus === 1 ? '已开通' : '去开通' }}
        </el-tag>
      </div>
    </div>

    <!-- 城际班线入口 -->
    <div class="app-section">
      <div class="campaign-entry bus-entry" @click="$router.push('/bus-line')">
        <div class="campaign-entry-icon">🚌</div>
        <div class="campaign-entry-text">
          <div class="campaign-entry-title">城际班线</div>
          <div class="campaign-entry-desc">南昌↔九江 · 省内城际 · 定时发车</div>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 常用路线 -->
    <div class="app-section">
      <h4 class="app-section-title">常用路线</h4>
      <div class="route-list">
        <div class="app-list-item route-item" v-for="addr in savedAddresses" :key="addr.id" @click="goOrder(addr)">
          <div class="route-left">
            <el-tag size="small" :type="addr.tag==='家'?'danger':addr.tag==='公司'?'primary':'info'">{{ addr.tag }}</el-tag>
            <span>{{ addr.address }}</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="app-list-item route-item empty" v-if="savedAddresses.length===0">
          暂无收藏地址，点击上方"收藏地址"添加
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserFilled, ArrowRight } from '@element-plus/icons-vue'
import { ElRow, ElCol } from 'element-plus'
import { aiApi } from '@/api/ai'
import { userApi } from '@/api/user'
import { vipApi } from '@/api/vip'
import CdnAvatar from '@/components/CdnAvatar.vue'

const router = useRouter()
const userStore = useUserStore()
const recommends = ref([])
const savedAddresses = ref([])
const myVipStatus = ref(0)
const vipDiscountText = ref('')

// 数据统计
const orderCount = computed(() => recommends.value.reduce((sum, r) => sum + (r.orderCount || 0), 0))
const couponCount = computed(() => 3)
const addressCount = computed(() => savedAddresses.value.length)

const statsList = computed(() => [
  { label: '总订单', value: orderCount.value },
  { label: '优惠券', value: couponCount.value },
  { label: '收藏地址', value: addressCount.value }
])

onMounted(async () => {
  try {
    const [r, a] = await Promise.all([
      aiApi.recommendDest(),
      userApi.getAddresses()
    ])
    if (r.code === 200) recommends.value = r.data || []
    if (a.code === 200) savedAddresses.value = a.data || []
  } catch (e) {
    console.warn('首页数据加载失败:', e)
  }

  // 加载VIP状态
  try {
    const vipRes = await vipApi.myVip()
    if (vipRes.data) {
      myVipStatus.value = vipRes.data.status || 0
      if (vipRes.data.vipLevel?.discount) {
        vipDiscountText.value = (vipRes.data.vipLevel.discount * 10).toFixed(1) + '折'
      }
    }
  } catch { vipDiscountText.value = '无折扣' /* VIP 未开通或加载失败，降级显示 */ }
})

const goShortTrip = () => router.push('/order?tripType=0')
const goLongTrip = () => router.push('/order?tripType=1')

const quickActionsShort = [
  { label: '市内出行', icon: '🚗', desc: '快速接驾，即时到达', action: goShortTrip, bg: 'linear-gradient(135deg, #2D8A6E, #4CAF50)' },
  { label: '城际出行', icon: '🚙', desc: '跨城直达，舒适长途', action: goLongTrip, bg: 'linear-gradient(135deg, #E67E22, #F39C12)' },
  { label: '收藏地址', icon: '⭐', desc: '常用地址管理', action: () => router.push('/address'), bg: 'linear-gradient(135deg, #2196F3, #64B5F6)' },
  { label: '历史订单', icon: '📋', desc: '查看出行记录', action: () => router.push('/orders'), bg: 'linear-gradient(135deg, #9C27B0, #CE93D8)' }
]

const goOrder = (dest) => {
  router.push({
    path: '/order-create',
    query: { endAddress: dest.address || dest.name, endLat: dest.lat, endLng: dest.lng }
  })
}
</script>

<style scoped>
.home-page { padding: 0; }

/* 用户问候区 */
.user-greeting {
  display: flex; align-items: center; gap: 12px;
}
.user-greeting h3 { font-size: 1.1rem; margin: 0; }
.user-greeting p { font-size: 0.85rem; color: var(--color-text-secondary); margin: 2px 0 0; }
.greeting-avatar { cursor: pointer; }

/* 数据看板 */
.stats-cards {
  display: flex; gap: 8px;
}
.stat-card {
  flex: 1; text-align: center;
}
.stat-value { font-size: 1.4rem; font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: 0.75rem; color: var(--color-text-secondary); margin-top: 4px; }

/* 快捷功能卡片 */
.quick-action-card {
  cursor: pointer;
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 12px;
}
.qa-icon {
  width: 44px; height: 44px; border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.4rem; flex-shrink: 0;
}
.qa-label { font-size: 0.95rem; font-weight: 600; }
.qa-desc { font-size: 0.7rem; color: var(--color-text-secondary); }

/* 活动中心/VIP/班线入口 */
.campaign-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: var(--radius-md);
  color: #fff;
  cursor: pointer;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
  box-shadow: var(--shadow-sm);
}
.campaign-entry:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.bus-entry { background: linear-gradient(135deg, #1a73e8, #0d47a1); }
.campaign-entry-icon { font-size: 2rem; }
.campaign-entry-text { flex: 1; }
.campaign-entry-title { font-size: 1rem; font-weight: 600; }
.campaign-entry-desc { font-size: 0.75rem; opacity: 0.85; margin-top: 2px; }

.vip-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #ffd700, #f0c040);
  border-radius: var(--radius-md);
  color: #7c6a00;
  cursor: pointer;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
  box-shadow: var(--shadow-sm);
}
.vip-entry:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.vip-entry-icon { font-size: 2rem; }
.vip-entry-text { flex: 1; }
.vip-entry-title { font-size: 1rem; font-weight: 600; }
.vip-entry-desc { font-size: 0.75rem; opacity: 0.85; margin-top: 2px; }

/* 常用路线 */
.route-item {
  display: flex; align-items: center; justify-content: space-between;
  cursor: pointer; font-size: 0.9rem;
}
.route-item.empty {
  color: var(--color-text-muted);
  justify-content: center;
  cursor: default;
}
.route-left { display: flex; align-items: center; gap: 8px; min-width: 0; }
.route-left span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
