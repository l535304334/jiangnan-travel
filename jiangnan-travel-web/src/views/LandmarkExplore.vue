<template>
  <div class="landmark-explore app-page" v-loading="loadingLandmarks" element-loading-text="加载中...">
    <!-- 城市筛选 -->
    <div class="city-filter">
      <span
        v-for="city in allCities"
        :key="city"
        class="city-pill"
        :class="{ active: activeCity === city }"
        @click="activeCity = city"
      >{{ city === '全部' ? '🏔️ 全部' : city }}</span>
    </div>

    <!-- 搜索 -->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索地标..."
      size="small"
      clearable
      class="landmark-search"
      @clear="resetSearch"
      @input="onSearchInput"
    />

    <!-- 地标卡片 -->
    <div class="landmark-grid" v-if="displayLandmarks.length > 0">
      <div class="landmark-card" v-for="lm in displayLandmarks" :key="lm.id" @click="goOrder(lm)">
        <div class="lm-gradient" :style="{ background: getCityColor(lm.city).bg }">
          <span class="lm-emoji-lg">{{ typeEmoji(lm.name) }}</span>
        </div>
        <div class="lm-info">
          <div class="lm-name" v-html="highlightMatch(lm.name)"></div>
          <el-tag size="small" :type="getCityColor(lm.city).tag">{{ lm.city }}</el-tag>
        </div>
        <div class="lm-footer">
          <span class="lm-desc-text" v-html="highlightMatch(lm.description || lm.name + ' — 江南文化名胜')"></span>
          <el-button text type="primary" size="small" @click.stop="goOrder(lm)">打车去</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else-if="!loadingLandmarks" description="暂无匹配的地标" />

    <!-- 城市寄语 -->
    <div class="section app-section" v-if="quotes.length > 0">
      <h4 class="app-section-title">🌿 城市寄语</h4>
      <div class="quote-wall">
        <div class="quote-card app-card" v-for="q in quotes" :key="q.id">
          <div class="quote-content">"{{ q.content }}"</div>
          <div class="quote-author">—— {{ q.author || '匿名' }}</div>
          <el-tag size="small" class="quote-city-tag">{{ q.city }}</el-tag>
        </div>
      </div>
      <el-empty v-if="quotes.length === 0" description="暂无寄语" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'

const router = useRouter()
const allLandmarks = ref([])
const quotes = ref([])
const searchKeyword = ref('')
const searchedLandmarks = ref([])
const isSearching = ref(false)
const loadingLandmarks = ref(false)
const activeCity = ref('全部')

const allCities = computed(() => {
  const cities = ['全部']
  const citySet = new Set(allLandmarks.value.map(l => l.city))
  citySet.forEach(c => cities.push(c))
  return cities
})

const displayLandmarks = computed(() => {
  let list = isSearching.value ? searchedLandmarks.value : allLandmarks.value
  if (activeCity.value !== '全部') {
    list = list.filter(l => l.city === activeCity.value)
  }
  return list
})

// 城市颜色映射
const cityColors = {
  '南昌': { bg: 'var(--gradient-landmark-nanchang)', tag: 'danger' },
  '九江': { bg: 'var(--gradient-landmark-jiujiang)', tag: 'primary' },
  '赣州': { bg: 'var(--gradient-landmark-ganzhou)', tag: 'warning' },
  '景德镇': { bg: 'var(--gradient-landmark-jingdezhen)', tag: 'success' },
  '吉安': { bg: 'linear-gradient(135deg, #9b59b6, #8e44ad)', tag: '' },
  '上饶': { bg: 'linear-gradient(135deg, #2ecc71, #27ae60)', tag: 'success' },
  '抚州': { bg: 'linear-gradient(135deg, #f39c12, #e67e22)', tag: 'warning' },
  '宜春': { bg: 'linear-gradient(135deg, #1abc9c, #2ecc71)', tag: 'success' },
  '鹰潭': { bg: 'linear-gradient(135deg, #34495e, #2c3e50)', tag: 'info' },
  '萍乡': { bg: 'linear-gradient(135deg, #e74c3c, #c0392b)', tag: 'danger' },
  '新余': { bg: 'linear-gradient(135deg, #3498db, #9b59b6)', tag: 'primary' },
}
const getCityColor = (city) => cityColors[city] || { bg: 'var(--gradient-landmark-default)', tag: 'primary' }

