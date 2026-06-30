<template>
  <div class="login-page">
    <div class="card login-card">
      <div class="login-header">
        <h2>江南出行</h2>
        <p>智慧服务平台</p>
      </div>

      <div class="login-tabs">
        <span :class="{ active: role === 'user' }" @click="switchRole('user')">🧑 乘客登录</span>
        <span :class="{ active: role === 'driver' }" @click="switchRole('driver')">🚗 司机登录</span>
        <span :class="{ active: role === 'admin' }" @click="switchRole('admin')">🛡 管理员</span>
      </div>

      <!-- 乘客 -->
      <template v-if="role === 'user'">
        <div class="login-mode">
          <span :class="{ active: mode === 'password' }" @click="mode = 'password'">密码</span>
          <span :class="{ active: mode === 'sms' }" @click="mode = 'sms'">验证码</span>
        </div>
        <el-form ref="userFormRef" :model="form" :rules="userRules" @submit.prevent="handleLogin">
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Phone" maxlength="11" />
          </el-form-item>
          <el-form-item v-if="mode === 'sms'" prop="code">
            <el-input v-model="form.code" placeholder="验证码" :prefix-icon="Lock" maxlength="6">
              <template #append>
                <el-button :disabled="countdown > 0 || sending" @click="sendCode" class="code-btn btn-text" size="small">
                  {{ countdown > 0 ? `${countdown}s` : sending ? '...' : '获取' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item v-if="mode === 'password'" prop="password">
            <el-input v-model="form.password" placeholder="密码" :prefix-icon="Lock" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" class="login-btn" :loading="loading">登录</el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- 司机 -->
      <template v-if="role === 'driver'">
        <el-form ref="driverFormRef" :model="driverForm" :rules="driverRules" @submit.prevent="handleDriverLogin">
          <el-form-item prop="phone">
            <el-input v-model="driverForm.phone" placeholder="司机手机号" :prefix-icon="Phone" maxlength="11" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="driverForm.carPlate" placeholder="车牌号（选填）" :prefix-icon="Van" maxlength="10" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" class="login-btn" :loading="loading">司机登录</el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- 管理员 -->
      <template v-if="role === 'admin'">
        <el-form ref="adminFormRef" :model="adminForm" :rules="adminRules" @submit.prevent="handleAdminLogin">
          <el-form-item prop="username">
            <el-input v-model="adminForm.username" placeholder="用户名" :prefix-icon="UserFilled" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="adminForm.password" placeholder="密码" :prefix-icon="Lock" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" class="login-btn" :loading="loading">管理员登录</el-button>
          </el-form-item>
        </el-form>
      </template>
    </div>

    <!-- 测试账号 -->
    <div class="card test-accounts">
      <p style="font-weight:500;margin-bottom:6px">测试账号（密码: 123456）</p>
      <p style="font-size:0.75rem;color:var(--color-text-muted)">乘客</p>
      <div class="test-list">
        <span @click="quickFillUser('13900001111')">13900001111</span>
        <span @click="quickFillUser('13920000001')">13920000001</span>
      </div>
      <p style="font-size:0.75rem;color:var(--color-text-muted);margin-top:4px">司机</p>
      <div class="test-list">
        <span @click="quickFillDriver('13810000001','赣A12345')">13810000001</span>
        <span @click="quickFillDriver('13810000002','赣B67890')">13810000002</span>
      </div>
      <p style="font-size:0.75rem;color:var(--color-text-muted);margin-top:4px">管理员</p>
      <div class="test-list">
        <span @click="quickFillAdmin('admin','123456')">admin / 123456</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Lock, UserFilled, Van } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { driverApi } from '@/api/driver'
import { adminApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import { useSmsCode } from '@/composables/useSmsCode'
import { useFeedback } from '@/composables/useFeedback'

const router = useRouter()
const userStore = useUserStore()
const { loading, withLoading, notifySuccess, notifyError, notifyWarning } = useFeedback()
const role = ref('user')
const mode = ref('password')

const form = reactive({ phone: '', code: '', password: '123456' })
const driverForm = reactive({ phone: '', carPlate: '' })
const adminForm = reactive({ username: '', password: '' })

const userFormRef = ref(null)
const driverFormRef = ref(null)
const adminFormRef = ref(null)

// 统一短信验证码 composable（乘客模式）
const { sendCode, countdown, sending } = useSmsCode(
  () => form.phone,
  { formRef: userFormRef }
)

// 表单验证规则
const phoneRule = { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
const userRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, phoneRule],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const driverRules = {
  phone: [{ required: true, message: '请输入司机手机号', trigger: 'blur' }, phoneRule]
}
const adminRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const switchRole = (r) => { role.value = r; loading.value = false }

const handleLogin = async () => {
  const valid = await userFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await withLoading(async () => {
    try {
      const res = mode.value === 'password'
        ? await userApi.passwordLogin({ phone: form.phone, password: form.password })
        : await userApi.login({ phone: form.phone, code: form.code })
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      notifySuccess(`欢迎, ${res.data.nickname || '用户'}`)
      router.push('/home')
    } catch {
      notifyError('登录失败')
    }
  })
}

const handleDriverLogin = async () => {
  const valid = await driverFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await withLoading(async () => {
    try {
      const res = await driverApi.login(driverForm.phone)
      const driverInfo = { ...res.data, role: 'driver' }
      userStore.setToken(res.data.token)
      userStore.setUserInfo(driverInfo)
      localStorage.setItem('driverToken', res.data.token)
      localStorage.setItem('driverInfo', JSON.stringify(driverInfo))
      notifySuccess(`司机 ${res.data.nickname} 登录成功`)
      router.push('/driver/home')
    } catch {
      notifyError('司机登录失败')
    }
  })
}

const handleAdminLogin = async () => {
  const valid = await adminFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await withLoading(async () => {
    try {
      const res = await adminApi.login(adminForm.username, adminForm.password)
      const adminInfo = {
        ...res.data,
        name: res.data?.nickname || adminForm.username,
        role: 'admin'
      }
      userStore.setToken(res.data.token)
      userStore.setUserInfo(adminInfo)
      localStorage.setItem('adminToken', res.data.token)
      localStorage.setItem('adminInfo', JSON.stringify(adminInfo))
      notifySuccess('管理员登录成功')
      router.push('/admin/dashboard')
    } catch {
      notifyError('管理员登录失败，请使用 admin / 123456')
    }
  })
}

const quickFillUser = (phone) => { role.value = 'user'; mode.value = 'password'; form.phone = phone; form.password = '123456' }
const quickFillDriver = (phone, plate) => { role.value = 'driver'; driverForm.phone = phone; driverForm.carPlate = plate }
const quickFillAdmin = (u, p) => { role.value = 'admin'; adminForm.username = u; adminForm.password = p }
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-bg) 0%, var(--color-bg) 50%, var(--color-accent-light) 100%);
  padding: 20px; gap: 20px;
}
.login-card { width: 100%; max-width: 400px; }
.login-header { text-align: center; margin-bottom: 20px; }
.login-header h2 { font-size: 1.5rem; color: var(--color-primary-dark); font-weight: 700; letter-spacing: 4px; }
.login-header p { color: var(--color-text-muted); margin-top: 4px; font-size: 0.85rem; }

