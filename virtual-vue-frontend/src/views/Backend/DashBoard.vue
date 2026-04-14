<script setup>
import * as echarts from 'echarts';
import { ref, onMounted } from 'vue';
import { getModelCategoryCount } from '@/api';

// 定义分类数据
const modelCategories = ref([]);
const totalModels = ref(0);
const totalCategories = ref(0);

// 获取分类数据
const fetchCategories = async () => {
  try {
    const response = await getModelCategoryCount();
    const data = response.data;
    const categories = Object.keys(data).map(key => ({
      name: key,
      value: data[key]
    }));
    modelCategories.value = categories;
    totalCategories.value = categories.length;
    totalModels.value = categories.reduce((sum, cat) => sum + cat.value, 0);
    initChart();
  } catch (error) {
    console.error('获取分类数据失败:', error);
  }
};

// 初始化 ECharts 饼图
const initChart = () => {
  setTimeout(() => {
    const chartDom = document.getElementById('pie-chart');
    if (!chartDom) {
      console.error('ECharts容器未找到');
      return;
    }
    const chart = echarts.init(chartDom);
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b} <br/>{c}个 ({d}%)',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        textStyle: {
          color: '#333',
        },
        borderColor: '#e0e0e0',
        borderWidth: 1,
        borderRadius: 8,
        boxShadow: '0 2px 12px rgba(0, 0, 0, 0.1)',
      },
      color: ['#64b5f6', '#42a5f5', '#2196f3', '#1e88e5', '#1976d2', '#1565c0', '#0d47a1', '#0a3b80'],
      series: [
        {
          name: '分类',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '55%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 3,
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold',
              formatter: '{b}\n{c}个'
            },
            itemStyle: {
              shadowBlur: 15,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.3)',
            }
          },
          labelLine: {
            show: true,
            length: 10,
            length2: 10,
          },
          data: modelCategories.value,
          animationType: 'scale',
          animationEasing: 'elasticOut',
          animationDelay: function(idx) {
            return Math.random() * 200;
          },
        },
      ],
    };
    chart.setOption(option);
  }, 100);
};

// 在组件挂载时请求数据
onMounted(() => {
  fetchCategories();
});
</script>

<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <h1>控制面板</h1>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-number">{{ totalModels }}</div>
          <div class="stat-label">总模型数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏷️</div>
        <div class="stat-content">
          <div class="stat-number">{{ totalCategories }}</div>
          <div class="stat-label">分类数量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-number">0</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🔄</div>
        <div class="stat-content">
          <div class="stat-number">0</div>
          <div class="stat-label">今日访问</div>
        </div>
      </div>
    </div>
    
    <!-- 图表卡片 -->
    <div class="chart-card">
      <div class="chart-header">
        <h2>模型分类分布</h2>
        <span class="chart-subtitle">按分类显示</span>
      </div>
      <div class="chart-content">
        <div id="pie-chart" style="height: 400px; width: 100%;"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 180px);
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 32px;
  color: #333;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 统计卡片样式 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #64b5f6;
}

.stat-icon {
  font-size: 48px;
  margin-right: 20px;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #1e88e5;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 16px;
  color: #6c757d;
  font-weight: 500;
}

/* 图表卡片样式 */
.chart-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.chart-header {
  text-align: center;
  margin-bottom: 30px;
}

.chart-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 700;
}

.chart-subtitle {
  font-size: 14px;
  color: #6c757d;
}

.chart-content {
  position: relative;
  height: 400px;
}

#pie-chart {
  height: 100%;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 24px;
  }
}
</style>
