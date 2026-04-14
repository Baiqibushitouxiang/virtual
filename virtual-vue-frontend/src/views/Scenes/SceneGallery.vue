<template>
  <div class="scene-gallery">
    <div class="toolbar">
      <el-button @click="goBack">返回</el-button>
      <el-button type="primary" @click="openUpload">上传场景(JSON/GLB/GLTF)</el-button>
      <el-button @click="load" type="warning">刷新列表</el-button>
    </div>
    <div v-if="debug" style="padding: 10px; background: #f5f5f5;">
      数据条数: {{ scenes.length }}, 加载状态: {{ loading }}
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else class="table-container">
      <el-table
          :data="scenes"
          border
          style="width: 100%"
          :height="tableHeight"
          v-if="scenes.length > 0"
      >
        <el-table-column prop="id" label="ID" width="100"/>
        <el-table-column prop="name" label="名称" width="260"/>
        <el-table-column prop="fileType" label="类型" width="100">
          <template #default="{row}">
            <el-tag :type="getFileTypeTag(row.fileType)">
              {{ row.fileType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="存储路径" show-overflow-tooltip/>
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{row}">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="success" @click="useScene(row)">使用</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-else class="empty-state">
        <el-empty description="暂无场景数据">
          <el-button type="primary" @click="openUpload">上传第一个场景</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listScenes, uploadScene, deleteScene } from '@/api/scenes'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const scenes = ref([])
const loading = ref(false)
const debug = ref(false) // 可通过控制台设置为true开启调试信息

// 计算表格高度（减去工具栏和边距）
const tableHeight = computed(() => {
  return `calc(100vh - 120px)`
})

// 加载场景列表
const load = async () => {
  loading.value = true
  try {
    console.log('开始加载场景列表...')
    const response = await listScenes()
    console.log('API响应:', response)

    // 处理响应数据（根据实际接口返回格式调整）
    if (Array.isArray(response)) {
      scenes.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      scenes.value = response.data
    } else {
      scenes.value = []
      console.warn('响应数据格式异常:', response)
    }
    console.log('场景列表加载成功:', scenes.value)
  } catch (error) {
    console.error('加载场景列表失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '未知错误'))
    scenes.value = []
  } finally {
    loading.value = false
  }
}

// 上传场景
const openUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json,.glb,.gltf'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    try {
      // 获取文件类型（不含点）
      const fileType = file.name.split('.').pop().toLowerCase()
      if (!['json', 'glb', 'gltf'].includes(fileType)) {
        ElMessage.error('不支持的文件类型，请上传JSON/GLB/GLTF格式')
        return
      }

      // 提示输入场景名称
      const sceneName = await ElMessageBox.prompt('请输入场景名称', '场景名称', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: file.name.replace(/\.(json|glb|gltf)$/i, ''),
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '场景名称不能为空'
          }
          return true
        }
      }).then(({ value }) => value)

      if (sceneName) {
        // 提示输入场景描述（可选）
        const description = await ElMessageBox.prompt('请输入场景描述（可选）', '场景描述', {
          confirmButtonText: '确定',
          cancelButtonText: '跳过',
          inputType: 'textarea'
        }).then(({ value }) => value).catch(() => '')

        // 调用上传接口
        await uploadScene(file, sceneName, description || '')
        ElMessage.success('场景上传成功')
        await load() // 重新加载列表
      }
    } catch (error) {
      console.error('上传失败:', error)
      if (error !== 'cancel') { // 排除用户主动取消的情况
        ElMessage.error('上传失败: ' + (error.message || '未知错误'))
      }
    }
  }
  input.click()
}

// 删除场景
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除场景 "${row.name}" 吗？`, '确认删除', {
      type: 'warning'
    })

    await deleteScene(row.id)
    ElMessage.success('删除成功')
    await load() // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') { // 排除用户主动取消的情况
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 使用场景（跳转至编辑器）
const useScene = (row) => {
  router.push({ name: 'ThreeScene', query: { sceneId: row.id } })
}

// 返回上一页
const goBack = () => router.back()

// 格式化日期时间
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 根据文件类型获取标签样式
const getFileTypeTag = (fileType) => {
  if (!fileType) return 'default'
  const type = fileType.toLowerCase()
  switch (type) {
    case 'glb':
      return 'primary' // 蓝色
    case 'gltf':
      return 'success' // 绿色
    case 'json':
      return 'warning' // 橙色
    default:
      return 'info' // 灰色
  }
}

// 组件挂载时加载列表
onMounted(() => {
  console.log('SceneGallery 组件挂载')
  load()
})
</script>

<style scoped>
.scene-gallery {
  padding: 16px;
  height: 100vh;
  box-sizing: border-box;
  overflow: hidden;
}

.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.table-container {
  height: calc(100% - 50px);
  overflow: hidden;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .scene-gallery {
    padding: 8px;
  }

  .toolbar {
    flex-wrap: wrap;
  }
}
</style>