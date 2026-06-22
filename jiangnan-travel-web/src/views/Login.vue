<template>
  <div class="login-page">
    <div class="login-card">
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
        <el-form @submit.prevent="handleLogin">
          <el-form-item><el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Phone" maxlength="11" /></el-form-item>
          <el-form-item v-if="mode === 'sms'">
            <el-input v-model="form.code" placeholder="验证码" :prefix-icon="Lock" maxlength="6">
              <template #append>
                <el-button :disabled="countdown > 0 || sending" @click="sendCode" class="code-btn" size="small">
                  {{ countdown > 0 ? `${countdown}s` : sending ? '...' : '获取' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item v-if="mode === 'password'">
            <el-input v-model="form.password" placeholder="密码" :prefix-icon="Lock" type="password" show-password />
          </el-form-item>
          <el-form-item><el-button type="primary" native-type="submit" class="login-btn" :loading="loading">登录</el-button></el-form-item>
        </el-form>
      </template>

      <!-- 司机 -->
      <template v-if="role === 'driver'">
        <el-form @submit.prevent="handleDriverLogin">
          <el-form-item><el-input v-model="driverForm.phone" placeholder="司机手机号" :prefix-icon="Phone" maxlength="11" /></el-form-item>
          <el-form-item>
            <el-input v-model="driverForm.carPlate" placeholder="车牌号（选填）" :prefix-icon="Van" maxlength="10" />
          </el-form-item>
          <el-form-item><el-button type="primary" native-type="submit" class="login-btn" :loading="loading">司机登录</el-button></el-form-item>
        </el-form>
      </template>

      <!-- 管理员 -->
      <template v-if="role === 'admin'">
        <el-form @submit.prevent="handleAdminLogin">
          <el-form-item><el-input v-model="adminForm.username" placeholder="用户名" :prefix-icon="UserFilled" /></el-form-item>
          <el-form-item><el-input v-model="adminForm.password" placeholder="密码" :prefix-icon="Lock" type="password" show-password @keyup.enter="handleAdminLogin" /></el-form-item>
          <el-form-item><el-button type="primary" native-type="submit" class="login-btn" :loading="loading">管理员登录</el-button></el-form-item>
        </el-form>
      </template>
    </div>

    <!-- 测试账号 -->
    <div class="test-accounts">
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
        <span @click="quickFillAdmin('admin','admin123')">admin / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Lock, UserFilled, Van } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { driverApi } from '@/api/driver'
import { adminApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const role = ref('user')
const mode = ref('password')

const form = reactive({ phone: '', code: '', password: '123456' })
const driverForm = reactive({ phone: '', carPlate: '' })
const adminForm = reactive({ username: '', password: '' })

const switchRole = (r) => { role.value = r; loading.value = false }

const sendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) { ElMessage.warning('手机号不正确'); return }
  sending.value = true
  try {
    await userApi.sendCode(form.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const t = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(t) }, 1000)
  } catch (e) {}
  sending.value = false
}

const handleLogin = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) { ElMessage.warning('手机号不正确'); return }
  loading.value = true
  try {
    let res
    if (mode.value === 'password') {
      res = await userApi.passwordLogin({ phone: form.phone, password: form.password })
    } else {
      res = await userApi.login({ phone: form.phone, code: form.code })
    }
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success(`欢迎, ${res.data.nickname || '用户'}`)
    router.push('/home')
  } catch (e) {
    ElMessage.error('登录失败')
  }
  loading.value = false
}

const handleDriverLogin = async () => {
  if (!/^1[3-9]\d{9}$/.test(driverForm.phone)) { ElMessage.warning('手机号不正确'); return }
  loading.value = true
  try {
    const res = await driverApi.login(driverForm.phone)
    userStore.setToken(res.data.token)
    userStore.setUserInfo({ ...res.data, role: 'driver' })
    ElMessage.success(`司机 ${res.data.nickname} 登录成功`)
    router.push('/driver/home')
  } catch (e) {
    ElMessage.error('司机登录失败')
  }
  loading.value = false
}

const handleAdminLogin = async () => {
  if (!adminForm.username || !adminForm.password) { ElMessage.warning('请输入账号密码'); return }
  loading.value = true
  try {
    const res = await adminApi.login(adminForm.username, adminForm.password)
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      ElMessage.success('管理员登录成功')
      router.push('/admin/dashboard')
    }
  } catch (e) {
    ElMessage.error('管理员登录失败，请使用 admin / admin123')
  }
  loading.value = false
}

const quickFillUser = (phone) => { role.value = 'user'; mode.value = 'password'; form.phone = phone; form.password = '123456' }
const quickFillDriver = (phone, plate) => { role.value = 'driver'; driverForm.phone = phone; driverForm.carPlate = plate }
const quickFillAdmin = (u, p) => { role.value = 'admin'; adminForm.username = u; adminForm.password = p }
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  background: linear-gradient(135deg, #E8F5EE 0%, #F7F5F0 50%, #FDE4A5 100%);
  padding: 20px; gap: 20px;
}
.login-card {
  width: 100%; max-width: 400px;
  background: #fff; border-radius: 16px;
  padding: 32px 24px; box-shadow: 0 8px 24px rgba(0,0,0,0.10);
}
.login-header { text-align: center; margin-bottom: 20px; }
.login-header h2 { font-size: 1.5rem; color: var(--color-primary); font-weight: 700; letter-spacing: 4px; }
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
.code-btn { color: var(--color-primary); border: none; padding: 0 6px; font-size: 0.75rem; }

.test-accounts {
  width: 100%; max-width: 400px; background: rgba(255,255,255,0.9);
  border-radius: 12px; padding: 14px; font-size: 0.85rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.test-list { display: flex; flex-wrap: wrap; gap: 6px; }
.test-list span {
  background: var(--color-primary-bg); color: var(--color-primary);
  padding: 3px 10px; border-radius: 16px; cursor: pointer; font-size: 0.8rem; transition: 0.2s;
}
.test-list span:hover { background: var(--color-primary); color: #fff; }

@media (max-width: 360px) { .login-card { padding: 24px 16px; } }
</style>