const typeEmoji = (name) => {
  if (!name) return '📍'
  if (name.includes('山') || name.includes('峰')) return '🏔️'
  if (name.includes('湖') || name.includes('江') || name.includes('水')) return '🌊'
  if (name.includes('寺') || name.includes('庙')) return '🏛️'
  if (name.includes('楼') || name.includes('阁') || name.includes('台')) return '🏯'
  if (name.includes('古镇') || name.includes('村')) return '🏘️'
  if (name.includes('园') || name.includes('馆') || name.includes('博物')) return '🏛️'
  if (name.includes('塔')) return '🗼'
  if (name.includes('桥')) return '🌉'
  if (name.includes('窑') || name.includes('瓷')) return '🏺'
  return '📍'
}

// 搜索防抖
let searchTimer = null
const onSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { searchLandmarks() }, 300)
}

const highlightMatch = (text) => {
  if (!searchKeyword.value.trim() || !text) return text
  const kw = searchKeyword.value.trim()
  const escaped = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return text.replace(regex, '<mark style="background:#e6f7ff;color:#1890ff;padding:0 2px;border-radius:2px">$1</mark>')
}

const searchLandmarks = async () => {
  const kw = searchKeyword.value.trim()
  if (!kw) { resetSearch(); return }
  isSearching.value = true
  try {
    const res = await aiApi.searchLandmarks(kw)
    if (res.code === 200) {
      searchedLandmarks.value = res.data || []
    }
  } catch {
    searchedLandmarks.value = allLandmarks.value.filter(lm =>
      lm.name.includes(kw) || lm.city.includes(kw)
    )
  }
}

const resetSearch = () => {
  searchKeyword.value = ''
  isSearching.value = false
  searchedLandmarks.value = []
}

const goOrder = (dest) => {
  router.push({
    path: '/order-create',
    query: { endAddress: dest.address || dest.name, endLat: dest.lat, endLng: dest.lng }
  })
}

onMounted(async () => {
  loadingLandmarks.value = true
  try {
    const [lmRes, quoteRes] = await Promise.all([
      aiApi.getLandmarks(),
      aiApi.getCityQuotes()
    ])
    if (lmRes.code === 200) allLandmarks.value = lmRes.data || []
    if (quoteRes.code === 200) quotes.value = quoteRes.data || []
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    loadingLandmarks.value = false
  }
})
</script>

<style scoped>
.landmark-explore {
  padding: var(--spacing-md);
}

/* 城市筛选 */
.city-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.city-pill {
  padding: 4px 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--color-border);
  font-size: 0.8rem;
  cursor: pointer;
  transition: 0.2s;
  color: var(--color-text-secondary);
}
.city-pill.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

/* 搜索 */
.landmark-search {
  margin-bottom: 12px;
}

/* 地标网格 */
.landmark-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}
.landmark-card {
  cursor: pointer;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.lm-gradient {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lm-emoji-lg { font-size: 3rem; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2)); }
.lm-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px 4px;
}
.lm-name { font-size: 0.9rem; font-weight: 600; }
.lm-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 12px 12px;
}
.lm-desc-text {
  font-size: 0.72rem;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 4px;
}

/* 城市寄语 */
.quote-wall {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.quote-card {
  padding: 14px 16px;
  position: relative;
}
.quote-content {
  font-size: 0.9rem;
  font-style: italic;
  color: var(--color-text);
  line-height: 1.6;
  margin-bottom: 8px;
}
.quote-author {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  text-align: right;
}
.quote-city-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}
</style>
