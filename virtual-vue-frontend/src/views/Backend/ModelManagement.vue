<template>
  <div class="model-page">
    <section class="model-header">
      <div>
        <p class="model-header__eyebrow">资源管理</p>
        <h1>模型管理</h1>
        <p>按业务分类查找模型，选中模型后在右侧查看文件、路径和维护操作。</p>
      </div>

      <div class="model-header__actions">
        <el-button type="primary" :icon="Plus" @click="showAddDialog">新增模型</el-button>
        <el-button type="success" :icon="Upload" @click="showUploadDialog">上传模型</el-button>
        <el-button :icon="Refresh" @click="loadModelData">刷新</el-button>
      </div>
    </section>

    <section class="model-layout">
      <aside class="catalog-panel">
        <div class="panel-title">
          <div>
            <h2>分类目录</h2>
            <span>{{ models.length }} 个模型，{{ categoryRows.length }} 个分类</span>
          </div>
        </div>

        <el-input
          v-model="categoryKeyword"
          clearable
          placeholder="搜索分类"
          :prefix-icon="Search"
          class="catalog-panel__search"
        />

        <div class="catalog-panel__list">
          <button
            class="catalog-row"
            :class="{ 'is-active': selectedCategory === '' }"
            type="button"
            @click="selectCategory('')"
          >
            <span class="catalog-row__icon">
              <el-icon><Files /></el-icon>
            </span>
            <span class="catalog-row__name">全部模型</span>
            <span class="catalog-row__count">{{ models.length }}</span>
          </button>

          <button
            v-for="row in visibleCategoryRows"
            :key="row.path"
            class="catalog-row"
            :class="{ 'is-active': selectedCategory === row.path }"
            :style="{ '--level': row.level }"
            type="button"
            @click="selectCategory(row.path)"
          >
            <span class="catalog-row__toggle" @click.stop="toggleCategory(row.path)">
              <el-icon v-if="row.children.length">
                <ArrowDown v-if="isExpanded(row.path)" />
                <ArrowRight v-else />
              </el-icon>
            </span>
            <span class="catalog-row__icon">
              <el-icon><FolderOpened /></el-icon>
            </span>
            <span class="catalog-row__name">{{ row.name }}</span>
            <span class="catalog-row__count">{{ row.modelCount }}</span>
          </button>
        </div>
      </aside>

      <main class="model-list-panel">
        <div class="model-list-panel__header">
          <div>
            <div class="breadcrumb-line">
              <span>当前位置</span>
              <strong>{{ selectedCategory || '全部模型' }}</strong>
            </div>
            <h2>{{ selectedCategoryName }}</h2>
          </div>
          <el-input
            v-model="modelKeyword"
            clearable
            placeholder="搜索模型名称、路径或描述"
            :prefix-icon="Search"
            class="model-list-panel__search"
          />
        </div>

        <div class="model-summary">
          <div>
            <span>当前范围</span>
            <strong>{{ scopedModels.length }}</strong>
          </div>
          <div>
            <span>筛选结果</span>
            <strong>{{ filteredModels.length }}</strong>
          </div>
          <div>
            <span>已选择</span>
            <strong>{{ selectedModel?.name || '无' }}</strong>
          </div>
        </div>

        <el-scrollbar class="model-list">
          <button
            v-for="model in filteredModels"
            :key="model.id"
            class="model-card"
            :class="{ 'is-active': selectedModel?.id === model.id }"
            type="button"
            @click="selectModel(model)"
          >
            <span class="model-card__file">
              <el-icon><Document /></el-icon>
            </span>
            <span class="model-card__content">
              <strong>{{ model.name }}</strong>
              <span>{{ model.category || '未分类' }}</span>
              <small>{{ model.filePath }}</small>
            </span>
            <el-icon class="model-card__arrow"><ArrowRight /></el-icon>
          </button>

          <el-empty v-if="!loading && filteredModels.length === 0" description="没有找到匹配的模型" />
        </el-scrollbar>
      </main>

      <aside class="detail-panel" v-loading="loading">
        <template v-if="selectedModel">
          <div class="detail-panel__top">
            <span class="detail-panel__file-icon">
              <el-icon><Document /></el-icon>
            </span>
            <div>
              <p>模型文件</p>
              <h2>{{ selectedModel.name }}</h2>
            </div>
          </div>

          <el-descriptions :column="1" border class="detail-panel__descriptions">
            <el-descriptions-item label="模型 ID">{{ selectedModel.id }}</el-descriptions-item>
            <el-descriptions-item label="分类路径">{{ selectedModel.category || '未分类' }}</el-descriptions-item>
            <el-descriptions-item label="文件路径">{{ selectedModel.filePath || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="访问地址">
              <a v-if="selectedModel.url" :href="selectedModel.url" target="_blank" rel="noreferrer">
                <el-icon><Link /></el-icon>
                打开模型文件
              </a>
              <span v-else>暂无</span>
            </el-descriptions-item>
            <el-descriptions-item label="描述">{{ selectedModel.description || '暂无描述' }}</el-descriptions-item>
          </el-descriptions>

          <div class="detail-panel__actions">
            <el-button type="primary" :icon="Edit" @click="editModel(selectedModel)">编辑模型</el-button>
            <el-button type="danger" plain :icon="Delete" @click="deleteModelConfirm(selectedModel)">删除</el-button>
          </div>
        </template>

        <div v-else class="detail-empty">
          <el-icon><Document /></el-icon>
          <h2>选择一个模型查看详情</h2>
          <p>先在左侧选分类，再从中间列表里选择模型。</p>
        </div>
      </aside>
    </section>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '新增模型'" width="540px">
      <el-form :model="form" label-width="96px" class="backend-form">
        <el-form-item label="模型名称">
          <el-input v-model="form.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="分类路径">
          <el-select v-model="form.category" filterable allow-create placeholder="例如：水务水利/监测设备" style="width: 100%">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="form.filePath" placeholder="例如：/models/水务水利/监测设备/水位监测设备.glb" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="补充模型用途、来源或说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" title="上传模型" width="540px">
      <el-form :model="uploadForm" label-width="96px" class="backend-form">
        <el-form-item label="分类路径">
          <el-select v-model="uploadForm.category" filterable allow-create placeholder="选择或输入分类路径" style="width: 100%">
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
            :on-remove="handleFileRemove"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 glb、gltf、fbx、obj 文件</div>
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
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  ArrowRight,
  Delete,
  Document,
  Edit,
  Files,
  FolderOpened,
  Link,
  Plus,
  Refresh,
  Search,
  Upload
} from '@element-plus/icons-vue'
import { createModel, deleteModel, getAllModels, updateModel, uploadModel } from '@/api/index.js'

const models = ref([])
const loading = ref(false)
const selectedCategory = ref('')
const selectedModel = ref(null)
const categoryKeyword = ref('')
const modelKeyword = ref('')
const expandedCategories = ref(new Set())

const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const editId = ref(null)
const selectedFile = ref(null)
const uploadRef = ref(null)

const form = ref({ name: '', category: '', filePath: '', description: '' })
const uploadForm = ref({ name: '', category: '' })

const categories = computed(() => Array.from(new Set(models.value.map((item) => item.category).filter(Boolean))).sort(compareZh))

const categoryTree = computed(() => {
  const rootMap = new Map()
  const nodeMap = new Map()

  categories.value.forEach((category) => {
    const parts = category.split('/').filter(Boolean)
    let path = ''
    let siblings = rootMap

    parts.forEach((part, index) => {
      path = path ? `${path}/${part}` : part
      if (!nodeMap.has(path)) {
        const node = {
          name: part,
          path,
          level: index,
          modelCount: 0,
          children: [],
          childMap: new Map()
        }
        nodeMap.set(path, node)
        siblings.set(part, node)
      }
      const node = nodeMap.get(path)
      node.modelCount += models.value.filter((model) => model.category === category).length
      siblings = node.childMap
    })
  })

  const materialize = (nodes) => Array.from(nodes.values())
    .sort((a, b) => compareZh(a.name, b.name))
    .map((node) => ({
      ...node,
      childMap: undefined,
      children: materialize(node.childMap)
    }))

  return materialize(rootMap)
})

const categoryRows = computed(() => {
  const rows = []
  const visit = (nodes) => {
    nodes.forEach((node) => {
      rows.push(node)
      visit(node.children)
    })
  }
  visit(categoryTree.value)
  return rows
})

const visibleCategoryRows = computed(() => {
  const keyword = categoryKeyword.value.trim().toLowerCase()
  if (keyword) {
    return categoryRows.value.filter((row) => row.path.toLowerCase().includes(keyword))
  }

  return categoryRows.value.filter((row) => {
    const parentPath = getParentPath(row.path)
    return !parentPath || expandedCategories.value.has(parentPath)
  })
})

const scopedModels = computed(() => {
  if (!selectedCategory.value) return models.value
  return models.value.filter((item) => item.category === selectedCategory.value || item.category?.startsWith(`${selectedCategory.value}/`))
})

const filteredModels = computed(() => {
  const keyword = modelKeyword.value.trim().toLowerCase()
  const list = scopedModels.value
  if (!keyword) return list
  return list.filter((item) => [item.name, item.category, item.filePath, item.description].filter(Boolean).join(' ').toLowerCase().includes(keyword))
})

const selectedCategoryName = computed(() => {
  if (!selectedCategory.value) return '全部模型'
  const parts = selectedCategory.value.split('/')
  return parts[parts.length - 1]
})

watch(filteredModels, (list) => {
  if (!list.length) {
    selectedModel.value = null
    return
  }
  if (!selectedModel.value || !list.some((item) => item.id === selectedModel.value.id)) {
    selectedModel.value = list[0]
  }
})

const loadModelData = async () => {
  loading.value = true
  const previousModelId = selectedModel.value?.id
  try {
    const response = await getAllModels()
    models.value = response.data || []
    expandInitialCategories()

    const previousModel = models.value.find((item) => item.id === previousModelId)
    selectedModel.value = previousModel || filteredModels.value[0] || null
  } catch (error) {
    ElMessage.error('加载模型数据失败')
  } finally {
    loading.value = false
  }
}

const compareZh = (a, b) => a.localeCompare(b, 'zh-Hans-CN')

const expandInitialCategories = () => {
  if (expandedCategories.value.size) return
  const next = new Set()
  categoryTree.value.slice(0, 3).forEach((node) => next.add(node.path))
  expandedCategories.value = next
}

const getParentPath = (path) => {
  const index = path.lastIndexOf('/')
  return index > -1 ? path.slice(0, index) : ''
}

const isExpanded = (path) => expandedCategories.value.has(path)

const toggleCategory = (path) => {
  const next = new Set(expandedCategories.value)
  if (next.has(path)) {
    next.delete(path)
  } else {
    next.add(path)
  }
  expandedCategories.value = next
}

const selectCategory = (category) => {
  selectedCategory.value = category
  modelKeyword.value = ''
  const row = categoryRows.value.find((item) => item.path === category)
  if (row?.children.length && !expandedCategories.value.has(category)) {
    toggleCategory(category)
  }
  selectedModel.value = scopedModels.value[0] || null
}

const selectModel = (model) => {
  selectedModel.value = model
}

const getActiveCategory = () => selectedCategory.value || selectedModel.value?.category || ''

const resetForm = () => {
  form.value = { name: '', category: getActiveCategory(), filePath: '', description: '' }
  editId.value = null
  isEdit.value = false
}

const showAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const editModel = (row) => {
  if (!row?.id) return
  isEdit.value = true
  editId.value = row.id
  form.value = {
    name: row.name || '',
    category: row.category || '',
    filePath: row.filePath || '',
    description: row.description || ''
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name || !form.value.category || !form.value.filePath) {
    ElMessage.warning('请填写模型名称、分类路径和文件路径')
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
    selectedCategory.value = form.value.category
    await loadModelData()
  } catch (error) {
    ElMessage.error('保存模型失败')
  } finally {
    submitting.value = false
  }
}

const deleteModelConfirm = async (row) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确定删除模型“${row.name}”吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteModel(row.id)
    ElMessage.success('模型已删除')
    await loadModelData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除模型失败')
    }
  }
}

