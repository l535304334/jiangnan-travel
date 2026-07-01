import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { useSmsCode } from '../useSmsCode.js'

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  }
}))

// Mock user API
vi.mock('@/api/user', () => ({
  userApi: {
    sendCode: vi.fn()
  }
}))

import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

describe('useSmsCode', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('手机号格式正确时调用 API 发送验证码', async () => {
    const phoneRef = ref('13800138000')
    const { sendCode, sending } = useSmsCode(phoneRef)

    userApi.sendCode.mockResolvedValue({ code: 200 })
    await sendCode()

    expect(userApi.sendCode).toHaveBeenCalledWith('13800138000')
    expect(ElMessage.success).toHaveBeenCalledWith('验证码已发送')
    expect(sending.value).toBe(false)
  })

  it('手机号格式错误时提示且不调用 API', async () => {
    const phoneRef = ref('12345')
    const { sendCode } = useSmsCode(phoneRef)

    await sendCode()

    expect(ElMessage.warning).toHaveBeenCalledWith('手机号不正确')
    expect(userApi.sendCode).not.toHaveBeenCalled()
  })

  it('发送成功后启动 60 秒倒计时', async () => {
    const phoneRef = ref('13800138000')
    const { sendCode, countdown } = useSmsCode(phoneRef)

    userApi.sendCode.mockResolvedValue({ code: 200 })
    await sendCode()

    expect(countdown.value).toBe(60)

    vi.advanceTimersByTime(1000)
    expect(countdown.value).toBe(59)

    vi.advanceTimersByTime(59000)
    expect(countdown.value).toBe(0) // 倒计时结束
  })

  it('函数式 phoneRef 也能正确获取手机号', async () => {
    const phone = '13912345678'
    const phoneFn = () => phone
    const { sendCode } = useSmsCode(phoneFn)

    userApi.sendCode.mockResolvedValue({ code: 200 })
    await sendCode()

    expect(userApi.sendCode).toHaveBeenCalledWith('13912345678')
  })

  it('发送失败时提示错误且不启动倒计时', async () => {
    const phoneRef = ref('13800138000')
    const { sendCode, countdown } = useSmsCode(phoneRef)

    userApi.sendCode.mockRejectedValue(new Error('网络错误'))
    await sendCode()

    expect(ElMessage.error).toHaveBeenCalledWith('验证码发送失败，请稍后重试')
    expect(countdown.value).toBe(0)
  })

  it('sending 状态在调用期间为 true', async () => {
    const phoneRef = ref('13800138000')
    const { sendCode, sending } = useSmsCode(phoneRef)

    let resolvePromise
    userApi.sendCode.mockImplementation(() => new Promise(resolve => { resolvePromise = resolve }))

    const promise = sendCode()
    expect(sending.value).toBe(true) // 调用期间 sending 为 true

    resolvePromise({ code: 200 })
    await promise

    expect(sending.value).toBe(false) // 完成后 sending 恢复 false
  })
})
