<template>
  <div class="device-location-management">
    <h1>设备位置管理</h1>

    <div v-if="!isSceneReady" class="scene-not-ready">
      <el-alert
        title="3D场景未加载"
        type="warning"
        description="请先打开 3D 场景编辑器页面，然后再访问设备位置管理。"
        show-icon
        :closable="false"
      />
      <el-button type="primary" @click="goToThreeScene" style="margin-top: 16px;">
        前往 3D 场景编辑器
      </el-button>
    </div>

    <template v-else>
      <div class="action-bar">
        <ElButton type="primary" @click="refreshData">手动刷新</ElButton>
        <ElButton type="warning" @click="startAutoRefresh">自动刷新({{ autoRefreshInterval/1000 }}s)</ElButton>
        <ElButton @click="stopAutoRefresh">停止自动刷新</ElButton>
        <ElButton type="success" @click="exportData">导出JSON</ElButton>
      </div>

      <ElTable
          :data="models"
          border
          style="width: 100%"
          height="calc(100vh - 180px)"
      >
        <ElTableColumn prop="name" label="模型名称" width="150" fixed />

        <ElTableColumn label="位置(X/Y/Z)" width="180">
          <template #default="{row}">
            {{ row.position.x }} / {{ row.position.y }} / {{ row.position.z }}
          </template>
        </ElTableColumn>

        <ElTableColumn label="旋转(X/Y/Z)" width="180">
          <template #default="{row}">
            {{ row.rotation.x }}° / {{ row.rotation.y }}° / {{ row.rotation.z }}°
          </template>
        </ElTableColumn>

        <ElTableColumn label="缩放(X/Y/Z)" width="180">
          <template #default="{row}">
            {{ row.scale.x }} / {{ row.scale.y }} / {{ row.scale.z }}
          </template>
        </ElTableColumn>
      </ElTable>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElTable, ElTableColumn, ElButton, ElMessage, ElAlert } from 'element-plus'
import * as THREE from 'three'

const router = useRouter()
const models = ref([])
const autoRefreshInterval = ref(2000)
const isSceneReady = ref(false)
let refreshTimer = null

const checkSceneReady = () => {
  isSceneReady.value = !!(window.threeScene?.scene)
  return isSceneReady.value
}

const goToThreeScene = () => {
  router.push('/three')
}

const fetchModelsFromScene = () => {
  if (!checkSceneReady()) {
    models.value = []
    return
  }

  try {
    const scene = window.threeScene.scene
    const sceneModels = scene.children || []

    models.value = sceneModels
        .filter(model => {
          return (model.isMesh || model.isGroup) &&
              model !== window.threeScene?.helperGroup
        })
        .map(model => ({
          name: model.name || '未命名模型',
          position: {
            x: model.position.x.toFixed(2),
            y: model.position.y.toFixed(2),
            z: model.position.z.toFixed(2)
          },
          rotation: {
            x: THREE.MathUtils.radToDeg(model.rotation.x).toFixed(2),
            y: THREE.MathUtils.radToDeg(model.rotation.y).toFixed(2),
            z: THREE.MathUtils.radToDeg(model.rotation.z).toFixed(2)
          },
          scale: {
            x: model.scale.x.toFixed(2),
            y: model.scale.y.toFixed(2),
            z: model.scale.z.toFixed(2)
          }
        }))

    if (models.value.length === 0) {
      ElMessage.warning('场景中没有找到模型对象')
    } else {
      ElMessage.success(`已更新 ${models.value.length} 个模型数据`)
    }
  } catch (error) {
    ElMessage.error(`获取模型数据失败: ${error.message}`)
    console.error('获取模型数据错误:', error)
  }
}

const refreshData = () => {
  if (!checkSceneReady()) {
    ElMessage.warning('请先打开 3D 场景编辑器')
    return
  }
  fetchModelsFromScene()
}

const startAutoRefresh = () => {
  if (!checkSceneReady()) {
    ElMessage.warning('请先打开 3D 场景编辑器')
    return
  }
  stopAutoRefresh()
  refreshTimer = setInterval(fetchModelsFromScene, autoRefreshInterval.value)
  ElMessage.info('已开启自动刷新')
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
    ElMessage.info('已停止自动刷新')
  }
}

const exportData = () => {
  if (models.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  const exportModels = models.value
  const dataStr = JSON.stringify(exportModels, null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = `model_positions_${new Date().toISOString().slice(0, 10)}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  setTimeout(() => URL.revokeObjectURL(url), 100)
}

onMounted(() => {
  checkSceneReady()
  if (isSceneReady.value) {
    fetchModelsFromScene()
    startAutoRefresh()
  }
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.device-location-management {
  padding: 20px;
  height: 100%;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

h1 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.scene-not-ready {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.el-table__row.highlight-row {
  background-color: #fffbe6;
}
</style>
