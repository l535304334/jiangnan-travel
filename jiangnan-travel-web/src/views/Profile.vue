<template>
  <div class="profile">
    <div class="profile-header">
      <div class="avatar" @click="showAvatarDialog = true">
        <CdnAvatar
          :src="userStore.userInfo?.avatar"
          :seed="userStore.userInfo?.phone || String(userStore.userInfo?.id)"
          type="user"
          :size="60"
          fallback="🧑"
        />
        <div class="avatar-overlay"><el-icon :size="18"><Edit /></el-icon></div>
      </div>
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
        <div class="phone">{{ userStore.userInfo?.phone || '' }}
          <el-tag size="small" style="margin-left:6px;cursor:pointer" @click="ElMessage.info('请联系客服修改手机号')">已绑定</el-tag>
        </div>
      </div>
    </div>

    <!-- 头像修改对话框 -->
    <el-dialog v-model="showAvatarDialog" title="修改头像" width="90%" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="快捷选择默认头像">
          <div class="avatar-presets">
            <img
              v-for="url in presetAvatars"
              :key="url"
              :src="url"
              class="avatar-preset"
              :class="{ active: avatarInput === url }"
              @click="avatarInput = url; avatarError = false"
            />
          </div>
        </el-form-item>
        <el-form-item label="或输入自定义头像URL">
          <el-input v-model="avatarInput" placeholder="输入图片URL地址，留空可清除头像" clearable @input="avatarError = false" />
        </el-form-item>
        <el-form-item v-if="avatarInput" label="预览">
          <img :src="avatarInput" style="width:80px;height:80px;border-radius:50%;object-fit:cover;border:2px solid var(--color-primary-bg)" @error="avatarError=true" @load="avatarError=false" />
          <span v-if="avatarError" style="color:#999;font-size:0.8rem;margin-left:8px">图片加载失败</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAvatar" @click="handleSaveAvatar">确认修改</el-button>
      </template>
    </el-dialog>

    <div class="menu-list app-card">
      <div class="menu-item" @click="$router.push('/address')">
        <span>收藏地址</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/coupon')">
        <span>优惠券</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/invoice-center')">
        <span>发票中心</span>
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

    <el-button type="danger" class="logout-btn" @click="handleLogout">退出登录</el-button>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import CdnAvatar from '@/components/CdnAvatar.vue'
import { getAvatarUrl } from '@/utils/imageCDN'

const router = useRouter()
const userStore = useUserStore()
const editing = ref(false)
const nicknameInput = ref(userStore.userInfo?.nickname || '')
const nickInput = ref(null)
const showAvatarDialog = ref(false)
const avatarInput = ref(userStore.userInfo?.avatar || '')
const avatarError = ref(false)
const savingAvatar = ref(false)
const presetAvatars = [getAvatarUrl('1'), getAvatarUrl('2'), getAvatarUrl('3'), getAvatarUrl('4')]

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

const handleSaveAvatar = async () => {
  if (avatarError.value) { ElMessage.warning('图片加载失败，请检查 URL'); return }
  savingAvatar.value = true
  try {
    await userApi.updateProfile({ avatar: avatarInput.value })
    userStore.setUserInfo({ ...userStore.userInfo, avatar: avatarInput.value })
    showAvatarDialog.value = false
    ElMessage.success(avatarInput.value ? '头像已更新' : '头像已清除')
  } catch (e) {
    ElMessage.error('头像更新失败')
  }
  savingAvatar.value = false
}

const handleSecurity = () => {
  router.push('/security-settings')
}

const handleAbout = () => {
  router.push('/about-company')
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
  cursor: pointer;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}
.avatar-img {
  width: 100%; height: 100%; object-fit: cover; border-radius: 50%;
}
.avatar-emoji { line-height: 1; }
.avatar-overlay {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.3); color: #fff; opacity: 0; transition: opacity 0.2s;
  border-radius: 50%;
}
.avatar:hover .avatar-overlay { opacity: 1; }
.avatar-presets {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.avatar-preset {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s, transform 0.2s;
}
.avatar-preset:hover { transform: scale(1.05); }
.avatar-preset.active { border-color: var(--color-primary); }
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
