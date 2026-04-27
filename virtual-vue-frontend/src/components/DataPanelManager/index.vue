<template>
  <el-dialog
    :model-value="visible"
    title="展板管理"
    width="860px"
    :before-close="handleClose"
    class="panel-manager-dialog"
  >
    <div class="panel-manager">
      <div class="manager-tip">
        {{ sceneId ? '当前场景的展板只属于当前场景，不会与其他场景共享。' : '当前是未保存场景，展板会先作为本地草稿保存，保存场景后再正式落库。' }}
      </div>
      <div class="manager-header">
        <el-button type="primary" @click="showCreateDialog" size="small">
          <el-icon><Plus /></el-icon> 新增展板
        </el-button>
        <el-button type="success" @click="openAllBoundPanels" size="small" :disabled="boundPanelsCount === 0">
          <el-icon><VideoPlay /></el-icon> 打开已绑定展板 ({{ boundPanelsCount }})
        </el-button>
        <el-button type="warning" @click="closeAllPanels" size="small" :disabled="openedPanelsCount === 0">
          <el-icon><VideoPause /></el-icon> 关闭全部展板 ({{ openedPanelsCount }})
        </el-button>
      </div>

      <el-table :data="panels" style="width: 100%" max-height="420">
        <el-table-column prop="name" label="展板名称" min-width="150">
          <template #default="{ row }">
            <span>{{ row.name }}</span>
            <el-tag v-if="openedPanelIds.includes(row.id)" type="success" size="small" style="margin-left: 8px;">已打开</el-tag>
            <el-tag v-if="!sceneId" type="warning" size="small" style="margin-left: 8px;">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="绑定模型" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'bound-text': row.modelId }">{{ row.modelName || '未绑定模型' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="绑定设备" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'bound-text': row.deviceId }">{{ row.deviceName || '未绑定设备' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button
                size="small"
                :type="openedPanelIds.includes(row.id) ? 'warning' : 'primary'"
                @click="togglePanel(row)"
              >
                {{ openedPanelIds.includes(row.id) ? '关闭' : '打开' }}
              </el-button>
              <el-button size="small" @click="openBindingDialog(row)">绑定</el-button>
              <el-button size="small" @click="editPanel(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeletePanel(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="createDialogVisible" :title="editingPanel ? '编辑展板' : '新增展板'" width="450px" append-to-body>
      <el-form :model="panelForm" label-width="80px" :rules="formRules" ref="formRef">
        <el-form-item label="展板名称" prop="name">
          <el-input v-model="panelForm.name" placeholder="请输入展板名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="panelForm.description" type="textarea" :rows="2" placeholder="请输入描述（可选）" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePanel">{{ editingPanel ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindingDialogVisible" title="绑定设置" width="520px" append-to-body>
      <template v-if="bindingPanel">
        <div class="binding-title">{{ bindingPanel.name }}</div>
        <div class="binding-section">
          <div class="binding-section__header">
            <span>设备直连</span>
            <el-button
              v-if="bindingPanel.deviceId"
              size="small"
              text
              type="danger"
              @click="emit('unbindDevice', bindingPanel)"
            >
              解除设备
            </el-button>
          </div>
          <el-select
            v-model="bindingDeviceId"
            filterable
            clearable
            placeholder="选择设备"
            class="binding-device-select"
          >
            <el-option
              v-for="device in devices"
              :key="device.id"
              :label="`${device.name} (${device.deviceId})`"
              :value="device.id"
            />
          </el-select>
          <div class="binding-actions">
            <el-button type="primary" size="small" :disabled="!bindingDeviceId" @click="emitBindDevice">绑定设备</el-button>
          </div>
        </div>

        <div class="binding-section">
          <div class="binding-section__header">
            <span>模型映射</span>
            <el-button
              v-if="bindingPanel.modelId"
              size="small"
              text
              type="danger"
              @click="emit('unbindModel', bindingPanel)"
            >
              解除模型
            </el-button>
          </div>
          <div class="binding-summary">
            <span>{{ bindingPanel.modelName || '未绑定模型' }}</span>
          </div>
          <div class="binding-actions">
            <el-button size="small" type="primary" @click="emit('bindModel', bindingPanel)">
              进入场景点选模型
            </el-button>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="bindingDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Plus, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  sceneId: { type: [Number, String], default: null },
  panels: { type: Array, default: () => [] },
  openedPanelIds: { type: Array, default: () => [] },
  devices: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'update:visible',
  'openPanel',
  'closePanel',
  'openAll',
  'closeAll',
  'savePanel',
  'deletePanel',
  'bindModel',
  'unbindModel',
  'bindDevice',
  'unbindDevice'
])

const createDialogVisible = ref(false)
const bindingDialogVisible = ref(false)
const editingPanel = ref(null)
const bindingPanel = ref(null)
const bindingDeviceId = ref(null)
const formRef = ref(null)
const panelForm = ref({ name: '', description: '' })
const formRules = { name: [{ required: true, message: '请输入展板名称', trigger: 'blur' }] }

const boundPanelsCount = computed(() => props.panels.filter(p => p.modelId || p.deviceId).length)
const openedPanelsCount = computed(() => props.openedPanelIds.length)

watch(bindingPanel, (value) => {
  bindingDeviceId.value = value?.deviceId ?? null
})

const showCreateDialog = () => {
  editingPanel.value = null
  panelForm.value = { name: '', description: '' }
  createDialogVisible.value = true
}

const editPanel = (panel) => {
  editingPanel.value = panel
  panelForm.value = {
    name: panel.name,
    description: panel.description || ''
  }
  createDialogVisible.value = true
}

const openBindingDialog = (panel) => {
  bindingPanel.value = panel
  bindingDialogVisible.value = true
}

const savePanel = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  emit('savePanel', {
    existingPanel: editingPanel.value,
    values: {
      name: panelForm.value.name,
      description: panelForm.value.description
    }
  })
  createDialogVisible.value = false
}

const emitBindDevice = () => {
  if (!bindingPanel.value || !bindingDeviceId.value) {
    return
  }
  emit('bindDevice', {
    panel: bindingPanel.value,
    deviceId: bindingDeviceId.value
  })
}

const handleDeletePanel = async (panel) => {
  try {
    await ElMessageBox.confirm(`确定要删除展板“${panel.name}”吗？`, '提示', { type: 'warning' })
    emit('deletePanel', panel)
  } catch (e) {
    if (e !== 'cancel') {
      console.error('delete panel failed:', e)
    }
  }
}

const togglePanel = (panel) => {
  if (props.openedPanelIds.includes(panel.id)) {
    emit('closePanel', panel)
  } else {
    emit('openPanel', panel)
  }
}

const openAllBoundPanels = () => {
  emit('openAll', props.panels.filter(p => p.modelId || p.deviceId))
}

const closeAllPanels = () => emit('closeAll')
const handleClose = () => emit('update:visible', false)
</script>

<style scoped>
.panel-manager {
  min-height: 300px;
}

.manager-tip {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f4f8ff;
  color: #335;
}

.manager-header {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.bound-text {
  color: #1677ff;
  font-weight: 500;
}

.binding-title {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.binding-section {
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.binding-section + .binding-section {
  margin-top: 12px;
}

.binding-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.binding-device-select {
  width: 100%;
}

.binding-actions {
  margin-top: 12px;
}

.binding-summary {
  min-height: 34px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  border-radius: 6px;
  background: #f8fafc;
  color: #334155;
  font-size: 13px;
}

:deep(.el-dialog__body) {
  padding: 15px 20px;
}
</style>
