<template>
  <div class="composite-model-editor" v-if="visible">
    <div class="editor-overlay" @click="closeEditor"></div>
    <div class="editor-container">
      <div class="editor-header">
        <h2 class="editor-title">组合模型编辑器</h2>
        <el-button @click="closeEditor" type="danger" size="medium">×</el-button>
      </div>
      
      <div class="editor-content">
        <!-- 左侧面板 -->
        <div class="left-panel">
          <!-- 组合模型信息 -->
          <div class="panel-section model-info-section">
            <div class="section-header">
              <h3><i class="el-icon-info"></i> 组合模型信息</h3>
            </div>
            <el-form :model="compositeModel" label-width="80px" size="small" class="model-form">
              <el-form-item label="名称">
                <el-input v-model="compositeModel.name" placeholder="请输入组合模型名称" clearable></el-input>
              </el-form-item>
              <el-form-item label="描述">
                <el-input 
                  v-model="compositeModel.description" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入组合模型描述">
                </el-input>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 组件列表 -->
          <div class="panel-section components-section">
            <div class="section-header">
              <h3><i class="el-icon-files"></i> 组件列表</h3>
              <el-button @click="selectModelsFromScene" type="primary" size="small" icon="el-icon-plus">
                添加模型
              </el-button>
            </div>
            
            <div class="components-list">
              <div 
                v-for="(component, index) in components" 
                :key="index"
                :class="['component-item', { selected: selectedComponentIndex === index }]"
                @click="selectComponent(index)"
              >
                <div class="component-info">
                  <div class="component-name">
                    <i class="el-icon-box"></i> {{ component.modelName }}
                  </div>
                  <div class="component-transform">
                    位置: ({{ component.position.x.toFixed(2) }}, {{ component.position.y.toFixed(2) }}, {{ component.position.z.toFixed(2) }})
                  </div>
                </div>
                <el-button 
                  @click.stop="removeComponent(index)" 
                  type="danger" 
                  size="mini" 
                  icon="el-icon-delete"
                  circle
                ></el-button>
              </div>
              
              <div v-if="components.length === 0" class="empty-components">
                <div class="empty-placeholder">
                  <i class="el-icon-folder-opened empty-icon"></i>
                  <p>暂无组件</p>
                  <el-button @click="selectModelsFromScene" type="primary" size="mini" icon="el-icon-plus">
                    添加模型
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧面板 -->
        <div class="right-panel">
          <div class="panel-section properties-section">
            <div class="section-header">
              <h3><i class="el-icon-setting"></i> 属性面板</h3>
            </div>
            
            <div v-if="selectedComponentIndex >= 0 && components[selectedComponentIndex]" class="component-properties">
              <div class="selected-component-header">
                <h4><i class="el-icon-box"></i> {{ components[selectedComponentIndex].modelName }}</h4>
              </div>
              
              <div class="property-groups">
                <div class="property-group">
                  <div class="group-header">
                    <i class="el-icon-location-outline"></i>
                    <h5>位置</h5>
                  </div>
                  <div class="property-row">
                    <el-form-item label="X">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].position.x" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                    <el-form-item label="Y">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].position.y" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                    <el-form-item label="Z">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].position.z" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                  </div>
                </div>
                
                <div class="property-group">
                  <div class="group-header">
                    <i class="el-icon-refresh-right"></i>
                    <h5>旋转</h5>
                  </div>
                  <div class="property-row">
                    <el-form-item label="X">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].rotation.x" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                    <el-form-item label="Y">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].rotation.y" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                    <el-form-item label="Z">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].rotation.z" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                  </div>
                </div>
                
                <div class="property-group">
                  <div class="group-header">
                    <i class="el-icon-full-screen"></i>
                    <h5>缩放</h5>
                  </div>
                  <div class="property-row">
                    <el-form-item label="X">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].scale.x" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                    <el-form-item label="Y">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].scale.y" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                    <el-form-item label="Z">
                      <el-input-number 
                        v-model="components[selectedComponentIndex].scale.z" 
                        :step="0.1" 
                        size="mini"
                        controls-position="right"
                      ></el-input-number>
                    </el-form-item>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-else class="no-selection">
              <div class="empty-placeholder">
                <i class="el-icon-select empty-icon"></i>
                <p>请选择一个组件</p>
              </div>
            </div>
          </div>
          
          <div class="panel-section action-section">
            <el-button 
              @click="saveCompositeModel" 
              type="success" 
              size="medium" 
              icon="el-icon-check"
              style="width: 100%"
            >
              保存组合模型
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 模型选择器 -->
    <ModelSelector 
      v-if="isModelSelectorVisible"
      :visible="isModelSelectorVisible"
      :data="modelData"
      @model-selected="handleModelSelection"
      @close="isModelSelectorVisible = false"
    />
  </div>
