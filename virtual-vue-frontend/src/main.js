import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'
import axios from 'axios'
import ElementPlus from 'element-plus'
import store from "@/store/index.js"
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome"
import '@fortawesome/fontawesome-free/css/all.css';
import '@fortawesome/fontawesome-free/js/all.js';
import mitt from 'mitt'
import { API_CONFIG } from './config/apiConfig'

axios.defaults.baseURL = API_CONFIG.baseURL
axios.defaults.timeout = API_CONFIG.timeout

// 创建事件总线
const eventBus = mitt()

createApp(App)
    .use(router)
    .use(ElementPlus)
    .use(store)
    .component('font-awesome-icon', FontAwesomeIcon)
    .mount('#app')

// 将事件总线挂载到全局
window.eventBus = eventBus