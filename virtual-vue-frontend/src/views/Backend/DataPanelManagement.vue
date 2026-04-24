<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">后台管理</p>
        <h1 class="backend-page__title">展板管理</h1>
        <p class="backend-page__subtitle">统一维护展板信息和展示状态，支持搜索与本地分页。</p>
      </div>
      <div class="backend-page__hero-actions">
        <el-button type="primary" @click="showAddDialog">新增展板</el-button>
        <el-button @click="loadPanels">刷新</el-button>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">展板列表</h2>
            <p class="backend-page__section-desc">可查看展板名称、模型绑定、状态和位置。</p>
          </div>
          <div class="backend-page__actions">
            <el-input v-model="searchText" clearable placeholder="搜索展板" style="width: 220px" @input="resetPage" />
          </div>
        </div>
      </template>

      <el-table :data="pagedPanels" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="展板名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="绑定模型" min-width="160">
          <template #default="{ row }">
            <span v-if="row.modelName">{{ row.modelName }}</span>
            <el-tag v-else type="info" size="small">未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editPanel(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deletePanelConfirm(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="backend-page__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="pageSizes"
          :total="total"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑展板' : '新增展板'" width="520px" class="backend-page__dialog">
      <el-form :model="form" label-width="96px" class="backend-form">
        <el-form-item label="展板名称">
          <el-input v-model="form.name" placeholder="请输入展板名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="可选，补充展板用途说明" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createDataPanel, deleteDataPanel, getDataPanels, updateDataPanel } from '@/api/dataPanel'
import { useLocalPagination } from '@/composables/useLocalPagination'

const panels = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref(null)
const searchText = ref('')
const form = ref({ name: '', description: '', status: 1 })

const filteredPanels = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) return panels.value
  return panels.value.filter((item) => [item.name, item.description, item.modelName].filter(Boolean).join(' ').toLowerCase().includes(keyword))
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedPanels, resetPage } = useLocalPagination(filteredPanels, { pageSize: 10 })

const resetForm = () => {
  form.value = { name: '', description: '', status: 1 }
  editId.value = null
  isEdit.value = false
}

const loadPanels = async () => {
  loading.value = true
  try {
    const response = await getDataPanels()
    panels.value = response.data || response || []
  } catch (error) {
    ElMessage.error('加载展板列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const editPanel = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { name: row.name, description: row.description || '', status: row.status }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请填写展板名称')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateDataPanel(editId.value, form.value)
    } else {
      await createDataPanel(form.value)
    }
    ElMessage.success('展板已保存')
    dialogVisible.value = false
    await loadPanels()
  } catch (error) {
    ElMessage.error('保存展板失败')
  } finally {
    submitting.value = false
  }
}

const deletePanelConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除展板“${row.name}”吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDataPanel(row.id)
    ElMessage.success('展板已删除')
    await loadPanels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除展板失败')
    }
  }
}

onMounted(loadPanels)
</script>
