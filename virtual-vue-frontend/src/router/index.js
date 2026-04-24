import Layout from '@/Layout/component/Menu.vue'
import store from '@/store/index.js'
import DashBoard from '@/views/Backend/DashBoard.vue'
import DeviceLocationManagement from '@/views/Backend/DeviceLocationManagement.vue'
import DeviceManagement from '@/views/Backend/DeviceManagement.vue'
import UserManagement from '@/views/Backend/UserManagement.vue'
import DeviceDataMonitor from '@/views/Backend/DeviceDataMonitor.vue'
import OpcUaManagement from '@/views/Backend/OpcUaManagement.vue'
import ModelManagement from '@/views/Backend/ModelManagement.vue'
import DataPanelManagement from '@/views/Backend/DataPanelManagement.vue'
import AlertRuleManagement from '@/views/Backend/AlertRuleManagement.vue'
import Login from '@/views/Login/Login.vue'
import Register from '@/views/Login/Register.vue'
import ThreeScene from '@/views/Three/ThreeScene.vue'
import SceneGallery from '@/views/Scenes/SceneGallery.vue'
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'ThreeScene',
        component: ThreeScene,
        meta: { requiresAuth: true, menuGroup: 'editor' }
      },
      {
        path: '/backend/dashboard',
        name: 'DashBoard',
        component: DashBoard,
        meta: { requiresAuth: true, menuGroup: 'workbench' }
      },
      {
        path: '/backend/scenes',
        name: 'SceneGallery',
        component: SceneGallery,
        meta: { requiresAuth: true, menuGroup: 'scene-resource' }
      },
      {
        path: '/backend/models',
        name: 'ModelManagement',
        component: ModelManagement,
        meta: { requiresAuth: true, menuGroup: 'scene-resource' }
      },
      {
        path: '/backend/devices',
        name: 'DeviceManagement',
        component: DeviceManagement,
        meta: { requiresAuth: true, menuGroup: 'device-access' }
      },
      {
        path: '/backend/opcua',
        name: 'OpcUaManagement',
        component: OpcUaManagement,
        meta: { requiresAuth: true, menuGroup: 'device-access' }
      },
      {
        path: '/backend/devices-location',
        name: 'DeviceLocationManagement',
        component: DeviceLocationManagement,
        meta: { requiresAuth: true, menuGroup: 'device-access' }
      },
      {
        path: '/backend/device-data',
        name: 'DeviceDataMonitor',
        component: DeviceDataMonitor,
        meta: { requiresAuth: true, menuGroup: 'data-alert' }
      },
      {
        path: '/backend/data-panels',
        name: 'DataPanelManagement',
        component: DataPanelManagement,
        meta: { requiresAuth: true, menuGroup: 'data-alert' }
      },
      {
        path: '/backend/alert-rules',
        name: 'AlertRuleManagement',
        component: AlertRuleManagement,
        meta: { requiresAuth: true, menuGroup: 'data-alert' }
      },
      {
        path: '/backend/users',
        name: 'UserManagement',
        component: UserManagement,
        meta: { requiresAuth: true, menuGroup: 'admin' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/scenes',
    redirect: '/backend/scenes'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters.isAuthenticated
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ path: '/login' })
  } else {
    next()
  }
})

export default router
