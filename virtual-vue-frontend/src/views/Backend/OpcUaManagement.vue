<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">后台管理</p>
        <h1 class="backend-page__title">OPC UA 管理</h1>
        <p class="backend-page__subtitle">统一查看服务状态、同步设备节点，并支持搜索和分页浏览节点数据。</p>
      </div>
      <div class="backend-page__stats" v-if="status">
        <div class="backend-page__stat">
          <span class="backend-page__stat-label">服务状态</span>
          <span class="backend-page__stat-value">{{ status.running ? '运行中' : '已停止' }}</span>
        </div>
        <div class="backend-page__stat">
          <span class="backend-page__stat-label">节点数量</span>
          <span class="backend-page__stat-value">{{ status.deviceCount || 0 }}</span>
        </div>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">节点列表</h2>
            <p class="backend-page__section-desc">可查看节点状态、最后上报时间和原始数据。</p>
          </div>
          <div class="backend-page__actions">
            <el-input v-model="searchText" clearable placeholder="搜索节点" style="width: 220px" @input="resetPage" />
            <el-button @click="refreshStatus" :loading="loading">刷新</el-button>
            <el-button type="success" :loading="syncing" @click="syncDevices">同步设备</el-button>
            <el-button type="primary" @click="showCreateDialog">创建设备节点</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedDevices" v-loading="loading" stripe>
        <el-table-column prop="name" label="节点名称" min-width="160" />
        <el-table-column label="在线状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.online ? 'success' : (row.enabled ? 'info' : 'danger')">
              {{ row.online ? '在线' : (row.enabled ? '离线' : '停用') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="节点状态" width="120" />
        <el-table-column label="最后上报" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastSeenAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="data" label="数据" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDeviceData(row)">查看数据</el-button>
            <el-button size="small" type="danger" @click="deleteDeviceNode(row)">删除</el-button>
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

    <el-dialog v-model="createDialogVisible" title="创建设备节点" width="480px" class="backend-page__dialog">
      <el-form :model="createForm" label-width="96px" class="backend-form">
        <el-form-item label="设备 ID">
          <el-input v-model="createForm.deviceName" placeholder="例如 DEV-001" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" placeholder="可选，补充节点说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="createDeviceNode">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialogVisible" title="节点数据" width="560px" class="backend-page__dialog">
      <el-input v-model="deviceDataContent" type="textarea" :rows="12" readonly />
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="dataDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createOpcUaDevice, deleteOpcUaDevice, getOpcUaStatus, syncOpcUaDevices } from '@/api/index.js'
import { useLocalPagination } from '@/composables/useLocalPagination'

const loading = ref(false)
const syncing = ref(false)
const creating = ref(false)
const status = ref(null)
const searchText = ref('')
const createDialogVisible = ref(false)
const dataDialogVisible = ref(false)
const deviceDataContent = ref('')
let refreshTimer = null

const createForm = ref({ deviceName: '', description: '' })

const devices = computed(() => status.value?.devices || [])
const filteredDevices = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) return devices.value
  return devices.value.filter((item) => [item.name, item.data, item.status].filter(Boolean).join(' ').toLowerCase().includes(keyword))
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedDevices, resetPage } = useLocalPagination(filteredDevices, { pageSize: 10 })

const refreshStatus = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const response = await getOpcUaStatus()
    status.value = response.data?.data || response.data || null
  } catch (error) {
    ElMessage.error('加载 OPC UA 状态失败')
  } finally {
    if (!silent) loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const syncDevices = async () => {
  syncing.value = true
  try {
    const response = await syncOpcUaDevices()
    const result = response.data?.data || response.data || {}
    ElMessage.success(`同步完成：新增 ${result.created || 0} 个，跳过 ${result.skipped || 0} 个`)
    await refreshStatus()
  } catch (error) {
    ElMessage.error('同步设备失败')
  } finally {
    syncing.value = false
  }
}

const showCreateDialog = () => {
  createForm.value = { deviceName: '', description: '' }
  createDialogVisible.value = true
}

const createDeviceNode = async () => {
  if (!createForm.value.deviceName) {
    ElMessage.warning('请填写设备 ID')
    return
  }

  creating.value = true
  try {
    await createOpcUaDevice(createForm.value)
    ElMessage.success('设备节点已创建')
    createDialogVisible.value = false
    await refreshStatus()
  } catch (error) {
    ElMessage.error('创建设备节点失败')
  } finally {
    creating.value = false
  }
}

const viewDeviceData = (device) => {
  try {
    const data = JSON.parse(device.data || '{}')
    deviceDataContent.value = JSON.stringify(data, null, 2)
  } catch {
    deviceDataContent.value = device.data || '{}'
  }
  dataDialogVisible.value = true
}

const deleteDeviceNode = async (device) => {
  try {
    await ElMessageBox.confirm(`确定删除节点“${device.name}”吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteOpcUaDevice(device.name)
    ElMessage.success('节点已删除')
    await refreshStatus()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除节点失败')
    }
  }
}

onMounted(async () => {
  await refreshStatus()
  refreshTimer = window.setInterval(() => refreshStatus(true), 10000)
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>
