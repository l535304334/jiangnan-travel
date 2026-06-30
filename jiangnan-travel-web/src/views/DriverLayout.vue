<template>
  <div class="driver-layout">
    <header class="driver-header">
      <h2>江南出行·司机端</h2>
      <div class="header-right">
        <el-switch
          v-model="isOnline"
          :active-text="isOnline ? '在线' : '离线'"
          :active-value="1"
          :inactive-value="0"
          @change="handleStatusChange"
        />
        <el-dropdown trigger="click" @command="handleHeaderCommand">
          <div class="header-user">
            <div class="header-avatar">
              <CdnAvatar type="driver" :size="28" icon="UserFilled" />
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="switch">切换账号</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <main class="driver-main">
      <router-view v-slot="{ Component, route }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { driverApi } from '@/api/driver'
import { useUserStore } from '@/stores/user'
import { HomeFilled, Money, UserFilled } from '@element-plus/icons-vue'
import CdnAvatar from '@/components/CdnAvatar.vue'

const router = useRouter()
const userStore = useUserStore()
const isOnline = ref(1)

const tabs = [
  { path: '/driver/home', label: '首页', icon: HomeFilled },
  { path: '/driver/earnings', label: '收入', icon: Money },
  { path: '/driver/profile', label: '我的', icon: UserFilled }
]

const handleStatusChange = async (val) => {
  try {
    await driverApi.updateStatus(val)
  } catch {}
}

const handleHeaderCommand = (command) => {
  userStore.logout()
  router.push('/driver/login')
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
.driver-header h2 { font-size: var(--font-size-h3); font-weight: 700; }
.header-right { display: flex; align-items: center; gap: 12px; }
.header-user { cursor: pointer; }
.header-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
}
.driver-main { padding: 12px 16px; }
.driver-tabs {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 100;
  height: 60px; display: flex;
  background: #fff; border-top: 1px solid var(--color-border);
  padding-bottom: env(safe-area-inset-bottom);
}
.tab-item {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: var(--color-text-muted); font-size: 0.7rem; cursor: pointer;
  transition: color 0.2s;
}
.tab-item.active { color: var(--color-primary); }
.tab-item span { margin-top: 2px; }
</style>
