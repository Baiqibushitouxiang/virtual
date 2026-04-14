<template>
  <div id="composite-model-selector" v-if="visible">
    <div class="header">
      <h3>组合模型库</h3>
      <el-button @click="closeSelector" type="danger" size="medium">×</el-button>
    </div>

    <div class="composite-models-list" v-if="compositeModels.length > 0">
      <div 
        v-for="model in compositeModels" 
        :key="model.id"
        class="composite-model-item"
        @click="loadCompositeModel(model)"
      >
        <div class="model-info">
          <div class="model-name">{{ model.name }}</div>
          <div class="model-description">{{ model.description || '无描述' }}</div>
          <div class="model-meta">
            <span class="model-components">{{ getComponentCount(model) }} 个组件</span>
            <span class="model-date">{{ formatDate(model.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="暂无组合模型" :image-size="80"></el-empty>
    </div>
  </div>
</template>

<script>
import { getCompositeModels } from '@/api/index.js';

export default {
  name: "CompositeModelSelector",
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      compositeModels: [],
      loading: false
    };
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal) {
          this.$nextTick(() => {
            this.loadCompositeModels();
          });
        }
      },
      immediate: true
    }
  },
  mounted() {
    // 监听组合模型保存事件
    this.$nextTick(() => {
      if (window.eventBus) {
        // 先移除可能存在的监听器，避免重复监听
        window.eventBus.off('composite-model-saved', this.loadCompositeModels);
        // 添加监听器
        window.eventBus.on('composite-model-saved', this.loadCompositeModels);
      }
    });
  },
  beforeUnmount() {
    // 清理事件监听器
    if (window.eventBus) {
      window.eventBus.off('composite-model-saved', this.loadCompositeModels);
    }
  },
  methods: {
    async loadCompositeModels() {
      // 防止重复请求
      if (this.loading) {
        return;
      }
      
      try {
        this.loading = true;
        console.log('开始加载组合模型列表');
        const response = await getCompositeModels();
        console.log('获取到的组合模型响应:', response);
        // 确保正确处理响应数据，使用 Set 去重
        const rawData = response.data || response || [];
        const uniqueModels = [];
        const seenIds = new Set();
        
        rawData.forEach(model => {
          if (model.id && !seenIds.has(model.id)) {
            seenIds.add(model.id);
            uniqueModels.push(model);
          }
        });
        
        this.compositeModels = uniqueModels;
        console.log('处理后的组合模型数据:', this.compositeModels);
        
        // 验证每个模型的数据结构
        this.compositeModels.forEach(model => {
          if (!model.components) {
            model.components = [];
          }
        });
      } catch (error) {
        console.error('加载组合模型失败:', error);
        console.error('错误详情:', error.response || error.message || error);
        this.$message.error('加载组合模型失败: ' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
      }
    },
    
    getComponentCount(model) {
      // 直接从模型对象中获取组件数量，避免重复请求
      if (model.components && Array.isArray(model.components)) {
        return model.components.length;
      }
      return 0;
    },
    
    loadCompositeModel(model) {
      this.$emit('load-composite-model', model);
    },
    
    closeSelector() {
      this.$emit('close');
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    }
  }
};
</script>

<style scoped>
#composite-model-selector {
  position: fixed;
  top: 15%;
  left: 15%;
  width: 350px;
  max-height: 65%;
  background: linear-gradient(180deg, #ffffff 0%, #f5faff 100%);
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  overflow-y: auto;
  z-index: 10000;
  border: 1px solid #e3f2fd;
  animation: slideIn 0.3s ease forwards;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
  color: white;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

.header .el-button--danger {
  font-size: 18px;
  padding: 8px 12px;
  min-width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header h3 {
  margin: 0;
  color: #1976d2;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header h3::before {
  content: "📦";
  font-size: 18px;
}

.composite-models-list {
  max-height: 450px;
  overflow-y: auto;
}

/* 滚动条样式 */
.composite-models-list::-webkit-scrollbar {
  width: 6px;
}

.composite-models-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.composite-models-list::-webkit-scrollbar-thumb {
  background: #bbdefb;
  border-radius: 3px;
}

.composite-models-list::-webkit-scrollbar-thumb:hover {
  background: #90caf9;
}

.composite-model-item {
  padding: 18px;
  border: 2px solid #e3f2fd;
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.composite-model-item:hover {
  border-color: #42a5f5;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  box-shadow: 0 6px 20px rgba(66, 165, 245, 0.25);
  transform: translateY(-2px);
}

.model-info .model-name {
  font-weight: 600;
  margin-bottom: 8px;
  color: #1976d2;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-info .model-name::before {
  content: "📁";
  font-size: 14px;
}

.model-info .model-description {
  font-size: 13px;
  color: #546e7a;
  margin-bottom: 10px;
  line-height: 1.5;
  background: rgba(66, 165, 245, 0.05);
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #42a5f5;
}

.model-info .model-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #78909c;
  font-weight: 500;
}

.model-info .model-meta span {
  background: rgba(25, 118, 210, 0.1);
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid rgba(66, 165, 245, 0.2);
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f5faff 100%);
  border-radius: 12px;
  border: 2px dashed #bbdefb;
  margin-top: 20px;
}

/* 按钮样式优化 */
.el-button--danger {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-button--danger:hover {
  background: linear-gradient(135deg, #e53935 0%, #c62828 100%);
  box-shadow: 0 4px 15px rgba(229, 57, 53, 0.3);
  transform: translateY(-1px);
}
</style>