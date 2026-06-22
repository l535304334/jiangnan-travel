<template>
  <div class="admin-dashboard">
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="dash-card" :style="{ borderTopColor: c.color }">
          <div class="dash-card-icon">
            <el-icon :size="28" :color="c.color"><component :is="c.icon" /></el-icon>
          </div>
          <div class="dash-card-body">
            <div class="dash-card-value">{{ c.value }}</div>
            <div class="dash-card-label">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="dash-section">
      <h3>数据概览</h3>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="平台注册用户">8,562</el-descriptions-item>
        <el-descriptions-item label="认证司机">326</el-descriptions-item>
        <el-descriptions-item label="累计订单">128,430</el-descriptions-item>
        <el-descriptions-item label="今日新增用户">128</el-descriptions-item>
        <el-descriptions-item label="今日投诉">3</el-descriptions-item>
        <el-descriptions-item label="今日风控告警">5</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { User, Van, Tickets, Warning } from '@element-plus/icons-vue'

const cards = [
  { label: '总用户数', value: '8,562', icon: User, color: '#1890FF' },
  { label: '今日订单', value: '1,285', icon: Tickets, color: '#52C41A' },
  { label: '在线司机', value: '128', icon: Van, color: '#2D8A6E' },
  { label: '风控告警', value: '5', icon: Warning, color: '#FAAD14' }
]
</script>

<style scoped>
.dash-card {
  background: #fff; border-radius: var(--radius-md); padding: 20px;
  display: flex; align-items: center; gap: 16px;
  border-top: 3px solid; box-shadow: var(--shadow-sm);
}
.dash-card-value { font-size: 1.6rem; font-weight: 700; }
.dash-card-label { font-size: 0.85rem; color: var(--color-text-muted); }
.dash-section { margin-top: 24px; }
.dash-section h3 { margin-bottom: 12px; }
</style>
