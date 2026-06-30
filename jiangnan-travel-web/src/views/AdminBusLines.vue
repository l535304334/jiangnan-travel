<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h3>班线管理</h3>
      <el-button type="primary" @click="openForm(null)">新建班线</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe :border="false">
        <el-table-column prop="lineName" label="班线名称" width="150" />
        <el-table-column prop="startCity" label="出发" width="80" />
        <el-table-column label="→" width="30" align="center" />
        <el-table-column prop="endCity" label="到达" width="80" />
        <el-table-column prop="busType" label="车型" width="80">
          <template #default="{ row }">{{ row.busType === 'express' ? '快线' : '豪华大巴' }}</template>
        </el-table-column>
        <el-table-column prop="price" label="票价" width="70" />
        <el-table-column prop="duration" label="时长(分)" width="80" />
        <el-table-column prop="status" label="状态" width="60">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '运营' : '停运' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openSchedules(row)">班次</el-button>
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination-wrap" v-if="total > 0">
        <el-pagination
          :total="total"
          v-model:current-page="page"
          :page-size="20"
          layout="total, prev, pager, next, jumper"
          background
          small
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="editing ? '编辑班线' : '新建班线'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="班线名称" prop="lineName"><el-input v-model="form.lineName" placeholder="如 南昌→九江" /></el-form-item>
        <el-form-item label="出发城市" prop="startCity"><el-input v-model="form.startCity" /></el-form-item>
        <el-form-item label="到达城市" prop="endCity"><el-input v-model="form.endCity" /></el-form-item>
        <el-form-item label="车型" prop="busType">
          <el-select v-model="form.busType">
            <el-option label="豪华大巴" value="regular" />
            <el-option label="快线" value="express" />
          </el-select>
        </el-form-item>
        <el-form-item label="票价(元)" prop="price"><el-input-number v-model="form.price" :min="0" :step="10" /></el-form-item>
        <el-form-item label="时长(分)" prop="duration"><el-input-number v-model="form.duration" :min="10" :step="10" /></el-form-item>
        <el-form-item label="里程(km)" prop="distance"><el-input-number v-model="form.distance" :min="0" :step="10" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="运营" inactive-text="停运" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 班次管理弹窗 -->
    <el-dialog v-model="scheduleVisible" :title="'班次管理 - ' + (currentLine?.lineName)" width="600px">
      <div class="schedule-actions">
        <el-button type="success" @click="openScheduleForm(null)">新增班次</el-button>
      </div>
      <el-table :data="schedules" stripe :border="false">
        <el-table-column label="发车" prop="departTime" width="80" />
        <el-table-column label="到达" prop="arriveTime" width="80" />
        <el-table-column label="总票" prop="ticketCount" width="60" />
        <el-table-column label="余票" prop="remaining" width="60" />
        <el-table-column label="状态" width="60">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '运营' : '停运' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button size="small" link @click="openScheduleForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="deleteSchedule(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 班次编辑弹窗 -->
    <el-dialog v-model="schedFormVisible" :title="editingSched ? '编辑班次' : '新增班次'" width="400px">
      <el-form :model="schedForm" label-width="80px">
        <el-form-item label="发车时间"><el-time-picker v-model="schedForm.departTime" format="HH:mm" value-format="HH:mm:ss" placeholder="选择时间" /></el-form-item>
        <el-form-item label="到达时间"><el-time-picker v-model="schedForm.arriveTime" format="HH:mm" value-format="HH:mm:ss" placeholder="选择时间" /></el-form-item>
        <el-form-item label="总票数"><el-input-number v-model="schedForm.ticketCount" :min="1" :max="200" /></el-form-item>
        <el-form-item label="余票"><el-input-number v-model="schedForm.remaining" :min="0" :max="200" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="schedForm.status" :active-value="1" :inactive-value="0" active-text="运营" inactive-text="停运" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="schedFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleScheduleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const formVisible = ref(false)
const editing = ref(null)
const submitting = ref(false)
const formRef = ref()
const form = ref({ lineName: '', startCity: '', endCity: '', busType: 'regular', price: 0, duration: 60, distance: 0, status: 1 })
const rules = {
  lineName: [{ required: true, message: '请输入班线名称' }],
  startCity: [{ required: true }],
  endCity: [{ required: true }]
}

// 班次管理
const scheduleVisible = ref(false)
const currentLine = ref(null)
const schedules = ref([])
const schedFormVisible = ref(false)
const editingSched = ref(null)
const schedForm = ref({ lineId: 0, departTime: '', arriveTime: '', ticketCount: 45, remaining: 45, status: 1 })

onMounted(() => load())

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const res = await adminApi.busLines({ page: page.value, size: 20 })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {}
  loading.value = false
}

function openForm(row) {
  editing.value = row
  if (row) {
    form.value = { lineName: row.lineName, startCity: row.startCity, endCity: row.endCity,
      busType: row.busType || 'regular', price: row.price, duration: row.duration,
      distance: row.distance || 0, status: row.status ?? 1 }
  } else {
    form.value = { lineName: '', startCity: '', endCity: '', busType: 'regular', price: 0, duration: 60, distance: 0, status: 1 }
  }
  formVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editing.value) {
      await adminApi.updateBusLine(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminApi.createBusLine(form.value)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    await load()
  } catch {}
  submitting.value = false
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该班线？', '提示')
    await adminApi.deleteBusLine(id)
    ElMessage.success('已删除')
    await load()
  } catch {}
}

async function openSchedules(line) {
  currentLine.value = line
  scheduleVisible.value = true
  try {
    const res = await adminApi.busSchedules(line.id)
    schedules.value = res.data || []
  } catch {}
}

function openScheduleForm(sched) {
  editingSched.value = sched
  if (sched) {
    schedForm.value = { lineId: currentLine.value.id, departTime: sched.departTime, arriveTime: sched.arriveTime,
      ticketCount: sched.ticketCount, remaining: sched.remaining, status: sched.status ?? 1 }
  } else {
    schedForm.value = { lineId: currentLine.value.id, departTime: '08:00:00', arriveTime: '10:00:00',
      ticketCount: 45, remaining: 45, status: 1 }
  }
  schedFormVisible.value = true
}

async function handleScheduleSubmit() {
  submitting.value = true
  try {
    if (editingSched.value) {
      await adminApi.updateBusSchedule(editingSched.value.id, schedForm.value)
      ElMessage.success('更新成功')
    } else {
      await adminApi.createBusSchedule(schedForm.value)
      ElMessage.success('创建成功')
    }
    schedFormVisible.value = false
    const res = await adminApi.busSchedules(currentLine.value.id)
    schedules.value = res.data || []
  } catch {}
  submitting.value = false
}

async function deleteSchedule(id) {
  try {
    await ElMessageBox.confirm('确定删除该班次？', '提示')
    await adminApi.deleteBusSchedule(id)
    ElMessage.success('已删除')
    const res = await adminApi.busSchedules(currentLine.value.id)
    schedules.value = res.data || []
  } catch {}
}
</script>

<style scoped>
.schedule-actions { margin-bottom: var(--spacing-md); }
</style>
