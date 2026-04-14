<template>
  <div class="model-management">
    <h1>模型管理</h1>

    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加模型
      </el-button>
      <el-button type="success" @click="showUploadDialog">
        <el-icon><Upload /></el-icon> 上传模型
      </el-button>
      <el-button @click="loadModels">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-table :data="models" style="width: 100%" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="模型名称" width="180" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="filePath" label="文件路径" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editModel(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteModelConfirm(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '添加模型'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="模型名称" required>
          <el-input v-model="form.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="建筑" value="建筑" />
            <el-option label="设备" value="设备" />
            <el-option label="管道" value="管道" />
            <el-option label="阀门" value="阀门" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="form.filePath" placeholder="模型文件路径" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="模型描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" title="上传模型" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="分类" required>
          <el-select v-model="uploadForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="建筑" value="建筑" />
            <el-option label="设备" value="设备" />
            <el-option label="管道" value="管道" />
            <el-option label="阀门" value="阀门" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称" required>
          <el-input v-model="uploadForm.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".glb,.gltf,.fbx,.obj"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 .glb, .gltf, .fbx, .obj 格式</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Upload, Refresh } from '@element-plus/icons-vue';
import { getAllModels, createModel, updateModel, deleteModel, uploadModel } from '@/api/index.js';

const models = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const uploadDialogVisible = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const uploading = ref(false);
const editId = ref(null);
const uploadRef = ref(null);
const selectedFile = ref(null);

const form = ref({
  name: '',
  category: '',
  filePath: '',
  description: ''
});

const uploadForm = ref({
  name: '',
  category: ''
});

const resetForm = () => {
  form.value = {
    name: '',
    category: '',
    filePath: '',
    description: ''
  };
  editId.value = null;
  isEdit.value = false;
};

const loadModels = async () => {
  loading.value = true;
  try {
    const response = await getAllModels();
    models.value = response.data || [];
  } catch (error) {
    ElMessage.error('加载模型列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const showAddDialog = () => {
  resetForm();
  dialogVisible.value = true;
};

const editModel = (row) => {
  isEdit.value = true;
  editId.value = row.id;
  form.value = {
    name: row.name,
    category: row.category,
    filePath: row.filePath,
    description: row.description || ''
  };
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!form.value.name || !form.value.category) {
    ElMessage.warning('请填写必填项');
    return;
  }

  submitting.value = true;
  try {
    if (isEdit.value) {
      await updateModel(editId.value, form.value);
      ElMessage.success('更新成功');
    } else {
      await createModel(form.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    loadModels();
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败');
    console.error(error);
  } finally {
    submitting.value = false;
  }
};

const deleteModelConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除模型 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteModel(row.id);
    ElMessage.success('删除成功');
    loadModels();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }
};

const showUploadDialog = () => {
  uploadForm.value = { name: '', category: '' };
  selectedFile.value = null;
  uploadDialogVisible.value = true;
};

const handleFileChange = (file) => {
  selectedFile.value = file.raw;
  if (!uploadForm.value.name && file.name) {
    uploadForm.value.name = file.name.replace(/\.[^.]+$/, '');
  }
};

const submitUpload = async () => {
  if (!uploadForm.value.name || !uploadForm.value.category) {
    ElMessage.warning('请填写必填项');
    return;
  }
  if (!selectedFile.value) {
    ElMessage.warning('请选择模型文件');
    return;
  }

  uploading.value = true;
  try {
    await uploadModel(uploadForm.value.category, uploadForm.value.name, selectedFile.value);
    ElMessage.success('上传成功');
    uploadDialogVisible.value = false;
    loadModels();
  } catch (error) {
    ElMessage.error('上传失败');
    console.error(error);
  } finally {
    uploading.value = false;
  }
};

onMounted(() => {
  loadModels();
});
</script>

<style scoped>
.model-management {
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
