<template>
  <div class="layout">
    <!-- Header -->
    <el-header class="header">
      <div class="logo-container">
        <img src="https://preview.qiantucdn.com/freepik/512/560/560257.png!w1024_new_small_1" alt="Logo" />
        <span class="logo-text">数字孪生建模平台</span>
      </div>

      <div class="auth-buttons">
        <el-button v-if="isAuthenticated" type="text" @click="handleLogout">
          <i class="fas fa-sign-out-alt"></i> 退出登录
        </el-button>
        <el-button v-if="!isAuthenticated" type="text" @click="handleLogin">
          <i class="fas fa-sign-in-alt"></i> 登录
        </el-button>
      </div>
    </el-header>

    <!-- Aside (Sidebar) -->
    <el-aside class="aside">
      <el-menu
          class="el-menu-vertical-demo"
          :default-active="activeMenu"
          @select="handleSelect"
          background-color="#34495e"
          text-color="#ecf0f1"
          active-text-color="#1abc9c"
          style="border-right: none;">
        <el-menu-item
            v-for="(menuLabel, index) in noChildren()"
            :key="index"
            :index="menuLabel.path">
          <el-icon><component :is="menuLabel.icon" /></el-icon>
          <template #title>{{ menuLabel.label }}</template>
        </el-menu-item>

        <el-sub-menu
            v-for="(menuLabel, index) in hasChildren()"
            :key="index"
            :index="menuLabel.path">
          <template #title>
            <el-icon><component :is="menuLabel.icon" /></el-icon>
            <span>{{ menuLabel.label }}</span>
          </template>
          <el-menu-item-group>
            <el-menu-item
                v-for="(item, index) in menuLabel.children"
                :key="index"
                :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </el-menu-item>
          </el-menu-item-group>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- Content Area -->
    <el-main class="main">
      <router-view></router-view>
    </el-main>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex';

export default {
  data() {
    return {
      activeMenu: this.$route.name,
      menuData: [
        { label: '主界面', path: '/', icon: 'el-icon-house', children: [] },
        {
          label: '后台管理', path: '/backend', icon: 'el-icon-menu', children: [
            { label: '控制面板', path: '/backend/dashboard', icon: 'el-icon-s-grid' },
            { label: '模型管理', path: '/backend/models', icon: 'el-icon-box' },
            { label: '设备管理', path: '/backend/devices', icon: 'el-icon-device' },
            { label: '数据展板管理', path: '/backend/data-panels', icon: 'el-icon-monitor' },
            { label: '位置信息管理', path: '/backend/devicesLoc', icon: 'el-icon-location' },
            { label: '用户管理', path: '/backend/users', icon: 'el-icon-user' },
            { label: '设备数据监控', path: '/backend/device-data', icon: 'el-icon-data-line' },
            { label: 'OPC UA管理', path: '/backend/opcua', icon: 'el-icon-connection' }
          ]
        }
      ]
    };
  },
  computed: {
    ...mapGetters({
      isAuthenticated: 'isAuthenticated',
      userInfo: "getUser"
    })
  },
  methods: {
    handleSelect(index) {
      this.activeMenu = index;

      // 如果是主菜单项 (没有子菜单)
      const menu = this.menuData.find(item => item.path === index);
      if (menu) {
        this.$router.push({ path: menu.path });  // 跳转到完整路径
      } else {
        // 如果是二级菜单，跳转到子菜单的路径
        const subMenu = this.menuData
            .flatMap(item => item.children)
            .find(subItem => subItem.path === index);
        if (subMenu) {
          this.$router.push({ path: subMenu.path });  // 跳转到完整路径
        }
      }
    },

    // 获取没有子菜单的菜单项
    noChildren() {
      return this.menuData.filter(menu => menu.children.length === 0);
    },

    // 获取有子菜单的菜单项
    hasChildren() {
      return this.menuData.filter(menu => menu.children.length > 0);
    },

    // 跳转到登录页面
    handleLogin() {
      this.$router.push('/login');
    },
    // 执行退出登录操作
    handleLogout() {
      this.logout(); // 调用 Vuex 中的 logout 方法
      this.$router.push('/'); // 跳转到首页
    },
    ...mapActions(['logout'])
  },
  watch: {
    '$route.path': function(newRoute) {
      this.activeMenu = newRoute;
    }
  }
};
</script>

<style scoped>
/* Layout 基本样式 */
.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f7f7f7;
}

/* Header 样式 */
.header {
  padding: 0 20px;
  background: linear-gradient(135deg, #3498db, #2c3e50); /* 渐变背景 */
  color: #ecf0f1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* Logo 样式 */
.logo-container {
  display: flex;
  align-items: center;
}

.logo-container img {
  height: 40px;
  margin-right: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #ecf0f1;
}

/* 认证按钮样式 */
.auth-buttons {
  display: flex;
  align-items: center;
}

.auth-buttons .el-button {
  color: #ecf0f1;
}

.auth-buttons .el-button:hover {
  color: #1abc9c;
}

/* Aside 样式 */
.aside {
  position: fixed;
  top: 60px;
  left: 0;
  bottom: 0;
  width: 220px; /* 稍微增宽 */
  background-color: #34495e;
  z-index: 999;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease-in-out; /* 添加平滑的过渡效果 */
}

/* Main 样式 */
.main {
  margin-left: 220px; /* 主体内容区域增加左边距 */
  margin-top: 60px;
  padding: 30px;
  background-color: #f4f6f9;
  flex-grow: 1;
  overflow: auto;
  border-radius: 8px; /* 给内容区域添加圆角 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* Element Plus 菜单样式 */
.el-menu-vertical-demo {
  background-color: #34495e;
  transition: background-color 0.3s ease;
}

.el-menu-item {
  border-radius: 4px;
  padding: 12px;
  font-weight: 600; /* 增强字体的粗细 */
}

.el-menu-item:hover {
  background-color: #1abc9c;
}

/* 选中菜单项样式 */
.el-menu-item.is-active {
  background-color: #3498db !important;
  color: white !important;
}

/* 二级菜单项选中样式 */
.el-sub-menu.is-active > .el-menu-item {
  background-color: #3498db !important;
  color: white !important;
}

/* 二级菜单项 hover 效果 */
.el-sub-menu:hover > .el-menu-item {
  background-color: #3498db !important;
  color: white !important;
}

/* 图标大小 */
.el-icon {
  font-size: 20px;
  margin-right: 8px;
}

/* 调整二级菜单的缩进距离 */
.el-menu .el-sub-menu__title {
  padding-left: 20px;
}
</style>
