<template>
  <div class="driver-login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>江南出行·司机端</h2>
        <p>欢迎回来</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large" @submit.prevent="handleLogin">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入司机手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" class="login-btn" :loading="loading">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { driverApi } from '@/api/driver'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ phone: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await driverApi.login(form.phone)
    localStorage.setItem('token', res.data?.token || '')
    localStorage.setItem('driverInfo', JSON.stringify(res.data || {}))
    ElMessage.success('登录成功')
    router.push('/driver/home')
  } catch {
    // 错误由拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.driver-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
}
.login-card {
  width: 380px;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 48px 36px;
  box-shadow: var(--shadow-lg);
}
.login-header { text-align: center; margin-bottom: 36px; }
.login-header h2 { font-size: 1.5rem; color: var(--color-primary); font-weight: 700; }
.login-header p { color: var(--color-text-muted); margin-top: 8px; }
.login-btn { width: 100%; }
</style>
