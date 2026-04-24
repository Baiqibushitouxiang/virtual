<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">后台管理</p>
        <h1 class="backend-page__title">告警规则管理</h1>
        <p class="backend-page__subtitle">统一维护设备阈值规则，支持筛选与本地分页。</p>
      </div>
      <div class="backend-page__hero-actions">
        <el-button type="primary" @click="openCreate">新增规则</el-button>
        <el-button @click="loadRules">刷新</el-button>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">规则列表</h2>
            <p class="backend-page__section-desc">可查看设备、阈值条件、状态和消息模板。</p>
          </div>
          <div class="backend-page__actions">
            <el-input v-model="searchText" clearable placeholder="搜索规则" style="width: 220px" @input="resetPage" />
          </div>
        </div>
      </template>

      <el-table :data="pagedRules" v-loading="loading" stripe>
        <el-table-column prop="deviceName" label="设备" min-width="150" />
        <el-table-column prop="deviceCode" label="设备编码" min-width="160" />
        <el-table-column prop="dataType" label="数据项" width="120" />
        <el-table-column label="条件" width="150">
          <template #default="{ row }">
            {{ row.operator }} {{ row.threshold }}
          </template>
        </el-table-column>
        <el-table-column prop="cooldownSeconds" label="冷却时间(秒)" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled === 1" @change="toggleStatus(row, $event)" />
          </template>
        </el-table-column>
        <el-table-column prop="messageTemplate" label="消息模板" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeRule(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingRule ? '编辑告警规则' : '新增告警规则'" width="540px" class="backend-page__dialog">
      <el-form :model="form" label-width="100px" class="backend-form">
        <el-form-item label="设备">
          <el-select v-model="form.deviceId" placeholder="请选择设备" filterable style="width: 100%" @change="syncDeviceInfo">
            <el-option
              v-for="device in devices"
              :key="device.id"
              :label="`${device.name} (${device.deviceId})`"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数据项">
          <el-select v-model="form.dataType" placeholder="请选择数据项" style="width: 100%">
            <el-option v-for="item in dataTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="运算符">
          <el-select v-model="form.operator" style="width: 100%">
            <el-option label=">" value=">" />
            <el-option label=">=" value=">=" />
            <el-option label="<" value="<" />
            <el-option label="<=" value="<=" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值">
          <el-input-number v-model="form.threshold" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="冷却时间">
          <el-input-number v-model="form.cooldownSeconds" :min="0" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="消息模板">
          <el-input
            v-model="form.messageTemplate"
            type="textarea"
            :rows="3"
            placeholder="可使用 ${deviceName} ${dataType} ${value} ${operator} ${threshold}"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="backend-page__dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllDevices } from '@/api/device'
import { createAlertRule, deleteAlertRule, getAlertRules, updateAlertRule, updateAlertRuleStatus } from '@/api/alertRule'
import { useLocalPagination } from '@/composables/useLocalPagination'

const dataTypes = [
  { label: '温度', value: 'temperature' },
  { label: '湿度', value: 'humidity' },
  { label: '压力', value: 'pressure' },
  { label: '电压', value: 'voltage' },
  { label: '电流', value: 'current' },
  { label: '功率', value: 'power' }
]

const loading = ref(false)
const dialogVisible = ref(false)
const editingRule = ref(null)
const rules = ref([])
const devices = ref([])
const searchText = ref('')
const form = ref({
  deviceId: null,
  deviceCode: '',
  deviceName: '',
  dataType: '',
  operator: '>',
  threshold: 0,
  enabled: 1,
  cooldownSeconds: 60,
  messageTemplate: ''
})

const filteredRules = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) return rules.value
  return rules.value.filter((item) =>
    [item.deviceName, item.deviceCode, item.dataType, item.messageTemplate].filter(Boolean).join(' ').toLowerCase().includes(keyword)
  )
})

const { currentPage, pageSize, pageSizes, total, pagedItems: pagedRules, resetPage } = useLocalPagination(filteredRules, { pageSize: 10 })

const resetForm = () => {
  form.value = {
    deviceId: null,
    deviceCode: '',
    deviceName: '',
    dataType: '',
    operator: '>',
    threshold: 0,
    enabled: 1,
    cooldownSeconds: 60,
    messageTemplate: ''
  }
}

const loadDevices = async () => {
  const response = await getAllDevices()
  devices.value = response.data || response || []
}

const loadRules = async () => {
  loading.value = true
  try {
    const response = await getAlertRules()
    rules.value = response.data || response || []
  } catch (error) {
    ElMessage.error('加载告警规则失败')
  } finally {
    loading.value = false
  }
}

const syncDeviceInfo = () => {
  const device = devices.value.find((item) => item.id === form.value.deviceId)
  form.value.deviceCode = device?.deviceId || ''
  form.value.deviceName = device?.name || ''
}

const openCreate = () => {
  editingRule.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingRule.value = row
  form.value = { ...row }
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.value.deviceId || !form.value.dataType || !form.value.operator) {
    ElMessage.warning('请补全规则信息')
    return
  }
  syncDeviceInfo()
  try {
    if (editingRule.value) {
      await updateAlertRule(editingRule.value.id, form.value)
    } else {
      await createAlertRule(form.value)
    }
    ElMessage.success('告警规则已保存')
    dialogVisible.value = false
    await loadRules()
  } catch (error) {
    ElMessage.error('保存告警规则失败')
  }
}

const toggleStatus = async (row, checked) => {
  try {
    await updateAlertRuleStatus(row.id, checked ? 1 : 0)
    ElMessage.success('规则状态已更新')
    await loadRules()
  } catch (error) {
    ElMessage.error('更新状态失败')
  }
}

const removeRule = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除规则“${row.deviceName} / ${row.dataType}”吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAlertRule(row.id)
    ElMessage.success('规则已删除')
    await loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除规则失败')
    }
  }
}

onMounted(async () => {
  await loadDevices()
  await loadRules()
})
</script>
