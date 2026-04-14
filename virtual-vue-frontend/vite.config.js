import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'

export default defineConfig({
  // 插件
  plugins: [vue()],
  // 路径解析
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 定义全局变量
  define: {
    global: 'window'
  },
  // 开发服务器等核心配置项
  server: {
    host: true,
    port: 5173,

    // ✅ 正确的代理配置位置
    proxy: {
      '/api': {
        target: 'http://localhost:9999',
        changeOrigin: true,
        rewrite: (path) => path
      },
      '/scenes': {
        target: 'http://localhost:9999',
        changeOrigin: true,
        rewrite: (path) => path
      },
      '/external-api': {
        target: 'http://192.168.56.1:8888/',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/external-api/, ''),
        headers: {
          'X-Forwarded-For': 'localhost'
        }
      }
    }
  }
})



