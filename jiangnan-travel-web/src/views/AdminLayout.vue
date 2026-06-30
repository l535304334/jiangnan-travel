<template>
  <div class="admin-layout" :class="{ 'sidebar-collapsed': collapsed }">
    <aside class="admin-sidebar">
      <div class="sidebar-logo">
        <div class="logo-badge">
          <el-icon :size="22"><Van /></el-icon>
        </div>
        <span class="logo-text" v-show="!collapsed">江南出行</span>
      </div>

      <nav class="sidebar-menu">
        <div
          v-for="m in menus"
          :key="m.path"
          class="sidebar-item"
          :class="{ active: isActive(m.path) }"
          @click="$router.push(m.path)"
          :title="m.label"
        >
          <el-icon :size="20"><component :is="m.icon" /></el-icon>
          <span v-show="!collapsed">{{ m.label }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-admin-info" v-show="!collapsed">
          <div class="admin-avatar">
            <CdnAvatar
              type="admin"
              :seed="adminInfo.username || adminInfo.phone || String(adminInfo.id)"
              :size="32"
              icon="UserFilled"
            />
          </div>
          <div class="admin-meta">
            <div class="admin-name">{{ adminName }}</div>
            <div class="admin-role">超级管理员</div>
          </div>
        </div>
        <el-tooltip content="退出登录" placement="right" :disabled="!collapsed">
          <div class="sidebar-logout" @click="handleLogout" title="退出登录">
            <el-icon :size="18"><SwitchButton /></el-icon>
            <span v-show="!collapsed">退出登录</span>
          </div>
        </el-tooltip>
      </div>

      <div class="sidebar-collapse-btn" @click="collapsed = !collapsed">
        <el-icon :size="14">
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
      </div>
    </aside>

    <div class="admin-right">
      <header class="admin-header">
        <div class="header-left">
          <div class="header-breadcrumb">
            <span class="breadcrumb-root">管理后台</span>
            <el-icon :size="12" class="breadcrumb-sep"><ArrowRight /></el-icon>
            <span class="breadcrumb-current">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-tag size="small" effect="plain" type="success" class="header-env-tag">运营中</el-tag>
          <el-dropdown trigger="click" @command="handleHeaderCommand">
            <div class="header-user">
              <div class="header-avatar">
                <CdnAvatar
                  type="admin"
                  :seed="adminInfo.username || adminInfo.phone || String(adminInfo.id)"
                  :size="28"
                  icon="UserFilled"
                />
              </div>
              <span class="header-user-name">{{ adminName }}</span>
              <el-icon :size="14"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <span style="color: var(--color-danger)">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-main">
        <router-view v-slot="{ Component, route }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  DataAnalysis, User, List, Document, Warning, Tickets, Trophy, Coin, Van,
  UserFilled, Fold, Expand, ArrowDown, ArrowRight, SwitchButton
} from '@element-plus/icons-vue'
import CdnAvatar from '@/components/CdnAvatar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')
const adminName = computed(() => adminInfo.name || adminInfo.username || '管理员')

const currentTitle = computed(() => {
  const m = menus.find(m => route.path.startsWith(m.path))
  return m ? m.label : '管理后台'
})

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已安全退出')
  router.push('/login')
}

function handleHeaderCommand(command) {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/admin/profile')
  }
}

const menus = [
  { path: '/admin/dashboard', label: '数据大屏', icon: DataAnalysis },
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/drivers', label: '司机审核', icon: List },
  { path: '/admin/orders', label: '订单监控', icon: Document },
  { path: '/admin/alerts', label: '风控告警', icon: Warning },
  { path: '/admin/car-types', label: '定价管理', icon: Tickets },
  { path: '/admin/campaigns', label: '活动管理', icon: Trophy },
  { path: '/admin/vip-levels', label: 'VIP等级', icon: Coin },
  { path: '/admin/bus-lines', label: '班线管理', icon: Van }
]

onMounted(() => {
  document.body.classList.add('admin-layout-open')
})

