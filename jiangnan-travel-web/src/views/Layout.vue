<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="header-left">
        <h2 class="header-title">江南出行</h2>
      </div>
      <div class="header-right">
        <el-badge :value="0" :hidden="true">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
        <span class="header-phone">{{ userStore.userInfo?.phone || '' }}</span>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>

    <AiChatFloat />

    <nav class="app-tabs">
      <div class="tab-item" :class="{ active: $route.path === '/home' }" @click="$router.push('/home')">
        <el-icon :size="22"><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path === '/order-create' }" @click="$router.push('/order-create')">
        <el-icon :size="22"><Plus /></el-icon>
        <span>下单</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path.startsWith('/order') }" @click="$router.push('/order-list')">
        <el-icon :size="22"><List /></el-icon>
        <span>订单</span>
      </div>
      <div class="tab-item" :class="{ active: $route.path.startsWith('/profile') || $route.path.startsWith('/coupon') || $route.path.startsWith('/address') }" @click="$router.push('/profile')">
        <el-icon :size="22"><UserFilled /></el-icon>
        <span>我的</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { Bell, HomeFilled, Plus, List, UserFilled } from '@element-plus/icons-vue'
import AiChatFloat from '@/components/AiChatFloat.vue'

const userStore = useUserStore()
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
