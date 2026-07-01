import { defineStore } from 'pinia'
import { ref } from 'vue'

const parseSafe = (json, fallback) => {
  try { return JSON.parse(json) } catch { return fallback }
}

// ponytail: set JWT as cookie for WebSocket handshake auth (C6 fix)
const setTokenCookie = (val) => {
  const maxAge = val ? 86400 : 0 // 24h or expire now
  document.cookie = `token=${val || ''};path=/;max-age=${maxAge};SameSite=Lax`
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(parseSafe(localStorage.getItem('userInfo'), null))

  const setToken = (val) => {
    token.value = val
    localStorage.setItem('token', val)
    setTokenCookie(val)
  }

  const setUserInfo = (val) => {
    userInfo.value = val
    localStorage.setItem('userInfo', JSON.stringify(val))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
    localStorage.removeItem('driverToken')
    localStorage.removeItem('driverInfo')
    setTokenCookie('')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
