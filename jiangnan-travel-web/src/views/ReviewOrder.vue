<template>
  <div class="review-page">
    <div class="review-card">
      <h3>评价订单</h3>
      <p class="order-no">订单号：{{ orderNo }}</p>

      <div class="rating-row">
        <label>整体评分</label>
        <el-rate v-model="form.rating" :colors="colors" size="large" />
      </div>

      <div class="tags-row">
        <label>评价标签</label>
        <div class="tag-list">
          <el-check-tag
            v-for="tag in tagOptions"
            :key="tag"
            :checked="form.tags.includes(tag)"
            @change="toggleTag(tag)"
          >{{ tag }}</el-check-tag>
        </div>
      </div>

      <div class="content-row">
        <label>评价内容</label>
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="分享您的乘车体验..."
          maxlength="200"
          show-word-limit
        />
      </div>

      <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSubmit">
        提交评价
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id
const orderNo = ref('')
const submitting = ref(false)

const form = ref({
  rating: 5,
  tags: [],
  content: ''
})

const colors = ['#99A9BF', '#F7BA2A', '#FF9900']
const tagOptions = ['准时到达', '车辆整洁', '服务态度好', '驾驶平稳', '路线合理']

const toggleTag = (tag) => {
  const idx = form.value.tags.indexOf(tag)
  if (idx === -1) {
    form.value.tags.push(tag)
  } else {
    form.value.tags.splice(idx, 1)
  }
}

onMounted(async () => {
  try {
    const res = await orderApi.detail(orderId)
    orderNo.value = res.data?.orderNo || ''
  } catch (e) {
    ElMessage.error('加载订单信息失败')
  }
})

const handleSubmit = async () => {
  if (!form.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  submitting.value = true
  try {
    await orderApi.review(orderId, {
      rating: form.value.rating,
      tags: form.value.tags.join(','),
      content: form.value.content
    })
    ElMessage.success('评价成功')
    router.push(`/order/${orderId}`)
  } catch (e) {
    ElMessage.error(e?.message || '评价失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.review-page { padding: 16px; background: #f5f5f5; min-height: calc(100vh - 120px); }
.review-card { background: #fff; border-radius: 12px; padding: 20px; }
.review-card h3 { margin: 0 0 6px; }
.order-no { font-size: 0.8rem; color: var(--color-text-muted); margin: 0 0 20px; }
.rating-row, .tags-row, .content-row { margin-bottom: 20px; }
.rating-row label, .tags-row label, .content-row label {
  display: block; font-size: 0.85rem; color: var(--color-text-secondary); margin-bottom: 10px;
}
.tag-list { display: flex; flex-wrap: wrap; gap: 8px; }
.submit-btn { width: 100%; }
</style>
