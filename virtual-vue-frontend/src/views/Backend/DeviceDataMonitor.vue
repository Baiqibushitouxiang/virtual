<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">后台管理</p>
        <h1 class="backend-page__title">实时数据监控</h1>
        <p class="backend-page__subtitle">通过 WebSocket 观察实时上报数据、告警和通知消息，支持搜索与本地分页。</p>
      </div>
      <div class="backend-page__stats">
        <div class="backend-page__stat">
          <span class="backend-page__stat-label">连接状态</span>
          <span class="backend-page__stat-value">{{ isConnected ? '已连接' : '未连接' }}</span>
        </div>
        <div class="backend-page__stat">
          <span class="backend-page__stat-label">消息数量</span>
          <span class="backend-page__stat-value">{{ total }}</span>
        </div>
      </div>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">消息列表</h2>
            <p class="backend-page__section-desc">可查看设备编码、消息类型、接收时间和格式化后的消息内容。</p>
          </div>
          <div class="backend-page__actions">
            <el-input v-model="searchText" clearable placeholder="搜索消息" style="width: 220px" @input="resetPage" />
            <el-button type="primary" :disabled="isConnected" @click="connect">连接</el-button>
            <el-button :disabled="!isConnected" @click="disconnect">断开</el-button>
            <el-button @click="clearData">清空</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedItems" stripe>
        <el-table-column prop="deviceId" label="设备编码" width="160" />
        <el-table-column prop="messageTypeLabel" label="消息类型" width="120" />
        <el-table-column prop="formattedTime" label="接收时间" width="180" />
        <el-table-column prop="formattedData" label="消息内容" min-width="320" show-overflow-tooltip />
      </el-table>

      <div class="backend-page__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="pageSizes"
          :total="total"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { API_CONFIG } from '@/config/apiConfig'
import { useLocalPagination } from '@/composables/useLocalPagination'
import { formatMessageContent, formatMessageTypeLabel } from '@/utils/deviceMessage'

const isConnected = ref(false)
const deviceDataList = ref([])
const searchText = ref('')
const ws = ref(null)

const getWebSocketUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = API_CONFIG.baseURL.replace(/^https?:\/\//, '')
  return `${protocol}${host}/ws/device-data`
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const filteredItems = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  if (!keyword) {
    return deviceDataList.value
  }
  return deviceDataList.value.filter((item) =>
    [item.deviceId, item.messageTypeLabel, item.formattedData].filter(Boolean).join(' ').toLowerCase().includes(keyword)
  )
})

const { currentPage, pageSize, pageSizes, total, pagedItems, resetPage } = useLocalPagination(filteredItems, { pageSize: 10 })

const connect = () => {
  if (ws.value) {
    ws.value.close()
  }
  ws.value = new WebSocket(getWebSocketUrl())

  ws.value.onopen = () => {
    isConnected.value = true
  }

  ws.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      deviceDataList.value.unshift({
        deviceId: data.deviceId || '--',
        type: data.type || '',
        messageTypeLabel: formatMessageTypeLabel(data.type),
        formattedData: formatMessageContent(data),
        timestamp: data.timestamp,
        formattedTime: formatTime(data.timestamp)
      })
      if (deviceDataList.value.length > 200) {
        deviceDataList.value.pop()
      }
    } catch (error) {
      console.error('解析 WebSocket 消息失败:', error)
    }
  }

  ws.value.onclose = () => {
    isConnected.value = false
  }

  ws.value.onerror = () => {
    isConnected.value = false
  }
}

const disconnect = () => {
  if (ws.value) {
    ws.value.close()
    ws.value = null
  }
  isConnected.value = false
}

const clearData = () => {
  deviceDataList.value = []
}

onMounted(connect)
onUnmounted(disconnect)
</script>