</template>

<script>
import { createCompositeModel, getFileUrl } from '@/api/index.js';
import ModelSelector from './ModelSelector.vue';

export default {
  name: "CompositeModelEditor",
  components: {
    ModelSelector
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    sceneModels: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      compositeModel: {
        name: '',
        description: ''
      },
      components: [],
      selectedComponentIndex: -1,
      isModelSelectorVisible: false,
      modelData: [], // 添加模型数据属性
      saveEventTimeout: null // 用于防抖的定时器
    };
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal) {
          // 当编辑器打开时，确保模型数据已加载
          if (this.modelData.length === 0) {
            this.loadModelData();
          }
        } else {
          // 关闭时重置表单
          this.compositeModel = {
            name: '',
            description: ''
          };
          this.components = [];
          this.selectedComponentIndex = -1;
          this.isModelSelectorVisible = false;
        }
      }
    }
  },
  methods: {
    // 加载模型数据
    async loadModelData() {
      try {
        // 通过事件向父组件请求模型数据
        this.$emit('request-model-data');
      } catch (error) {
        console.error('加载模型数据失败:', error);
      }
    },
    
    // 接收模型数据
    receiveModelData(data) {
      console.log('CompositeModelEditor 接收到模型数据:', data);
      console.log('数据长度:', data ? data.length : 'null');
      console.log('数据类型:', typeof data);
      if (Array.isArray(data)) {
        console.log('第一个元素:', data[0]);
      }
      this.modelData = data || [];
    },
    
    // 从场景中选择模型
    selectModelsFromScene() {
      this.isModelSelectorVisible = true;
      // 确保在打开选择器前数据已加载
      if (this.modelData.length === 0) {
        this.loadModelData();
      }
    },
    
    // 处理模型选择
    handleModelSelection(modelData) {
      console.log('接收到选中的模型:', modelData);
      
      // 验证模型数据
      if (!modelData || !modelData.file) {
        this.$message.error('模型数据不完整');
        return;
      }
      
      // 提取模型ID（文件名）
      let modelId = modelData.file;
      const fileUrlPrefix = getFileUrl('');
      if (modelId.startsWith(fileUrlPrefix)) {
        modelId = modelId.replace(fileUrlPrefix, '');
      }
      
      // 检查是否已添加该模型
      const exists = this.components.some(component => component.modelId === modelId);
      if (!exists) {
        this.components.push({
          modelId: modelId, // 使用清理后的modelId
          modelName: modelData.name || '未命名模型',
          position: { x: 0, y: 0, z: 0 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        });
        this.$message.success(`已添加模型: ${modelData.name}`);
      } else {
        this.$message.warning('该模型已添加到组合中');
      }
      
      this.isModelSelectorVisible = false;
    },
    
    // 选择组件
    selectComponent(index) {
      this.selectedComponentIndex = index;
    },
    
    // 移除组件
    removeComponent(index) {
      this.components.splice(index, 1);
      if (this.selectedComponentIndex >= index && this.selectedComponentIndex > 0) {
        this.selectedComponentIndex--;
      } else if (this.selectedComponentIndex >= this.components.length) {
        this.selectedComponentIndex = this.components.length - 1;
      }
    },
    
    // 保存组合模型
    async saveCompositeModel() {
      if (!this.compositeModel.name) {
        this.$message.warning('请输入组合模型名称');
        return;
      }
      
      if (this.components.length === 0) {
        this.$message.warning('请至少添加一个组件');
        return;
      }
      
      // 验证组件数据
      const invalidComponents = this.components.filter(comp => 
        !comp.modelId || comp.modelId.trim() === ''
      );
      
      if (invalidComponents.length > 0) {
        this.$message.warning('存在无效的模型组件，请检查');
        return;
      }
      
      // 构造要发送到后端的数据
      const compositeModelData = {
        name: this.compositeModel.name.trim(),
        description: this.compositeModel.description ? this.compositeModel.description.trim() : '',
        createdBy: 'user', // 实际项目中应该从登录用户信息获取
        components: this.components.map(component => {
          // 确保modelId是清理后的格式
          let modelId = component.modelId;
          const fileUrlPrefix = getFileUrl('');
          if (modelId && modelId.startsWith(fileUrlPrefix)) {
            modelId = modelId.replace(fileUrlPrefix, '');
          }
          
          return {
            modelId: modelId,
            position_x: parseFloat(component.position.x) || 0,
            position_y: parseFloat(component.position.y) || 0,
            position_z: parseFloat(component.position.z) || 0,
            rotation_x: parseFloat(component.rotation.x) || 0,
            rotation_y: parseFloat(component.rotation.y) || 0,
            rotation_z: parseFloat(component.rotation.z) || 0,
            scale_x: parseFloat(component.scale.x) || 1,
            scale_y: parseFloat(component.scale.y) || 1,
            scale_z: parseFloat(component.scale.z) || 1
          };
        })
      };
      
      console.log('发送到后端的组合模型数据:', JSON.stringify(compositeModelData, null, 2));
      
      try {
        // 调用API保存组合模型
        const response = await createCompositeModel(compositeModelData);
        console.log('保存组合模型成功:', response);
        this.$message.success('组合模型保存成功');
        
        // 触发事件通知其他组件刷新数据
        if (window.eventBus) {
          // 使用防抖机制，确保事件只触发一次
          clearTimeout(this.saveEventTimeout);
          this.saveEventTimeout = setTimeout(() => {
            window.eventBus.emit('composite-model-saved');
          }, 100);
        }
        
        this.closeEditor();
      } catch (error) {
        console.error('保存组合模型失败:', error);
        console.error('错误详情:', error.response || error.message || error);
        
        // 提供更详细的错误信息
        if (error.response) {
          const status = error.response.status;
          const data = error.response.data;
          
          if (status === 400) {
            this.$message.error(`请求数据格式错误: ${data.message || '请检查数据格式'}`);
          } else if (status === 409) {
            this.$message.error('保存失败: 已存在同名的组合模型');
          } else if (status === 500) {
            this.$message.error('服务器内部错误，请联系管理员');
            console.error('服务器错误详情:', data);
          } else {
            this.$message.error(`保存失败: ${data.message || '未知错误'}`);
          }
        } else {
          this.$message.error('保存失败: ' + (error.message || '网络错误'));
        }
      }
    },
    
    // 关闭编辑器
    closeEditor() {
      // 重置表单
      this.compositeModel = {
        name: '',
        description: ''
      };
      this.components = [];
      this.selectedComponentIndex = -1;
      this.isModelSelectorVisible = false;
      
      this.$emit('close');
    }
  }
};
</script>