const showUploadDialog = () => {
  uploadForm.value = { name: '', category: getActiveCategory() }
  selectedFile.value = null
  uploadRef.value?.clearFiles()
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  if (!uploadForm.value.name && file.name) {
    uploadForm.value.name = file.name.replace(/\.[^.]+$/, '')
  }
}

const handleFileRemove = () => {
  selectedFile.value = null
}

const submitUpload = async () => {
  if (!uploadForm.value.name || !uploadForm.value.category) {
    ElMessage.warning('请填写模型名称和分类路径')
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
    selectedCategory.value = uploadForm.value.category
    await loadModelData()
  } catch (error) {
    ElMessage.error('上传模型失败')
  } finally {
    uploading.value = false
  }
}

onMounted(loadModelData)
</script>

<style scoped>
.model-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  padding: 24px;
  box-sizing: border-box;
}

.model-header,
.catalog-panel,
.model-list-panel,
.detail-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 24px;
}

.model-header__eyebrow {
  margin: 0 0 8px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
}

.model-header h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 1.25;
}

.model-header p {
  margin: 10px 0 0;
  color: #667085;
  font-size: 14px;
}

.model-header__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.model-layout {
  display: grid;
  grid-template-columns: minmax(260px, 300px) minmax(360px, 1fr) minmax(320px, 380px);
  gap: 16px;
  min-height: 650px;
}

