<template>
  <div v-if="selectedModel" id="object-info">
    <el-tabs type="border-card">
      <el-tab-pane label="模型参数">
        <div class="property-group">
          <div class="property">
            <label>名称: <input type="text" v-model="selectedModel.name" disabled></label>
          </div>
        </div>
        <div class="property-group">
          <strong>位置</strong>
          <div class="property"><label>X: <input type="number" v-model.number="selectedModel.position.x" step="0.1"></label></div>
          <div class="property"><label>Y: <input type="number" v-model.number="selectedModel.position.y" step="0.1"></label></div>
          <div class="property"><label>Z: <input type="number" v-model.number="selectedModel.position.z" step="0.1"></label></div>
        </div>
        <div class="property-group">
          <strong>缩放</strong>
          <div class="property"><label>X: <input type="number" v-model.number="selectedModel.scale.x" step="0.1"></label></div>
          <div class="property"><label>Y: <input type="number" v-model.number="selectedModel.scale.y" step="0.1"></label></div>
          <div class="property"><label>Z: <input type="number" v-model.number="selectedModel.scale.z" step="0.1"></label></div>
        </div>
        <div class="property-group">
          <strong>旋转</strong>
          <div class="property"><label>X: <input type="number" v-model.number="selectedModel.rotation.x" step="0.1"></label></div>
          <div class="property"><label>Y: <input type="number" v-model.number="selectedModel.rotation.y" step="0.1"></label></div>
          <div class="property"><label>Z: <input type="number" v-model.number="selectedModel.rotation.z" step="0.1"></label></div>
        </div>
        <button @click="deleteModel" class="delete-button">删除模型</button>
      </el-tab-pane>

      <el-tab-pane label="贴图">
        <div class="texture-panel scrollable-panel">
          <div class="property-group">
            <label>选择贴图文件:</label>
            <input type="file" @change="handleTextureUpload" accept="image/*" multiple>
          </div>

          <div v-if="selectedTextures.length > 0" class="file-list">
            <div
              v-for="(file, index) in selectedTextures"
              :key="index"
              class="file-item"
              :class="{ selected: index === selectedTextureIndex }"
              @click="selectTexture(index)"
            >
              <span class="file-name">{{ file.name }}</span>
              <button @click.stop="removeFile(index)" class="remove-file-btn">x</button>
            </div>
          </div>

          <div v-if="selectedTextures.length > 0" class="property-group">
            <label>贴图类型:</label>
            <select v-model="selectedTextureType" class="texture-type-select">
              <option value="color">基础颜色贴图</option>
              <option value="normal">法线贴图</option>
              <option value="roughness">粗糙度贴图</option>
              <option value="metalness">金属度贴图</option>
              <option value="ao">AO 贴图</option>
              <option value="emissive">自发光贴图</option>
            </select>
          </div>

          <div class="property-group">
            <button @click="applyTexture" :disabled="selectedTextures.length === 0">应用贴图</button>
            <button @click="removeTexture">移除贴图</button>
          </div>

          <div v-if="texturePreviews[selectedTextureIndex]" class="property-group">
            <label>当前预览:</label>
            <div class="texture-preview-container">
              <img :src="texturePreviews[selectedTextureIndex]" alt="Texture Preview" class="texture-preview" />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="数据绑定">
        <div class="binding-intro">
          先给模型绑定设备与数据项，展板只负责展示这个模型的数据。
        </div>
        <div class="property-group">
          <label>当前模型ID</label>
          <div class="binding-value">{{ modelStorageId }}</div>
        </div>
        <div class="property-group">
          <label>选择设备</label>
          <select v-model="bindingForm.deviceId" @change="handleDeviceChange">
            <option :value="null">请选择设备</option>
            <option v-for="device in devices" :key="device.id" :value="device.id">
              {{ device.name }} ({{ device.deviceId }})
            </option>
          </select>
        </div>
        <div class="property-group">
          <label>选择数据项</label>
          <select v-model="bindingForm.dataType">
            <option value="">请选择数据项</option>
            <option v-for="item in dataTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div class="property-group">
          <label>状态</label>
          <select v-model.number="bindingForm.ruleStatus">
            <option :value="1">启用</option>
            <option :value="0">停用</option>
          </select>
        </div>
        <div class="property-group current-binding" v-if="currentBinding">
          <strong>当前绑定</strong>
          <div>设备: {{ currentBinding.deviceName || '-' }}</div>
          <div>设备编码: {{ currentBinding.deviceCode || '-' }}</div>
          <div>数据项: {{ currentBinding.dataType || '-' }}</div>
        </div>
        <div class="property-group actions-row">
          <button @click="saveBinding" :disabled="!canSaveBinding">保存绑定</button>
          <button @click="removeBinding" :disabled="!currentBinding">解除绑定</button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="环境设置">
        <div class="property-group">
          <strong>天空背景</strong>
          <el-select v-model="skyType" @change="changeSkyType">
            <el-option label="纯色" value="color"></el-option>
            <el-option label="天空盒" value="skybox"></el-option>
            <el-option label="动态天空" value="dynamic"></el-option>
            <el-option label="无" value="none"></el-option>
          </el-select>
        </div>
        <div v-if="skyType === 'color'" class="property-group">
          <label>天空颜色:
            <input type="color" v-model="skyColor" @change="updateSkyColor">
          </label>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getAllDevices } from '@/api/device'

