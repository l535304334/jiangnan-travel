import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

/**
 * 统一短信验证码发送 composable
 *
 * @param {Function|import('vue').Ref} phoneRef - 获取手机号的函数或 ref（如 () => form.phone）
 * @param {Object} [options] - 可选配置
 * @param {import('vue').Ref} [options.formRef] - el-form 的 ref，提供后用 validateField 校验
 * @param {string} [options.fieldName='phone'] - 要校验的表单字段名
 * @param {number} [options.countdownSeconds=60] - 倒计时秒数
 * @returns {{ sendCode: Function, countdown: import('vue').Ref<number>, sending: import('vue').Ref<boolean> }}
 */
export function useSmsCode(phoneRef, options = {}) {
  const {
    formRef = null,
    fieldName = 'phone',
    countdownSeconds = 60
  } = options

  const countdown = ref(0)
  const sending = ref(false)
  let timer = null

  /** 清除倒计时定时器 */
  const clearTimer = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  /** 开始倒计时 */
  const startCountdown = () => {
    clearTimer()
    countdown.value = countdownSeconds
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearTimer()
    }, 1000)
  }

  /** 发送验证码 */
  const sendCode = async () => {
    // 获取手机号
    const phone = typeof phoneRef === 'function' ? phoneRef() : phoneRef.value

    // 手机号校验
    if (formRef && formRef.value) {
      try {
        await formRef.value.validateField(fieldName)
      } catch {
        return // 表单校验不通过，由 el-form 显示错误信息
      }
    } else if (!/^1[3-9]\d{9}$/.test(phone)) {
      ElMessage.warning('手机号不正确')
      return
    }

    sending.value = true
    try {
      await userApi.sendCode(phone)
      ElMessage.success('验证码已发送')
      startCountdown()
    } catch {
      ElMessage.error('验证码发送失败，请稍后重试')
    } finally {
      sending.value = false
    }
  }

  return { sendCode, countdown, sending }
}