onUnmounted(() => {
  document.body.classList.remove('admin-layout-open')
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* ========== 侧边栏 ========== */
.admin-sidebar {
  width: var(--admin-sidebar-width);
  background: var(--admin-sidebar-bg);
  color: var(--admin-sidebar-text);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width var(--transition-normal);
  overflow: hidden;
  z-index: 100;
  box-shadow: var(--shadow-md);
}

.sidebar-collapsed .admin-sidebar {
  width: 64px;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--admin-sidebar-border);
  min-height: var(--admin-header-height);
}

.logo-badge {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--admin-sidebar-logo-text);
  flex-shrink: 0;
}

.logo-text {
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
  color: var(--admin-sidebar-logo-text);
  white-space: nowrap;
  letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
  padding: var(--spacing-md) 0;
  overflow-y: auto;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px var(--spacing-lg);
  margin: 2px var(--spacing-sm);
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
  color: var(--admin-sidebar-text);
  white-space: nowrap;
  border-radius: var(--radius-sm);
  position: relative;
}

.sidebar-item:hover {
  background: var(--admin-sidebar-hover);
  color: var(--admin-sidebar-logo-text);
}

.sidebar-item.active {
  background: var(--admin-sidebar-active-bg);
  color: var(--admin-sidebar-active-text);
}

.sidebar-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 0 2px 2px 0;
  background: var(--admin-sidebar-active-accent);
}

.sidebar-item .el-icon {
  flex-shrink: 0;
}

.sidebar-footer {
  border-top: 1px solid var(--admin-sidebar-border);
  padding: var(--spacing-md) var(--spacing-lg);
}

.sidebar-admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: var(--spacing-sm);
}

.admin-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--admin-sidebar-logo-text);
  flex-shrink: 0;
}

.admin-meta {
  overflow: hidden;
}

.admin-name {
  font-size: var(--font-size-body-small);
  font-weight: var(--font-weight-medium);
  color: var(--admin-sidebar-logo-text);
  white-space: nowrap;
}

.admin-role {
  font-size: var(--font-size-caption);
  color: var(--admin-sidebar-text);
  white-space: nowrap;
}

.sidebar-logout {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px var(--spacing-sm);
  margin-top: var(--spacing-xs);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  font-size: var(--font-size-body-small);
  transition: background var(--transition-fast), color var(--transition-fast);
}

.sidebar-logout:hover {
  background: rgba(245, 108, 108, 0.15);
  color: #ffccc7;
}

.sidebar-collapse-btn {
  position: absolute;
  bottom: 18px;
  right: -12px;
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--color-border);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-text-secondary);
  z-index: 10;
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast), background var(--transition-fast);
}

.sidebar-collapse-btn:hover {
  transform: scale(1.1);
  background: var(--color-white);
}

.sidebar-collapsed .sidebar-collapse-btn {
  right: 20px;
}

/* ========== 右侧区域 ========== */
.admin-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-header {
  height: var(--admin-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-xl);
  background: var(--color-white);
  border-bottom: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-body);
}

.breadcrumb-root {
  color: var(--color-text-muted);
}

.breadcrumb-sep {
  color: var(--color-border);
}

.breadcrumb-current {
  font-weight: var(--font-weight-semi-bold);
  color: var(--color-text);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-env-tag {
  font-weight: var(--font-weight-medium);
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.header-user:hover {
  background: var(--color-bg);
}

.header-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-user-name {
  font-size: var(--font-size-body-small);
  color: var(--color-text);
  font-weight: var(--font-weight-medium);
}

.admin-main {
  padding: var(--spacing-lg);
  flex: 1;
  overflow-y: auto;
}

/* 响应式：小屏自动折叠 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 64px;
  }
  .admin-sidebar .logo-text,
  .admin-sidebar .sidebar-item span,
  .admin-sidebar .sidebar-footer .admin-meta,
  .admin-sidebar .sidebar-footer .sidebar-logout span {
    display: none;
  }
  .admin-sidebar .sidebar-footer {
    padding: var(--spacing-sm);
  }
  .sidebar-admin-info {
    justify-content: center;
    margin-bottom: 0;
  }
  .sidebar-logout {
    justify-content: center;
    margin-top: var(--spacing-sm);
  }
  .sidebar-collapse-btn {
    display: none;
  }
}
</style>