const DATA_TYPE_OPTIONS = [
  { label: '温度', value: 'temperature' },
  { label: '湿度', value: 'humidity' },
  { label: '压力', value: 'pressure' },
  { label: '电压', value: 'voltage' },
  { label: '电流', value: 'current' },
  { label: '功率', value: 'power' }
]

export default {
  props: {
    selectedModel: { type: Object, required: true },
    sceneId: { type: [String, Number], default: null }
  },
  data() {
    return {
      skyType: 'none',
      skyColor: '#eeeeee',
      selectedTextures: [],
      texturePreviews: [],
      textureApplied: false,
      selectedTextureIndex: 0,
      selectedTextureType: 'color',
      devices: [],
      bindingForm: {
        deviceId: null,
        deviceCode: '',
        deviceName: '',
        dataType: '',
        ruleStatus: 1
      },
      dataTypeOptions: DATA_TYPE_OPTIONS
    }
  },
  computed: {
    modelStorageId() {
      return this.selectedModel?.userData?.sceneModelId || this.selectedModel?.uuid || this.selectedModel?.name || ''
    },
    currentBinding() {
      return this.selectedModel?.boundData || null
    },
    canSaveBinding() {
      return !!this.modelStorageId && !!this.bindingForm.deviceId && !!this.bindingForm.dataType
    }
  },
  watch: {
    selectedModel: {
      deep: true,
      immediate: true,
      handler(newVal) {
        if (!newVal) return
        newVal.position.set(this.selectedModel.position.x, this.selectedModel.position.y, this.selectedModel.position.z)
        newVal.scale.set(this.selectedModel.scale.x, this.selectedModel.scale.y, this.selectedModel.scale.z)
        newVal.rotation.set(this.selectedModel.rotation.x, this.selectedModel.rotation.y, this.selectedModel.rotation.z)
        this.syncBindingForm()
      }
    }
  },
  mounted() {
    this.loadDevices()
  },
  methods: {
    async loadDevices() {
      try {
        const response = await getAllDevices()
        this.devices = response.data || response || []
      } catch (error) {
        console.error('load devices failed:', error)
      }
    },
    syncBindingForm() {
      const binding = this.currentBinding
      this.bindingForm = {
        deviceId: binding?.deviceId ?? null,
        deviceCode: binding?.deviceCode || '',
        deviceName: binding?.deviceName || '',
        dataType: binding?.dataType || '',
        ruleStatus: binding?.ruleStatus ?? 1
      }
    },
    handleDeviceChange() {
      const device = this.devices.find(item => item.id === this.bindingForm.deviceId)
      this.bindingForm.deviceCode = device?.deviceId || ''
      this.bindingForm.deviceName = device?.name || ''
    },
    saveBinding() {
      if (!this.canSaveBinding) {
        this.$message.warning('请先选择设备和数据项')
        return
      }
      this.$emit('save-model-binding', {
        sceneId: Number(this.sceneId),
        modelId: this.modelStorageId,
        modelName: this.selectedModel.name,
        deviceId: this.bindingForm.deviceId,
        deviceCode: this.bindingForm.deviceCode,
        deviceName: this.bindingForm.deviceName,
        dataType: this.bindingForm.dataType,
        ruleStatus: this.bindingForm.ruleStatus
      })
    },
    removeBinding() {
      this.$emit('remove-model-binding', this.modelStorageId)
      this.syncBindingForm()
    },
    deleteModel() {
      this.$emit('delete-model', this.selectedModel)
    },
    changeSkyType(type) {
      this.$emit('environment-change', { type, color: this.skyColor })
    },
    updateSkyColor() {
      this.$emit('environment-change', { type: this.skyType, color: this.skyColor })
    },
    handleTextureUpload(event) {
      for (let i = 0; i < event.target.files.length; i++) {
        const file = event.target.files[i]
        this.selectedTextures.push(file)
        this.texturePreviews.push(URL.createObjectURL(file))
      }
      event.target.value = ''
    },
    selectTexture(index) {
      this.selectedTextureIndex = index
    },
    removeFile(index) {
      this.selectedTextures.splice(index, 1)
      this.texturePreviews.splice(index, 1)
      if (this.selectedTextureIndex >= index && this.selectedTextureIndex > 0) {
        this.selectedTextureIndex--
      }
      if (this.selectedTextures.length === 0) {
        this.selectedTextureIndex = 0
      }
    },
    applyTexture() {
      if (this.selectedTextures.length === 0 || !this.selectedModel) return
      const file = this.selectedTextures[this.selectedTextureIndex]
      const reader = new FileReader()
      reader.onload = (e) => {
        this.$emit('apply-texture', this.selectedModel, e.target.result, this.selectedTextureType)
        this.textureApplied = true
      }
      reader.readAsDataURL(file)
    },
    removeTexture() {
      this.selectedTextures = []
      this.texturePreviews = []
      this.textureApplied = false
      this.selectedTextureIndex = 0
      this.$emit('remove-texture', this.selectedModel)
    }
  }
}
</script>

