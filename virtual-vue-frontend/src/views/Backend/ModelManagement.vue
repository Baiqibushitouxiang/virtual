<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">后台管理</p>
        <h1 class="backend-page__title">模型管理</h1>
        <p class="backend-page__subtitle">统一维护模型资料和上传入口，保留统一表格、表单和分页风格。</p>
      </div>
      <div class="backend-page__hero-actions">
        <el-button type="primary" @click="showAddDialog">新增模型</el-button>
        <el-button type="success" @click="showUploadDialog">上传模型</el-button>
        <el-button @click="loadModels">刷新</el-button>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">模型列表</h2>
            <p class="backend-page__section-desc">支持按模型名称、分类和描述搜索。</p>
          </div>
          <div class="backend-page__actions">
            <el-input v-model="searchText" clearable placeholder="搜索模型" style="width: 240px" @input="resetPage" />
          </div>
        </div>
      </template>

      <el-table :data="pagedModels" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模型名称" min-width="160" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="filePath" label="文件路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editModel(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteModelConfirm(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '新增模型'" width="520px" class="backend-page__dialog">
      <el-form :model="form" label-width="96px" class="backend-form">
        <el-form-item label="模型名称">
          <el-input v-model="form.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="form.filePath" placeholder="可选，填写模型资源路径" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="可选，补充模型用途说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" title="上传模型" width="520px" class="backend-page__dialog">
      <el-form :model="uploadForm" label-width="96px" class="backend-form">
        <el-form-item label="分类">
          <el-select v-model="uploadForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="uploadForm.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".glb,.gltf,.fbx,.obj"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持类型：.glb、.gltf、.fbx、.obj</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="uploading" @click="submitUpload">上传</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createModel, deleteModel, getAllModels, updateModel, uploadModel } from '@/api/index.js'
import { useLocalPagination } from '@/composables/useLocalPagination'

const categories = ['建筑', '设备', '管道', '阀门', '其他']

const models = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const editId = ref(null)
const uploadRef = ref(null)
const selectedFile = ref(null)
const searchText = ref('')

const form = ref({ name: '', category: '', filePath: '', description: '' })
const uploadForm = ref({ name: '', category: '' })

const filteredModels = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) return models.value
  return models.value.filter((item) => [item.name, item.category, item.description].filter(Boolean).join(' ').toLowerCase().includes(keyword))
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedModels, resetPage } = useLocalPagination(filteredModels, { pageSize: 10 })

const resetForm = () => {
  form.value = { name: '', category: '', filePath: '', description: '' }
  editId.value = null
  isEdit.value = false
}

const loadModels = async () => {
  loading.value = true
  try {
    const response = await getAllModels()
    models.value = response.data || []
  } catch (error) {
    ElMessage.error('加载模型列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const editModel = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    name: row.name,
    category: row.category,
    filePath: row.filePath,
    description: row.description || ''
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name || !form.value.category) {
    ElMessage.warning('请填写模型名称和分类')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateModel(editId.value, form.value)
    } else {
      await createModel(form.value)
    }
    ElMessage.success('模型已保存')
    dialogVisible.value = false
    await loadModels()
  } catch (error) {
    ElMessage.error('保存模型失败')
  } finally {
    submitting.value = false
  }
}

const deleteModelConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除模型“${row.name}”吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteModel(row.id)
    ElMessage.success('模型已删除')
    await loadModels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除模型失败')
    }
  }
}

const showUploadDialog = () => {
  uploadForm.value = { name: '', category: '' }
  selectedFile.value = null
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  if (!uploadForm.value.name && file.name) {
    uploadForm.value.name = file.name.replace(/\.[^.]+$/, '')
  }
}

const submitUpload = async () => {
  if (!uploadForm.value.name || !uploadForm.value.category) {
    ElMessage.warning('请填写模型名称和分类')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请先选择模型文件')
    return
  }

  uploading.value = true
  try {
    await uploadModel(uploadForm.value.category, uploadForm.value.name, selectedFile.value)
    ElMessage.success('模型已上传')
    uploadDialogVisible.value = false
    await loadModels()
  } catch (error) {
    ElMessage.error('上传模型失败')
  } finally {
    uploading.value = false
  }
}

onMounted(loadModels)
</script>

<style scoped>
.upload-tip {
  color: #667085;
}
</style>
