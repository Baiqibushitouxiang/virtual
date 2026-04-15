<template>
  <div id="model-selector" v-if="visible">
    <!-- 路径导航 -->
    <div class="breadcrumb">
      <span
          v-for="(node, index) in path"
          :key="index"
          class="breadcrumb-node"
          @click="goToLevel(index)"
      >
        {{ node.name }} <span v-if="index < path.length - 1">/</span>
      </span>
    </div>

    <!-- 当前层级内容 -->
    <div class="directory">
      <div v-for="(item, index) in currentLevel.children" :key="index" class="tree-item">
        <!-- 文件夹 -->
        <div
            v-if="isFolder(item)"
            class="folder"
            @click="openFolder(item)"
        >
          📂 {{ item.name }}
        </div>

        <!-- 文件 -->
        <div v-else class="file" @click="toggleModelSelection(item)" :class="{ selected: isSelected(item) }">
          <span class="selection-indicator">
            {{ isSelected(item) ? '✓' : '○' }}
          </span>
          🗂 {{ item.name }}
        </div>
      </div>
      
      <!-- 当没有内容时显示提示 -->
      <div v-if="!currentLevel.children || currentLevel.children.length === 0" class="empty-directory">
        该目录为空
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <button @click="goBack" v-if="path.length > 1">返回上一级</button>
      <button @click="selectModels" :disabled="selectedModels.length === 0">添加选中模型 ({{ selectedModels.length }})</button>
      <button @click="closeSelector">关闭</button>
    </div>
  </div>
</template>

<script>
import { getModelPathUrl, getModelStaticUrl } from '@/api/index.js';

export default {
  name: "ModelSelector",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    data: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      path: [], // 当前路径的栈
      selectedModels: [] // 选中的模型列表
    };
  },
  computed: {
    currentLevel() {
      const level = this.path.length ? this.path[this.path.length - 1] : { name: "根目录", children: this.data };
      console.log('当前层级:', level);
      return level;
    },
  },
  watch: {
    visible(newValue) {
      if (newValue) {
        this.initializePath();
      } else {
        // 关闭时清空选中列表
        this.selectedModels = [];
      }
    },
    data: {
      handler(newData) {
        console.log('ModelSelector 接收到新数据:', newData);
        if (this.visible) {
          this.initializePath(newData);
        }
      },
      immediate: true
    }
  },
  methods: {
    // 判断是否为文件夹
    isFolder(item) {
      // 检查是否有children属性且不为空
      if (item.hasOwnProperty('children') && Array.isArray(item.children) && item.children.length > 0) {
        return true;
      }
      return false;
    },
    
    initializePath(data = this.data) {
      console.log('ModelSelector 初始化数据:', data);
      console.log('数据类型:', typeof data);
      if (data) {
        console.log('数据是否为数组:', Array.isArray(data));
        if (Array.isArray(data) && data.length > 0) {
          console.log('第一个元素:', data[0]);
          console.log('第一个元素是否有children:', data[0].hasOwnProperty('children'));
          if (data[0].hasOwnProperty('children')) {
            console.log('children类型:', typeof data[0].children);
            console.log('children是否为数组:', Array.isArray(data[0].children));
            if (Array.isArray(data[0].children)) {
              console.log('children长度:', data[0].children.length);
            }
          }
        }
      }
      
      // 确保数据存在且为数组
      if (data && Array.isArray(data)) {
        this.path = [{ name: "模型目录", children: data }];
      } else {
        this.path = [{ name: "模型目录", children: [] }];
      }
      console.log('ModelSelector 初始化后的路径:', this.path);
    },
    openFolder(folder) {
      console.log('尝试打开文件夹:', folder);
      console.log('检查是否为文件夹:', this.isFolder(folder));
      if (this.isFolder(folder)) {
        this.path.push(folder);
        console.log('成功打开文件夹，当前路径:', this.path);
      } else {
        console.log('项目不是文件夹:', folder);
      }
    },
    // 切换模型选中状态
    toggleModelSelection(model) {
      console.log('尝试选择模型:', model);
      const index = this.selectedModels.findIndex(m => m.name === model.name);
      if (index >= 0) {
        // 如果已选中，则取消选中
        this.selectedModels.splice(index, 1);
        console.log('取消选中模型，当前选中:', this.selectedModels);
      } else {
        // 如果未选中，则添加到选中列表
        this.selectedModels.push(model);
        console.log('添加选中模型，当前选中:', this.selectedModels);
      }
    },
    // 检查模型是否已选中
    isSelected(model) {
      return this.selectedModels.some(m => m.name === model.name);
    },
    // 选择模型（添加到场景中）
    // 在选择模型时构建完整的数据
    selectModels() {
      console.log('选择的模型:', this.selectedModels);

      this.selectedModels.forEach(model => {
        let fileUrl;
        if (model.url) {
          fileUrl = model.url;
        } else if (model.filePath) {
          fileUrl = getModelPathUrl(model.filePath);
        } else if (model.file) {
          fileUrl = model.file;
        } else {
          const baseName = model.name.replace(/\.glb$/i, '');
          fileUrl = getModelStaticUrl(baseName);
        }
        
        const modelData = {
          name: model.name,
          file: fileUrl,
          position: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
          rotation: { x: 0, y: 0, z: 0 },
          boundData: null,
        };

        console.log('发送模型数据:', modelData);
        this.$emit("model-selected", modelData);
      });

      this.selectedModels = [];
      this.closeSelector();
    },
    goBack() {
      if (this.path.length > 1) {
        this.path.pop();
      }
    },
    goToLevel(index) {
      this.path = this.path.slice(0, index + 1);
    },
    closeSelector() {
      this.$emit("close");
    },
  },
  created() {
  },
};
</script>

<style scoped>
#model-selector {
  position: fixed;
  top:15%;
  left: 15%;
  width: 20%;
  max-height: 65%;
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  overflow-y: auto;
  z-index: 10000;
}

.breadcrumb {
  font-size: 14px;
  margin-bottom: 10px;
  color: #555;
}

.breadcrumb-node {
  cursor: pointer;
  color: #007bff;
  text-decoration: underline;
}

.breadcrumb-node:hover {
  text-decoration: none;
}

.directory {
  margin: 10px 0;
  max-height: 350px;  /* 增加目录区域高度 */
  overflow-y: auto;
}

.empty-directory {
  text-align: center;
  padding: 20px;
  color: #999;
  font-style: italic;
}

.tree-item {
  margin: 5px 0;
  padding: 4px 0;
}

.folder {
  cursor: pointer;
  font-weight: bold;
  color: #333;
  padding: 6px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.folder:hover {
  background-color: #f0f0f0;
}

.file {
  cursor: pointer;
  margin-left: 20px;
  color: #555;
  padding: 6px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
}

.file:hover {
  background-color: #f0f0f0;
}

.file.selected {
  background-color: #e3f2fd;
  color: #1976d2;
}

.selection-indicator {
  margin-right: 8px;
  font-weight: bold;
}

.actions {
  margin-top: 20px;
  text-align: right;
  padding-top: 10px;
  border-top: 1px solid #e8e8e8;
}

button {
  padding: 8px 16px;
  margin: 5px;
  font-size: 14px;
  cursor: pointer;
  background-color: #f0f0f0;
  border: none;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #e0e0e0;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* 滚动条样式 */
.directory::-webkit-scrollbar {
  width: 6px;
}

.directory::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.directory::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.directory::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
