<template>
  <div v-if="selectedModel" id="object-info">
    <el-tabs type="border-card">
      <el-tab-pane label="模型参数">
        <div v-if="selectedModel">
          <div class="property-group">
            <div class="property">
              <label>名称: <input type="string" v-model.string="selectedModel.name" disabled></label>
            </div>
          </div>
          <!-- 位置 -->
          <div class="property-group">
            <strong>位置：</strong>
            <div class="property">
              <label>X: <input type="number" v-model.number="selectedModel.position.x" step="0.1"></label>
            </div>
            <div class="property">
              <label>Y: <input type="number" v-model.number="selectedModel.position.y" step="0.1"></label>
            </div>
            <div class="property">
              <label>Z: <input type="number" v-model.number="selectedModel.position.z" step="0.1"></label>
            </div>
          </div>

          <!-- 缩放 -->
          <div class="property-group">
            <strong>缩放：</strong>
            <div class="property">
              <label>X: <input type="number" v-model.number="selectedModel.scale.x" step="0.1"></label>
            </div>
            <div class="property">
              <label>Y: <input type="number" v-model.number="selectedModel.scale.y" step="0.1"></label>
            </div>
            <div class="property">
              <label>Z: <input type="number" v-model.number="selectedModel.scale.z" step="0.1"></label>
            </div>
          </div>

          <!-- 旋转 -->
          <div class="property-group">
            <strong>旋转：</strong>
            <div class="property">
              <label>X: <input type="number" v-model.number="selectedModel.rotation.x" step="0.1" /></label>
            </div>
            <div class="property">
              <label>Y: <input type="number" v-model.number="selectedModel.rotation.y" step="0.1" /></label>
            </div>
            <div class="property">
              <label>Z: <input type="number" v-model.number="selectedModel.rotation.z" step="0.1" /></label>
            </div>
          </div>
        </div>
        <button @click="deleteModel" class="delete-button">删除模型</button>
      </el-tab-pane>
      <el-tab-pane label="贴图">
        <div class="texture-panel scrollable-panel">
          <div class="property-group">
            <label>选择贴图文件:</label>
            <input type="file" @change="handleTextureUpload" accept="image/*" multiple ref="fileInput" />
          </div>
          
          <!-- 文件列表显示 -->
          <div v-if="selectedTextures.length > 0" class="file-list">
            <div 
              v-for="(file, index) in selectedTextures" 
              :key="index" 
              class="file-item"
              :class="{ 'selected': index === selectedTextureIndex }"
              @click="selectTexture(index)"
            >
              <span class="file-name">{{ file.name }}</span>
              <button @click.stop="removeFile(index)" class="remove-file-btn">×</button>
            </div>
          </div>
          
          <!-- 贴图类型选择 -->
          <div v-if="selectedTextures.length > 0" class="property-group">
            <label>贴图类型:</label>
            <select v-model="selectedTextureType" class="texture-type-select">
              <option value="color">基础颜色贴图</option>
              <option value="normal">法线贴图</option>
              <option value="roughness">粗糙度贴图</option>
              <option value="metalness">金属度贴图</option>
              <option value="ao">环境光遮蔽贴图</option>
              <option value="emissive">自发光贴图</option>
            </select>
          </div>
          
          <div class="property-group">
            <button @click="applyTexture" :disabled="selectedTextures.length === 0">应用贴图</button>
            <button @click="removeTexture">移除贴图</button>
          </div>
          
          <!-- 预览当前选中的贴图 -->
          <div v-if="texturePreviews[selectedTextureIndex]" class="property-group">
            <label>当前贴图预览 ({{ getTextureTypeName(selectedTextureType) }}):</label>
            <div class="texture-preview-container">
              <img :src="texturePreviews[selectedTextureIndex]" :alt="`Texture Preview`" class="texture-preview" />
            </div>
          </div>
          <div class="property-group" v-if="textureApplied">
            <p style="color: green; font-weight: bold;">✓ 贴图已应用</p>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="关联数据">
        <div v-if="selectedModel.boundData">
          <pre>{{ selectedModel.boundData }}</pre>
        </div>
        <div v-else>
          无关联数据
          <button @click="fetchAndBindData">绑定数据</button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="环境设置">
        <div class="property-group">
          <strong>天空背景:</strong>
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
import * as THREE from "three";