.catalog-panel,
.model-list-panel,
.detail-panel {
  min-width: 0;
  overflow: hidden;
}

.catalog-panel {
  display: flex;
  flex-direction: column;
}

.panel-title,
.model-list-panel__header {
  padding: 18px;
  border-bottom: 1px solid #eef2f7;
}

.panel-title h2,
.model-list-panel__header h2,
.detail-panel__top h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.panel-title span,
.breadcrumb-line,
.detail-panel__top p {
  color: #667085;
  font-size: 13px;
}

.catalog-panel__search {
  padding: 14px 16px 10px;
  box-sizing: border-box;
}

.catalog-panel__list {
  flex: 1;
  padding: 0 10px 14px;
  overflow: auto;
}

.catalog-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 38px;
  margin: 2px 0;
  padding: 0 10px 0 calc(10px + var(--level, 0) * 18px);
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #344054;
  text-align: left;
  cursor: pointer;
}

.catalog-row:hover {
  background: #f3f8f6;
}

.catalog-row.is-active {
  background: #e7f5f0;
  color: #0f766e;
  font-weight: 700;
}

.catalog-row__toggle {
  width: 16px;
  display: inline-flex;
  justify-content: center;
  color: #98a2b3;
}

.catalog-row__icon {
  color: #0f766e;
  display: inline-flex;
}

