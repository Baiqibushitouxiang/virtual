<template>
  <div class="backend-page">
    <section class="backend-page__hero">
      <div class="backend-page__hero-copy">
        <p class="backend-page__eyebrow">工作台</p>
        <h1 class="backend-page__title">后台工作台</h1>
        <p class="backend-page__subtitle">建议按设备接入、数据验证、模型绑定、展板配置和告警设置的顺序推进联调。</p>
      </div>
      <div class="backend-page__stats">
        <div class="backend-page__stat">
          <span class="backend-page__stat-label">步骤 1</span>
          <span class="backend-page__stat-value">设备接入</span>
        </div>
        <div class="backend-page__stat">
          <span class="backend-page__stat-label">步骤 2</span>
          <span class="backend-page__stat-value">模型绑定</span>
        </div>
        <div class="backend-page__stat">
          <span class="backend-page__stat-label">步骤 3</span>
          <span class="backend-page__stat-value">告警配置</span>
        </div>
      </div>
    </section>

    <section class="guide-grid">
      <article v-for="item in guideCards" :key="item.title" class="guide-card">
        <div class="step">{{ item.step }}</div>
        <h2>{{ item.title }}</h2>
        <p>{{ item.description }}</p>
        <router-link :to="item.path" class="link-btn">进入页面</router-link>
      </article>
    </section>

    <el-card class="backend-page__card">
      <template #header>
        <div class="backend-page__card-header">
          <div>
            <h2 class="backend-page__section-title">模块关系</h2>
            <p class="backend-page__section-desc">设备、模型、展板和告警各自承担的职责如下。</p>
          </div>
        </div>
      </template>

      <div class="relation-list">
        <div class="relation-item"><strong>设备管理</strong><span>维护设备档案、启停状态和证书信息。</span></div>
        <div class="relation-item"><strong>实时监控</strong><span>确认设备是否持续向系统上报数据。</span></div>
        <div class="relation-item"><strong>模型绑定</strong><span>在场景编辑器中把设备和数据项绑定到模型上。</span></div>
        <div class="relation-item"><strong>展板管理</strong><span>控制展示对象、位置和面板状态。</span></div>
        <div class="relation-item"><strong>告警规则</strong><span>维护阈值条件和规则启停状态。</span></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
const guideCards = [
  { step: '01', title: '创建设备', description: '先在设备管理中注册并启用需要接入的设备。', path: '/backend/devices' },
  { step: '02', title: '验证数据接入', description: '通过实时监控或 OPC UA 页面确认设备数据已进入系统。', path: '/backend/device-data' },
  { step: '03', title: '准备资源', description: '在进入三维编辑前先上传模型和场景资源。', path: '/backend/models' },
  { step: '04', title: '绑定模型', description: '进入场景编辑器，把设备和数据项绑定到模型。', path: '/' },
  { step: '05', title: '配置展板', description: '设置展板展示对象、位置和启用状态。', path: '/backend/data-panels' },
  { step: '06', title: '设置告警', description: '最后维护阈值规则并启用需要的告警。', path: '/backend/alert-rules' }
]
</script>

<style scoped>
.guide-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.guide-card {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.06);
}

.step {
  font-size: 12px;
  font-weight: 700;
  color: #dd6b20;
}

.guide-card h2 {
  margin: 12px 0 10px;
  font-size: 20px;
}

.guide-card p {
  min-height: 66px;
  color: #4b5563;
  line-height: 1.7;
}

.link-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 92px;
  padding: 9px 14px;
  border-radius: 999px;
  background: #0f766e;
  color: #fff;
}

.relation-list {
  display: grid;
  gap: 12px;
}

.relation-item {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eceff3;
}

.relation-item:last-child {
  border-bottom: none;
}

@media (max-width: 768px) {
  .relation-item {
    grid-template-columns: 1fr;
  }
}
</style>
