<template>
  <div class="quote-wall app-page" v-loading="loading" element-loading-text="加载中...">
    <div class="quote-card app-card" v-for="q in quotes" :key="q.id">
      <div class="quote-content">"{{ q.content }}"</div>
      <div class="quote-footer">
        <span class="quote-author">—— {{ q.author || '匿名' }}</span>
        <el-tag size="small">{{ q.city }}</el-tag>
      </div>
    </div>
    <el-empty v-if="!loading && quotes.length === 0" description="暂无寄语" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { aiApi } from '@/api/ai'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const quotes = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await aiApi.getCityQuotes()
    if (res.code === 200) quotes.value = res.data || []
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.quote-card {
  padding: 14px 16px;
}
.quote-content {
  font-size: 0.9rem;
  font-style: italic;
  color: var(--color-text);
  line-height: 1.6;
  margin-bottom: 8px;
}
.quote-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.quote-author {
  font-size: 0.78rem;
  color: var(--color-text-muted);
}
</style>
