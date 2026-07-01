<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="header-left">
        <h2 class="header-title">江南出行</h2>
      </div>
      <div class="header-right">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0">
          <el-icon :size="20" style="cursor:pointer" @click="handleBellClick"><Bell /></el-icon>
        </el-badge>
        <el-dropdown trigger="click" @command="handleHeaderCommand">
          <div class="header-user">
            <div class="header-avatar">
              <el-icon :size="16"><UserFilled /></el-icon>
            </div>
            <span class="header-phone">{{ userStore.userInfo?.phone || '' }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="switch">切换账号</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="app-main">
      <router-view v-slot="{ Component, route }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>

    <AiChatFloat />

    <nav class="app-tabs">
      <div class="tab-item" :class="{ active: $route.path === '/home' }" @click="$router.push('/home')">
        <el-icon :size="22"><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path === '/landmark-explore' }" @click="$router.push('/landmark-explore')">
        <el-icon :size="22"><Compass /></el-icon>
        <span>发现</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path === '/ai-assistant' }" @click="$router.push('/ai-assistant')">
        <el-icon :size="22"><MagicStick /></el-icon>
        <span>AI</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path === '/order-create' }" @click="$router.push('/order-create')">
        <el-icon :size="22"><Plus /></el-icon>
        <span>下单</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path.startsWith('/order') }" @click="$router.push('/order-list')">
        <el-icon :size="22"><List /></el-icon>
        <span>订单</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path.startsWith('/profile') || $route.path.startsWith('/coupon') || $route.path.startsWith('/address') || $route.path.startsWith('/security') || $route.path.startsWith('/about') }" @click="$router.push('/profile')">
        <el-icon :size="22"><UserFilled /></el-icon>
        <span>我的</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Bell, HomeFilled, Compass, Plus, List, UserFilled, MagicStick } from '@element-plus/icons-vue'
import AiChatFloat from '@/components/AiChatFloat.vue'
import { notificationApi } from '@/api/notification'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)

// WebSocket 连接（用于实时更新未读数量）
let ws = null
const userInfo = userStore.userInfo || {}
const token = localStorage.getItem('token')

function connectWebSocket() {
t// ponytail: token via cookie (handshake), not URL query param
  if (!token || !userInfo.id) return
  const wsUrl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/notification/${userInfo.id}`
  try {
    ws = new WebSocket(wsUrl)
    ws.onmessage = () => { loadUnreadCount() }
    ws.onclose = () => { ws = null }
    ws.onerror = () => { ws = null }
  } catch { /* ignore */ }
}

async function loadUnreadCount() {
  try {
    const res = await notificationApi.unreadCount()
    unreadCount.value = res.data || 0
  } catch { /* ignore */ }
}

const handleBellClick = () => {
  router.push('/message-center')
}

const handleHeaderCommand = (command) => {
  if (command === 'switch') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

onMounted(() => {
  loadUnreadCount()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) { ws.close(); ws = null }
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  padding-top: 50px;
  padding-bottom: 60px;
  background: var(--color-bg);
}
.app-header {
  position: fixed;
  top: 0; left: 0; right: 0; z-index: 100;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  color: #fff;
}
.header-title { font-size: 1.2rem; font-weight: 700; letter-spacing: 2px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.header-user {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.header-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.header-phone { font-size: 0.85rem; opacity: 0.9; }
.app-main { padding: 12px 16px; }
.app-tabs {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 100;
  height: 60px;
  display: flex;
  background: #fff;
  border-top: 1px solid var(--color-border);
  padding-bottom: env(safe-area-inset-bottom);
}
.tab-item {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: var(--color-text-muted); font-size: 0.7rem;
  cursor: pointer; transition: color 0.2s;
}
.tab-item.active { color: var(--color-primary); }
.tab-item span { margin-top: 2px; }
</style>
