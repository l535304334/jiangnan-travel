import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动附加 JWT Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => {
    const body = response.data
    // 业务错误统一 reject，让调用方 catch 处理
    if (body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('adminInfo')
      window.location.href = window.location.pathname.startsWith('/admin') ? '/admin/login' : '/login'
      ElMessage.error('登录已过期，请重新登录')
    } else if (status === 403) {
      ElMessage.error('权限不足')
    } else if (status === 400) {
      ElMessage.error(error.response?.data?.message || '请求参数错误')
    } else if (status >= 500) {
      ElMessage.error('服务器繁忙，请稍后重试')
    } else {
      ElMessage.error(error.response?.data?.message || '网络请求失败')
    }
    return Promise.reject(error)
  }
)

export default request
