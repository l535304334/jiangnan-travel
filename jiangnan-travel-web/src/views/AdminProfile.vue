<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h3>个人资料</h3>
    </div>

    <el-card>
      <div class="profile-main">
        <div class="profile-avatar">
          <CdnAvatar
            type="admin"
            :seed="adminInfo.username || adminInfo.phone || String(adminInfo.id)"
            :size="72"
            icon="UserFilled"
          />
        </div>
        <div class="profile-info">
          <div class="profile-name">
            {{ adminInfo.name || adminInfo.username || adminInfo.phone || '管理员' }}
            <el-tag type="danger" size="small" effect="dark">超级管理员</el-tag>
          </div>
          <div class="profile-meta">
            <span>账号：{{ adminInfo.username || adminInfo.phone || '-' }}</span>
            <span>登录时间：{{ loginTime }}</span>
          </div>
        </div>
      </div>

      <el-divider />

      <el-descriptions :column="1" border>
        <el-descriptions-item label="昵称">{{ adminInfo.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ adminInfo.username || adminInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">超级管理员</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag type="success">正常</el-tag></el-descriptions-item>
      </el-descriptions>

      <div class="profile-actions">
        <el-button type="danger" size="large" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { UserFilled, SwitchButton } from '@element-plus/icons-vue'
import CdnAvatar from '@/components/CdnAvatar.vue'

const router = useRouter()
const userStore = useUserStore()

const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')

const loginTime = computed(() => {
  const t = adminInfo.loginTime || adminInfo.createTime
  if (t) return t.replace('T', ' ')
  return new Date().toLocaleString()
})

function handleLogout() {
  userStore.logout()
  ElMessage.success('已安全退出')
  router.push('/login')
}
</script>

<style scoped>
.profile-main {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-name {
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-semi-bold);
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.profile-meta {
  font-size: var(--font-size-body-small);
  color: var(--color-text-secondary);
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.profile-actions {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: flex-end;
}
</style>