.login-tabs { display: flex; margin-bottom: 16px; border-radius: 8px; overflow: hidden; border: 1px solid var(--color-primary); }
.login-tabs span {
  flex: 1; text-align: center; padding: 10px 4px; cursor: pointer; font-size: 0.82rem;
  color: var(--color-primary); transition: 0.2s; background: #fff;
}
.login-tabs span.active { background: var(--color-primary); color: #fff; }

.login-mode { display: flex; justify-content: center; gap: 20px; margin-bottom: 16px; }
.login-mode span { cursor: pointer; font-size: 0.85rem; color: var(--color-text-muted); padding-bottom: 4px; }
.login-mode span.active { color: var(--color-primary); border-bottom: 2px solid var(--color-primary); }

.login-btn { width: 100%; }

.test-accounts {
  width: 100%; max-width: 400px;
  background: var(--color-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  font-size: 0.85rem;
  box-shadow: var(--shadow-sm);
}
.test-list { display: flex; flex-wrap: wrap; gap: 6px; }
.test-list span {
  background: var(--color-primary-bg); color: var(--color-primary);
  padding: 3px 10px; border-radius: 16px; cursor: pointer; font-size: 0.8rem; transition: 0.2s;
}
.test-list span:hover { background: var(--color-primary); color: #fff; }

@media (max-width: 360px) { .login-card { padding: 24px 16px; } }
</style>
