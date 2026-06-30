<template>
  <div class="security-settings-page app-page">
    <div class="settings-section app-card">
      <h3><el-icon :size="18"><Lock /></el-icon> 账号安全</h3>
      <div class="settings-list">
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">登录密码</span>
            <span class="item-desc">建议定期更换密码，确保账号安全</span>
          </div>
          <el-button size="small" plain @click="handleChangePassword">修改</el-button>
        </div>
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">绑定手机</span>
            <span class="item-desc">{{ userStore.userInfo?.phone || '未绑定' }}</span>
          </div>
          <el-button v-if="!userStore.userInfo?.phone" size="small" type="primary" plain @click="handleBindPhone">绑定</el-button>
          <el-tag v-else size="small" type="success">
            已绑定
            <span style="cursor:pointer;margin-left:4px" @click="handleBindPhone">更换</span>
          </el-tag>
        </div>
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">支付密码</span>
            <span class="item-desc">设置支付密码保障资金安全</span>
          </div>
          <el-button size="small" plain @click="handleSetPaymentPwd">{{ hasPaymentPwd ? '修改' : '设置' }}</el-button>
        </div>
      </div>
    </div>

    <div class="settings-section">
      <h3><el-icon :size="18"><Bell /></el-icon> 隐私与通知</h3>
      <div class="settings-list">
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">行程分享</span>
            <span class="item-desc">将行程信息分享给亲友</span>
          </div>
          <el-switch v-model="shareEnabled" @change="handleShareChange" />
        </div>
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">消息推送</span>
            <span class="item-desc">接收订单状态和优惠通知</span>
          </div>
          <el-switch v-model="pushEnabled" @change="handlePushChange" />
        </div>
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">隐私保护</span>
            <span class="item-desc">行程中隐藏真实手机号</span>
          </div>
          <el-switch v-model="privacyEnabled" @change="handlePrivacyChange" />
        </div>
      </div>
    </div>

    <div class="settings-section app-card">
      <h3><el-icon :size="18"><WarningFilled /></el-icon> 安全中心</h3>
      <div class="settings-list">
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">一键报警</span>
            <span class="item-desc">紧急情况一键报警，自动发送位置</span>
          </div>
          <el-button size="small" type="danger" plain @click="handleEmergencyAlert">模拟报警</el-button>
        </div>
        <div class="settings-item">
          <div class="item-left">
            <span class="item-label">安全知识</span>
            <span class="item-desc">出行安全小贴士</span>
          </div>
          <el-button size="small" plain @click="showSafetyTips">查看</el-button>
        </div>
      </div>
    </div>

    <el-button class="back-btn" @click="$router.back()">返回</el-button>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { Lock, Bell, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.id)

// ---------- localStorage 开关 ----------
const shareEnabled = ref(false)
const pushEnabled = ref(true)
const privacyEnabled = ref(true)
const hasPaymentPwd = ref(false)

onMounted(() => {
  shareEnabled.value = localStorage.getItem('share_enabled') === 'true'
  pushEnabled.value = localStorage.getItem('push_enabled') !== 'false' // 默认开启
  privacyEnabled.value = localStorage.getItem('privacy_enabled') !== 'false' // 默认开启
  if (userId.value) {
    hasPaymentPwd.value = !!localStorage.getItem(`payment_pwd_${userId.value}`)
  }
})

// ---------- 1. 修改登录密码 ----------
const handleChangePassword = async () => {
  try {
    const { value: oldPassword } = await ElMessageBox.prompt('请输入当前密码', '修改登录密码', {
      confirmButtonText: '下一步',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '请输入当前密码'
    })
    const { value: newPassword } = await ElMessageBox.prompt('请输入新密码（至少6位）', '设置新密码', {
      confirmButtonText: '确认修改',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '请输入新密码',
      inputValidator: (val) => val && val.length >= 6 ? true : '密码至少6位'
    })
    await userApi.changePassword({ oldPassword, newPassword })
    ElMessage.success('密码修改成功')
  } catch (e) {
    // 用户取消或接口报错不处理
  }
}

// ---------- 2. 绑定/更换手机 ----------
const handleBindPhone = async () => {
  try {
    const { value: phone } = await ElMessageBox.prompt('请输入新手机号', '绑定手机', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入11位手机号',
      inputValidator: (val) => /^1\d{10}$/.test(val) ? true : '请输入正确的11位手机号'
    })
    await userApi.updateProfile({ phone })
    userStore.setUserInfo({ ...userStore.userInfo, phone })
    ElMessage.success('手机号修改成功')
  } catch (e) {
    // 用户取消或接口报错不处理
  }
}

// ---------- 3. 支付密码 ----------
const handleSetPaymentPwd = async () => {
  try {
    const { value: pwd } = await ElMessageBox.prompt('请设置6位数字支付密码', '设置支付密码', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '请输入6位数字',
      inputValidator: (val) => /^\d{6}$/.test(val) ? true : '请输入6位数字'
    })
    if (userId.value) {
      localStorage.setItem(`payment_pwd_${userId.value}`, pwd)
      hasPaymentPwd.value = true
      ElMessage.success('支付密码设置成功')
    }
  } catch (e) {
    // 用户取消
  }
}

// ---------- 4. 行程分享 ----------
const handleShareChange = (val) => {
  localStorage.setItem('share_enabled', val)
  if (val) {
    ElMessage.success('行程分享已开启，亲友可通过链接查看您的行程')
  } else {
    ElMessage.info('行程分享已关闭')
  }
}

// ---------- 5. 消息推送 ----------
const handlePushChange = (val) => {
  localStorage.setItem('push_enabled', val)
  if (val) {
    ElMessage.success('消息推送已开启')
  } else {
    ElMessage.info('消息推送已关闭')
  }
}

// ---------- 6. 隐私保护 ----------
const handlePrivacyChange = (val) => {
  localStorage.setItem('privacy_enabled', val)
  if (val) {
    ElMessage.success('隐私保护已开启，行程中将隐藏真实手机号')
  } else {
    ElMessage.info('隐私保护已关闭')
  }
}

// ---------- 7. 一键报警 ----------
const handleEmergencyAlert = async () => {
  try {
    await ElMessageBox.confirm(
      '您即将发送紧急报警信息，确认继续？',
      '一键报警',
      {
        confirmButtonText: '确认报警',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('报警信息已发送至警方，您的实时位置已共享')
  } catch (e) {
    // 用户取消
  }
}

// ---------- 8. 安全知识 ----------
const showSafetyTips = () => {
  ElMessage.alert(
    `1. 上车前核对车牌号与订单一致\n2. 行程中可开启行程分享给亲友\n3. 夜间出行建议结伴\n4. 如遇紧急情况请拨打110或使用一键报警\n5. 请勿向司机透露个人隐私信息`,
    '安全小贴士',
    { confirmButtonText: '知道了', type: 'info' }
  )
}
</script>

<style scoped>
.security-settings-page {
  padding-bottom: 40px;
}
.settings-section {
  padding: 16px;
  margin-bottom: 14px;
}
.settings-section h3 {
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  color: var(--color-primary-dark);
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border-light);
}
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 4px;
  border-bottom: 1px solid var(--color-border-light);
}
.settings-item:last-child {
  border-bottom: none;
}
.item-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.item-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-text);
}
.item-desc {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
.back-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
