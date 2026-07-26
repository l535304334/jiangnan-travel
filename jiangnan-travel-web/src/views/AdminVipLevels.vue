<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h3>VIP等级管理</h3>
      <el-button type="primary" @click="openForm(null)">新建等级</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe :border="false">
        <el-table-column prop="level" label="等级" width="60" align="center">
          <template #default="{ row }">
            <el-tag :color="levelColor(row.level)" style="color:#fff">Lv{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="80" />
        <el-table-column prop="discount" label="折扣率" width="90">
          <template #default="{ row }">{{ row.discount }}折</template>
        </el-table-column>
        <el-table-column prop="monthlyFee" label="月费(元)" width="100" />
        <el-table-column prop="yearlyFee" label="年费(元)" width="100" />
        <el-table-column prop="minSpend" label="累计消费门槛" min-width="120" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination-wrap" v-if="list.length > 0">
        <el-pagination
          :total="list.length"
          :page-size="list.length"
          layout="total, prev, pager, next"
          background
          small
          hide-on-single-page
        />
      </div>
    </el-card>

    <el-dialog v-model="formVisible" :title="editing ? '编辑等级' : '新建等级'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级(Lv)" prop="level">
          <el-input-number v-model="form.level" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="折扣率" prop="discount">
          <el-input-number v-model="form.discount" :min="0.5" :max="1" :step="0.05" />
          <span style="margin-left:6px;font-size:0.78rem;color:#888">如0.95表示95折</span>
        </el-form-item>
        <el-form-item label="月费(元)" prop="monthlyFee">
          <el-input-number v-model="form.monthlyFee" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="年费(元)" prop="yearlyFee">
          <el-input-number v-model="form.yearlyFee" :min="0" :step="50" />
        </el-form-item>
        <el-form-item label="消费门槛" prop="minSpend">
          <el-input-number v-model="form.minSpend" :min="0" :step="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
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
const formVisible = ref(false)
const editing = ref(null)
const submitting = ref(false)
const formRef = ref()

const form = ref({ level: 1, name: '', discount: 0.95, monthlyFee: 0, yearlyFee: 0, minSpend: 0 })
const rules = {
  level: [{ required: true, message: '请输入等级' }],
  name: [{ required: true, message: '请输入名称' }],
  discount: [{ required: true, message: '请设置折扣率' }]
}

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await adminApi.vipLevels()
    list.value = res.data || []
  } catch {}
  loading.value = false
}

function levelColor(lv) {
  const colors = ['#909399','#a0a0a0','#cd7f32','#C0C0C0','#FFD700']
  return colors[lv - 1] || '#FFD700'
}

function openForm(row) {
  editing.value = row
  if (row) {
    form.value = {
      level: row.level, name: row.name, discount: row.discount,
      monthlyFee: row.monthlyFee, yearlyFee: row.yearlyFee, minSpend: row.minSpend
    }
  } else {
    form.value = { level: 1, name: '', discount: 0.95, monthlyFee: 0, yearlyFee: 0, minSpend: 0 }
  }
  formVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editing.value) {
      await adminApi.updateVipLevel(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminApi.createVipLevel(form.value)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    await load()
  } catch {}
  submitting.value = false
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该VIP等级？', '提示')
    await adminApi.deleteVipLevel(id)
    ElMessage.success('已删除')
    await load()
  } catch {}
}
</script>

<style scoped>
</style>
