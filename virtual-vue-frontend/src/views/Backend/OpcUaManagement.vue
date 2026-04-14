<template>
  <div class="opcua-management">
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>OPC UA 服务器状态</span>
          <el-button type="primary" @click="refreshStatus" :loading="loading">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <div v-if="status" class="status-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="运行状态">
            <el-tag :type="status.running ? 'success' : 'danger'">
              {{ status.running ? '运行中' : '已停止' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设备数量">
            <el-tag type="info">{{ status.deviceCount || 0 }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="action-buttons">
          <el-button type="success" @click="syncDevices" :loading="syncing">
            <el-icon><Connection /></el-icon> 同步设备到OPC UA
          </el-button>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 创建设备节点
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="devices-card">
      <template #header>
        <div class="card-header">
          <span>OPC UA 设备节点列表</span>
          <el-input
            v-model="searchText"
            placeholder="搜索设备..."
            style="width: 200px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>

      <el-table :data="filteredDevices" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="设备ID" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'Online' ? 'success' : 'info'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="data" label="数据">
          <template #default="scope">
            <el-text truncated>{{ scope.row.data || '{}' }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDeviceData(scope.row)">查看数据</el-button>
            <el-button size="small" type="danger" @click="deleteDeviceNode(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="创建设备节点" width="400px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="设备ID" required>
          <el-input v-model="createForm.deviceName" placeholder="例如: DEV-001" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" placeholder="设备描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createDeviceNode" :loading="creating">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialogVisible" title="设备数据" width="500px">
      <el-input
        v-model="deviceDataContent"
        type="textarea"
        :rows="10"
        readonly
      />
      <template #footer>
        <el-button @click="dataDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Connection, Plus, Search } from '@element-plus/icons-vue';
import { getOpcUaStatus, syncOpcUaDevices, createOpcUaDevice, deleteOpcUaDevice } from '@/api/index.js';

const loading = ref(false);
const syncing = ref(false);
const creating = ref(false);
const status = ref(null);
const searchText = ref('');
const createDialogVisible = ref(false);
const dataDialogVisible = ref(false);
const deviceDataContent = ref('');

const createForm = ref({
  deviceName: '',
  description: ''
});

const devices = computed(() => status.value?.devices || []);

const filteredDevices = computed(() => {
  if (!searchText.value) return devices.value;
  return devices.value.filter(d => 
    d.name.toLowerCase().includes(searchText.value.toLowerCase())
  );
});

const refreshStatus = async () => {
  loading.value = true;
  try {
    const response = await getOpcUaStatus();
    status.value = response.data.data;
  } catch (error) {
    ElMessage.error('获取OPC UA状态失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const syncDevices = async () => {
  syncing.value = true;
  try {
    const response = await syncOpcUaDevices();
    if (response.data.data) {
      ElMessage.success(`同步完成: 创建 ${response.data.data.created} 个, 跳过 ${response.data.data.skipped} 个`);
      refreshStatus();
    }
  } catch (error) {
    ElMessage.error('同步设备失败');
    console.error(error);
  } finally {
    syncing.value = false;
  }
};

const showCreateDialog = () => {
  createForm.value = { deviceName: '', description: '' };
  createDialogVisible.value = true;
};

const createDeviceNode = async () => {
  if (!createForm.value.deviceName) {
    ElMessage.warning('请输入设备ID');
    return;
  }

  creating.value = true;
  try {
    await createOpcUaDevice(createForm.value);
    ElMessage.success('设备节点创建成功');
    createDialogVisible.value = false;
    refreshStatus();
  } catch (error) {
    ElMessage.error('创建设备节点失败');
    console.error(error);
  } finally {
    creating.value = false;
  }
};

const viewDeviceData = (device) => {
  try {
    const data = JSON.parse(device.data || '{}');
    deviceDataContent.value = JSON.stringify(data, null, 2);
  } catch {
    deviceDataContent.value = device.data || '{}';
  }
  dataDialogVisible.value = true;
};

const deleteDeviceNode = async (device) => {
  try {
    await ElMessageBox.confirm(`确定要删除设备节点 "${device.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await deleteOpcUaDevice(device.name);
    ElMessage.success('设备节点已删除');
    refreshStatus();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除设备节点失败');
      console.error(error);
    }
  }
};

onMounted(() => {
  refreshStatus();
});
</script>

<style scoped>
.opcua-management {
  padding: 20px;
}

.status-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-content {
  margin-top: 10px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.devices-card {
  margin-top: 20px;
}
</style>
