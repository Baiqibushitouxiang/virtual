<template>
  <el-dialog
    :model-value="visible"
    title="数据展板管理"
    width="700px"
    :before-close="handleClose"
    class="panel-manager-dialog"
  >
    <div class="panel-manager">
      <div class="manager-header">
        <el-button type="primary" @click="showCreateDialog" size="small">
          <el-icon><Plus /></el-icon> 新增展板
        </el-button>
        <el-button type="success" @click="openAllBoundPanels" size="small" :disabled="boundPanelsCount === 0">
          <el-icon><VideoPlay /></el-icon> 打开所有已绑定展板 ({{ boundPanelsCount }})
        </el-button>
        <el-button type="warning" @click="closeAllPanels" size="small" :disabled="openedPanelsCount === 0">
          <el-icon><VideoPause /></el-icon> 关闭所有展板 ({{ openedPanelsCount }})
        </el-button>
      </div>

      <el-table :data="panels" style="width: 100%" max-height="400" v-loading="loading">
        <el-table-column prop="name" label="展板名称" min-width="120">
          <template #default="{ row }">
            <span>{{ row.name }}</span>
            <el-tag v-if="openedPanelIds.includes(row.id)" type="success" size="small" style="margin-left: 8px;">已打开</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="绑定设备" min-width="100">
          <template #default="{ row }">
            <span :class="{ 'bound-text': row.deviceId }">{{ row.deviceName || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="绑定模型" min-width="100">
          <template #default="{ row }">
            <span :class="{ 'bound-text': row.modelId }">{{ row.modelName || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button 
                size="small" 
                :type="openedPanelIds.includes(row.id) ? 'warning' : 'primary'"
                @click="togglePanel(row)"
              >
                {{ openedPanelIds.includes(row.id) ? '关闭' : '打开' }}
              </el-button>
              <el-button size="small" @click="editPanel(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeletePanel(row)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="createDialogVisible"
      :title="editingPanel ? '编辑展板' : '新增展板'"
      width="450px"
      append-to-body
    >
      <el-form :model="panelForm" label-width="80px" :rules="formRules" ref="formRef">
        <el-form-item label="展板名称" prop="name">
          <el-input v-model="panelForm.name" placeholder="请输入展板名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="panelForm.description" type="textarea" :rows="2" placeholder="请输入描述（可选）" maxlength="200" />
        </el-form-item>
        <el-form-item label="绑定设备">
          <el-select v-model="panelForm.deviceId" placeholder="选择设备" clearable filterable style="width: 100%;">
            <el-option
              v-for="device in devices"
              :key="device.id"
              :label="device.name"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePanel" :loading="saving">{{ editingPanel ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { Plus, VideoPlay, VideoPause } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import { getDataPanels, createDataPanel, updateDataPanel, deleteDataPanel } from '@/api/dataPanel';
import { getAllDevices } from '@/api/device';

const props = defineProps({
  visible: { type: Boolean, default: false },
  openedPanelIds: { type: Array, default: () => [] }
});

const emit = defineEmits(['update:visible', 'openPanel', 'closePanel', 'openAll', 'closeAll', 'refresh']);

const panels = ref([]);
const devices = ref([]);
const loading = ref(false);
const saving = ref(false);
const createDialogVisible = ref(false);
const editingPanel = ref(null);
const formRef = ref(null);

const panelForm = ref({
  name: '',
  description: '',
  deviceId: null
});

const formRules = {
  name: [{ required: true, message: '请输入展板名称', trigger: 'blur' }]
};

const boundPanelsCount = computed(() => {
  return panels.value.filter(p => p.deviceId || p.modelId).length;
});

const openedPanelsCount = computed(() => {
  return props.openedPanelIds.length;
});

const loadPanels = async () => {
  loading.value = true;
  try {
    const res = await getDataPanels();
    panels.value = res.data || res || [];
  } catch (e) {
    console.error('加载展板列表失败:', e);
  } finally {
    loading.value = false;
  }
};

const loadDevices = async () => {
  try {
    const res = await getAllDevices();
    devices.value = res.data || res || [];
  } catch (e) {
    console.error('加载设备列表失败:', e);
  }
};

const showCreateDialog = () => {
  editingPanel.value = null;
  panelForm.value = {
    name: '',
    description: '',
    deviceId: null
  };
  createDialogVisible.value = true;
};

const editPanel = (panel) => {
  editingPanel.value = panel;
  panelForm.value = {
    name: panel.name,
    description: panel.description || '',
    deviceId: panel.deviceId
  };
  createDialogVisible.value = true;
};

const savePanel = async () => {
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  saving.value = true;
  try {
    if (editingPanel.value) {
      await updateDataPanel(editingPanel.value.id, {
        name: panelForm.value.name,
        description: panelForm.value.description,
        deviceId: panelForm.value.deviceId,
        deviceName: devices.value.find(d => d.id === panelForm.value.deviceId)?.name || null
      });
    } else {
      await createDataPanel({
        name: panelForm.value.name,
        description: panelForm.value.description,
        deviceId: panelForm.value.deviceId,
        deviceName: devices.value.find(d => d.id === panelForm.value.deviceId)?.name || null
      });
    }
    createDialogVisible.value = false;
    await loadPanels();
    emit('refresh');
  } catch (e) {
    console.error('保存展板失败:', e);
  } finally {
    saving.value = false;
  }
};

const handleDeletePanel = async (panel) => {
  try {
    await ElMessageBox.confirm(`确定要删除展板 "${panel.name}" 吗？`, '提示', {
      type: 'warning'
    });
    await deleteDataPanel(panel.id);
    if (props.openedPanelIds.includes(panel.id)) {
      emit('closePanel', panel);
    }
    await loadPanels();
    emit('refresh');
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除展板失败:', e);
    }
  }
};

const togglePanel = (panel) => {
  if (props.openedPanelIds.includes(panel.id)) {
    emit('closePanel', panel);
  } else {
    emit('openPanel', panel);
  }
};

const openAllBoundPanels = () => {
  const boundPanels = panels.value.filter(p => p.deviceId || p.modelId);
  emit('openAll', boundPanels);
};

const closeAllPanels = () => {
  emit('closeAll');
};

const handleClose = () => {
  emit('update:visible', false);
};

watch(() => props.visible, (val) => {
  if (val) {
    loadPanels();
    loadDevices();
  }
});
</script>

<style scoped>
.panel-manager {
  min-height: 300px;
}

.manager-header {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.bound-text {
  color: #52c41a;
  font-weight: 500;
}

:deep(.el-dialog__body) {
  padding: 15px 20px;
}
</style>
