<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">场景库</p>
        <h1 class="backend-page__title">场景库</h1>
        <p class="backend-page__subtitle">统一管理 JSON、GLB、GLTF 场景资源，支持上传、删除和本地分页。</p>
      </div>
      <div class="backend-page__hero-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="openUpload">上传场景</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">场景列表</h2>
            <p class="backend-page__section-desc">可查看资源类型、存储路径和创建时间。</p>
          </div>
          <div class="backend-page__actions">
            <el-input v-model="searchText" clearable placeholder="搜索场景" style="width: 220px" @input="resetPage" />
          </div>
        </div>
      </template>

      <el-table :data="pagedScenes" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="fileType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getFileTypeTag(row.fileType)">
              {{ row.fileType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="存储路径" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="useScene(row)">使用</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!filteredScenes.length" class="backend-page__empty">
        <el-empty description="暂无场景资源">
          <el-button type="primary" @click="openUpload">上传场景</el-button>
        </el-empty>
      </div>

      <div class="backend-page__pagination" v-if="filteredScenes.length">
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
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteScene, listScenes, uploadScene } from '@/api/scenes'
import { useLocalPagination } from '@/composables/useLocalPagination'

const router = useRouter()
const scenes = ref([])
const loading = ref(false)
const searchText = ref('')

const filteredScenes = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) return scenes.value
  return scenes.value.filter((item) => [item.name, item.fileType, item.path].filter(Boolean).join(' ').toLowerCase().includes(keyword))
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedScenes, resetPage } = useLocalPagination(filteredScenes, { pageSize: 10 })

const normalizeList = (response) => {
  if (Array.isArray(response)) return response
  if (Array.isArray(response?.data)) return response.data
  return []
}

const load = async () => {
  loading.value = true
  try {
    const response = await listScenes()
    scenes.value = normalizeList(response)
  } catch (error) {
    ElMessage.error(`Failed to load scenes: ${error.message || 'unknown error'}`)
    scenes.value = []
  } finally {
    loading.value = false
  }
}

const openUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json,.glb,.gltf'
  input.onchange = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return

    try {
      const fileType = file.name.split('.').pop()?.toLowerCase()
      if (!['json', 'glb', 'gltf'].includes(fileType)) {
        ElMessage.error('仅支持上传 JSON、GLB、GLTF 文件')
        return
      }

      const sceneName = await ElMessageBox.prompt('请输入场景名称', '场景名称', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: file.name.replace(/\.(json|glb|gltf)$/i, ''),
        inputValidator: (value) => (value?.trim() ? true : '场景名称不能为空')
      }).then(({ value }) => value)

      const description = await ElMessageBox.prompt('请输入场景描述（可选）', '场景描述', {
        confirmButtonText: '确定',
        cancelButtonText: '跳过',
        inputType: 'textarea'
      }).then(({ value }) => value).catch(() => '')

      if (sceneName) {
        await uploadScene(file, sceneName, description || '')
        ElMessage.success('场景已上传')
        await load()
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(`上传场景失败：${error.message || '未知错误'}`)
      }
    }
  }
  input.click()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除场景“${row.name}”吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteScene(row.id)
    ElMessage.success('场景已删除')
    await load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`删除场景失败：${error.message || '未知错误'}`)
    }
  }
}

const useScene = (row) => {
  router.push({ name: 'ThreeScene', query: { sceneId: row.id } })
}

const goBack = () => router.back()

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const getFileTypeTag = (fileType) => {
  switch ((fileType || '').toLowerCase()) {
    case 'glb':
      return 'primary'
    case 'gltf':
      return 'success'
    case 'json':
      return 'warning'
    default:
      return 'info'
  }
}

onMounted(load)
</script>
