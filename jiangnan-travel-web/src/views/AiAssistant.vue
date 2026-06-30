<template>
  <div class="ai-assistant app-page">
    <!-- 头部 -->
    <div class="ai-header">
      <CdnAvatar type="ai" :size="48" fallback="🤖" />
      <div class="ai-header-text">
        <h3>AI 智能助手</h3>
        <p>为您推荐目的地 · 查询景点 · 出行建议</p>
      </div>
    </div>

    <!-- 功能 Tab -->
    <el-tabs v-model="activeTab" class="ai-tabs" :stretch="true">
      <!-- Tab 1: 智能问答 -->
      <el-tab-pane label="智能问答" name="chat">
        <div class="chat-container" ref="chatRef">
          <div class="chat-msg" v-for="(msg, i) in messages" :key="i"
               :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'">
            <div class="msg-avatar">
              <CdnAvatar v-if="msg.role === 'ai'" type="ai" :size="32" fallback="🤖" />
              <span v-else>👤</span>
            </div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
          <div v-if="streamingText" class="chat-msg msg-ai">
            <div class="msg-avatar">
              <CdnAvatar type="ai" :size="32" fallback="🤖" />
            </div>
            <div class="msg-content">{{ streamingText }}</div>
          </div>
        </div>
        <div class="chat-input-bar">
          <el-input
            v-model="chatInput"
            placeholder="问问AI..." size="small"
            :disabled="streaming"
            @keyup.enter="sendChat"
          />
          <el-button type="primary" size="small" :loading="streaming"
                     :icon="Promotion" @click="sendChat" />
        </div>
      </el-tab-pane>

      <!-- Tab 2: 推荐目的地 -->
      <el-tab-pane label="推荐目的地" name="recommend">
        <div class="ai-section" v-loading="loadingRecommends" element-loading-text="加载中...">
          <p class="ai-section-desc">基于您的出行数据，AI 为您推荐以下目的地</p>
          <div class="dest-cards">
            <div class="dest-card" v-for="d in recommends" :key="d.address" @click="goOrder(d)">
              <div class="dest-rank" :style="{ background: rankBg(d.orderCount) }">{{ d.orderCount }}次</div>
              <span class="dest-name">{{ d.address }}</span>
            </div>
          </div>
          <el-empty v-if="!loadingRecommends && recommends.length === 0" description="暂无推荐" :image-size="60" />
        </div>
      </el-tab-pane>

      <!-- Tab 3: 热门景点 -->
      <el-tab-pane label="热门景点" name="hotspots">
        <div class="ai-section" v-loading="loadingHotspots" element-loading-text="加载中...">
          <p class="ai-section-desc">江南地区最受欢迎的文旅景点</p>
          <div class="hotspot-cards">
            <div class="hotspot-card" v-for="(hs, i) in hotspots" :key="i" @click="goOrder(hs)">
              <div class="hotspot-rank">{{ i + 1 }}</div>
              <div class="hotspot-info">
                <div class="hotspot-name">{{ hs.address || hs.name }}</div>
                <div class="hotspot-count">{{ hs.count || hs.orderCount || 0 }} 次出行</div>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
          <el-empty v-if="!loadingHotspots && hotspots.length === 0" description="暂无热点" :image-size="60" />
        </div>
      </el-tab-pane>

      <!-- Tab 4: 城市寄语 -->
      <el-tab-pane label="城市寄语" name="quotes">
        <div class="ai-section" v-loading="loadingQuotes" element-loading-text="加载中...">
          <p class="ai-section-desc">来自江南各城市的诗意寄语</p>
          <div class="quote-list">
            <div class="quote-item app-card" v-for="q in quotes" :key="q.id">
              <div class="quote-text">"{{ q.content }}"</div>
              <div class="quote-footer">
                <span class="quote-author">—— {{ q.author || '匿名' }}</span>
                <el-tag size="small">{{ q.city }}</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-if="!loadingQuotes && quotes.length === 0" description="暂无寄语" :image-size="60" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Promotion } from '@element-plus/icons-vue'
import { aiApi } from '@/api/ai'
import CdnAvatar from '@/components/CdnAvatar.vue'

const router = useRouter()

// Tab
const activeTab = ref('chat')

// Chat
const chatInput = ref('')
const messages = ref([])
const streaming = ref(false)
const streamingText = ref('')
const chatRef = ref(null)
let abortController = null
let sessionId = ref(null)

function sendChat() {
  const msg = chatInput.value.trim()
  if (!msg || streaming.value) return
  messages.value.push({ role: 'user', content: msg })
  chatInput.value = ''
  streaming.value = true

  abortController = aiApi.chatStream(
    msg,
    sessionId.value,
    (delta) => { streamingText.value += delta },
    () => {
      messages.value.push({ role: 'ai', content: streamingText.value })
      streamingText.value = ''
      streaming.value = false
      sessionId.value = Date.now().toString()
      scrollToBottom()
    },
    (err) => {
      ElMessage.error(err || 'AI 回复失败')
      streaming.value = false
    }
  )
}

