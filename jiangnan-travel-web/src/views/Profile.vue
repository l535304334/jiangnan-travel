<template>
  <div class="profile">
    <div class="profile-header">
      <div class="avatar">🧑</div>
      <div class="user-info">
        <div class="nickname" v-if="!editing" @click="editing = true">
          {{ userStore.userInfo?.nickname || '点击设置昵称' }}
          <el-icon :size="14"><Edit /></el-icon>
        </div>
        <el-input
          v-if="editing"
          v-model="nicknameInput"
          size="small"
          @blur="handleSaveNickname"
          @keyup.enter="handleSaveNickname"
          ref="nickInput"
          class="nick-input"
        />
        <div class="phone">{{ userStore.userInfo?.phone || '' }}</div>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="$router.push('/address')">
        <span>收藏地址</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/coupon')">
        <span>优惠券</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="handleSecurity">
        <span>安全设置</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="handleAbout">
        <span>关于我们</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <el-button class="logout-btn" @click="handleLogout">退出登录</el-button>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const editing = ref(false)
const nicknameInput = ref(userStore.userInfo?.nickname || '')
const nickInput = ref(null)

const handleSaveNickname = async () => {
  editing.value = false
  if (nicknameInput.value && nicknameInput.value !== userStore.userInfo?.nickname) {
    try {
      await userApi.updateProfile({ nickname: nicknameInput.value })
      userStore.setUserInfo({ ...userStore.userInfo, nickname: nicknameInput.value })
      ElMessage.success('昵称已更新')
    } catch (e) {
      ElMessage.error('昵称更新失败，请重试')
    }
  }
}

const handleSecurity = () => {
  ElMessage.info('安全设置功能开发中')
}

const handleAbout = () => {
  ElMessageBox.alert('江南出行智慧服务平台 v1.0', '关于我们')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-header {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  border-radius: var(--radius-lg);
  padding: 28px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}
.user-info {
  color: #fff;
}
.nickname {
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.nick-input {
  max-width: 160px;
}
.phone {
  font-size: 0.85rem;
  opacity: 0.85;
  margin-top: 2px;
}
.menu-list {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  margin-bottom: 24px;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  font-size: 0.95rem;
}
.menu-item:last-child {
  border-bottom: none;
}
.logout-btn {
  width: 100%;
  color: var(--color-text-muted);
}
</style>
