<template>
  <div class="ai-chat-float" v-show="visible">
    <transition name="slide">
      <div class="chat-panel" v-if="open">
        <div class="chat-header">
          <span>🤖 AI 江南出行助手</span>
          <el-icon class="close-btn" @click="open = false"><Close /></el-icon>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
            {{ m.content }}
          </div>
          <div v-if="loading" class="msg assistant typing">思考中...</div>
        </div>
        <div class="chat-input">
          <el-input v-model="input" placeholder="问路、计价、景点..." @keyup.enter="send" size="small" />
          <el-button type="primary" :icon="Promotion" circle size="small" @click="send" :disabled="!input.trim()" />
        </div>
      </div>
    </transition>
    <div class="chat-bubble" @click="open = !open" v-if="!open">
      <el-badge :value="unread" :hidden="unread===0">
        <el-button type="primary" circle size="large"><el-icon :size="24"><ChatDotRound /></el-icon></el-button>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { Close, Promotion, ChatDotRound } from '@element-plus/icons-vue'
import { aiApi } from '@/api/ai'

const visible = ref(true)
const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const sessionId = ref(null)
const unread = ref(0)
const chatBody = ref(null)

const send = async () => {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await nextTick()
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight

  try {
    const res = await aiApi.chat(text, sessionId.value)
    if (res.code === 200 && res.data) {
      messages.value.push({ role: 'assistant', content: res.data.reply })
      if (res.data.sessionId) sessionId.value = res.data.sessionId
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，AI 服务暂时不可用，请稍后再试。' })
  }
  loading.value = false
  await nextTick()
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  if (!open.value) unread.value++
}
</script>

<style scoped>
.ai-chat-float { position: fixed; bottom: 80px; right: 16px; z-index: 999; }
.chat-panel {
  width: 320px; max-width: 90vw; height: 420px; max-height: 65vh;
  background: #fff; border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  display: flex; flex-direction: column; overflow: hidden;
}
.chat-header {
  padding: 12px 16px; background: var(--color-primary); color: #fff;
  display: flex; justify-content: space-between; align-items: center; font-size: 0.9rem;
}
.close-btn { cursor: pointer; font-size: 1rem; }
.chat-body { flex: 1; overflow-y: auto; padding: 12px; display: flex; flex-direction: column; gap: 8px; }
.msg { max-width: 80%; padding: 8px 12px; border-radius: 10px; font-size: 0.85rem; line-height: 1.5; word-break: break-word; }
.msg.user { align-self: flex-end; background: var(--color-primary); color: #fff; }
.msg.assistant { align-self: flex-start; background: var(--color-bg-secondary); color: var(--color-text); }
.msg.typing { color: var(--color-text-muted); font-style: italic; }
.chat-input { padding: 8px; display: flex; gap: 8px; border-top: 1px solid var(--color-border); }
.chat-bubble { position: fixed; bottom: 80px; right: 16px; z-index: 999; }
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(20px); }
</style>
