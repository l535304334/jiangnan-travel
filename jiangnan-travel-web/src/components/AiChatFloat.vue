<template>
  <div class="ai-chat-float" v-show="visible">
    <transition name="slide">
      <div class="chat-panel" v-if="open">
        <!-- Header -->
        <div class="chat-header">
          <div class="header-left">
            <el-icon class="menu-btn" @click="toggleSessions" size="18">
              <List v-if="!showSessions" />
              <ChatDotRound v-else />
            </el-icon>
            <span>江小游</span>
          </div>
          <el-icon class="close-btn" @click="open = false"><Close /></el-icon>
        </div>

        <!-- Session List Sidebar -->
        <div class="session-list" v-if="showSessions">
          <div class="session-header">
            <span>历史会话</span>
            <el-button type="primary" size="small" @click="newSession">新对话</el-button>
          </div>
          <div v-for="s in sessions" :key="s" class="session-item"
               :class="{ active: s === sessionId }" @click="switchSession(s)">
            <span class="session-label">{{ s.substring(0, 8) }}...</span>
          </div>
          <div v-if="sessions.length === 0" class="session-empty">暂无历史会话</div>
        </div>

        <!-- Chat Area -->
        <template v-if="!showSessions">
          <div class="chat-body" ref="chatBody">
            <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
              {{ m.content }}
            </div>
            <div v-if="loading && !hasPlaceholder" class="msg assistant typing">思考中...</div>
          </div>
          <div class="chat-input">
            <el-input v-model="input" placeholder="问路、计价、景点..." @keyup.enter="send" size="small" />
            <el-button type="primary" :icon="Promotion" circle size="small" @click="send" :disabled="!input.trim()" />
          </div>
        </template>
      </div>
    </transition>

    <!-- Bubble Button -->
    <div class="chat-bubble" @click="open = !open" v-if="!open">
      <el-badge :value="unread" :hidden="unread === 0">
        <el-button type="primary" circle size="large">
          <el-icon :size="24"><ChatDotRound /></el-icon>
        </el-button>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, computed } from 'vue'
import { Close, Promotion, ChatDotRound, List } from '@element-plus/icons-vue'
import { aiApi } from '@/api/ai'

const visible = ref(true)
const open = ref(false)
const showSessions = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const sessionId = ref(null)
const sessions = ref([])
const unread = ref(0)
const chatBody = ref(null)
let streamController = null

const hasPlaceholder = computed(() =>
  messages.value.some(m => m.role === 'assistant' && m.content === '')
)

/** 打开弹窗时加载会话列表 */
watch(open, async (val) => {
  if (val) {
    await loadSessions()
  } else {
    // 关闭时取消流请求
    if (streamController) {
      streamController.abort()
      streamController = null
    }
    loading.value = false
    showSessions.value = false
    unread.value = 0
  }
})

/** 加载会话列表 */
const loadSessions = async () => {
  try {
    const res = await aiApi.getSessions()
    if (res.code === 200) {
      sessions.value = res.data || []
    }
  } catch {
    // 静默失败
  }
}

/** 切换会话列表面板 */
const toggleSessions = () => {
  showSessions.value = !showSessions.value
}

/** 新建会话 */
const newSession = () => {
  messages.value = []
  sessionId.value = null
  showSessions.value = false
}

/** 切换会话 */
const switchSession = async (sid) => {
  sessionId.value = sid
  showSessions.value = false
  loading.value = true
  try {
    const res = await aiApi.getSessionMessages(sid)
    if (res.code === 200) {
      messages.value = (res.data || []).map(m => ({
        role: m.role,
        content: m.content
      }))
    }
  } catch {
    messages.value = []
  }
  loading.value = false
  await nextTick()
  scrollToBottom()
}

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await nextTick()
  scrollToBottom()

  // 添加占位符 AI 消息（流式填充）
  const assistantIdx = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })

  // 使用 SSE 流式对话
  streamController = aiApi.chatStream(
    text,
    sessionId.value,
    (delta) => {
      if (messages.value[assistantIdx]) {
        messages.value[assistantIdx].content += delta
      }
      scrollToBottom()
    },
    () => {
      loading.value = false
      streamController = null
      if (!messages.value[assistantIdx]?.content) {
        messages.value[assistantIdx].content = '抱歉，我没能理解您的问题。'
      }
      scrollToBottom()
      // 发送完成后刷新会话列表
      loadSessions()
    },
    () => {
      fallbackChat(text, assistantIdx)
    }
  )
}

/** 降级：当 SSE 失败时使用普通 POST */
const fallbackChat = async (text, msgIdx) => {
  try {
    const res = await aiApi.chat(text, sessionId.value)
    if (res.code === 200 && res.data) {
      messages.value[msgIdx].content = res.data.reply
      if (res.data.sessionId) sessionId.value = res.data.sessionId
    }
  } catch {
    messages.value[msgIdx].content = '抱歉，AI 服务暂时不可用，请稍后再试。'
  }
  loading.value = false
  scrollToBottom()
  loadSessions()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
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
.header-left { display: flex; align-items: center; gap: 8px; }
.menu-btn { cursor: pointer; opacity: 0.8; transition: opacity 0.2s; }
.menu-btn:hover { opacity: 1; }
.close-btn { cursor: pointer; font-size: 1rem; }
.session-list {
  flex: 1; overflow-y: auto; padding: 8px;
  display: flex; flex-direction: column; gap: 4px;
}
.session-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 4px 8px 8px; font-size: 0.85rem; color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-border);
}
.session-item {
  padding: 8px 12px; border-radius: 8px; cursor: pointer;
  font-size: 0.8rem; color: var(--color-text); transition: background 0.2s;
}
.session-item:hover { background: var(--color-bg-secondary); }
.session-item.active { background: var(--color-primary); color: #fff; }
.session-empty {
  text-align: center; padding: 20px; color: var(--color-text-muted); font-size: 0.8rem;
}
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
