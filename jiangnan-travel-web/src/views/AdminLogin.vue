<template>
  <div class="admin-login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>江南出行·管理后台</h2>
        <p>管理员登录</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
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
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    // TODO: 调用管理员登录 API
    localStorage.setItem('token', 'admin-token')
    localStorage.setItem('adminInfo', JSON.stringify({ name: form.username, role: 'admin' }))
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
}
.login-card {
  width: 380px;
  background: #fff; border-radius: var(--radius-lg);
  padding: 48px 36px; box-shadow: var(--shadow-lg);
}
.login-header { text-align: center; margin-bottom: 36px; }
.login-header h2 { font-size: 1.4rem; color: var(--color-primary); font-weight: 700; }
.login-header p { color: var(--color-text-muted); margin-top: 8px; }
.login-btn { width: 100%; }
</style>