<style scoped>
#object-info {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: white;
  padding: 15px;
  width: 340px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  font-family: Arial, sans-serif;
  z-index: 10;
}

.property-group {
  margin-bottom: 15px;
}

.property-group strong {
  display: block;
  margin-bottom: 5px;
  color: #333;
}

.property {
  margin-bottom: 5px;
  display: flex;
  justify-content: space-between;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

input,
select {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  box-sizing: border-box;
}

button {
  padding: 8px 12px;
  border: none;
  background: #409eff;
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.delete-button {
  background: #f56c6c;
  width: 100%;
}

.binding-intro {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f4f8ff;
  color: #456;
  font-size: 12px;
  line-height: 1.5;
}

.binding-value {
  padding: 8px 10px;
  border-radius: 6px;
  background: #f7f7f7;
  color: #333;
  word-break: break-all;
}

.current-binding {
  padding: 10px 12px;
  border-radius: 8px;
  background: #f6fff2;
  color: #333;
}

.actions-row {
  display: flex;
  gap: 8px;
}

.texture-preview-container {
  display: flex;
  justify-content: center;
}

.texture-preview {
  width: 100%;
  max-height: 150px;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f7f7f7;
  cursor: pointer;
}

.file-item.selected {
  background: #eaf3ff;
  border: 1px solid #409eff;
}

.file-name {
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-file-btn {
  width: auto;
  padding: 4px 8px;
  background: #f56c6c;
}
</style>