<style scoped>
.composite-model-editor {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.editor-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(3px);
  transition: all 0.3s ease;
}

.editor-container {
  position: relative;
  width: 90%;
  max-width: 1200px;
  height: 85%;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  z-index: 1;
  transform: scale(0.95);
  animation: scaleIn 0.3s ease forwards;
}

@keyframes scaleIn {
  to {
    transform: scale(1);
  }
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
  color: white;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  box-shadow: 0 4px 12px rgba(66, 165, 245, 0.3);
}

.editor-header .el-button--danger {
  font-size: 18px;
  padding: 8px 12px;
  min-width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.editor-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.editor-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.left-panel {
  width: 60%;
  border-right: 1px solid #e3f2fd;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #f5faff 100%);
}

.right-panel {
  width: 40%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.panel-section {
  border-bottom: 1px solid #e3f2fd;
  padding: 25px;
  margin: 0;
  transition: all 0.3s ease;
}

.panel-section:last-child {
  border-bottom: none;
}

.model-info-section {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.components-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.properties-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.action-section {
  background: linear-gradient(180deg, #f8f9ff 0%, #f0f4ff 100%);
  padding: 20px 25px;
  border-top: 1px solid #e3f2fd;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e3f2fd;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1976d2;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.section-header h3 i {
  font-size: 20px;
  color: #42a5f5;
}

.model-form .el-form-item {
  margin-bottom: 20px;
}

.model-form .el-input__inner,
.model-form .el-textarea__inner {
  border-radius: 8px;
  border: 1px solid #bbdefb;
  transition: all 0.3s ease;
}

.model-form .el-input__inner:focus,
.model-form .el-textarea__inner:focus {
  border-color: #42a5f5;
  box-shadow: 0 0 0 3px rgba(66, 165, 245, 0.1);
}

.components-list {
  flex: 1;
  overflow-y: auto;
  background: white;
  border-radius: 10px;
  border: 1px solid #e3f2fd;
  padding: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

/* 滚动条样式 */
.components-list::-webkit-scrollbar,
.property-groups::-webkit-scrollbar {
  width: 6px;
}

.components-list::-webkit-scrollbar-track,
.property-groups::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.components-list::-webkit-scrollbar-thumb,
.property-groups::-webkit-scrollbar-thumb {
  background: #bbdefb;
  border-radius: 3px;
}

.components-list::-webkit-scrollbar-thumb:hover,
.property-groups::-webkit-scrollbar-thumb:hover {
  background: #90caf9;
}

.component-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border: 2px solid #e3f2fd;
  margin-bottom: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
}

.component-item:hover {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-color: #42a5f5;
  box-shadow: 0 4px 15px rgba(66, 165, 245, 0.2);
  transform: translateY(-2px);
}

.component-item.selected {
  background: linear-gradient(135deg, #bbdefb 0%, #90caf9 100%);
  border-color: #1976d2;
  box-shadow: 0 6px 20px rgba(25, 118, 210, 0.3);
}

.component-info {
  flex: 1;
}

.component-name {
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1976d2;
  font-size: 16px;
}

.component-name i {
  color: #42a5f5;
}

.component-transform {
  font-size: 13px;
  color: #546e7a;
  font-family: 'Courier New', monospace;
  background: rgba(255, 255, 255, 0.7);
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid rgba(66, 165, 245, 0.2);
}

.empty-components {
  text-align: center;
  padding: 60px 0;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-placeholder {
  text-align: center;
  padding: 30px;
  background: linear-gradient(135deg, #ffffff 0%, #f5faff 100%);
  border-radius: 12px;
  border: 2px dashed #bbdefb;
}

.empty-placeholder .empty-icon {
  font-size: 64px;
  color: #bbdefb;
  margin-bottom: 20px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.empty-placeholder p {
  margin: 0 0 20px 0;
  color: #546e7a;
  font-size: 16px;
}

.component-properties {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.selected-component-header {
  padding: 15px 0;
  border-bottom: 2px solid #e3f2fd;
  margin-bottom: 20px;
}

.selected-component-header h4 {
  margin: 0;
  text-align: center;
  color: #1976d2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
}

.selected-component-header h4 i {
  color: #42a5f5;
}

.property-groups {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px;
}

.property-group {
  margin-bottom: 25px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e3f2fd;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.property-group:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border-color: #bbdefb;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: #1976d2;
  padding-bottom: 10px;
  border-bottom: 1px solid #e3f2fd;
}

.group-header h5 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.group-header i {
  font-size: 18px;
  color: #42a5f5;
}

.property-row {
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.property-row .el-form-item {
  margin-bottom: 0;
  flex: 1;
}

.property-row .el-form-item__label {
  font-size: 13px;
  padding: 0 0 8px 0;
  color: #546e7a;
  font-weight: 500;
}

.property-row .el-input-number {
  width: 100%;
}

.property-row .el-input-number__decrease,
.property-row .el-input-number__increase {
  border-radius: 0;
  background: #f5faff;
  border-color: #bbdefb;
}

.property-row .el-input-number__decrease:hover,
.property-row .el-input-number__increase:hover {
  background: #bbdefb;
  color: #1976d2;
}

.property-row .el-input-number__inner {
  border-radius: 8px;
  border: 1px solid #bbdefb;
  transition: all 0.3s ease;
}

.property-row .el-input-number__inner:focus {
  border-color: #42a5f5;
  box-shadow: 0 0 0 3px rgba(66, 165, 245, 0.1);
}

.no-selection {
  text-align: center;
  padding: 60px 20px;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffffff 0%, #f5faff 100%);
  border-radius: 12px;
  border: 2px dashed #bbdefb;
}

.action-section .el-button {
  border-radius: 10px;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
  border: none;
  transition: all 0.3s ease;
}

.action-section .el-button:hover {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
  box-shadow: 0 6px 20px rgba(66, 165, 245, 0.4);
  transform: translateY(-2px);
}

.action-section .el-button--success {
  background: linear-gradient(135deg, #81c784 0%, #4caf50 100%);
}

.action-section .el-button--success:hover {
  background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
}

/* 按钮通用样式优化 */
.el-button {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-button--primary {
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
  border: none;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
  box-shadow: 0 4px 15px rgba(66, 165, 245, 0.3);
}

.el-button--danger {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  border: none;
}

.el-button--danger:hover {
  background: linear-gradient(135deg, #e53935 0%, #c62828 100%);
  box-shadow: 0 4px 15px rgba(229, 57, 53, 0.3);
}

.el-button--small {
  padding: 6px 12px;
}

.el-button--mini {
  padding: 4px 8px;
}

@media (max-width: 992px) {
  .editor-container {
    width: 98%;
    height: 95%;
    border-radius: 10px;
  }
  
  .editor-content {
    flex-direction: column;
  }
  
  .left-panel, .right-panel {
    width: 100%;
  }
  
  .left-panel {
    border-right: none;
    border-bottom: 1px solid #e3f2fd;
  }
  
  .panel-section {
    padding: 20px;
  }
  
  .editor-title {
    font-size: 20px;
  }
}
</style>