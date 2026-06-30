<template>
  <div class="cdn-avatar" :style="wrapperStyle">
    <img
      v-if="imgSrc && !loadError"
      :src="imgSrc"
      :alt="type"
      :style="imgStyle"
      @error="loadError = true"
    />
    <slot v-else>
      <el-icon v-if="icon" :size="iconSize"><component :is="icon" /></el-icon>
      <span v-else class="fallback-text">{{ fallback }}</span>
    </slot>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getAvatarUrl, getDriverAvatarUrl, getAdminAvatarUrl, getAiAvatarUrl } from '@/utils/imageCDN'

const props = defineProps({
  src: { type: String, default: '' },
  seed: { type: String, default: '' },
  type: { type: String, default: 'user' },
  size: { type: Number, default: 40 },
  icon: { type: Object, default: null },
  fallback: { type: String, default: '' },
  iconSize: { type: Number, default: 18 },
  square: { type: Boolean, default: false }
})

const loadError = ref(false)

const imgSrc = computed(() => {
  if (props.src) return props.src
  switch (props.type) {
    case 'driver': return getDriverAvatarUrl(props.seed)
    case 'admin': return getAdminAvatarUrl(props.seed)
    case 'ai': return getAiAvatarUrl()
    default: return getAvatarUrl(props.seed)
  }
})

watch(() => props.src, () => { loadError.value = false })

const wrapperStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
  borderRadius: props.square ? 'var(--radius-sm)' : '50%',
  overflow: 'hidden',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  flexShrink: 0,
  background: 'var(--color-primary-bg)',
  color: 'var(--color-primary)',
  fontSize: `${props.size * 0.45}px`
}))

const imgStyle = computed(() => ({
  width: '100%',
  height: '100%',
  objectFit: 'cover',
  borderRadius: props.square ? 'var(--radius-sm)' : '50%'
}))
</script>

<style scoped>
.cdn-avatar {
  vertical-align: middle;
}
.fallback-text {
  line-height: 1;
  user-select: none;
}
</style>
