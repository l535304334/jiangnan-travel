<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h2>江南出行</h2>
        <p>注册账号</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="code">
          <el-input v-model="form.code" placeholder="请输入验证码" :prefix-icon="Lock">
            <template #append>
              <el-button :disabled="countdown > 0" @click="sendCode" class="code-btn">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请设置密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-btn" :loading="loading" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<router-link to="/login">去登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Lock } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
const form = reactive({ phone: '', code: '', password: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ]
}

const sendCode = async () => {
  await formRef.value.validateField('phone')
  await userApi.sendCode(form.phone)
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await userApi.register({
      phone: form.phone,
      code: form.code,
      password: form.password
    })
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.userInfo)
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E8F5EE 0%, #F7F5F0 50%, #FDE4A5 100%);
}
.register-card {
  width: 400px;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 48px 40px;
  box-shadow: var(--shadow-lg);
}
.register-header {
  text-align: center;
  margin-bottom: 36px;
}
.register-header h2 {
  font-size: 1.8rem;
  color: var(--color-primary);
  font-weight: 700;
  letter-spacing: 4px;
}
.register-header p {
  color: var(--color-text-muted);
  margin-top: 8px;
  letter-spacing: 6px;
}
.register-btn {
  width: 100%;
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-hover-bg-color: var(--color-primary-dark);
}
.code-btn {
  color: var(--color-primary);
  border: none;
}
.login-link {
  text-align: center;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}
.login-link a {
  color: var(--color-primary);
}
</style>
