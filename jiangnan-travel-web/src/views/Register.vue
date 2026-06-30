<template>
  <div class="register-page">
    <div class="card register-card">
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
              <el-button :disabled="countdown > 0" @click="sendCode" class="code-btn btn-text">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请设置密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleRegister">
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
import { useSmsCode } from '@/composables/useSmsCode'
import { useFeedback } from '@/composables/useFeedback'

const router = useRouter()
const userStore = useUserStore()
const { loading, withLoading, notifySuccess } = useFeedback()
const formRef = ref(null)
const form = reactive({ phone: '', code: '', password: '' })

// 统一短信验证码 composable（使用 el-form 字段校验）
const { sendCode, countdown } = useSmsCode(
  () => form.phone,
  { formRef }
)

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

const handleRegister = async () => {
  await formRef.value.validate()
  await withLoading(async () => {
    const res = await userApi.register({
      phone: form.phone,
      code: form.code,
      password: form.password
    })
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.userInfo)
    notifySuccess('注册成功')
    router.push('/home')
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-bg) 0%, var(--color-bg) 50%, var(--color-accent-light) 100%);
  padding: 20px;
}
.register-card { width: 100%; max-width: 400px; }
.register-header {
  text-align: center;
  margin-bottom: 28px;
}
.register-header h2 {
  font-size: 1.5rem;
  color: var(--color-primary-dark);
  font-weight: 700;
  letter-spacing: 4px;
}
.register-header p {
  color: var(--color-text-muted);
  margin-top: 4px;
  font-size: 0.85rem;
  letter-spacing: 4px;
}
.login-link {
  text-align: center;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}
.login-link a {
  color: var(--color-primary);
}
.login-btn { width: 100%; }
</style>
