<template>
  <div class="address-manage">
    <div class="address-header">
      <h3>我的地址</h3>
      <el-button type="primary" size="small" :icon="Plus" @click="showDialog = true">添加地址</el-button>
    </div>

    <div class="address-list">
      <div class="address-card" v-for="item in addresses" :key="item.id">
        <div class="address-tag">{{ item.tag }}</div>
        <div class="address-detail">{{ item.address }}</div>
        <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(item.id)" />
      </div>
      <el-empty v-if="!addresses.length" description="暂无收藏地址" />
    </div>

    <el-dialog v-model="showDialog" title="添加地址" width="90%">
      <el-form :model="form" label-position="top">
        <el-form-item label="标签">
          <el-input v-model="form.tag" placeholder="如：家、公司" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" type="textarea" placeholder="请输入详细地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const addresses = ref([])
const showDialog = ref(false)
const form = reactive({ tag: '', address: '' })

onMounted(async () => {
  try {
    const res = await userApi.getAddresses()
    addresses.value = res.data || []
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  }
})

const handleAdd = async () => {
  try {
    const res = await userApi.addAddress({ tag: form.tag, address: form.address })
    addresses.value.push(res.data || { id: Date.now(), ...form })
    showDialog.value = false
    form.tag = ''
    form.address = ''
    ElMessage.success('添加成功')
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该地址？', '提示', { type: 'warning' })
    await userApi.deleteAddress(id)
    addresses.value = addresses.value.filter(a => a.id !== id)
    ElMessage.success('删除成功')
  } catch (e) {
    if (e === 'cancel') return
    if (e?.message) ElMessage.error(e.message)
  }
}
</script>

<style scoped>
.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.address-header h3 {
  font-size: 1.05rem;
}
.address-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  gap: 12px;
}
.address-tag {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}
.address-detail {
  flex: 1;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
