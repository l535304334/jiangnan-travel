<template>
  <div class="driver-profile app-page">
    <div class="profile-header app-card">
      <CdnAvatar
        type="driver"
        :seed="info.phone || String(info.id)"
        :size="60"
        icon="UserFilled"
      />
      <div class="profile-info">
        <div class="profile-name">{{ info.name || '司机' }}</div>
        <div class="profile-detail">{{ info.plate || '---' }} · {{ info.carType || '---' }}</div>
      </div>
    </div>

    <div class="rating-row app-card">
      <div class="rating-item">
        <span class="rating-value">{{ info.rating }}</span>
        <span class="rating-label">评分</span>
      </div>
      <div class="rating-item">
        <span class="rating-value">{{ info.totalOrders }}</span>
        <span class="rating-label">总订单</span>
      </div>
    </div>

    <div class="menu-list app-card">
      <div class="menu-item app-list-item" v-for="m in menus" :key="m.label" @click="handleMenu(m)">
        <span>{{ m.label }}</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <el-button type="danger" class="logout-btn" @click="handleLogout">退出登录</el-button>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { driverApi } from '@/api/driver'
import CdnAvatar from '@/components/CdnAvatar.vue'

const router = useRouter()

const info = reactive({
  id: '',
  name: '司机',
  phone: '',
  plate: '---',
  carType: '---',
  rating: '0',
  totalOrders: '0'
})

const menus = [
  { label: '车辆信息' },
  { label: '审核状态' },
  { label: '设置' }
]

const loadProfile = async () => {
  try {
    const res = await driverApi.getProfile()
    if (res.code === 200) {
      const data = res.data
      info.id = data.id || ''
      info.name = data.realName || '司机'
      info.phone = data.phone || ''
      info.plate = data.carPlate || '---'
      info.carType = data.carTypeName || '---'
      info.rating = data.avgRating != null ? data.avgRating.toString() : '0'
      info.totalOrders = (data.totalOrders || '0').toString()
    }
  } catch {}
}

const handleMenu = (m) => {
  ElMessage.info(m.label)
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确定要退出吗？', '提示', { type: 'warning' })
  localStorage.removeItem('token')
  localStorage.removeItem('driverInfo')
  router.push('/driver/login')
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-header {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 12px;
}
.profile-name { font-size: 1.1rem; font-weight: 700; }
.profile-detail { font-size: 0.85rem; color: var(--color-text-muted); margin-top: 4px; }
.rating-row {
  display: flex;
  margin-bottom: 12px;
}
.rating-item { flex: 1; text-align: center; }
.rating-value { font-size: 1.3rem; font-weight: 700; color: var(--color-accent); display: block; }
.rating-label { font-size: 0.8rem; color: var(--color-text-muted); }
.menu-list {
  margin-bottom: 12px;
}
.menu-item {
  display: flex; justify-content: space-between; align-items: center;
  cursor: pointer;
}
.logout-btn { width: 100%; margin-top: 8px; }
</style>
