<template>
  <div class="device-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <el-button type="primary" @click="showRegisterDialog">注册设备</el-button>
        </div>
      </template>

      <el-table :data="devices" style="width: 100%" v-loading="loading">
        <el-table-column prop="deviceId" label="设备ID" width="180" />
        <el-table-column prop="name" label="设备名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="在线状态" width="100">
          <template #default="scope">
            <el-tag :type="isOnline(scope.row) ? 'success' : 'info'">
              {{ isOnline(scope.row) ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certificateThumbprint" label="证书状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.certificateThumbprint ? 'success' : 'info'">
              {{ scope.row.certificateThumbprint ? '已颁发' : '未颁发' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastSeenAt" label="最后在线" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.lastSeenAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="info" @click="showCertificateDialog(scope.row)">证书</el-button>
            <el-button size="small" type="danger" @click="deleteDevice(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="registerDialogVisible" title="注册设备" width="500px">
      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="设备名称">
          <el-input v-model="registerForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="registerForm.description" type="textarea" placeholder="请输入设备描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="registerDevice">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑设备" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="设备名称">
          <el-input v-model="editForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" placeholder="请输入设备描述" />
        </el-form-item>
        <el-form-item label="绑定用户">
          <el-input v-model.number="editForm.userId" placeholder="请输入用户ID" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateDevice">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="certificateDialogVisible" title="设备证书" width="600px">
      <div v-if="currentDevice">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备ID">{{ currentDevice.deviceId }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
          <el-descriptions-item label="证书状态">
            <el-tag :type="currentDevice.certificateThumbprint ? 'success' : 'info'">
              {{ currentDevice.certificateThumbprint ? '已颁发' : '未颁发' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDevice.certificateThumbprint" label="证书指纹">
            {{ currentDevice.certificateThumbprint }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="certificateData" class="certificate-content">
          <h4>证书内容</h4>
          <el-input
            v-model="certificateData.certificate"
            type="textarea"
            :rows="10"
            readonly
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="certificateDialogVisible = false">关闭</el-button>
        <el-button v-if="!currentDevice?.certificateThumbprint" type="primary" @click="generateCertificate">
          生成证书
        </el-button>
        <el-button v-else type="danger" @click="revokeCertificate">吊销证书</el-button>
        <el-button v-if="certificateData" type="success" @click="downloadCertificate">下载证书</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import deviceApi from '../../api/device';

export default {
  name: 'DeviceManagement',
  setup() {
    const devices = ref([]);
    const loading = ref(false);
    const registerDialogVisible = ref(false);
    const editDialogVisible = ref(false);
    const certificateDialogVisible = ref(false);
    const currentDevice = ref(null);
    const certificateData = ref(null);
    let refreshTimer = null;

    const registerForm = ref({
      name: '',
      description: ''
    });

    const editForm = ref({
      id: null,
      name: '',
      description: '',
      userId: null
    });

    const loadDevices = async (silent = false) => {
      if (!silent) loading.value = true;
      try {
        const response = await deviceApi.getDevices();
        if (response.data) {
          devices.value = response.data;
        }
      } catch (error) {
        ElMessage.error('加载设备列表失败');
      } finally {
        if (!silent) loading.value = false;
      }
    };

    const showRegisterDialog = () => {
      registerForm.value = { name: '', description: '' };
      registerDialogVisible.value = true;
    };

    const registerDevice = async () => {
      try {
        await deviceApi.registerDevice(registerForm.value);
        ElMessage.success('设备注册成功');
        registerDialogVisible.value = false;
        loadDevices();
      } catch (error) {
        ElMessage.error('设备注册失败');
      }
    };

    const showEditDialog = (device) => {
      editForm.value = {
        id: device.id,
        name: device.name,
        description: device.description,
        userId: device.userId
      };
      editDialogVisible.value = true;
    };

    const updateDevice = async () => {
      try {
        await deviceApi.updateDevice(editForm.value.id, {
          name: editForm.value.name,
          description: editForm.value.description
        });
        
        if (editForm.value.userId) {
          await deviceApi.bindDevice(editForm.value.id, editForm.value.userId);
        }
        
        ElMessage.success('设备更新成功');
        editDialogVisible.value = false;
        loadDevices();
      } catch (error) {
        ElMessage.error('设备更新失败');
      }
    };

    const toggleStatus = async (device) => {
      const newStatus = device.status === 1 ? 0 : 1;
      try {
        await deviceApi.updateDeviceStatus(device.id, newStatus);
        ElMessage.success('状态更新成功');
        loadDevices();
      } catch (error) {
        ElMessage.error('状态更新失败');
      }
    };

    const deleteDevice = async (device) => {
      try {
        await ElMessageBox.confirm('确定要删除该设备吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        await deviceApi.deleteDevice(device.id);
        ElMessage.success('设备删除成功');
        loadDevices();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('设备删除失败');
        }
      }
    };

    const showCertificateDialog = async (device) => {
      currentDevice.value = device;
      certificateData.value = null;
      
      if (device.certificateThumbprint) {
        try {
          const response = await deviceApi.getDeviceCertificate(device.id);
          if (response.data) {
            certificateData.value = response.data;
          }
        } catch (error) {
          console.error('获取证书失败', error);
        }
      }
      
      certificateDialogVisible.value = true;
    };

    const generateCertificate = async () => {
      try {
        const response = await deviceApi.generateDeviceCertificate(currentDevice.value.id);
        if (response.data) {
          certificateData.value = response.data;
          currentDevice.value.certificateThumbprint = response.data.thumbprint;
          ElMessage.success('证书生成成功');
          loadDevices();
        }
      } catch (error) {
        ElMessage.error('证书生成失败');
      }
    };

    const revokeCertificate = async () => {
      try {
        await ElMessageBox.confirm('确定要吊销该证书吗？吊销后设备将无法连接。', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        await deviceApi.revokeDeviceCertificate(currentDevice.value.id);
        currentDevice.value.certificateThumbprint = null;
        certificateData.value = null;
        ElMessage.success('证书已吊销');
        loadDevices();
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('证书吊销失败');
        }
      }
    };

    const downloadCertificate = () => {
      if (!certificateData.value?.certificate) return;
      
      const blob = new Blob([certificateData.value.certificate], { type: 'application/x-pem-file' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${currentDevice.value.deviceId}.pem`;
      link.click();
      URL.revokeObjectURL(url);
    };

    const formatTime = (time) => {
      if (!time) return '-';
      return new Date(time).toLocaleString('zh-CN');
    };

    const isOnline = (device) => {
      if (!device || device.status !== 1 || !device.lastSeenAt) return false;
      const lastSeen = new Date(device.lastSeenAt).getTime();
      return Number.isFinite(lastSeen) && Date.now() - lastSeen <= 30000;
    };

    onMounted(() => {
      loadDevices();
      refreshTimer = window.setInterval(() => loadDevices(true), 10000);
    });

    onUnmounted(() => {
      if (refreshTimer) {
        window.clearInterval(refreshTimer);
        refreshTimer = null;
      }
    });

    return {
      devices,
      loading,
      registerDialogVisible,
      editDialogVisible,
      certificateDialogVisible,
      registerForm,
      editForm,
      currentDevice,
      certificateData,
      loadDevices,
      showRegisterDialog,
      registerDevice,
      showEditDialog,
      updateDevice,
      toggleStatus,
      deleteDevice,
      showCertificateDialog,
      generateCertificate,
      revokeCertificate,
      downloadCertificate,
      formatTime,
      isOnline
    };
  }
};
</script>

<style scoped>
.device-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.certificate-content {
  margin-top: 20px;
}

.certificate-content h4 {
  margin-bottom: 10px;
}
</style>
