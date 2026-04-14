<template>
  <div class="app-container">
    <router-view></router-view>
  </div>
</template>

<script setup>
// 引入 Layout 组件
// import Layout from '@/Layout/component/Menu.vue'

// 引入 Vuex store 和 Vue Router
import { useRoute } from 'vue-router';
import { useStore } from 'vuex';

const store = useStore()
const route = useRoute()

// 可以根据需要在此处理全局逻辑
</script>

<style>
/* 全局样式，确保布局正常显示 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  overflow-x: hidden;
}

#app {
  height: 100%;
  width: 100%;
  position: relative;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 249, 250, 0.95) 100%);
  background-attachment: fixed;
  position: relative;
  z-index: 1;
  overflow: hidden;
}

/* 朦胧背景图片 */
.app-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('https://images.unsplash.com/photo-1518655048521-f130df041f66?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80') center/cover no-repeat;
  opacity: 0.25;
  z-index: -1;
  filter: blur(8px);
}



/* 保持原有网格动画但调整为淡色系 */
.app-container::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(108, 117, 125, 0.08) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
  z-index: -1;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}
</style>


