<template>
  <div class="data-panel-management">
    <h1>数据展板管理</h1>

    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 新增展板
      </el-button>
      <el-button @click="loadPanels">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-table :data="panels" style="width: 100%" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="展板名称" width="150" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="绑定设备" width="150">
        <template #default="{ row }">
          <span v-if="row.deviceName">{{ row.deviceName }}</span>
          <el-tag v-else type="info" size="small">未绑定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="绑定模型" width="150">
        <template #default="{ row }">
          <span v-if="row.modelName">{{ row.modelName }}</span>
          <el-tag v-else type="info" size="small">未绑定</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editPanel(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="showBindDeviceDialog(row)">绑定设备</el-button>
          <el-button size="small" type="danger" @click="deletePanelConfirm(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑展板' : '新增展板'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="展板名称" required>
          <el-input v-model="form.name" placeholder="请输入展板名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="展板描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindDeviceDialogVisible" title="绑定设备" width="500px">
      <el-form label-width="100px">
        <el-form-item label="当前展板">
          <el-input :value="currentPanel?.name" disabled />
        </el-form-item>
        <el-form-item label="选择设备">
          <el-select v-model="selectedDeviceId" placeholder="请选择设备" style="width: 100%" filterable>
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
        <el-button @click="bindDeviceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBindDevice" :loading="binding">绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import { getDataPanels, createDataPanel, updateDataPanel, deleteDataPanel, bindDevice } from '@/api/dataPanel';
import { getAllDevices } from '@/api/device';

const panels = ref([]);
const devices = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const bindDeviceDialogVisible = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const binding = ref(false);
const editId = ref(null);
const currentPanel = ref(null);
const selectedDeviceId = ref(null);

const form = ref({
  name: '',
  description: '',
  status: 1
});

const resetForm = () => {
  form.value = {
    name: '',
    description: '',
    status: 1
  };
  editId.value = null;
  isEdit.value = false;
};

const loadPanels = async () => {
  loading.value = true;
  try {
    const response = await getDataPanels();
    panels.value = response.data || [];
  } catch (error) {
    ElMessage.error('加载展板列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const loadDevices = async () => {
  try {
    const response = await getAllDevices();
    devices.value = response.data || [];
  } catch (error) {
    console.error('加载设备列表失败:', error);
  }
};

const showAddDialog = () => {
  resetForm();
  dialogVisible.value = true;
};

const editPanel = (row) => {
  isEdit.value = true;
  editId.value = row.id;
  form.value = {
    name: row.name,
    description: row.description || '',
    status: row.status
  };
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请填写展板名称');
    return;
  }

  submitting.value = true;
  try {
    if (isEdit.value) {
      await updateDataPanel(editId.value, form.value);
      ElMessage.success('更新成功');
    } else {
      await createDataPanel(form.value);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    loadPanels();
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败');
    console.error(error);
  } finally {
    submitting.value = false;
  }
};

const deletePanelConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除展板 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteDataPanel(row.id);
    ElMessage.success('删除成功');
    loadPanels();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }
};

const showBindDeviceDialog = (row) => {
  currentPanel.value = row;
  selectedDeviceId.value = row.deviceId;
  bindDeviceDialogVisible.value = true;
};

const submitBindDevice = async () => {
  if (!selectedDeviceId.value) {
    ElMessage.warning('请选择设备');
    return;
  }

  binding.value = true;
  try {
    await bindDevice(currentPanel.value.id, selectedDeviceId.value);
    ElMessage.success('绑定成功');
    bindDeviceDialogVisible.value = false;
    loadPanels();
  } catch (error) {
    ElMessage.error('绑定失败');
    console.error(error);
  } finally {
    binding.value = false;
  }
};

onMounted(() => {
  loadPanels();
  loadDevices();
});
</script>

<style scoped>
.data-panel-management {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}
</style>
