<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">后台管理</p>
        <h1 class="backend-page__title">设备管理</h1>
        <p class="backend-page__subtitle">统一维护设备档案、状态和证书信息，支持搜索与本地分页。</p>
      </div>
      <div class="backend-page__hero-actions">
        <el-button type="primary" @click="showRegisterDialog">新增设备</el-button>
        <el-button @click="loadDevices()">刷新</el-button>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">设备列表</h2>
            <p class="backend-page__section-desc">可按设备编码、名称和描述快速筛选。</p>
          </div>
          <div class="backend-page__actions">
            <el-input
              v-model="searchText"
              clearable
              placeholder="搜索设备"
              style="width: 240px"
              @input="resetPage"
            />
          </div>
        </div>
      </template>

      <div class="backend-page__table-wrap">
        <el-table :data="pagedDevices" v-loading="loading" stripe>
          <el-table-column prop="deviceId" label="设备编码" min-width="160" />
          <el-table-column prop="name" label="设备名称" min-width="140" />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="在线状态" width="110">
            <template #default="{ row }">
              <el-tag :type="isOnline(row) ? 'success' : 'info'">
                {{ isOnline(row) ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="证书状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.certificateThumbprint ? 'success' : 'info'">
                {{ row.certificateThumbprint ? '已签发' : '未签发' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastSeenAt" label="最后在线" width="180">
            <template #default="{ row }">
              {{ formatTime(row.lastSeenAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-button size="small" type="warning" @click="toggleStatus(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button size="small" type="info" @click="showCertificateDialog(row)">证书</el-button>
              <el-button size="small" type="danger" @click="deleteDevice(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

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

    <el-dialog v-model="registerDialogVisible" title="新增设备" width="520px" class="backend-page__dialog">
      <el-form :model="registerForm" label-width="96px" class="backend-form">
        <el-form-item label="设备名称">
          <el-input v-model="registerForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="registerForm.description" type="textarea" placeholder="可选，补充设备用途说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="registerDevice">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑设备" width="520px" class="backend-page__dialog">
      <el-form :model="editForm" label-width="96px" class="backend-form">
        <el-form-item label="设备名称">
          <el-input v-model="editForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" placeholder="可选，补充设备用途说明" />
        </el-form-item>
        <el-form-item label="绑定用户">
          <el-input v-model.number="editForm.userId" type="number" placeholder="可选，填写用户 ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateDevice">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="certificateDialogVisible" title="设备证书" width="640px" class="backend-page__dialog">
      <div v-if="currentDevice">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备编码">{{ currentDevice.deviceId }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
          <el-descriptions-item label="证书状态">
            <el-tag :type="currentDevice.certificateThumbprint ? 'success' : 'info'">
              {{ currentDevice.certificateThumbprint ? '已签发' : '未签发' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDevice.certificateThumbprint" label="指纹">
            {{ currentDevice.certificateThumbprint }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="certificateData" class="certificate-content">
          <h4>证书内容</h4>
          <el-input v-model="certificateData.certificate" type="textarea" :rows="10" readonly />
        </div>
      </div>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="certificateDialogVisible = false">关闭</el-button>
          <el-button v-if="!currentDevice?.certificateThumbprint" type="primary" @click="generateCertificate">
            生成证书
          </el-button>
          <el-button v-else type="danger" @click="revokeCertificate">吊销证书</el-button>
          <el-button v-if="certificateData" type="success" @click="downloadCertificate">下载证书</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import deviceApi from '@/api/device'
import { useLocalPagination } from '@/composables/useLocalPagination'

const devices = ref([])
const loading = ref(false)
const searchText = ref('')
const registerDialogVisible = ref(false)
const editDialogVisible = ref(false)
const certificateDialogVisible = ref(false)
const currentDevice = ref(null)
const certificateData = ref(null)
let refreshTimer = null

const registerForm = ref({ name: '', description: '' })
const editForm = ref({ id: null, name: '', description: '', userId: null })

const filteredDevices = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) return devices.value
  return devices.value.filter((item) => {
    const text = [item.deviceId, item.name, item.description].filter(Boolean).join(' ').toLowerCase()
    return text.includes(keyword)
  })
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedDevices, resetPage } = useLocalPagination(filteredDevices, { pageSize: 10 })

const loadDevices = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const response = await deviceApi.getDevices()
    devices.value = response.data || []
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  } finally {
    if (!silent) loading.value = false
  }
}

const showRegisterDialog = () => {
  registerForm.value = { name: '', description: '' }
  registerDialogVisible.value = true
}

const registerDevice = async () => {
  try {
    await deviceApi.registerDevice(registerForm.value)
    ElMessage.success('设备已创建')
    registerDialogVisible.value = false
    await loadDevices()
  } catch (error) {
    ElMessage.error('创建设备失败')
  }
}

const showEditDialog = (device) => {
  editForm.value = {
    id: device.id,
    name: device.name,
    description: device.description,
    userId: device.userId
  }
  editDialogVisible.value = true
}

const updateDevice = async () => {
  try {
    await deviceApi.updateDevice(editForm.value.id, {
      name: editForm.value.name,
      description: editForm.value.description
    })
    if (editForm.value.userId) {
      await deviceApi.bindDevice(editForm.value.id, editForm.value.userId)
    }
    ElMessage.success('设备已更新')
    editDialogVisible.value = false
    await loadDevices()
  } catch (error) {
    ElMessage.error('更新设备失败')
  }
}

const toggleStatus = async (device) => {
  const nextStatus = device.status === 1 ? 0 : 1
  try {
    await deviceApi.updateDeviceStatus(device.id, nextStatus)
    ElMessage.success('状态已更新')
    await loadDevices()
  } catch (error) {
    ElMessage.error('更新状态失败')
  }
}

const deleteDevice = async (device) => {
  try {
    await ElMessageBox.confirm(`确定删除设备“${device.name}”吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deviceApi.deleteDevice(device.id)
    ElMessage.success('设备已删除')
    await loadDevices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除设备失败')
    }
  }
}

const showCertificateDialog = async (device) => {
  currentDevice.value = device
  certificateData.value = null
  if (device.certificateThumbprint) {
    try {
      const response = await deviceApi.getDeviceCertificate(device.id)
      certificateData.value = response.data || null
    } catch (error) {
      ElMessage.error('加载证书失败')
    }
  }
  certificateDialogVisible.value = true
}

const generateCertificate = async () => {
  try {
    const response = await deviceApi.generateDeviceCertificate(currentDevice.value.id)
    certificateData.value = response.data || null
    currentDevice.value.certificateThumbprint = response.data?.thumbprint || ''
    ElMessage.success('证书已生成')
    await loadDevices()
  } catch (error) {
    ElMessage.error('生成证书失败')
  }
}

const revokeCertificate = async () => {
  try {
    await ElMessageBox.confirm('确定吊销当前证书吗？', '提示', {
      confirmButtonText: '吊销',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deviceApi.revokeDeviceCertificate(currentDevice.value.id)
    currentDevice.value.certificateThumbprint = null
    certificateData.value = null
    ElMessage.success('证书已吊销')
    await loadDevices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('吊销证书失败')
    }
  }
}

const downloadCertificate = () => {
  if (!certificateData.value?.certificate) return
  const blob = new Blob([certificateData.value.certificate], { type: 'application/x-pem-file' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${currentDevice.value.deviceId}.pem`
  link.click()
  URL.revokeObjectURL(url)
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const isOnline = (device) => {
  if (!device || device.status !== 1 || !device.lastSeenAt) return false
  const lastSeen = new Date(device.lastSeenAt).getTime()
  return Number.isFinite(lastSeen) && Date.now() - lastSeen <= 30000
}

onMounted(async () => {
  await loadDevices()
  refreshTimer = window.setInterval(() => loadDevices(true), 10000)
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.certificate-content {
  margin-top: 20px;
}

.certificate-content h4 {
  margin: 0 0 10px;
  color: #1f2937;
}
</style>
