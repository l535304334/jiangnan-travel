<template>
  <div class="driver-profile">
    <div class="profile-header">
      <el-avatar :size="60" icon="UserFilled" />
      <div class="profile-info">
        <div class="profile-name">{{ info.name || '司机' }}</div>
        <div class="profile-detail">{{ info.plate || '---' }} · {{ info.carType || '---' }}</div>
      </div>
    </div>

    <div class="rating-row">
      <div class="rating-item">
        <span class="rating-value">{{ info.rating || '0' }}</span>
        <span class="rating-label">评分</span>
      </div>
      <div class="rating-item">
        <span class="rating-value">{{ info.totalOrders || '0' }}</span>
        <span class="rating-label">总订单</span>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" v-for="m in menus" :key="m.label" @click="handleMenu(m)">
        <span>{{ m.label }}</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <el-button type="danger" class="logout-btn" @click="handleLogout">退出登录</el-button>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const info = reactive({
  name: '张师傅',
  plate: '苏B·A8888',
  carType: '舒适型',
  rating: '4.9',
  totalOrders: '1236'
})

const menus = [
  { label: '车辆信息' },
  { label: '审核状态' },
  { label: '设置' }
]

const handleMenu = (m) => {
  ElMessage.info(m.label)
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确定要退出吗？', '提示', { type: 'warning' })
  localStorage.removeItem('token')
  localStorage.removeItem('driverInfo')
  router.push('/driver/login')
}
</script>

<style scoped>
.profile-header {
  display: flex; align-items: center; gap: 16px;
  background: #fff; border-radius: var(--radius-md); padding: 20px; margin-bottom: 12px;
}
.profile-name { font-size: 1.1rem; font-weight: 700; }
.profile-detail { font-size: 0.85rem; color: var(--color-text-muted); margin-top: 4px; }
.rating-row {
  display: flex; background: #fff; border-radius: var(--radius-md);
  padding: 16px; margin-bottom: 12px;
}
.rating-item { flex: 1; text-align: center; }
.rating-value { font-size: 1.3rem; font-weight: 700; color: var(--color-accent); display: block; }
.rating-label { font-size: 0.8rem; color: var(--color-text-muted); }
.menu-list {
  background: #fff; border-radius: var(--radius-md); margin-bottom: 12px;
}
.menu-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; border-bottom: 1px solid var(--color-border-light); cursor: pointer;
}
.menu-item:last-child { border-bottom: none; }
.logout-btn { width: 100%; margin-top: 8px; }
</style>