export default {
  data() {
    return {
      skyType: 'none',
      skyColor: '#eeeeee',
      selectedTextures: [], // 改为数组以支持多个文件
      texturePreviews: [], // 改为数组以支持多个预览
      textureApplied: false,
      selectedTextureIndex: 0, // 当前选中的贴图索引
      selectedTextureType: 'color' // 当前贴图类型
    }},
  watch: {
    selectedModel: {
      deep: true,
      handler(newVal) {
        if (newVal) {
          // 同步场景中模型的属性
          newVal.position.set(
              this.selectedModel.position.x,
              this.selectedModel.position.y,
              this.selectedModel.position.z
          );
          newVal.scale.set(
              this.selectedModel.scale.x,
              this.selectedModel.scale.y,
              this.selectedModel.scale.z
          );
          newVal.rotation.set(
              this.selectedModel.rotation.x,
              this.selectedModel.rotation.y,
              this.selectedModel.rotation.z
          );
        }
      }
    }
  },
  props: ["selectedModel"],
  methods: {
    fetchAndBindData() {
      const mockData = { id: 1, name: "Sample Data" };
      this.$emit("bind-data", this.selectedModel, mockData);
    },
    deleteModel() {
      // 触发删除事件，通知父组件
      this.$emit("delete-model", this.selectedModel);
    },
    changeSkyType(type) {
      // 触发父组件的环境设置
      this.$emit('environment-change', {
        type: type,
        color: this.skyColor
      });
    },

    updateSkyColor() {
      this.$emit('environment-change', {
        type: this.skyType,
        color: this.skyColor
      });
    },
    
    handleTextureUpload(event) {
      // 处理所有选中的文件
      for (let i = 0; i < event.target.files.length; i++) {
        const file = event.target.files[i];
        this.selectedTextures.push(file);
        this.texturePreviews.push(URL.createObjectURL(file));
      }
      
      // 重置文件输入框，允许重复选择相同文件
      event.target.value = '';
    },
    
    // 选择贴图
    selectTexture(index) {
      this.selectedTextureIndex = index;
    },
    
    // 移除单个文件
    removeFile(index) {
      // 移除文件和对应的预览
      this.selectedTextures.splice(index, 1);
      this.texturePreviews.splice(index, 1);
      
      // 调整当前选中的索引
      if (this.selectedTextureIndex >= index && this.selectedTextureIndex > 0) {
        this.selectedTextureIndex--;
      }
      
      // 如果删除后没有文件了，重置索引
      if (this.selectedTextures.length === 0) {
        this.selectedTextureIndex = 0;
      }
    },
    
    // 获取贴图类型名称
    getTextureTypeName(type) {
      const types = {
        'color': '基础颜色',
        'normal': '法线',
        'roughness': '粗糙度',
        'metalness': '金属度',
        'ao': '环境光遮蔽',
        'emissive': '自发光'
      };
      return types[type] || '未知';
    },
    
    applyTexture() {
      console.log('应用贴图按钮被点击');
      if (this.selectedTextures.length === 0 || !this.selectedModel) {
        console.log('缺少必要条件:', { 
          hasSelectedTextures: this.selectedTextures.length > 0, 
          hasSelectedModel: !!this.selectedModel 
        });
        return;
      }
      
      // 应用当前选中的贴图
      const file = this.selectedTextures[this.selectedTextureIndex];
      const reader = new FileReader();
      
      console.log('选中的贴图文件:', file.name);
      console.log('贴图类型:', this.getTextureTypeName(this.selectedTextureType));
      console.log('选中的模型:', this.selectedModel);
      
      reader.onload = (e) => {
        console.log('文件读取完成，准备发送贴图数据');
        console.log('贴图数据大小:', e.target.result.length);
        console.log('贴图数据预览(前100字符):', e.target.result.substring(0, 100));
        
        // 发送贴图数据，附加贴图类型信息
        this.$emit("apply-texture", this.selectedModel, e.target.result, this.selectedTextureType);
        
        // 显示成功消息
        this.$message.success(`已应用"${file.name}"作为${this.getTextureTypeName(this.selectedTextureType)}贴图`);
        this.textureApplied = true;
      };
      
      reader.onerror = (e) => {
        console.error('文件读取出错:', e);
        this.$message.error(`贴图文件"${file.name}"读取失败`);
      };
      
      reader.readAsDataURL(file);
    },
    
    removeTexture() {
      this.selectedTextures = [];
      this.texturePreviews = [];
      this.textureApplied = false;
      this.selectedTextureIndex = 0;
      this.$emit("remove-texture", this.selectedModel);
      this.$message.success("贴图已移除");
    }
  },
};

</script>

<style scoped>
#object-info {
  position: fixed;
  bottom: 20px;
  right: 20px; /* 调整到右侧 */
  background-color: white;
  padding: 15px;
  width: 300px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  font-family: Arial, sans-serif;
  z-index: 10; /* 确保位于最前面 */
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
  font-size: 14px;
  color: #555;
  display: flex;
  justify-content: space-between;
  width: 100%;
}

input {
  margin-left: 10px;
  flex-grow: 1;
  padding: 5px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

input[type="color"] {
  padding: 0;
  border: none;
}

input[type="file"] {
  width: 100%;
  margin-top: 5px;
}

input:focus {
  outline: none;
  border-color: #007bff;
}

.delete-button, button {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 7px 15px;
  font-size: 12px;
  cursor: pointer;
  border-radius: 5px;
  margin-right: 5px;
}

.delete-button:hover, button:hover {
  background-color: #0056b3;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.delete-button {
  background-color: #ff4d4f;
}

.delete-button:hover {
  background-color: #d9363e;
}

.texture-preview {
  max-width: 100%;
  height: auto;
  margin-top: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.texture-preview-container {
  margin-bottom: 10px;
}

.texture-panel {
  padding: 10px 0;
}

/* 文件列表样式 */
.file-list {
  max-height: 150px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 5px;
  padding: 5px;
  margin-bottom: 10px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.file-item:last-child {
  border-bottom: none;
}

.file-item.selected {
  background-color: #e3f2fd;
}

.file-name {
  font-size: 12px;
  color: #333;
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-file-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.remove-file-btn:hover {
  background-color: #d9363e;
}

/* 滚动面板样式 */
.scrollable-panel {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 5px;
}

/* 滚动条样式 */
.scrollable-panel::-webkit-scrollbar {
  width: 6px;
}

.scrollable-panel::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.scrollable-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.scrollable-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 贴图类型选择器 */
.texture-type-select {
  width: 100%;
  padding: 5px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background-color: white;
  font-size: 14px;
}
</style>