<template>
  <div class="bus-page app-page" v-loading="loading">
    <!-- 城市筛选 -->
    <div class="city-filter app-card">
      <div class="city-swap">
        <el-select v-model="startCity" placeholder="出发城市" size="small" clearable @change="loadLines">
          <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button :icon="Refresh" circle size="small" @click="swapCities" />
        <el-select v-model="endCity" placeholder="到达城市" size="small" clearable @change="loadLines">
          <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
        </el-select>
      </div>
    </div>

    <!-- 班线列表 -->
    <TransitionGroup name="list-fade" tag="div" class="line-list" v-if="lines.length > 0">
      <div class="card-list-item" v-for="line in lines" :key="line.id" @click="showDetail(line)">
        <div class="line-cities">
          <span class="city-name">{{ line.startCity }}</span>
          <span class="arrow">→</span>
          <span class="city-name">{{ line.endCity }}</span>
          <el-tag size="small" class="type-tag">{{ line.busTypeName }}</el-tag>
        </div>
        <div class="line-meta">
          <span class="line-name">{{ line.lineName }}</span>
          <span class="duration">{{ Math.floor(line.duration / 60) }}h{{ line.duration % 60 > 0 ? (line.duration % 60) + 'min' : '' }}</span>
        </div>
        <div class="line-footer">
          <span class="price">¥{{ line.price }}</span>
          <span class="distance">{{ line.distance }}km</span>
        </div>
      </div>
    </TransitionGroup>
    <el-empty v-else description="暂无班线" :image-size="60" />

    <!-- 班线详情弹窗（含时刻表） -->
    <el-dialog v-model="detailVisible" :title="currentLine?.lineName" width="92%" :close-on-click-modal="false">
      <div v-if="currentLine">
        <div class="detail-info">
          <div class="detail-row"><span>车型</span><span>{{ currentLine.busTypeName }}</span></div>
          <div class="detail-row"><span>时长</span><span>{{ Math.floor(currentLine.duration / 60) }}h{{ currentLine.duration % 60 }}min</span></div>
          <div class="detail-row"><span>里程</span><span>{{ currentLine.distance }}km</span></div>
          <div class="detail-row"><span>票价</span><span class="price">¥{{ currentLine.price }}</span></div>
        </div>
        <p class="schedule-title">时刻表</p>
        <div class="schedule-list" v-if="currentLine.schedules?.length">
          <div class="schedule-item" v-for="s in currentLine.schedules" :key="s.id">
            <div class="schedule-time">
              <span class="depart">{{ s.departTime }}</span>
              <span class="arrow">→</span>
              <span class="arrive">{{ s.arriveTime }}</span>
            </div>
            <div class="schedule-ticket">
              <span :class="{ 'low-ticket': s.remaining <= 5 }">{{ s.remaining }}张</span>
            </div>
            <el-button size="small" type="primary"
                       :disabled="s.remaining <= 0"
                       :loading="purchasingId === s.id"
                       @click.stop="handlePurchase(s)">
              {{ s.remaining <= 0 ? '已售罄' : '购票' }}
            </el-button>
          </div>
        </div>
        <div v-else class="empty-state"><el-empty description="暂无时刻表" /></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { busApi } from '@/api/bus'

const cities = ['南昌', '九江', '赣州', '景德镇', '吉安', '上饶', '抚州', '宜春', '鹰潭', '萍乡', '新余']
const startCity = ref('')
const endCity = ref('')
const loading = ref(true)
const lines = ref([])

const detailVisible = ref(false)
const currentLine = ref(null)
const purchasingId = ref(0)

onMounted(() => loadLines())

async function loadLines() {
  loading.value = true
  try {
    const res = await busApi.listLines(startCity.value, endCity.value)
    lines.value = res.data || []
  } catch (e) {
    ElMessage.error('加载班线失败')
  } finally {
    loading.value = false
  }
}

function swapCities() {
  const tmp = startCity.value
  startCity.value = endCity.value
  endCity.value = tmp
  loadLines()
}

async function showDetail(line) {
  try {
    const res = await busApi.lineDetail(line.id)
    currentLine.value = res.data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

async function handlePurchase(schedule) {
  purchasingId.value = schedule.id
  try {
    const res = await busApi.purchase(schedule.id)
    schedule.remaining = res.data.remaining
    ElMessage.success('购票成功！余票 ' + res.data.remaining + ' 张')
  } catch (e) {
    // 错误已由响应拦截器处理
  } finally {
    purchasingId.value = 0
  }
}
</script>

<style scoped>
.bus-page { min-height: calc(100vh - 120px); }

.city-filter { margin-bottom: 12px; }
.city-swap { display: flex; align-items: center; gap: 8px; }
.city-swap :deep(.el-select) { flex: 1; }

.line-list { display: flex; flex-direction: column; gap: 10px; }
.line-cities { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.city-name { font-size: 1rem; font-weight: 600; }
.arrow { color: var(--color-text-muted); font-size: 0.9rem; }
.type-tag { margin-left: auto; }
.line-meta { display: flex; justify-content: space-between; font-size: 0.82rem; color: var(--color-text-muted); }
.line-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.price { font-size: 1.15rem; font-weight: 700; color: var(--color-primary); }
.distance { font-size: 0.78rem; color: var(--color-text-muted); }

.detail-info { background: #f9f9f9; border-radius: 10px; padding: 12px; margin-bottom: 12px; }
.detail-row { display: flex; justify-content: space-between; padding: 4px 0; font-size: 0.85rem; }
.schedule-title { font-size: 0.9rem; font-weight: 600; margin: 0 0 8px; }
.schedule-list { display: flex; flex-direction: column; gap: 8px; }
.schedule-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 12px; background: #f9f9f9; border-radius: 8px;
}
.schedule-time { display: flex; align-items: center; gap: 6px; }
.depart, .arrive { font-weight: 600; font-size: 0.9rem; }
.schedule-time .arrow { font-size: 0.75rem; }
.schedule-ticket { font-size: 0.82rem; }
.low-ticket { color: #f44336; font-weight: 600; }
</style>
