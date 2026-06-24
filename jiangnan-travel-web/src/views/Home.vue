<template>
  <div class="home-page">
    <div class="user-greeting">
      <el-avatar :size="40" :icon="UserFilled" />
      <div>
        <h3>{{ userStore.userInfo?.nickname || '用户' }}</h3>
        <p>欢迎使用江南出行</p>
      </div>
    </div>

    <div class="quick-shortcuts">
      <div class="shortcut" @click="$router.push('/order-create')">
        <el-icon :size="24"><MapLocation /></el-icon>
        <span>现在出发</span>
      </div>
      <div class="shortcut" @click="$router.push('/address')">
        <el-icon :size="24"><Star /></el-icon>
        <span>收藏地址</span>
      </div>
      <div class="shortcut" @click="$router.push('/coupon')">
        <el-icon :size="24"><Present /></el-icon>
        <span>优惠券</span>
      </div>
      <div class="shortcut" @click="$router.push('/order-list')">
        <el-icon :size="24"><Clock /></el-icon>
        <span>历史订单</span>
      </div>
    </div>

    <div class="section" v-if="recommends.length > 0">
      <h4>AI 推荐目的地</h4>
      <div class="dest-cards">
        <div class="dest-card" v-for="d in recommends.slice(0,4)" :key="d.address"
             @click="goOrder(d)">
          <span class="dest-name">{{ d.address }}</span>
          <span class="dest-count">{{ d.orderCount }}次</span>
        </div>
      </div>
    </div>

    <div class="section">
      <h4>常用路线</h4>
      <div class="route-list">
        <div class="route-item" v-for="addr in savedAddresses" :key="addr.id"
             @click="goOrder(addr)">
          <div class="route-left">
            <el-tag size="small" :type="addr.tag==='家'?'danger':addr.tag==='公司'?'primary':'info'">{{ addr.tag }}</el-tag>
            <span>{{ addr.address }}</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="route-item" v-if="savedAddresses.length===0" style="color:var(--color-text-muted);justify-content:center">
          暂无收藏地址，点击上方"收藏地址"添加
        </div>
      </div>
    </div>

    <div class="section">
      <h4>江西文旅地标</h4>
      <div class="landmark-scroll">
        <div class="landmark-item" v-for="lm in landmarks" :key="lm.id">
          <div class="lm-name">{{ lm.name }}</div>
          <div class="lm-city">{{ lm.city }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserFilled, MapLocation, Star, Present, Clock, ArrowRight } from '@element-plus/icons-vue'
import { aiApi } from '@/api/ai'
import { userApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const recommends = ref([])
const landmarks = ref([])
const savedAddresses = ref([])

onMounted(async () => {
  try {
    const [r, l, a] = await Promise.all([
      aiApi.recommendDest(),
      aiApi.getLandmarks(),
      userApi.getAddresses()
    ])
    if (r.code === 200) recommends.value = r.data || []
    if (l.code === 200) landmarks.value = l.data || []
    if (a.code === 200) savedAddresses.value = a.data || []
  } catch (e) {
    console.warn('首页数据加载失败:', e)
  }
})

const goOrder = (dest) => {
  router.push({
    path: '/order-create',
    query: { endAddress: dest.address, endLat: dest.lat, endLng: dest.lng }
  })
}
</script>

<style scoped>
.home-page { padding: 0 4px; }
.user-greeting {
  display: flex; align-items: center; gap: 12px;
  padding: 16px; background: #fff; border-radius: var(--radius-md);
  margin-bottom: 12px; box-shadow: var(--shadow-sm);
}
.user-greeting h3 { font-size: 1.1rem; }
.user-greeting p { font-size: 0.85rem; color: var(--color-text-secondary); }

.quick-shortcuts {
  display: flex; gap: 8px; margin-bottom: 16px;
}
.shortcut {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  gap: 6px; padding: 14px 8px; background: #fff;
  border-radius: var(--radius-md); box-shadow: var(--shadow-sm);
  cursor: pointer; font-size: 0.8rem; color: var(--color-text-secondary);
  transition: 0.2s;
}
.shortcut:hover { color: var(--color-primary); }

.section { margin-bottom: 16px; }
.section h4 { font-size: 0.95rem; margin-bottom: 10px; color: var(--color-text); }

.dest-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.dest-card {
  background: #fff; padding: 12px; border-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm); cursor: pointer;
  display: flex; flex-direction: column; gap: 4px;
}
.dest-name { font-size: 0.9rem; font-weight: 500; }
.dest-count { font-size: 0.75rem; color: var(--color-text-muted); }

.route-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px; background: #fff; border-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm); margin-bottom: 6px; cursor: pointer;
  font-size: 0.9rem;
}
.route-left { display: flex; align-items: center; gap: 8px; }

.landmark-scroll {
  display: flex; gap: 8px; overflow-x: auto; padding-bottom: 8px;
}
.landmark-item {
  flex-shrink: 0; background: #fff; padding: 10px 14px;
  border-radius: var(--radius-sm); box-shadow: var(--shadow-sm);
  min-width: 80px; text-align: center;
}
.lm-name { font-size: 0.85rem; font-weight: 500; }
.lm-city { font-size: 0.7rem; color: var(--color-text-muted); margin-top: 2px; }
</style>
