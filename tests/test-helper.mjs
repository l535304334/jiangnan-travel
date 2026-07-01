// 测试辅助模块 — 共享 token 避免重复登录触发限流
import { existsSync, readFileSync, writeFileSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const TOKEN_FILE = join(__dirname, '.test_token')
const BASE = 'http://localhost:8080'

let cachedToken = null

export async function request(method, path, data = null, token = null) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: data ? JSON.stringify(data) : null
  })
  const body = await res.json().catch(() => null)
  return { status: res.status, body }
}

/** 获取共享 token（缓存 → 文件 → 重新登录） */
export async function getToken(phone = '13900001111', password = '123456') {
  if (cachedToken) return cachedToken
  if (existsSync(TOKEN_FILE)) {
    try {
      const data = JSON.parse(readFileSync(TOKEN_FILE, 'utf-8'))
      if (data.token && data.expires > Date.now()) {
        cachedToken = data.token
        return cachedToken
      }
    } catch { /* ignore */ }
  }
  // 重新登录
  const res = await request('POST', '/api/user/login-password', { phone, password })
  if (res.body?.code === 200 && res.body?.data?.token) {
    cachedToken = res.body.data.token
    writeFileSync(TOKEN_FILE, JSON.stringify({
      token: cachedToken,
      phone,
      expires: Date.now() + 23 * 3600 * 1000 // 23h
    }))
    return cachedToken
  }
  throw new Error(`登录失败: ${JSON.stringify(res.body)}`)
}

/** 测试辅助 */
export async function test(name, method, path, data, token, expectStatus, expectCode) {
  const { status, body } = await request(method, path, data, token)
  let ok = status === expectStatus
  if (expectCode !== undefined && body) ok = ok && body.code === expectCode
  return { name, status, expected: expectStatus, code: body?.code, ok }
}