function scrollToBottom() {
  nextTick(() => {
    const el = chatRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

watch(streamingText, scrollToBottom)

// Recommend destinations
const loadingRecommends = ref(false)
const recommends = ref([])

async function loadRecommends() {
  loadingRecommends.value = true
  try {
    const res = await aiApi.recommendDest()
    if (res.code === 200) recommends.value = res.data || []
  } catch { /* ignore */ } finally {
    loadingRecommends.value = false
  }
}

function rankBg(count) {
  if (count >= 10) return 'linear-gradient(135deg, #f44336, #e53935)'
  if (count >= 5) return 'linear-gradient(135deg, #ff9800, #fb8c00)'
  return 'linear-gradient(135deg, #4caf50, #43a047)'
}

// Hotspots
const loadingHotspots = ref(false)
const hotspots = ref([])

async function loadHotspots() {
  loadingHotspots.value = true
  try {
    const res = await aiApi.getHotspots()
    if (res.code === 200) hotspots.value = res.data || []
  } catch { /* ignore */ } finally {
    loadingHotspots.value = false
  }
}

// Quotes
const loadingQuotes = ref(false)
const quotes = ref([])

async function loadQuotes() {
  loadingQuotes.value = true
  try {
    const res = await aiApi.getCityQuotes()
    if (res.code === 200) quotes.value = res.data || []
  } catch { /* ignore */ } finally {
    loadingQuotes.value = false
  }
}

// Navigate to order
const goOrder = (dest) => {
  router.push({
    path: '/order-create',
    query: { endAddress: dest.address || dest.name, endLat: dest.lat, endLng: dest.lng }
  })
}

onMounted(() => {
  loadRecommends()
  loadHotspots()
  loadQuotes()
})
</script>

<style scoped>
.ai-assistant { padding: var(--spacing-md); }

/* 头部 */
.ai-header {
  display: flex; align-items: center; gap: 12px;
  padding: 16px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; margin-bottom: 12px;
}
.ai-avatar { font-size: 2.2rem; }
.ai-header-text h3 { margin: 0; font-size: 1.1rem; }
.ai-header-text p { margin: 4px 0 0; font-size: 0.75rem; opacity: 0.85; }

/* Tabs */
.ai-tabs { margin-bottom: 12px; }
:deep(.el-tabs__header) { margin-bottom: 0; }

/* Chat */
.chat-container {
  max-height: 380px; overflow-y: auto; padding: 12px 0;
  display: flex; flex-direction: column; gap: 10px;
}
.chat-msg { display: flex; gap: 8px; align-items: flex-start; }
.chat-msg.msg-user { flex-direction: row-reverse; }
.msg-avatar { font-size: 1.5rem; flex-shrink: 0; }
.msg-content {
  max-width: 80%; padding: 8px 12px; border-radius: 12px;
  font-size: 0.85rem; line-height: 1.5; word-break: break-word;
}
.msg-user .msg-content {
  background: var(--color-primary); color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-ai .msg-content {
  background: #f0f2f5; color: var(--color-text);
  border-bottom-left-radius: 4px;
}
.chat-input-bar {
  display: flex; gap: 8px; align-items: center;
  position: sticky; bottom: 0; background: #fff; padding-top: 8px;
}

/* 推荐目的地 */
.ai-section { padding: 12px 0; }
.ai-section-desc {
  font-size: 0.82rem; color: var(--color-text-muted);
  margin: 0 0 12px;
}
.dest-cards {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px;
}
.dest-card {
  cursor: pointer;
  display: flex; flex-direction: column; gap: 6px;
}
.dest-rank {
  align-self: flex-start; font-size: 0.7rem; color: #fff;
  padding: 2px 10px; border-radius: 10px;
}
.dest-name { font-size: 0.9rem; font-weight: 600; }

/* 热门景点 */
.hotspot-cards { display: flex; flex-direction: column; gap: 8px; }
.hotspot-card {
  display: flex; align-items: center; gap: 12px;
  cursor: pointer;
}
.hotspot-rank {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--color-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.8rem; font-weight: 700; flex-shrink: 0;
}
.hotspot-info { flex: 1; }
.hotspot-name { font-size: 0.9rem; font-weight: 500; }
.hotspot-count { font-size: 0.72rem; color: var(--color-text-muted); margin-top: 2px; }

/* 城市寄语 */
.quote-list { display: flex; flex-direction: column; gap: 10px; }
.quote-item {
  padding: 14px 16px;
}
.quote-text { font-size: 0.9rem; font-style: italic; line-height: 1.6; margin-bottom: 8px; }
.quote-footer {
  display: flex; justify-content: space-between; align-items: center;
}
.quote-author { font-size: 0.78rem; color: var(--color-text-muted); }
</style>
