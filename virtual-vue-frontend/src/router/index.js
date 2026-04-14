import Layout from "@/Layout/component/Menu.vue";
import store from "@/store/index.js";
import DashBoard from "@/views/Backend/DashBoard.vue";
import DeviceLocationManagement from "@/views/Backend/DeviceLocationManagement.vue";
import DeviceManagement from "@/views/Backend/DeviceManagement.vue";
import UserManagement from "@/views/Backend/UserManagement.vue";
import DeviceDataMonitor from "@/views/Backend/DeviceDataMonitor.vue";
import OpcUaManagement from "@/views/Backend/OpcUaManagement.vue";
import ModelManagement from "@/views/Backend/ModelManagement.vue";
import DataPanelManagement from "@/views/Backend/DataPanelManagement.vue";
import Login from "@/views/Login/Login.vue";
import Register from "@/views/Login/Register.vue";
import ThreeScene from "@/views/Three/ThreeScene.vue";
import { createRouter, createWebHistory } from 'vue-router';

import SceneGallery from '@/views/Scenes/SceneGallery.vue';

const routes = [
    {
        path: '/',
        component: Layout,
        children: [
            {
                path: '',
                name: 'ThreeScene',
                component: ThreeScene,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/dashboard',
                name: 'DashBoard',
                component: DashBoard,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/devices',
                name: 'DeviceManagement',
                component: DeviceManagement,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/devicesLoc',
                name: 'DeviceLocationManagement',
                component: DeviceLocationManagement,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/users',
                name: 'UserManagement',
                component: UserManagement,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/device-data',
                name: 'DeviceDataMonitor',
                component: DeviceDataMonitor,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/opcua',
                name: 'OpcUaManagement',
                component: OpcUaManagement,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/models',
                name: 'ModelManagement',
                component: ModelManagement,
                meta: { requiresAuth: true }
            },
            {
                path: '/backend/data-panels',
                name: 'DataPanelManagement',
                component: DataPanelManagement,
                meta: { requiresAuth: true }
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
        name: 'SceneGallery',
        component: SceneGallery,
        meta: { requiresAuth: true }
    }
]


const router = createRouter({
    history: createWebHistory(),
    routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
    const isAuthenticated = store.getters.isAuthenticated;  // 获取 Vuex 中的认证状态
    if (to.meta.requiresAuth && !isAuthenticated) {
        next({ path: '/login' });  // 如果需要授权并且用户未登录，重定向到登录页
    } else {
        next();  // 否则，继续访问目标页面
    }
});

export default router;
