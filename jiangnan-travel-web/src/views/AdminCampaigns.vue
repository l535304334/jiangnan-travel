<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h3>活动管理</h3>
      <el-button type="primary" @click="openForm(null)">新建活动</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe :border="false">
        <el-table-column prop="name" label="活动名称" min-width="140" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">{{ row.type === 0 ? '通用' : '新用户' }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="200">
          <template #default="{ row }">{{ row.startTime?.replace('T',' ') }} ~ {{ row.endTime?.replace('T',' ') }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination-wrap" v-if="total > 0">
        <el-pagination
          :total="total"
          v-model:current-page="page"
          :page-size="10"
          layout="total, prev, pager, next, jumper"
          background
          small
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="editing ? '编辑活动' : '新建活动'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="横幅图">
          <div class="banner-actions">
            <el-upload :auto-upload="false" :limit="1" accept="image/*" :show-file-list="false"
                       :on-change="(f) => form.bannerUrl = URL.createObjectURL(f.raw)">
              <el-button>上传</el-button>
            </el-upload>
            <el-button link type="primary" @click="form.bannerUrl = getCampaignBannerUrl(form.name)">
              生成默认 Banner
            </el-button>
          </div>
          <el-image v-if="form.bannerUrl" :src="form.bannerUrl" class="banner-preview" fit="cover" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">通用</el-radio>
            <el-radio :value="1">新用户专享</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择日期" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择日期" value-format="YYYY-MM-DDTHH:mm:ss" />
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
import { getCampaignBannerUrl } from '@/utils/imageCDN'

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const formVisible = ref(false)
const editing = ref(null)
const submitting = ref(false)
const formRef = ref()

const form = ref({
  name: '', description: '', bannerUrl: '', type: 0,
  startTime: '', endTime: '',
  couponIds: []
})
const rules = {
  name: [{ required: true, message: '请输入活动名称' }],
  description: [{ required: true, message: '请输入活动描述' }],
  startTime: [{ required: true, message: '请选择开始时间' }],
  endTime: [{ required: true, message: '请选择结束时间' }]
}

onMounted(() => load())

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const res = await adminApi.campaigns({ page: page.value, size: 10 })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {}
  loading.value = false
}

function openForm(row) {
  editing.value = row
  if (row) {
    form.value = {
      name: row.name, description: row.description, bannerUrl: row.bannerUrl || '',
      type: row.type ?? 0, startTime: row.startTime, endTime: row.endTime, couponIds: []
    }
  } else {
    form.value = { name: '', description: '', bannerUrl: '', type: 0, startTime: '', endTime: '', couponIds: [] }
  }
  formVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editing.value) {
      await adminApi.updateCampaign(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminApi.createCampaign(form.value)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    await load()
  } catch {}
  submitting.value = false
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该活动？', '提示')
    await adminApi.deleteCampaign(id)
    ElMessage.success('已删除')
    await load()
  } catch {}
}
</script>

<style scoped>
.banner-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.banner-preview {
  width: 100%;
  height: 120px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}
</style>
