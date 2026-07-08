---
name: vue-frontend
description: Vue 3 + Vite + Element Plus + Pinia 前端开发模式。在编写 Vue 组件、设计前端架构、处理路由/状态管理/API调用时自动触发。适用于本项目 jiangnan-travel-web 前端工程。
metadata:
  triggers: Vue, Vue3, Element Plus, Vite, Pinia, 前端, 页面, 组件, UI
  scope: implementation
  output-format: code
  related-skills: rest-api-design, code-review-and-quality, test-driven-development
---

# Vue 3 前端开发

Vue 3 Composition API + Element Plus + Vite 5 + Pinia 前端开发指南，适用于江南出行项目。

## 项目信息

| 项目 | 路径 | 技术栈 |
|------|------|--------|
| 前端工程 | `jiangnan-travel-web/` | Vue 3.4 + Vite 5 + Element Plus 2.7 + Pinia |
| 页面数量 | 41 个页面 | 路由在 `src/router/` 注册 |
| API 模块 | 13 个模块 | 在 `src/api/` 目录 |

## 核心工作流

1. **需求确认** — 明确页面功能、数据来源、交互流程
2. **路由注册** — 在 `src/router/` 注册新页面路由，配置三端鉴权守卫
3. **API 模块** — 在 `src/api/` 添加/更新对应 API 请求函数
4. **状态管理** — 如需要共享状态，在 `src/stores/` 添加 Pinia store
5. **组件开发** — 使用 `<script setup>` 编写页面/组件
6. **样式** — 使用 Element Plus 主题变量 + scoped CSS
7. **验证** — `npm run dev` 启动预览 → 功能验证 → E2E 测试

## 组件开发模板

```vue
<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <el-page-header @back="$router.back()">
      <template #content>{{ pageTitle }}</template>
    </el-page-header>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        @current-change="fetchData"
        layout="total, prev, pager, next"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, deleteItem } from '@/api/example'

// --- Props & Emits ---
const props = defineProps({
  defaultKeyword: { type: String, default: '' }
})

// --- State ---
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ keyword: props.defaultKeyword })
const pagination = reactive({ page: 1, size: 10, total: 0 })

// --- Methods ---
async function fetchData() {
  loading.value = true
  try {
    const { data } = await getList({
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  handleSearch()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除 "${row.name}"？`, '删除确认', {
      type: 'warning'
    })
    await deleteItem(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// --- Lifecycle ---
onMounted(() => fetchData())
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 16px;
}
</style>
```

## API 模块规范

```javascript
// src/api/example.js
import request from '@/utils/request'

// GET 请求
export function getList(params) {
  return request.get('/api/v1/example', { params })
}

// GET 详情
export function getById(id) {
  return request.get(`/api/v1/example/${id}`)
}

// POST 创建
export function create(data) {
  return request.post('/api/v1/example', data)
}

// PUT 更新
export function update(id, data) {
  return request.put(`/api/v1/example/${id}`, data)
}

// DELETE 删除
export function deleteItem(id) {
  return request.delete(`/api/v1/example/${id}`)
}
```

## Pinia Store 规范

```javascript
// src/stores/example.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getList } from '@/api/example'

export const useExampleStore = defineStore('example', () => {
  // State
  const items = ref([])
  const loading = ref(false)

  // Getters
  const activeItems = computed(() => items.value.filter(i => i.status === 1))

  // Actions
  async function fetchItems(params) {
    loading.value = true
    try {
      const { data } = await getList(params)
      items.value = data.records
    } finally {
      loading.value = false
    }
  }

  return { items, loading, activeItems, fetchItems }
})
```

## 路由注册

```javascript
// src/router/index.js 中添加
{
  path: '/example',
  name: 'Example',
  component: () => import('@/views/ExampleManagement.vue'),
  meta: {
    title: '示例管理',
    requiresAuth: true,      // 需要登录
    permissions: ['admin']    // 需要角色权限
  }
}
```

## 约束

### MUST DO
- 使用 `<script setup>` Composition API
- Props 和 Emits 声明类型
- 所有 API 调用通过 `src/api/` 模块
- 表单使用 Element Plus 的 `el-form` + `rules` 验证
- 大列表必须有分页
- 操作前有确认（删除、提交等）
- 异步操作显示 loading 状态
- 路由注册时配置鉴权守卫（`meta.requiresAuth` + `meta.permissions`）

### MUST NOT DO
- 在组件中直接 `import axios` 调用
- 使用 Options API（`data()`, `methods: {}` 等）
- 使用 `v-html` 渲染用户输入
- 状态通过 props 多层传递（用 Pinia 或 provide/inject）
- 遗留 `console.log` 调试代码

## 跨端适配

本项目支持三端（管理端/司机端/乘客端），开发时注意：

- **管理端**：后台管理功能，权限要求最高
- **司机端**：司机接单、导航功能
- **乘客端**：下单、支付、评价功能

路由注册时通过 `meta.permissions` 控制端侧访问权限。

## 验证

- [ ] `npm run dev` 启动无错误
- [ ] 页面正常渲染
- [ ] API 调用返回正确数据
- [ ] 错误状态有 UI 反馈
- [ ] 路由守卫正常工作
- [ ] 响应式布局正常（320-1920px）

## 知识参考

Vue 3 Composition API, Element Plus 2.7, Vite 5, Pinia, Vue Router 4, Axios, SCSS
