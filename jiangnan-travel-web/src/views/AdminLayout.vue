<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-logo">江南出行·管理后台</div>
      <div
        v-for="m in menus"
        :key="m.path"
        class="sidebar-item"
        :class="{ active: $route.path.startsWith(m.path) }"
        @click="$router.push(m.path)"
      >
        <el-icon :size="18"><component :is="m.icon" /></el-icon>
        <span>{{ m.label }}</span>
      </div>
    </aside>
    <div class="admin-right">
      <header class="admin-header">
        <span class="admin-name">{{ adminName }}</span>
      </header>
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  DataAnalysis, User, List, Document, Warning
} from '@element-plus/icons-vue'

const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')
const adminName = computed(() => adminInfo.name || '管理员')

const menus = [
  { path: '/admin/dashboard', label: '数据大屏', icon: DataAnalysis },
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/drivers', label: '司机审核', icon: List },
  { path: '/admin/orders', label: '订单监控', icon: Document },
  { path: '/admin/alerts', label: '风控告警', icon: Warning },
  { path: '/admin/car-types', label: '定价管理', icon: Tickets }
]
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.admin-sidebar {
  width: 200px; background: #1A1A2E; color: #ccc;
  flex-shrink: 0; padding-top: 16px;
}
.sidebar-logo {
  font-size: 0.9rem; font-weight: 700; color: #fff;
  padding: 0 16px 20px; border-bottom: 1px solid rgba(255,255,255,0.1);
  margin-bottom: 8px;
}
.sidebar-item {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 16px; cursor: pointer; transition: background 0.2s;
}
.sidebar-item:hover { background: rgba(255,255,255,0.05); }
.sidebar-item.active { background: var(--color-primary); color: #fff; }
.admin-right { flex: 1; display: flex; flex-direction: column; background: var(--color-bg); }
.admin-header {
  height: 50px; display: flex; align-items: center; justify-content: flex-end;
  padding: 0 20px; background: #fff; box-shadow: var(--shadow-sm);
}
.admin-name { font-weight: 600; color: var(--color-text); }
.admin-main { padding: 20px; flex: 1; overflow-y: auto; }
</style>
