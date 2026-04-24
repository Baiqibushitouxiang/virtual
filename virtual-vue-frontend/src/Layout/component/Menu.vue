<template>
  <div class="layout">
    <el-header class="header">
      <div class="logo-container">
        <img src="https://preview.qiantucdn.com/freepik/512/560/560257.png!w1024_new_small_1" alt="Logo" />
        <div>
          <div class="logo-text">数字孪生建模平台</div>
          <div class="logo-subtitle">场景、设备、数据、告警一体化管理</div>
        </div>
      </div>

      <div class="auth-buttons">
        <el-button type="primary" plain @click="$router.push('/')">场景编辑器</el-button>
        <el-button type="text" @click="handleLogout">{{ isAuthenticated ? '退出登录' : '前往登录' }}</el-button>
      </div>
    </el-header>

    <el-aside class="aside">
      <el-menu class="menu" :default-active="activeMenu" @select="handleSelect">
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>场景编辑器</span>
        </el-menu-item>

        <el-sub-menu v-for="group in menuGroups" :key="group.path" :index="group.path">
          <template #title>
            <el-icon><component :is="group.icon" /></el-icon>
            <span>{{ group.label }}</span>
          </template>
          <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-main class="main">
      <router-view></router-view>
    </el-main>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import {
  House,
  Monitor,
  Box,
  Files,
  Connection,
  DataLine,
  Bell,
  Location,
  User,
  Histogram
} from '@element-plus/icons-vue'

export default {
  components: {
    House,
    Monitor,
    Box,
    Files,
    Connection,
    DataLine,
    Bell,
    Location,
    User,
    Histogram
  },
  data() {
    return {
      activeMenu: this.$route.path,
      menuGroups: [
        {
          label: '工作台',
          path: '/backend/workbench',
          icon: 'Monitor',
          children: [
            { label: '后台首页', path: '/backend/dashboard', icon: 'Histogram' }
          ]
        },
        {
          label: '场景与资源',
          path: '/backend/resources',
          icon: 'Files',
          children: [
            { label: '场景库', path: '/backend/scenes', icon: 'Files' },
            { label: '模型管理', path: '/backend/models', icon: 'Box' }
          ]
        },
        {
          label: '设备与接入',
          path: '/backend/devices-group',
          icon: 'Connection',
          children: [
            { label: '设备管理', path: '/backend/devices', icon: 'Monitor' },
            { label: 'OPC UA 管理', path: '/backend/opcua', icon: 'Connection' },
            { label: '设备位置管理', path: '/backend/devices-location', icon: 'Location' }
          ]
        },
        {
          label: '数据与告警',
          path: '/backend/data-group',
          icon: 'DataLine',
          children: [
            { label: '实时数据监控', path: '/backend/device-data', icon: 'DataLine' },
            { label: '展板管理', path: '/backend/data-panels', icon: 'Monitor' },
            { label: '告警规则', path: '/backend/alert-rules', icon: 'Bell' }
          ]
        },
        {
          label: '系统管理',
          path: '/backend/system',
          icon: 'User',
          children: [
            { label: '用户管理', path: '/backend/users', icon: 'User' }
          ]
        }
      ]
    }
  },
  computed: {
    ...mapGetters({
      isAuthenticated: 'isAuthenticated'
    })
  },
  methods: {
    ...mapActions(['logout']),
    handleSelect(index) {
      this.activeMenu = index
      this.$router.push({ path: index })
    },
    handleLogout() {
      if (this.isAuthenticated) {
        this.logout()
      }
      this.$router.push('/login')
    }
  },
  watch: {
    '$route.path'(value) {
      this.activeMenu = value
    }
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fb;
}

.header {
  padding: 0 20px;
  background: linear-gradient(135deg, #0f4c5c, #1d7874);
  color: #ecf0f1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-container img {
  height: 40px;
  width: 40px;
  border-radius: 10px;
  object-fit: cover;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
}

.logo-subtitle {
  font-size: 12px;
  opacity: 0.8;
}

.auth-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.aside {
  position: fixed;
  top: 64px;
  left: 0;
  bottom: 0;
  width: 250px;
  background: #12343b;
  z-index: 999;
}

.menu {
  height: 100%;
  border-right: none;
  background: #12343b;
  --el-menu-bg-color: #12343b;
  --el-menu-text-color: #d8ece8;
  --el-menu-active-color: #ffd166;
}

.main {
  margin-left: 250px;
  margin-top: 64px;
  padding: 24px;
  background-color: #f5f7fb;
  flex-grow: 1;
  overflow: auto;
}
</style>
