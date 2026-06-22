<template>
  <div class="driver-layout">
    <header class="driver-header">
      <h2>江南出行·司机端</h2>
      <el-switch
        v-model="isOnline"
        :active-text="isOnline ? '在线' : '离线'"
        :active-value="1"
        :inactive-value="0"
        @change="handleStatusChange"
      />
    </header>
    <main class="driver-main">
      <router-view />
    </main>
    <nav class="driver-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: $route.path === tab.path }"
        @click="$router.push(tab.path)"
      >
        <el-icon :size="22"><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { driverApi } from '@/api/driver'
import { HomeFilled, Money, UserFilled } from '@element-plus/icons-vue'

const isOnline = ref(1)

const tabs = [
  { path: '/driver/home', label: '首页', icon: HomeFilled },
  { path: '/driver/earnings', label: '收入', icon: Money },
  { path: '/driver/profile', label: '我的', icon: UserFilled }
]

const driverId = computed(() => {
  const info = JSON.parse(localStorage.getItem('driverInfo') || '{}')
  return info.id || 0
})

const handleStatusChange = async (val) => {
  try {
    await driverApi.updateStatus(driverId.value, val)
  } catch {}
}
</script>

<style scoped>
.driver-layout {
  min-height: 100vh;
  padding-top: 50px;
  padding-bottom: 60px;
  background: var(--color-bg);
}
.driver-header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  height: 50px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  color: #fff;
}
.driver-header h2 { font-size: 1.1rem; font-weight: 700; }
.driver-main { padding: 12px 16px; }
.driver-tabs {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 100;
  height: 60px; display: flex;
  background: #fff; border-top: 1px solid var(--color-border);
}
.tab-item {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: var(--color-text-muted); font-size: 0.7rem; cursor: pointer;
}
.tab-item.active { color: var(--color-primary); }
.tab-item span { margin-top: 2px; }
</style>
