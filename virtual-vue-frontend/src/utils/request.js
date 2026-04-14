// @/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { API_CONFIG } from '@/config/apiconfig'

const request = axios.create({
    baseURL: API_CONFIG.baseURL,
    timeout: API_CONFIG.timeout
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 可以在这里添加认证token等
        const token = localStorage.getItem('token') || ''
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        // 根据你的后端返回结构进行调整
        return response.data
    },
    error => {
        console.error('API请求错误:', error)
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request