.catalog-row__name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.catalog-row__count {
  min-width: 28px;
  padding: 2px 7px;
  border-radius: 999px;
  background: #f2f4f7;
  color: #667085;
  font-size: 12px;
  text-align: center;
}

.model-list-panel {
  display: flex;
  flex-direction: column;
}

.model-list-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.breadcrumb-line {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.breadcrumb-line strong {
  color: #0f766e;
  font-weight: 700;
}

.model-list-panel__search {
  width: min(360px, 42%);
}

.model-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 14px 18px;
  border-bottom: 1px solid #eef2f7;
  background: #fbfcfd;
}

.model-summary div {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.model-summary span {
  display: block;
  color: #667085;
  font-size: 12px;
}

.model-summary strong {
  display: block;
  margin-top: 5px;
  overflow: hidden;
  color: #111827;
  font-size: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-list {
  flex: 1;
  padding: 14px;
}

.model-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 76px;
  margin-bottom: 10px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.model-card:hover {
  border-color: #99d5c3;
  background: #f7fcfa;
}

.model-card.is-active {
  border-color: #0f766e;
  box-shadow: inset 3px 0 0 #0f766e;
}

.model-card__file {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  border-radius: 8px;
  background: #eef7f4;
  color: #0f766e;
  font-size: 20px;
}

.model-card__content {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.model-card__content strong,
.model-card__content span,
.model-card__content small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-card__content strong {
  color: #111827;
  font-size: 15px;
}

.model-card__content span {
  color: #667085;
  font-size: 13px;
}

.model-card__content small {
  color: #98a2b3;
  font-size: 12px;
}

.model-card__arrow {
  color: #98a2b3;
}

.detail-panel {
  padding: 20px;
  box-sizing: border-box;
}

.detail-panel__top {
  display: flex;
  gap: 14px;
  align-items: center;
  padding-bottom: 18px;
  border-bottom: 1px solid #eef2f7;
}

.detail-panel__top p {
  margin: 0 0 6px;
}

.detail-panel__file-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: 8px;
  background: #eef7f4;
  color: #0f766e;
  font-size: 24px;
}

.detail-panel__descriptions {
  margin-top: 18px;
}

.detail-panel a {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0f766e;
}

.detail-panel__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 18px;
}

.detail-empty {
  display: flex;
  min-height: 460px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #667085;
}

.detail-empty .el-icon {
  margin-bottom: 12px;
  color: #98a2b3;
  font-size: 42px;
}

.detail-empty h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.detail-empty p {
  margin: 8px 0 0;
  font-size: 13px;
}

.upload-tip {
  color: #667085;
  font-size: 12px;
}

@media (max-width: 1280px) {
  .model-layout {
    grid-template-columns: minmax(240px, 280px) minmax(420px, 1fr);
  }

  .detail-panel {
    grid-column: 1 / -1;
  }
}

@media (max-width: 860px) {
  .model-page {
    padding: 16px;
  }

  .model-header,
  .model-list-panel__header {
    flex-direction: column;
  }

  .model-layout,
  .model-summary {
    grid-template-columns: 1fr;
  }

  .model-list-panel__search {
    width: 100%;
  }
}
</style>
