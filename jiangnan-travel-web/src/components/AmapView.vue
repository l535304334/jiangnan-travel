<template>
  <div class="amap-container">
    <div ref="mapContainer" class="map-div">
      <div class="map-fallback" v-if="loading || failed">
        <div class="fallback-icon">📍</div>
        <div class="fallback-text">{{ failed ? '地图加载失败' : '地图加载中...' }}</div>
        <div class="fallback-info" v-if="markers.length > 0">
          <div v-for="(m, i) in markers" :key="i" class="marker-info">
            {{ m.title || `标记点 ${i+1}` }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  center: { type: Array, default: () => [115.8759, 28.6842] },
  markers: { type: Array, default: () => [] },
  zoom: { type: Number, default: 14 }
})

const mapContainer = ref(null)
const loading = ref(true)
const failed = ref(false)
let map = null
let intervalId = null

const tryInit = () => {
  if (!window.AMap || !window.AMap.Map) { loading.value = true; return }
  loading.value = false
  try {
    if (!mapContainer.value || map) return
    map = new window.AMap.Map(mapContainer.value, {
      center: props.center, zoom: props.zoom, resizeEnable: true
    })
    failed.value = false
    renderMarkers()
  } catch (e) {
    console.warn('Map init failed:', e)
    failed.value = true; loading.value = false
  }
}

onMounted(() => {
  window._AMapSecurityConfig = { securityJsCode: '01646b6ce6b541c2057afc03bb2ea17a' }
  if (window.AMap) { tryInit(); return }
  const script = document.createElement('script')
  script.src = 'https://webapi.amap.com/maps?v=2.0&key=4b0b6a9db2b2a3f10ccfecd53afa7db9&callback=amapOnLoad'
  window.amapOnLoad = () => { tryInit(); if (intervalId) clearInterval(intervalId) }
  script.onerror = () => { failed.value = true; loading.value = false }
  document.head.appendChild(script)
  intervalId = setInterval(tryInit, 2000)
})

watch(() => props.markers, () => { if (map) renderMarkers() }, { deep: true })
watch(() => props.center, (v) => { if (map) map.setCenter(v) })

const renderMarkers = () => {
  if (!map) return
  map.clearMap()
  props.markers.forEach(m => {
    if (m.lng && m.lat) {
      new window.AMap.Marker({ position: [m.lng, m.lat], title: m.title || '', map })
    }
  })
}

onUnmounted(() => { if (intervalId) clearInterval(intervalId); if (map) { map.destroy(); map = null } })
</script>

<style scoped>
.amap-container { width: 100%; height: 100%; border-radius: 8px; overflow: hidden; background: #f0ede5; position: relative; min-height: 200px; }
.map-div { width: 100%; height: 100%; }
.map-fallback {
  position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 8px;
  background: linear-gradient(135deg, #f5f3ee 0%, #e8f5ee 100%);
}
.fallback-icon { font-size: 2rem; }
.fallback-text { font-size: 0.9rem; color: var(--color-text-muted); }
.fallback-info { margin-top: 8px; font-size: 0.8rem; }
.marker-info { padding: 4px 12px; background: rgba(255,255,255,0.8); border-radius: 20px; margin: 4px 0; }
</style>
