<template>
  <div id="three-scene">
    <ModelSelector
        v-if="isModelSelectorVisible"
        :visible="isModelSelectorVisible"
        :data="modelData"
        @model-selected="handleModelSelection"
        @close="isModelSelectorVisible = false"
    />

    <!-- 组合模型选择器 -->
    <CompositeModelSelector
        ref="compositeModelSelector"
        v-if="isCompositeModelSelectorVisible"
        :visible="isCompositeModelSelectorVisible"
        @load-composite-model="loadCompositeModel"
        @close="isCompositeModelSelectorVisible = false"
    />

    <!-- 组合模型编辑器 -->
    <CompositeModelEditor
        ref="compositeModelEditor"
        v-if="isCompositeEditorVisible"
        :visible="isCompositeEditorVisible"
        :scene-models="getSceneModels()"
        @save-composite-model="saveCompositeModel"
        @request-model-data="provideModelData"
        @close="isCompositeEditorVisible = false"
    />

    <!-- 工具栏 - 两侧布局 -->
    <div v-if="sceneLoaded || showGateStation" class="toolbar">
      <div class="toolbar-left">
        <el-button type="warning" @click="goHome" size="small" title="返回主界面"><i class="fas fa-arrow-left"></i></el-button>
        <el-button-group v-if="sceneLoaded && !showGateStation">
          <el-button type="primary" @click="showModelSelector" :loading="loading" size="small">
            <i class="el-icon-plus"></i> 选择模型
          </el-button>
          <el-button type="success" @click="saveScene" :disabled="!hasChanges" size="small">
            <i class="el-icon-download"></i> 保存场景
          </el-button>
        </el-button-group>
      </div>

      <div class="toolbar-center" v-if="sceneLoaded && !showGateStation">
        <el-button-group>
          <el-button @click="undo" :disabled="currentHistoryIndex < 0" size="small" title="撤销 (Ctrl+Z)">
            <i class="el-icon-refresh-left"></i> 撤销
          </el-button>
          <el-button @click="redo" size="small" title="清除场景中的模型">
            <i class="el-icon-delete"></i> 清除场景
          </el-button>
          <!-- 组合模型按钮 -->
          <el-button @click="openCompositeModelSelector" size="small" title="使用组合模型">
            <i class="el-icon-folder-opened"></i> 使用组合
          </el-button>
          <el-button @click="openCompositeModelEditor" size="small" title="创建组合模型">
            <i class="el-icon-folder-add"></i> 创建组合
          </el-button>
        </el-button-group>
      </div>

      <div class="toolbar-right">
        <el-button-group v-if="sceneLoaded && !showGateStation">
          <el-button
              @click="toggleSkybox"
              :type="skyboxEnabled ? 'success' : 'default'"
              size="small"
              title="切换天空背景"
          >
            <i class="el-icon-sunny"></i> {{ skyboxEnabled ? '关闭天空' : '开启天空' }}
          </el-button>
          <el-button @click="toggleDataPanelManager" size="small" title="数据展板管理">
            <i class="el-icon-data-line"></i> 数据展板
          </el-button>
          <el-button @click="openAllBoundPanels" size="small" title="打开所有已绑定展板" :disabled="boundPanelsCount === 0">
            <i class="el-icon-video-play"></i> 打开全部
          </el-button>
          <el-button @click="exportSceneToGLB" size="small" title="导出为 GLB 格式">
            <i class="el-icon-download"></i> 导出GLB
          </el-button>
          <el-button @click="exportSceneToJSON" size="small" title="导出为 JSON 格式">
            <i class="el-icon-document"></i> 导出JSON
          </el-button>
        </el-button-group>

        <!-- 闸站场景专用按钮 -->
        <el-button-group v-if="showGateStation">
          <el-button @click="refreshGateStation" size="small" title="刷新闸站场景">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
          <el-button @click="openGateStationInNewTab" size="small" title="在新标签页打开">
            <i class="el-icon-top-right"></i> 新标签页
          </el-button>
        </el-button-group>
      </div>

      <!-- 加载进度条 -->
      <el-progress v-if="loading" :percentage="loadingProgress" :show-text="false" class="loading-progress" />
    </div>

    <!-- 主内容区域 -->
    <div class="main-content" :class="{ 'with-toolbar': sceneLoaded || showGateStation }">
      <!-- 初始场景选择界面 -->
      <div v-if="!sceneLoaded && !showGateStation" class="scene-options">
        <h2>数字孪生场景编辑器</h2>
        <div class="scene-buttons">
          <el-button type="primary" @click="createNewScene" size="large">
            <i class="el-icon-plus"></i> 新建场景          </el-button>
          <el-button @click="$router.push({ name: 'SceneGallery' })" size="large">
            <i class="el-icon-folder-opened"></i> 读取场景
          </el-button>
          <el-button @click="importScene" size="large">
            <i class="el-icon-upload"></i> 导入场景
          </el-button>
          <el-button @click="openGateStationInterface" size="large">
            <i class="el-icon-office-building"></i> 闸站场景
          </el-button>
        </div>
      </div>

      <!-- 3D场景容器 -->
      <div v-if="sceneLoaded && !showGateStation" class="scene-container" id="scene-container">
        <!-- Three.js 场景将渲染到这里 -->
      </div>

      <!-- 闸站场景容器 -->
      <div v-if="showGateStation" class="gate-station-container">
        <div v-if="gateStationLoading" class="gate-station-loading">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
          <p>闸站场景加载中...</p>
        </div>
        <iframe
            ref="gateStationIframe"
            :src="getGateStationUrl()"
            class="gate-station-iframe"
            @load="onIframeLoad"
        ></iframe>
      </div>

      <!-- ObjectInfo 面板 -->
      <ObjectInfo
          v-if="selectedModel && sceneLoaded && !showGateStation"
          :selectedModel="selectedModel"
          :sceneId="currentSceneId"
          @bind-data="bindData"
          @save-model-binding="handleSaveModelBinding"
          @remove-model-binding="handleRemoveModelBinding"
          @delete-model="handleDeleteModel"
          @environment-change="handleEnvironmentChange"
          @apply-texture="handleApplyTexture"
          @remove-texture="handleRemoveTexture"
      />

      <!-- WebSocket 数据监控面板 - 多展板支持 -->
      <DeviceDataPanel
          v-for="panel in openedPanels"
          :key="getPanelKey(panel)"
          :visible="true"
          :panelConfig="panel"
          :boundModel="boundModels[getPanelKey(panel)]"
          :modelBinding="panelBindings[getPanelKey(panel)] || null"
          :bindMode="bindMode && bindingPanelId === getPanelKey(panel)"
          :sceneId="currentSceneId"
          @close="closePanel(panel)"
          @updatePanel="refreshPanels"
          @deletePanel="handleDeletePanel"
          @patchPanel="handlePatchPanel"
      />

      <!-- 展板管理弹窗 -->
      <DataPanelManager
          v-model:visible="showPanelManager"
          :sceneId="currentSceneId"
          :panels="allPanels"
          :openedPanelIds="openedPanelIds"
          :devices="devices"
          @openPanel="openPanel"
          @closePanel="closePanel"
          @openAll="openAllPanels"
          @closeAll="closeAllPanels"
          @savePanel="handleSavePanel"
          @deletePanel="handleDeletePanel"
          @bindModel="startBindMode"
          @unbindModel="handlePanelModelUnbind"
          @bindDevice="handlePanelDeviceBind"
          @unbindDevice="handlePanelDeviceUnbind"
      />

      <!-- 操作提示 -->
      <div v-if="showTips && sceneLoaded && !showGateStation" class="operation-tips">
        <div class="tips-content">
          <h4>操作提示</h4>
          <ul>
            <li><strong>Ctrl+Z:</strong> 撤销操作</li>
            <li><strong>Delete:</strong> 删除选中模型</li>
            <li><strong>鼠标左键:</strong> 选择模型</li>
            <li><strong>鼠标右键:</strong> 旋转视角</li>
            <li><strong>滚轮:</strong> 缩放视角</li>
            <li><strong>天空背景:</strong> 点击工具栏中的"开启天空"按钮添加天空背景</li>
          </ul>
          <el-button @click="showTips = false" size="small" type="text">
            知道了          </el-button>
        </div>
      </div>

      <!-- 帮助按钮 -->
      <el-button
          v-if="sceneLoaded && !showGateStation"
          class="help-button"
          type="text"
          @click="showTips = !showTips"
          title="显示操作帮助"
      >
        <i class="el-icon-question"></i>
      </el-button>
    </div>

    <!-- 保存场景对话框 -->
    <el-dialog
        title="保存场景"
        v-model="saveDialogVisible"
        width="400px"
        :before-close="handleSaveDialogClose"
    >
      <el-form :model="saveForm" label-width="80px">
        <el-form-item label="场景名称" required>
          <el-input
              v-model="saveForm.name"
              placeholder="请输入场景名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input
              v-model="saveForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入场景描述（可选）"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveScene" :loading="saveLoading">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script>
import { createCompositeModel, getCompositeModelComponents, getModelMenu, getFileUrl, getModelStaticUrl, getModelPathUrl, getScenePathUrl, getGateStationUrl } from "@/api/index.js";
import { uploadScene, saveSceneTextures, getScene } from '@/api/scenes';
import {
  getDataPanels,
  createDataPanel,
  updateDataPanel,
  deleteDataPanel,
  bindDevice as bindPanelDeviceApi,
  unbindDevice as unbindPanelDeviceApi,
  bindModel as bindModelApi,
  unbindModel as unbindPanelModelApi
} from '@/api/dataPanel';
import { getAllDevices } from '@/api/device';
import { getModelBindings, createModelBinding, updateModelBinding, deleteModelBindingBySceneAndModel } from '@/api/modelBinding';
import { Loading } from '@element-plus/icons-vue';
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls";
import { GLTFExporter } from 'three/examples/jsm/exporters/GLTFExporter.js';
import { DRACOLoader } from "three/examples/jsm/loaders/DRACOLoader";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
import CompositeModelEditor from "./CompositeModelEditor.vue";
import CompositeModelSelector from "./CompositeModelSelector.vue";
import ModelSelector from "./ModelSelector.vue";
import ObjectInfo from "./ObjectInfo.vue";
import DeviceDataPanel from "@/components/DeviceDataPanel/index.vue";
import DataPanelManager from "@/components/DataPanelManager/index.vue";

export default {
  name: "ThreeScene",
  components: {
    ObjectInfo,
    ModelSelector,
    CompositeModelSelector,
    CompositeModelEditor,
    DeviceDataPanel,
    DataPanelManager,
    Loading
  },
  data() {
    return {
      selectedModel: null,
      currentSceneId: null,
      currentSceneName: '',
      currentSceneDescription: '',
      sceneLoaded: false,
      isModelSelectorVisible: false,
      isCompositeModelSelectorVisible: false,
      isCompositeEditorVisible: false,
      modelData: [],
      loading: false,
      loadingProgress: 0,
      showTips: true,
      hasChanges: false,
      operationHistory: [],
      currentHistoryIndex: -1,
      skyboxEnabled: false,
      saveDialogVisible: false,
      saveLoading: false,
      saveForm: {
        name: '',
        description: ''
      },
      // 闸站场景相关数据
      showGateStation: false,
      gateStationLoading: false,
      animateId: null,
      // 防止组合模型重复保存
      savingCompositeModel: false,
      saveCompositeModelTimeout: null,
      // 贴图相关数据
      modelTextures: new Map(), // 存储模型贴图信息
      textureCache: new Map(), // 临时贴图缓存
      // 数据监控面板 - 多展板支持
      showPanelManager: false,
      openedPanels: [],
      allPanels: [],
      draftPanels: [],
      devices: [],
      modelBindings: [],
      bindMode: false,
      bindingPanelId: null,
      boundModels: {},
      cachedCanvasRect: null,
      cachedVector3: null
    };
  },

  computed: {
    openedPanelIds() {
      return this.openedPanels.map(p => p.id);
    },
    panelBindings() {
      return this.openedPanels.reduce((acc, panel) => {
        const binding = this.getBindingForPanel(panel);
        if (binding) {
          acc[this.getPanelKey(panel)] = binding;
        }
        return acc;
      }, {});
    },
    boundPanelsCount() {
      return this.allPanels.filter(p => p.modelId || p.deviceId).length;
    }
  },

  mounted() {
    const sceneId = this.$route?.query?.sceneId
    if (sceneId) {
      this.currentSceneId = sceneId
      this.loadFromSceneLibrary(sceneId)
    }
    
    this.loadAllPanels();
    this.loadDevices();
    this.loadSceneBindings();
    
    this.$nextTick(() => {
      setTimeout(() => {
        this.onWindowResize();
      }, 100);
    });
    
    window.testTextureApplication = this.testTextureApplication.bind(this);
  },

  beforeUnmount() {
    this.closeAllPanels();
    
    if (this.boundOnWindowResize) {
      window.removeEventListener("resize", this.boundOnWindowResize);
    }
    
    if (this.renderer && this.renderer.domElement) {
      this.renderer.domElement.removeEventListener("click", this.onClick.bind(this));
    }
    document.removeEventListener("keydown", this.handleKeyDown.bind(this));
    
    if (this.animateId) {
      cancelAnimationFrame(this.animateId);
      this.animateId = null;
    }
    
    if (this.renderer) {
      const container = document.querySelector('.scene-container');
      if (container && this.renderer.domElement && this.renderer.domElement.parentNode === container) {
        container.removeChild(this.renderer.domElement);
      }
      this.renderer.dispose();
      this.renderer = null;
    }
    
    this.scene = null;
    this.camera = null;
    this.controls = null;
    this.helperGroup = null;
  },

  watch: {
    '$route.query.sceneId': {
      immediate: false,
      handler(v) {
        if (v) {
          this.currentSceneId = v
          this.loadFromSceneLibrary(v)
          this.loadSceneBindings(v)
        }
      }
    }
  },

  methods: {
    getModelStorageId(model) {
      return model?.userData?.sceneModelId || model?.uuid || model?.name;
    },

    getPanelKey(panel) {
      if (!panel) {
        return null;
      }
      return panel._panelKey || (panel.id != null ? String(panel.id) : null);
    },

    normalizePanel(panel, preferredKey = null) {
      const panelKey = preferredKey || panel?._panelKey || (panel?.id != null ? String(panel.id) : `panel-${Date.now()}`);
      return {
        ...panel,
        _panelKey: panelKey
      };
    },

    createDraftPanel(values) {
      const panelKey = `draft-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`;
      return this.normalizePanel({
        id: panelKey,
        sceneId: null,
        name: values.name,
        description: values.description || '',
        status: values.status ?? 1,
        position: values.position || null,
        size: values.size || null,
        style: values.style || null,
        deviceId: values.deviceId || null,
        deviceName: values.deviceName || null,
        modelId: values.modelId || null,
        modelName: values.modelName || null,
        modelType: values.modelType || null
      }, panelKey);
    },

    normalizePanelForExport(panel) {
      return {
        name: panel.name,
        description: panel.description || '',
        status: panel.status ?? 1,
        deviceId: panel.deviceId ?? null,
        deviceName: panel.deviceName ?? null,
        modelId: panel.modelId ?? null,
        modelName: panel.modelName ?? null,
        modelType: panel.modelType ?? null,
        position: panel.position || null,
        size: panel.size || null,
        style: panel.style || null
      };
    },

    normalizeImportedPanel(panel, index) {
      return this.createDraftPanel({
        name: panel?.name || `展板 ${index + 1}`,
        description: panel?.description || '',
        status: panel?.status ?? 1,
        deviceId: panel?.deviceId ?? null,
        deviceName: panel?.deviceName ?? null,
        modelId: panel?.modelId ?? null,
        modelName: panel?.modelName ?? null,
        modelType: panel?.modelType ?? null,
        position: panel?.position || null,
        size: panel?.size || null,
        style: panel?.style || null
      });
    },

    resetPanelState({ clearDrafts = false } = {}) {
      this.closeAllPanels();
      this.allPanels = [];
      if (clearDrafts) {
        this.draftPanels = [];
      }
    },

    reconcileOpenedPanels() {
      const currentKeys = this.openedPanels.map(panel => this.getPanelKey(panel)).filter(Boolean);
      this.openedPanels = currentKeys
        .map(key => this.allPanels.find(panel => this.getPanelKey(panel) === key))
        .filter(Boolean);

      const nextBoundModels = {};
      currentKeys.forEach(key => {
        if (this.boundModels[key]) {
          nextBoundModels[key] = this.boundModels[key];
        }
      });
      this.boundModels = nextBoundModels;
    },

    async persistDraftPanels(sceneId) {
      if (!sceneId || this.draftPanels.length === 0) {
        return;
      }

      const createdPanels = [];
      for (const draftPanel of this.draftPanels) {
        const payload = {
          name: draftPanel.name,
          description: draftPanel.description,
          status: draftPanel.status,
          position: draftPanel.position,
          size: draftPanel.size,
          style: draftPanel.style,
          deviceId: draftPanel.deviceId,
          deviceName: draftPanel.deviceName,
          modelId: draftPanel.modelId,
          modelName: draftPanel.modelName,
          modelType: draftPanel.modelType,
          sceneId
        };
        const response = await createDataPanel(payload);
        const created = response.data || response;
        createdPanels.push(this.normalizePanel(created, this.getPanelKey(draftPanel)));
      }

      this.allPanels = createdPanels;
      this.draftPanels = [];
      this.reconcileOpenedPanels();
    },

    normalizeBindingPayload(binding) {
      return {
        ...binding,
        sceneId: binding.sceneId || this.currentSceneId,
        modelId: binding.modelId,
        modelName: binding.modelName || '',
        ruleStatus: binding.ruleStatus ?? 1
      };
    },

    buildSceneMetadataPayload() {
      return {
        version: '1.0.0',
        textures: Object.fromEntries(this.textureCache),
        bindings: this.modelBindings.map(binding => ({
          sceneId: binding.sceneId,
          modelId: binding.modelId,
          modelName: binding.modelName,
          deviceId: binding.deviceId,
          deviceCode: binding.deviceCode,
          deviceName: binding.deviceName,
          dataType: binding.dataType,
          ruleStatus: binding.ruleStatus
        })),
        panels: this.allPanels.map(panel => this.normalizePanelForExport(panel))
      };
    },

    parseSceneMetadata(textureInfo) {
      if (!textureInfo) {
        return { version: 'legacy', textures: {}, bindings: [], panels: [] };
      }

      try {
        const parsed = typeof textureInfo === 'string' ? JSON.parse(textureInfo) : textureInfo;
        if (parsed && parsed.textures) {
          return {
            version: parsed.version || '1.0.0',
            textures: parsed.textures || {},
            bindings: parsed.bindings || [],
            panels: parsed.panels || []
          };
        }
        return {
          version: 'legacy',
          textures: parsed || {},
          bindings: [],
          panels: []
        };
      } catch (error) {
        console.error('瑙ｆ瀽鍦烘櫙鍏冩暟鎹け璐?', error);
        return { version: 'legacy', textures: {}, bindings: [], panels: [] };
      }
    },

    normalizeSceneImportData(sceneData) {
      const version = sceneData?.version || 'legacy';
      const models = Array.isArray(sceneData?.models) ? sceneData.models : [];
      const bindings = Array.isArray(sceneData?.bindings) ? sceneData.bindings : [];
      const panels = Array.isArray(sceneData?.panels) ? sceneData.panels : [];
      const skybox = sceneData?.skybox || { enabled: !!sceneData?.skyboxEnabled };
      const sceneMeta = sceneData?.sceneMeta || {};

      return {
        version,
        sceneMeta,
        skybox,
        models: models.map((model, index) => {
          const file = model.file || model?.userData?.file;
          return {
            id: model.id || model.uuid || model.sceneModelId || `imported-model-${index}`,
            uuid: model.uuid || model.id || model.sceneModelId || `imported-model-${index}`,
            name: model.name || `模型_${index + 1}`,
            file,
            position: model.position || { x: 0, y: 0, z: 0 },
            rotation: model.rotation || { x: 0, y: 0, z: 0 },
            scale: model.scale || { x: 1, y: 1, z: 1 },
            boundData: model.boundData || null,
            userData: model.userData || {}
          };
        }).filter(model => !!model.file),
        bindings,
        panels: panels.map((panel, index) => this.normalizeImportedPanel(panel, index))
      };
    },

    serializeSceneModel(model) {
      return {
        id: this.getModelStorageId(model),
        uuid: model.uuid,
        name: model.name,
        file: model.userData?.file || model.file || model.userData?.originalModel?.file || null,
        position: {
          x: model.position.x,
          y: model.position.y,
          z: model.position.z
        },
        rotation: {
          x: model.rotation.x,
          y: model.rotation.y,
          z: model.rotation.z
        },
        scale: {
          x: model.scale.x,
          y: model.scale.y,
          z: model.scale.z
        },
        boundData: model.boundData || null,
        userData: {
          sceneModelId: this.getModelStorageId(model)
        }
      };
    },

    exportSceneStructure() {
      const models = [];
      this.scene.traverse((object) => {
        if (object.isGroup && object !== this.helperGroup && object.parent === this.scene) {
          const serialized = this.serializeSceneModel(object);
          if (serialized.file) {
            models.push(serialized);
          }
        }
      });

      return {
        version: '1.0.0',
        sceneMeta: {
          id: this.currentSceneId,
          name: this.currentSceneName,
          description: this.currentSceneDescription
        },
        skybox: {
          enabled: this.skyboxEnabled
        },
        models,
        bindings: this.modelBindings.map(binding => ({
          sceneId: binding.sceneId,
          modelId: binding.modelId,
          modelName: binding.modelName,
          deviceId: binding.deviceId,
          deviceCode: binding.deviceCode,
          deviceName: binding.deviceName,
          dataType: binding.dataType,
          ruleStatus: binding.ruleStatus
        })),
        panels: this.allPanels.map(panel => this.normalizePanelForExport(panel))
      };
    },
    // 添加测试贴图功能的方法
    testTextureApplication() {
      console.log('=== 测试贴图功能 ===');
      if (!this.selectedModel) {
        console.log('没有选中的模型');
        this.$message.warning('请先选择一个模型');
        return;
      }
      
      console.log('选中的模型', this.selectedModel);
      
      // 鍒涘缓涓€涓槑鏄剧殑娴嬭瘯璐村浘
      const canvas = document.createElement('canvas');
      canvas.width = 256;
      canvas.height = 256;
      const ctx = canvas.getContext('2d');
      
      // 创建非常明显的图案 - 四个大色块
      ctx.fillStyle = '#FF0000'; // 红色
      ctx.fillRect(0, 0, 128, 128);
      
      ctx.fillStyle = '#00FF00'; // 绿色
      ctx.fillRect(128, 0, 128, 128);
      
      ctx.fillStyle = '#0000FF'; // 蓝色
      ctx.fillRect(0, 128, 128, 128);
      
      ctx.fillStyle = '#FFFF00'; // 黄色
      ctx.fillRect(128, 128, 128, 128);
      
      // 添加粗体文字标识
      ctx.font = 'bold 30px Arial';
      ctx.fillStyle = '#000000';
      ctx.textAlign = 'center';
      ctx.fillText('TEST', 128, 140);
      
      const dataUrl = canvas.toDataURL();
      console.log('生成测试贴图数据:', dataUrl.substring(0, 100));
      
      this.previewTextureOnModel(this.selectedModel, dataUrl);
      this.$message.info("测试贴图已应用，请观察模型变化");
    },
    
    // 测试移除贴图功能
    testRemoveTexture() {
      console.log('=== 测试移除贴图功能 ===');
      if (!this.selectedModel) {
        console.log('没有选中的模型');
        this.$message.warning('请先选择一个模型');
        return;
      }
      
      console.log('选中的模型', this.selectedModel);
      this.removeTextureFromModel(this.selectedModel);
      this.$message.info("测试移除贴图已完成");
    },
    
    async createNewScene() {
      this.currentSceneId = null;
      this.currentSceneName = '';
      this.currentSceneDescription = '';
      this.resetPanelState({ clearDrafts: true });
      this.sceneLoaded = true;
      this.$nextTick(() => {
        this.initScene();
      });
    },
    // 添加防抖的窗口调整大小处理
    debouncedResize: null,
    
    onWindowResize() {
      // 清除之前的防抖定时器
      if (this.debouncedResize) {
        clearTimeout(this.debouncedResize);
      }
      
      // 设置新的防抖定时器
      this.debouncedResize = setTimeout(() => {
        this.handleResize();
      }, 100);
    },
    
    handleResize() {
      if (!this.camera || !this.renderer) {
        return;
      }

      let container = null;
      
      if (this.sceneLoaded && !this.showGateStation) {
        container = document.querySelector('.scene-container');
      } else if (this.showGateStation) {
        container = document.querySelector('.gate-station-container');
      }

      if (!container || container.clientWidth === 0 || container.clientHeight === 0) {
        return;
      }

      const width = container.clientWidth;
      const height = container.clientHeight;

      this.camera.aspect = width / height;
      this.cachedCanvasRect = null;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(width, height);
    },
    
    initScene() {
      const container = document.querySelector('.scene-container');
      if (!container) {
        console.error('Scene container not found');
        return;
      }

      // 妫€鏌ュ鍣ㄥ昂瀵?
      const width = container.clientWidth;
      const height = container.clientHeight;
      
      if (width === 0 || height === 0) {
        console.warn('Container has zero dimensions, waiting for layout...');
        // 鍙互娣诲姞閲嶈瘯閫昏緫鎴栦娇鐢ㄩ粯璁ゅ昂瀵?
        setTimeout(() => {
          this.initScene();
        }, 100);
        return;
      }

      console.log('Initializing scene with dimensions:', width, height);

      this.scene = new THREE.Scene();
      this.scene.background = new THREE.Color(0xeeeeee);

      this.camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 10000); // 澧炲姞杩滃钩闈㈣窛绂?
      this.camera.position.set(0, 9, 4);
      this.camera.updateProjectionMatrix();

      this.renderer = new THREE.WebGLRenderer({ antialias: true });
      this.renderer.setSize(width, height);
      this.renderer.setPixelRatio(window.devicePixelRatio); // 娣诲姞鍍忕礌姣斾緥鏀寔
      container.appendChild(this.renderer.domElement);

      this.controls = new OrbitControls(this.camera, this.renderer.domElement);
      this.controls.enableDamping = true;

      this.initLoaders();
      this.addLighting();

      this.helperGroup = new THREE.Group();
      // 鍒涘缓鏇村ぇ鐨勭綉鏍间互纭繚瀹屾暣鏄剧ず
      const gridHelper = new THREE.GridHelper(2000, 2000, 0x888888, 0xcccccc);
      gridHelper.material.opacity = 0.3;
      gridHelper.material.transparent = true;
      this.helperGroup.add(gridHelper);
      this.scene.add(this.helperGroup);

      // 纭繚缁戝畾姝ｇ‘鐨?this 涓婁笅鏂?
      this.boundOnWindowResize = this.onWindowResize.bind(this);
      window.addEventListener("resize", this.boundOnWindowResize);
      
      this.renderer.domElement.addEventListener("click", this.onClick.bind(this));
      document.addEventListener("keydown", this.handleKeyDown.bind(this));

      this.raycaster = new THREE.Raycaster();
      this.mouse = new THREE.Vector2();

      this.animate();

      // 缁熶竴璁剧疆绐楀彛寮曠敤
      window.threeScene = {
        scene: this.scene,
        camera: this.camera,
        renderer: this.renderer,
        controls: this.controls,
        helperGroup: this.helperGroup
      };
    },

    animate() {
      if (!this.renderer) return;
      this.animateId = requestAnimationFrame(this.animate.bind(this));
      this.renderer.render(this.scene, this.camera);
      this.controls.update();
      Object.keys(this.boundModels).forEach(panelId => {
        const boundModel = this.boundModels[panelId];
        if (boundModel && boundModel.object) {
          const pos = this.getScreenPosition(boundModel.object);
          if (pos && boundModel.screenPosition) {
            boundModel.screenPosition.x = pos.x;
            boundModel.screenPosition.y = pos.y;
          } else if (pos) {
            boundModel.screenPosition = pos;
          }
        }
      });
    },

    bindData(model, data) {
      model.boundData = data;
    },

    initLoaders() {
      this.dracoLoader = new DRACOLoader();
      this.dracoLoader.setDecoderPath("./draco/");
      this.gltfLoader = new GLTFLoader();
      this.gltfLoader.setDRACOLoader(this.dracoLoader);
    },

    addLighting() {
      const light = new THREE.DirectionalLight(0xffffff, 1);
      light.position.set(0, 10, 10);
      this.scene.add(light);
      const ambientLight = new THREE.AmbientLight(0x404040);
      this.scene.add(ambientLight);
      
      // 娣诲姞棰濆鐨勫厜婧愪互纭繚缃戞牸鍚勯儴鍒嗛兘鑳借鐓т寒
      const light2 = new THREE.DirectionalLight(0xffffff, 0.5);
      light2.position.set(0, -10, -10);
      this.scene.add(light2);
    },

    async selectScene(sceneName) {
      this.loadScene(sceneName);
    },

    async loadScene(sceneName) {
      try {
        const response = await fetch(`/scenes/${sceneName}.json`);
        if (!response.ok) {
          throw new Error('无法加载场景文件');
        }
        const sceneData = await response.json();

        this.sceneLoaded = true;
        this.$nextTick(() => {
          this.initScene();
          this.loadModels(sceneData.models);

          if (sceneData.skyboxEnabled) {
            this.skyboxEnabled = true;
            this.setSkyboxBackground();
          }

          console.log(`场景 ${sceneName} 加载成功`);
        });
      } catch (error) {
        console.error('加载场景失败:', error);
        this.$message.error(`加载场景失败: ${error.message}`);
      }
    },

    async saveScene() {
      const modelCount = this.scene.children.filter(child =>
          child !== this.helperGroup && child.isGroup
      ).length;

      this.saveForm.name = this.currentSceneName || `场景_${new Date().toLocaleDateString()}_${modelCount}个模型`;
      this.saveForm.description = this.currentSceneDescription || '';
      this.saveDialogVisible = true;
    },

    async confirmSaveScene() {
      if (!this.saveForm.name || this.saveForm.name.trim() === '') {
        this.$message.error('请输入场景名称');
        return;
      }

      this.saveLoading = true;
      try {
        const glbBlob = await this.exportSceneToGLBBlob();
        if (!glbBlob) {
          this.$message.error('场景导出失败');
          this.saveLoading = false;
          return;
        }

        const fileName = `${this.saveForm.name.replace(/[^\w\u4e00-\u9fa5]/g, '_')}.glb`;
        const file = new File([glbBlob], fileName, {
          type: 'application/octet-stream'
        });

        const sceneAsset = await uploadScene(
            file,
            this.saveForm.name.trim(),
            this.saveForm.description.trim(),
            this.currentSceneId
        );
        
        // 保存贴图信息
        if (sceneAsset && sceneAsset.id) {
          this.currentSceneId = sceneAsset.id;
          this.currentSceneName = sceneAsset.name || this.saveForm.name.trim();
          this.currentSceneDescription = sceneAsset.description || this.saveForm.description.trim();
          await this.syncSceneBindings(sceneAsset.id);
          await this.persistDraftPanels(sceneAsset.id);
          const texturesToSave = this.saveSceneTextures(sceneAsset.id);
          const shouldSaveMetadata = texturesToSave.length > 0 || this.modelBindings.length > 0;
          if (shouldSaveMetadata) {
            const textureInfo = JSON.stringify(this.buildSceneMetadataPayload());
            try {
              await saveSceneTextures(sceneAsset.id, textureInfo);
              console.log('贴图信息保存成功');
            } catch (err) {
              console.error('保存贴图信息失败:', err);
            }
          }
          await this.loadAllPanels(sceneAsset.id);
        }

        this.$message.success('场景已保存到场景库');
        this.saveDialogVisible = false;
        this.resetSaveForm();

      } catch (err) {
        console.error('保存场景失败:', err);
        this.$message.error(`保存场景失败: ${err.message}`);
      } finally {
        this.saveLoading = false;
      }
    },

    handleSaveDialogClose(done) {
      if (this.saveLoading) {
        this.$message.warning('姝ｅ湪淇濆瓨锛岃绋嶅€?..');
        return;
      }
      this.resetSaveForm();
      done();
    },

    resetSaveForm() {
      this.saveForm = {
        name: '',
        description: ''
      };
    },

    loadModel(modelData) {
      const normalizedModel = {
        ...modelData,
        id: modelData.id || modelData.uuid || `scene-model-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
        position: modelData.position || { x: 0, y: 0, z: 0 },
        scale: modelData.scale || { x: 1, y: 1, z: 1 },
        rotation: modelData.rotation || { x: 0, y: 0, z: 0 },
        userData: modelData.userData || {}
      };
      console.log(`load model: ${normalizedModel.file}`);
      this.gltfLoader.load(normalizedModel.file, (gltf) => {
        const model = gltf.scene;
        model.name = normalizedModel.name;
        model.file = normalizedModel.file;
        model.position.set(normalizedModel.position.x, normalizedModel.position.y, normalizedModel.position.z);
        model.scale.set(normalizedModel.scale.x, normalizedModel.scale.y, normalizedModel.scale.z);
        model.rotation.set(normalizedModel.rotation.x, normalizedModel.rotation.y, normalizedModel.rotation.z);
        model.boundData = normalizedModel.boundData || null;

        const modelGroup = new THREE.Group();
        modelGroup.add(model);
        modelGroup.name = normalizedModel.name;
        modelGroup.position.copy(model.position);
        modelGroup.scale.copy(model.scale);
        modelGroup.rotation.copy(model.rotation);
        modelGroup.boundData = normalizedModel.boundData || null;
        modelGroup.userData = {
          ...normalizedModel.userData,
          file: normalizedModel.file,
          sceneModelId: normalizedModel.id,
          originalModel: model
        };
        modelGroup.file = normalizedModel.file;

        this.scene.add(modelGroup);
        window.threeScene.scene = this.scene;
      }, undefined, (error) => {
        console.error('load model failed:', error);
        this.$message.error(`加载模型失败: ${normalizedModel.name}`);
      });
    },

    loadModels(models) {
      models.forEach((modelData) => this.loadModel(modelData));
    },

    onClick(event) {
      if (!this.sceneLoaded || this.showGateStation) return;

      console.log("点击事件触发");

      const container = document.querySelector('.scene-container');
      const rect = container.getBoundingClientRect();
      this.mouse.x = (event.clientX - rect.left) / rect.width * 2 - 1;
      this.mouse.y = - (event.clientY - rect.top) / rect.height * 2 + 1;

      console.log("鼠标坐标:", this.mouse.x, this.mouse.y);

      this.raycaster.setFromCamera(this.mouse, this.camera);

      const clickableObjects = this.scene.children.filter(child => child !== this.helperGroup);
      console.log("可点击对象数量", clickableObjects.length);

      const intersects = this.raycaster.intersectObjects(clickableObjects, true);
      console.log("射线检测结果", intersects);

      if (intersects.length > 0) {
        let selectedObject = intersects[0].object;
        while (selectedObject.parent && !selectedObject.parent.isScene) {
          selectedObject = selectedObject.parent;
        }

        console.log("找到模型:", selectedObject);
        if (this.bindMode) {
          this.bindModelToPanel(selectedObject);
        } else {
          this.selectModel(selectedObject);
          console.log("选中的模型", this.selectedModel);
        }
      } else {
        console.log("没有找到模型，取消选中");
        this.deselectModel();
      }
    },

    selectModel(model) {
      console.log("selectModel 被调用，模型:", model);

      this.deselectModel();

      this.selectedModel = model;
      this.selectedModel.boundData = this.getBindingForModel(this.getModelStorageId(model));
      console.log("selectedModel 已设置", this.selectedModel);

      this.addSelectionEffect(model);

      this.$forceUpdate();
    },

    deselectModel() {
      if (this.selectedModel) {
        this.removeSelectionEffect(this.selectedModel);
        this.selectedModel = null;
      }
    },

    addSelectionEffect(model) {
      model.traverse(child => {
        if (child.isMesh) {
          // 淇濆瓨鍘熷鏉愯川锛堝鏋滃皻鏈繚瀛橈級
          if (!child.userData.originalMaterial) {
            child.userData.originalMaterial = child.material.clone();
          }

          // 鍒涘缓楂樹寒鏉愯川锛堝熀浜庡綋鍓嶆潗璐級
          const highlightMaterial = child.material.clone();
          
          // 娣诲姞杞粨鍏夋晥鏋滆€屼笉鏄敼鍙橀鑹?
          // 閫氳繃娣诲姞杞诲井鐨勮嚜鍙戝厜鏉ヨ〃绀洪€変腑鐘舵€?
          if (child.material.map) {
            // 瀵逛簬鏈夎创鍥剧殑鏉愯川锛屾坊鍔犻潪甯歌交寰殑鍙戝厜鏁堟灉
            highlightMaterial.emissive = new THREE.Color(0x333333);
            highlightMaterial.emissiveIntensity = 0.4;
          } else {
            // 瀵逛簬娌℃湁璐村浘鐨勬潗璐紝浣跨敤缁胯壊楂樹寒
            highlightMaterial.emissive = new THREE.Color(0x00ff00);
            highlightMaterial.emissiveIntensity = 0.3;
          }
          
          child.material = highlightMaterial;
        }
      });
    },

    removeSelectionEffect(model) {
      model.traverse(child => {
        if (child.isMesh && child.userData.originalMaterial) {
          // 鎭㈠鍘熷鏉愯川锛堝甫璐村浘鐨勬潗璐級
          const originalMaterial = child.userData.originalMaterial;
          child.material = originalMaterial;
          
          // 娉ㄦ剰锛氫笉瑕佸垹闄riginalMaterial寮曠敤锛屽洜涓烘垜浠彲鑳借繕浼氬啀娆￠€変腑杩欎釜妯″瀷
        }
      });
    },

    showModelSelector() {
      this.isModelSelectorVisible = true;
      this.loadModelData();
    },

    async loadModelData() {
      try {
        this.loading = true;

        const response = await getModelMenu();
        if (response && response.data) {
          this.modelData = response.data;
          console.log('妯″瀷鏁版嵁鍔犺浇鎴愬姛:', this.modelData);
          // 閫氱煡缁勫悎妯″瀷缂栬緫鍣ㄦ暟鎹凡鍔犺浇
          if (this.isCompositeEditorVisible && this.$refs.compositeModelEditor) {
            console.log('閫氱煡缁勫悎妯″瀷缂栬緫鍣ㄦ暟鎹凡鍔犺浇');
            this.$refs.compositeModelEditor.receiveModelData(this.modelData);
          }
        }
        return this.modelData; // 杩斿洖鏁版嵁
      } catch (error) {
        console.error('鍔犺浇妯″瀷鏁版嵁澶辫触:', error);
        this.$message.error('鍔犺浇妯″瀷鏁版嵁澶辫触: ' + (error.message || '鏈煡閿欒'));
        throw error;
      } finally {
        this.loading = false;
      }
    },

    handleModelSelection(modelData) {
      console.log('selected model data:', modelData);

      if (modelData && modelData.file) {
        const modelInfo = {
          id: `scene-model-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
          name: modelData.name || '?????',
          file: modelData.file,
          position: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
          rotation: { x: 0, y: 0, z: 0 },
          boundData: null,
          userData: {}
        };

        this.loadModel(modelInfo);

        this.recordOperation({
          type: 'add_model',
          modelId: modelInfo.id,
          modelData: modelInfo
        });

        this.$message.success(`?? ${modelInfo.name} ??????`);
      } else {
        console.error('invalid model data:', modelData);
        this.$message.error('??????');
      }
    },

    handleDeleteModel(model) {
      const modelId = this.getModelStorageId(model);
      this.handleRemoveModelBinding(modelId);
      if (model && model.parent) {
        model.parent.remove(model);
      }

      this.selectedModel = null;
      console.log(`妯″瀷 ${model.name} 宸茶鍒犻櫎`);
    },

    // 闂哥珯鍦烘櫙鏂规硶
    openGateStationInterface() {
      this.showGateStation = true;
      this.gateStationLoading = true;

      // 娓呯悊3D鍦烘櫙
      this.cleanupThreeScene();
    },

    closeGateStation() {
      this.showGateStation = false;
      this.gateStationLoading = false;
    },

    refreshGateStation() {
      this.gateStationLoading = true;
      const iframe = this.$refs.gateStationIframe;
      if (iframe) {
        iframe.src = iframe.src;
      }
    },

    openGateStationInNewTab() {
      window.open(getGateStationUrl(), '_blank');
    },

    onIframeLoad() {
      this.gateStationLoading = false;
      console.log('闂哥珯鍦烘櫙鍔犺浇瀹屾垚');
    },

    cleanupThreeScene() {
      // 鏆傚仠鍔ㄧ敾
      if (this.animateId) {
        cancelAnimationFrame(this.animateId);
        this.animateId = null;
      }

      // 绉婚櫎娓叉煋鍣?
      if (this.renderer) {
        const container = document.querySelector('.scene-container');
        if (container && this.renderer.domElement) {
          container.removeChild(this.renderer.domElement);
        }
        this.renderer.dispose();
        this.renderer = null;
      }

      // 娓呯悊鍏朵粬璧勬簮
      this.scene = null;
      this.camera = null;
      this.controls = null;
      this.helperGroup = null;
      this.selectedModel = null;
    },

    async importScene() {
      this.currentSceneId = null;
      this.currentSceneName = '';
      this.currentSceneDescription = '';
      this.resetPanelState({ clearDrafts: true });
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.json,.glb,.gltf';
      input.onchange = (event) => {
        const file = event.target.files[0];
        if (!file) return;
        const ext = file.name.toLowerCase().split('.').pop();

        if (ext === 'json') {
          const reader = new FileReader();
          reader.onload = async () => {
            try {
              const imported = this.normalizeSceneImportData(JSON.parse(reader.result));
              this.sceneLoaded = true;
              this.modelBindings = imported.bindings || [];
              this.draftPanels = imported.panels || [];
              this.allPanels = this.draftPanels.map(panel => this.normalizePanel(panel, this.getPanelKey(panel)));
              this.reconcileOpenedPanels();
              this.$nextTick(() => {
                this.initScene();
                this.loadModels(imported.models);
                this.skyboxEnabled = !!imported.skybox?.enabled;
                if (this.skyboxEnabled) {
                  this.setSkyboxBackground();
                }
                this.$message.success('scene imported successfully');
              });
            } catch (error) {
              console.error('import scene failed:', error);
              this.$message.error('scene import failed');
            }
          };
          reader.readAsText(file);
          return;
        }

        if (ext === 'glb' || ext === 'gltf') {
          const url = URL.createObjectURL(file);
          this.sceneLoaded = true;
          this.$nextTick(() => {
            this.initScene();
            this.gltfLoader.load(url, (gltf) => {
              const model = gltf.scene;
              model.name = file.name.replace(/\.(glb|gltf)$/i, '');
              model.userData = {
                ...(model.userData || {}),
                sceneModelId: `scene-model-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
              };
              this.scene.add(model);
              window.threeScene.scene = this.scene;
              this.$message.success('GLB/GLTF imported successfully');
              URL.revokeObjectURL(url);
            }, undefined, (err) => {
              console.error('import glb failed:', err);
              this.$message.error('GLB/GLTF import failed');
              URL.revokeObjectURL(url);
            });
          });
          return;
        }

        this.$message.warning('unsupported file type');
      };
      input.click();
    },

    exportSceneToGLB() {
      if (!this.scene || !this.helperGroup) return;

      this.scene.remove(this.helperGroup);

      const exporter = new GLTFExporter();
      exporter.parse(
          this.scene,
          (gltf) => {
            if (gltf instanceof ArrayBuffer) {
              const blob = new Blob([gltf], { type: 'application/octet-stream' });
              const link = document.createElement('a');
              link.href = URL.createObjectURL(blob);
              link.download = 'scene.glb';
              link.click();
              URL.revokeObjectURL(link.href);
              this.$message.success('GLB瀵煎嚭鎴愬姛');
            }
          },
          (error) => {
            console.error('GLB瀵煎嚭澶辫触:', error);
            this.$message.error('GLB瀵煎嚭澶辫触');
          },
          { binary: true }
      );

      this.scene.add(this.helperGroup);
    },

    async exportSceneToGLBBlob() {
      return new Promise((resolve, reject) => {
        if (!this.scene || !this.helperGroup) {
          reject(new Error('鍦烘櫙鏈垵濮嬪寲'));
          return;
        }

        this.scene.remove(this.helperGroup);

        const exporter = new GLTFExporter();
        exporter.parse(
            this.scene,
            (gltf) => {
              if (gltf instanceof ArrayBuffer) {
                const blob = new Blob([gltf], { type: 'application/octet-stream' });
                this.scene.add(this.helperGroup);
                resolve(blob);
              } else {
                this.scene.add(this.helperGroup);
                reject(new Error('GLB导出失败：格式错误'));
              }
            },
            (error) => {
              this.scene.add(this.helperGroup);
              reject(error);
            },
            { binary: true }
        );
      });
    },

    exportSceneToJSON() {
      if (!this.scene || !this.helperGroup) return;

      this.scene.remove(this.helperGroup);

      const sceneData = this.exportSceneStructure();
      const jsonString = JSON.stringify(sceneData, null, 2);
      const blob = new Blob([jsonString], { type: 'application/json' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = 'scene.json';
      link.click();
      URL.revokeObjectURL(link.href);

      this.scene.add(this.helperGroup);
      this.$message.success('JSON exported successfully');
    },

    recordOperation(operation) {
      this.operationHistory.splice(this.currentHistoryIndex + 1);

      this.operationHistory.push({
        ...operation,
        timestamp: Date.now()
      });

      if (this.operationHistory.length > 50) {
        this.operationHistory.shift();
      } else {
        this.currentHistoryIndex++;
      }

      this.hasChanges = this.operationHistory.length > 0;
    },

    undo() {
      if (this.currentHistoryIndex >= 0) {
        const operation = this.operationHistory[this.currentHistoryIndex];
        this.executeUndo(operation);
        this.currentHistoryIndex--;
        this.$message.success('鎾ら攢鎿嶄綔');
      }
    },

    redo() {
      this.clearAllModels();
    },
    clearAllModels() {
      if (!this.scene) return;
      this.$confirm('确定要清除场景中的所有模型吗？此操作不可撤销。', '提示', {
        confirmButtonText: '纭畾',
        cancelButtonText: '鍙栨秷',
        type: 'warning'
      }).then(() => {
        // 鑾峰彇鎵€鏈夋ā鍨嬶紙鎺掗櫎杈呭姪瀵硅薄锛?
        const models = this.scene.children.filter(child =>
            child !== this.helperGroup && child.isGroup
        );
        models.forEach(model => {
          this.scene.remove(model);
        });

        this.deselectModel();

        // 閲嶇疆鎿嶄綔鍘嗗彶
        this.operationHistory = [];
        this.currentHistoryIndex = -1;
        this.hasChanges = false;

        this.$message.success(`已清除 ${models.length} 个模型`);
      }).catch(() => {
      });
    },

        executeUndo(operation) {
      switch (operation.type) {
        case 'add_model':
          this.removeModelFromScene(operation.modelId);
          break;
        case 'delete_model':
          this.addModelToScene(operation.modelData);
          break;
        case 'move_model':
          this.moveModelToPosition(operation.modelId, operation.oldPosition);
          break;
        case 'scale_model':
          this.scaleModelTo(operation.modelId, operation.oldScale);
          break;
        case 'rotate_model':
          this.rotateModelTo(operation.modelId, operation.oldRotation);
          break;
      }
    },

    executeRedo(operation) {
      switch (operation.type) {
        case 'add_model':
          this.addModelToScene(operation.modelData);
          break;
        case 'delete_model':
          this.removeModelFromScene(operation.modelId);
          break;
        case 'move_model':
          this.moveModelToPosition(operation.modelId, operation.newPosition);
          break;
        case 'scale_model':
          this.scaleModelTo(operation.modelId, operation.newScale);
          break;
        case 'rotate_model':
          this.rotateModelTo(operation.modelId, operation.newRotation);
          break;
      }
    },

    removeModelFromScene(modelId) {
      const model = this.scene.children.find(child => child.name === modelId);
      if (model) {
        this.scene.remove(model);
      }
    },

    addModelToScene(modelData) {
      this.loadModel(modelData);
    },

    moveModelToPosition(modelId, position) {
      const model = this.scene.children.find(child => child.name === modelId);
      if (model) {
        model.position.set(position.x, position.y, position.z);
      }
    },

    scaleModelTo(modelId, scale) {
      const model = this.scene.children.find(child => child.name === modelId);
      if (model) {
        model.scale.set(scale.x, scale.y, scale.z);
      }
    },

    rotateModelTo(modelId, rotation) {
      const model = this.scene.children.find(child => child.name === modelId);
      if (model) {
        model.rotation.set(rotation.x, rotation.y, rotation.z);
      }
    },

    handleKeyDown(event) {
      if (this.showGateStation) return; // 闂哥珯鍦烘櫙涓笉鍝嶅簲蹇嵎閿?

      if (event.ctrlKey || event.metaKey) {
        switch (event.key.toLowerCase()) {
          case 'z':
            event.preventDefault();
            this.undo();
            break;
          case 'y':
            event.preventDefault();
            this.redo();
            break;
        }
      } else if (event.key === 'Delete' && this.selectedModel) {
        event.preventDefault();
        this.handleDeleteModel(this.selectedModel);
      }
    },

    goHome() {
      this.closeAllPanels();
      this.allPanels = [];
      this.draftPanels = [];
      this.modelBindings = [];
      if (this.showGateStation) {
        this.closeGateStation();
      } else if (this.sceneLoaded) {
        this.cleanupThreeScene();
        this.sceneLoaded = false;
      }
    },

    handleEnvironmentChange(settings) {
      switch(settings.type) {
        case 'color':
          this.setSolidColor(settings.color);
          break;
        case 'skybox':
          this.setSkyboxBackground();
          break;
        case 'dynamic':
          this.setDynamicSky();
          break;
        case 'none':
          this.clearBackground();
          break;
      }
    },
    setSolidColor(color) {
      this.scene.background = new THREE.Color(color);
      this.skyboxEnabled = false;
      this.$message.success(`鑳屾櫙棰滆壊宸茶缃负 ${color}`);
    },

    setDynamicSky() {
      // 绠€鍖栫殑鍔ㄦ€佸ぉ绌哄疄鐜?
      const skyColor = new THREE.Color(0x87CEEB);
      this.scene.background = skyColor;
      this.skyboxEnabled = false;
      this.$message.success('鍔ㄦ€佸ぉ绌哄凡鍚敤锛堝熀纭€鐗堬級');
    },
    clearBackground() {
      this.scene.background = null;
      this.skyboxEnabled = false;
      this.$message.success('背景已清除');
    },

    toggleSkybox() {
      if (this.skyboxEnabled) {
        this.scene.background = new THREE.Color(0xeeeeee);
        this.skyboxEnabled = false;
        this.$message.success('天空背景已关闭');
      } else {
        this.setSkyboxBackground();
        this.skyboxEnabled = true;
        this.$message.success('天空背景已开启');
      }
    },

    toggleDataPanelManager() {
      this.showPanelManager = !this.showPanelManager;
    },

    async loadDevices() {
      try {
        const response = await getAllDevices();
        this.devices = response.data || response || [];
      } catch (error) {
        console.error('load devices failed:', error);
        this.devices = [];
      }
    },

    async loadAllPanels(sceneId = this.currentSceneId) {
      if (!sceneId) {
        this.allPanels = this.draftPanels.map(panel => this.normalizePanel(panel, this.getPanelKey(panel)));
        this.reconcileOpenedPanels();
        return;
      }
      try {
        const res = await getDataPanels({ sceneId });
        const panels = res.data || res || [];
        this.allPanels = panels.map(panel => {
          const existing = this.allPanels.find(item => item.id === panel.id);
          return this.normalizePanel(panel, existing ? this.getPanelKey(existing) : null);
        });
        this.reconcileOpenedPanels();
      } catch (e) {
        console.error('加载展板列表失败:', e);
        this.allPanels = [];
      }
    },

    async loadSceneBindings(sceneId = this.currentSceneId) {
      if (!sceneId) {
        this.modelBindings = [];
        return;
      }

      try {
        const res = await getModelBindings({ sceneId });
        this.modelBindings = res.data || res || [];
      } catch (error) {
        console.error('????????:', error);
        this.modelBindings = [];
      }
    },

    getBindingForModel(modelId) {
      return this.modelBindings.find(binding => binding.modelId === modelId) || null;
    },

    getBindingForPanel(panel) {
      if (!panel?.modelId) {
        return null;
      }
      const binding = this.getBindingForModel(panel.modelId);
      if (!binding) {
        return null;
      }
      if (this.currentSceneId && binding.sceneId && String(binding.sceneId) !== String(this.currentSceneId)) {
        return null;
      }
      return binding;
    },

    async handleSaveModelBinding(binding) {
      const payload = this.normalizeBindingPayload(binding);
      try {
        const existing = this.getBindingForModel(payload.modelId);
        let savedBinding = { ...existing, ...payload };
        if (payload.sceneId) {
          const response = existing?.id
            ? await updateModelBinding(existing.id, savedBinding)
            : await createModelBinding(savedBinding);
          savedBinding = response.data || response;
        }
        const index = this.modelBindings.findIndex(item => item.modelId === savedBinding.modelId);
        if (index > -1) {
          this.modelBindings.splice(index, 1, savedBinding);
        } else {
          this.modelBindings.push(savedBinding);
        }
        this.selectedModel.boundData = savedBinding;
        this.$message.success(payload.sceneId ? 'model binding saved' : 'binding cached locally, save scene to persist');
      } catch (error) {
        console.error('save model binding failed:', error);
        this.$message.error('save model binding failed');
      }
    },

    async handleRemoveModelBinding(modelId) {
      if (!modelId) return;
      try {
        if (this.currentSceneId) {
          await deleteModelBindingBySceneAndModel(this.currentSceneId, modelId);
        }
        this.modelBindings = this.modelBindings.filter(binding => binding.modelId !== modelId);
        if (this.selectedModel && this.getModelStorageId(this.selectedModel) === modelId) {
          this.selectedModel.boundData = null;
        }
        this.$message.success('model binding removed');
      } catch (error) {
        console.error('remove model binding failed:', error);
        this.$message.error('remove model binding failed');
      }
    },

    async syncSceneBindings(sceneId) {
      if (!sceneId) return;
      const tasks = this.modelBindings.map(binding => {
        const payload = { ...binding, sceneId };
        return binding.id
          ? updateModelBinding(binding.id, payload)
          : createModelBinding(payload);
      });
      if (tasks.length > 0) {
        await Promise.all(tasks);
        await this.loadSceneBindings(sceneId);
      }
    },

    openPanel(panel) {
      const exists = this.openedPanels.find(p => this.getPanelKey(p) === this.getPanelKey(panel));
      if (!exists) {
        this.openedPanels.push(this.normalizePanel(panel, this.getPanelKey(panel)));
        if (panel.modelId) {
          this.findAndBindModel(panel);
        }
      }
    },

    closePanel(panel) {
      const panelKey = this.getPanelKey(panel);
      const index = this.openedPanels.findIndex(p => this.getPanelKey(p) === panelKey);
      if (index > -1) {
        this.openedPanels.splice(index, 1);
        delete this.boundModels[panelKey];
      }
    },

    openAllPanels(panels) {
      panels.forEach(panel => {
        this.openPanel(panel);
      });
    },

    closeAllPanels() {
      this.openedPanels = [];
      this.boundModels = {};
      this.bindMode = false;
      this.bindingPanelId = null;
    },

    async refreshPanels() {
      await this.loadAllPanels();
    },

    async handleSavePanel({ existingPanel, values }) {
      try {
        if (existingPanel) {
          if (this.currentSceneId && typeof existingPanel.id === 'number') {
            await updateDataPanel(existingPanel.id, {
              ...existingPanel,
              ...values,
              sceneId: this.currentSceneId
            });
            await this.refreshPanels();
          } else {
            this.draftPanels = this.draftPanels.map(panel => (
              this.getPanelKey(panel) === this.getPanelKey(existingPanel)
                ? this.normalizePanel({ ...panel, ...values }, this.getPanelKey(panel))
                : panel
            ));
            await this.refreshPanels();
          }
        } else if (this.currentSceneId) {
          await createDataPanel({
            ...values,
            sceneId: this.currentSceneId,
            status: 1
          });
          await this.refreshPanels();
        } else {
          this.draftPanels.push(this.createDraftPanel(values));
          await this.refreshPanels();
        }
      } catch (error) {
        console.error('保存展板失败:', error);
        this.$message.error('保存展板失败');
      }
    },

    async handlePanelDeviceBind({ panel, deviceId }) {
      if (!panel || !deviceId) {
        return;
      }

      try {
        const selectedDevice = this.devices.find(item => item.id === deviceId) || null;
        if (this.currentSceneId && typeof panel.id === 'number') {
          await bindPanelDeviceApi(panel.id, deviceId, this.currentSceneId);
          await this.refreshPanels();
        } else {
          this.handlePatchPanel({
            panelId: panel.id,
            changes: {
              deviceId,
              deviceName: selectedDevice?.name || null
            }
          });
        }
        this.$message.success('设备绑定成功');
      } catch (error) {
        console.error('bind panel device failed:', error);
        this.$message.error('设备绑定失败');
      }
    },

    async handlePanelDeviceUnbind(panel) {
      if (!panel) {
        return;
      }

      try {
        if (this.currentSceneId && typeof panel.id === 'number') {
          await unbindPanelDeviceApi(panel.id, this.currentSceneId);
          await this.refreshPanels();
        } else {
          this.handlePatchPanel({
            panelId: panel.id,
            changes: {
              deviceId: null,
              deviceName: null
            }
          });
        }
        this.$message.success('设备绑定已解除');
      } catch (error) {
        console.error('unbind panel device failed:', error);
        this.$message.error('解除设备绑定失败');
      }
    },

    handlePatchPanel({ panelId, changes }) {
      const patch = (panels) => panels.map(panel => (
        panel.id === panelId
          ? this.normalizePanel({ ...panel, ...changes }, this.getPanelKey(panel))
          : panel
      ));

      this.draftPanels = patch(this.draftPanels);
      this.allPanels = patch(this.allPanels);
      this.openedPanels = patch(this.openedPanels);
    },

    async handleDeletePanel(panel) {
      try {
        if (this.currentSceneId && typeof panel.id === 'number') {
          await deleteDataPanel(panel.id, this.currentSceneId);
        } else {
          this.draftPanels = this.draftPanels.filter(item => this.getPanelKey(item) !== this.getPanelKey(panel));
        }
        this.closePanel(panel);
        await this.refreshPanels();
        this.$message.success('展板已删除');
      } catch (e) {
        console.error('删除展板失败:', e);
        this.$message.error('删除展板失败');
      }
    },

    openAllBoundPanels() {
      const boundPanels = this.allPanels.filter(p => p.modelId || p.deviceId);
      if (boundPanels.length === 0) {
        this.$message.warning('娌℃湁宸茬粦瀹氱殑灞曟澘');
        return;
      }
      boundPanels.forEach(panel => this.openPanel(panel));
      this.$message.success(`已打开 ${boundPanels.length} 个展板`);
    },

    findAndBindModel(panel) {
      if (!this.scene || !panel.modelId) return;
      
      let foundModel = null;
      this.scene.traverse((object) => {
        if (object.isGroup && object !== this.helperGroup) {
          if (this.getModelStorageId(object) === panel.modelId || object.uuid === panel.modelId || object.name === panel.modelName) {
            foundModel = object;
          }
        }
      });

      if (foundModel) {
        this.boundModels[this.getPanelKey(panel)] = {
          name: foundModel.name || panel.modelName,
          object: foundModel,
          screenPosition: this.getScreenPosition(foundModel)
        };
      }
    },

    startBindMode(panelConfig) {
      this.bindMode = true;
      this.bindingPanelId = this.getPanelKey(panelConfig);
      this.$message.info('请点击场景中的模型进行绑定');
    },

    cancelBind() {
      this.bindMode = false;
      this.bindingPanelId = null;
    },

    handleUnbindModel(panelId) {
      const panel = this.openedPanels.find(p => p.id === panelId);
      const panelKey = panel ? this.getPanelKey(panel) : String(panelId);
      if (this.boundModels[panelKey]) {
        delete this.boundModels[panelKey];
      }
      if (panel) {
        panel.modelId = null;
        panel.modelName = null;
        panel.modelType = null;
      }
      this.$message.success('已解除模型绑定');
    },

    async bindModelToPanel(model) {
      if (!this.bindingPanelId) return;
      
      const panel = this.openedPanels.find(p => this.getPanelKey(p) === this.bindingPanelId);
      if (!panel) return;

      try {
        const modelId = this.getModelStorageId(model);
        const modelName = model.name || 'model';
        if (this.currentSceneId && typeof panel.id === 'number') {
          await bindModelApi(panel.id, modelId, modelName, 'scene', this.currentSceneId);
          await this.refreshPanels();
        } else {
          this.handlePatchPanel({
            panelId: panel.id,
            changes: {
              modelId,
              modelName,
              modelType: 'scene'
            }
          });
        }
        this.boundModels[this.getPanelKey(panel)] = {
          name: model.name || '未命名模型',
          object: model,
          screenPosition: this.getScreenPosition(model)
        };
        const updatedPanel = this.allPanels.find(p => this.getPanelKey(p) === this.getPanelKey(panel));
        if (updatedPanel) {
          const idx = this.openedPanels.findIndex(p => this.getPanelKey(p) === this.getPanelKey(panel));
          if (idx > -1) {
            this.openedPanels[idx] = updatedPanel;
          }
        }
        this.$message.success(`已绑定到模型: ${model.name || '未命名模型'}`);
      } catch (e) {
        console.error('缁戝畾妯″瀷澶辫触:', e);
        this.$message.error('缁戝畾妯″瀷澶辫触');
      }
      
      this.bindMode = false;
      this.bindingPanelId = null;
    },

    startBindMode(panelConfig) {
      this.bindMode = true;
      this.bindingPanelId = this.getPanelKey(panelConfig);
      this.$message.info('请在场景中点击要绑定的模型');
    },

    handleUnbindModel(panelId) {
      const panel = this.allPanels.find(p => this.getPanelKey(p) === String(panelId) || p.id === panelId);
      const panelKey = panel ? this.getPanelKey(panel) : String(panelId);
      if (this.boundModels[panelKey]) {
        delete this.boundModels[panelKey];
      }
      if (panel) {
        this.handlePatchPanel({
          panelId: panel.id,
          changes: {
            modelId: null,
            modelName: null,
            modelType: null
          }
        });
      }
      this.$message.success('模型绑定已解除');
    },

    async bindModelToPanel(model) {
      if (!this.bindingPanelId) return;

      const panel = this.allPanels.find(p => this.getPanelKey(p) === this.bindingPanelId);
      if (!panel) return;

      try {
        const modelId = this.getModelStorageId(model);
        const modelName = model.name || 'model';
        if (this.currentSceneId && typeof panel.id === 'number') {
          await bindModelApi(panel.id, modelId, modelName, 'scene', this.currentSceneId);
          await this.refreshPanels();
        } else {
          this.handlePatchPanel({
            panelId: panel.id,
            changes: {
              modelId,
              modelName,
              modelType: 'scene'
            }
          });
        }
        this.boundModels[this.getPanelKey(panel)] = {
          name: model.name || '未命名模型',
          object: model,
          screenPosition: this.getScreenPosition(model)
        };
        const updatedPanel = this.allPanels.find(p => this.getPanelKey(p) === this.getPanelKey(panel));
        if (updatedPanel) {
          const idx = this.openedPanels.findIndex(p => this.getPanelKey(p) === this.getPanelKey(panel));
          if (idx > -1) {
            this.openedPanels[idx] = updatedPanel;
          }
        }
        this.$message.success(`已绑定到模型: ${model.name || '未命名模型'}`);
      } catch (e) {
        console.error('bind panel model failed:', e);
        this.$message.error('绑定展板模型失败');
      }

      this.bindMode = false;
      this.bindingPanelId = null;
    },

    async handlePanelModelUnbind(panel) {
      if (!panel) {
        return;
      }

      try {
        if (this.currentSceneId && typeof panel.id === 'number') {
          await unbindPanelModelApi(panel.id, this.currentSceneId);
          await this.refreshPanels();
        } else {
          this.handlePatchPanel({
            panelId: panel.id,
            changes: {
              modelId: null,
              modelName: null,
              modelType: null
            }
          });
        }
        this.handleUnbindModel(this.getPanelKey(panel));
      } catch (error) {
        console.error('unbind panel model failed:', error);
        this.$message.error('解除模型绑定失败');
      }
    },

    getScreenPosition(object) {
      if (!this.camera || !this.renderer) return null;
      
      if (!this.cachedVector3) {
        this.cachedVector3 = new THREE.Vector3();
      }
      object.getWorldPosition(this.cachedVector3);
      
      this.cachedVector3.project(this.camera);
      
      if (!this.cachedCanvasRect) {
        this.cachedCanvasRect = this.renderer.domElement.getBoundingClientRect();
      }
      const rect = this.cachedCanvasRect;
      
      return {
        x: (this.cachedVector3.x * 0.5 + 0.5) * rect.width + rect.left,
        y: (-this.cachedVector3.y * 0.5 + 0.5) * rect.height + rect.top
      };
    },

    setSkyboxBackground() {
      const cubeTextureLoader = new THREE.CubeTextureLoader();
      cubeTextureLoader.setPath("/texture/sky/");

      cubeTextureLoader.load(
          [
            "sky.left.jpg",
            "sky.right.jpg",
            "sky.top.jpg",
            "sky.bottom.jpg",
            "sky.back.jpg",
            "sky.front.jpg",
          ],
          (texture) => {
            this.scene.background = texture;
            console.log('天空盒背景加载成功');
          },
          undefined,
          (error) => {
            console.error('澶╃┖鐩掕儗鏅姞杞藉け璐?', error);
            this.scene.background = new THREE.Color(0x87CEEB);
            this.skyboxEnabled = false;
            this.$message.error('天空背景加载失败，使用默认背景');
          }
      );
    },

    async loadFromSceneLibrary(id) {
      try {
        const { getScene } = await import('@/api/scenes');
        const sceneData = await getScene(id);
        const { fileType, path, name, description, url } = sceneData;
        this.currentSceneId = sceneData.id || id;
        this.currentSceneName = name || '';
        this.currentSceneDescription = description || '';
        this.resetPanelState({ clearDrafts: true });

        this.sceneLoaded = true;
        this.$nextTick(() => {
          this.initScene();

          if (fileType === 'json') {
            const sceneUrl = this.withCacheBust(url || (path.startsWith('http') ? path : getScenePathUrl(path.startsWith('/') ? path.slice(1) : path)));
            fetch(sceneUrl)
              .then(resp => resp.json())
              .then(async (jsonData) => {
                const normalized = this.normalizeSceneImportData(jsonData);
                this.loadModels(normalized.models);
                this.skyboxEnabled = !!normalized.skybox?.enabled;
                if (this.skyboxEnabled) {
                  this.setSkyboxBackground();
                }
                this.modelBindings = normalized.bindings || [];
                await this.loadSceneBindings(id);
                await this.loadAllPanels(id);
                this.$message.success('scene loaded successfully');
              });
            this.loadSceneTextures(id);
            return;
          }

          if (fileType === 'glb' || fileType === 'gltf') {
            const loader = new GLTFLoader();
            const sceneUrl = this.withCacheBust(url || (path.startsWith('http') ? path : getScenePathUrl(path.startsWith('/') ? path.slice(1) : path)));
            loader.load(
              sceneUrl,
              async (gltf) => {
                const model = gltf.scene;
                model.name = name;
                model.position.set(0, 0, 0);
                model.scale.set(1, 1, 1);
                model.userData = {
                  ...(model.userData || {}),
                  sceneModelId: model.userData?.sceneModelId || `scene-model-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
                };
                this.scene.add(model);
                window.threeScene.scene = this.scene;
                await this.loadSceneBindings(id);
                await this.loadAllPanels(id);
                this.$message.success('scene loaded successfully');
              },
              undefined,
              (err) => {
                console.error(err);
                this.$message.error('scene load failed');
              }
            );
          }

          this.loadSceneTextures(id);
        });
      } catch (e) {
        console.error('load scene library failed:', e);
        this.$message.error('scene load failed');
      }
    },

    withCacheBust(url) {
      const separator = url.includes('?') ? '&' : '?';
      return `${url}${separator}t=${Date.now()}`;
    },
    
    // 鍔犺浇鍦烘櫙璐村浘淇℃伅
    async loadSceneTextures(sceneId) {
      try {
        const sceneAsset = await getScene(sceneId);
        if (sceneAsset && sceneAsset.textureInfo) {
          const metadata = this.parseSceneMetadata(sceneAsset.textureInfo);
          this.modelTextures = new Map(Object.entries(metadata.textures || {}));
          if ((!this.modelBindings || this.modelBindings.length === 0) && metadata.bindings?.length) {
            this.modelBindings = metadata.bindings;
          }
          if ((!this.allPanels || this.allPanels.length === 0) && metadata.panels?.length) {
            this.draftPanels = metadata.panels.map((panel, index) => this.normalizeImportedPanel(panel, index));
            this.allPanels = this.draftPanels.map(panel => this.normalizePanel(panel, this.getPanelKey(panel)));
          }
          this.applySavedTextures();
        }
      } catch (error) {
        console.error('load scene metadata failed:', error);
      }
    },

    // 搴旂敤宸蹭繚瀛樼殑璐村浘鍒版ā鍨?
    applySavedTextures() {
      // 閬嶅巻鍦烘櫙涓殑鎵€鏈夋ā鍨?
      this.scene.traverse((object) => {
        if (object.isGroup && object !== this.helperGroup) {
          const textureData = this.modelTextures.get(this.getModelStorageId(object));
          if (textureData) {
            this.previewTextureOnModel(object, textureData);
          }
        }
      });
    },
    
    // 鎻愪緵妯″瀷鏁版嵁缁欑粍鍚堟ā鍨嬬紪杈戝櫒
    provideModelData() {
      console.log('鎻愪緵妯″瀷鏁版嵁缁欑粍鍚堟ā鍨嬬紪杈戝櫒');
      // 鐩存帴浼犻€掑綋鍓嶇殑妯″瀷鏁版嵁锛屽鏋滀负绌哄垯鍏堝姞杞?
      if (this.modelData.length === 0) {
        this.loadModelData();
      } else {
        // 濡傛灉鏁版嵁宸茬粡瀛樺湪锛岀洿鎺ヤ紶閫掔粰缁勫悎妯″瀷缂栬緫鍣?
        if (this.$refs.compositeModelEditor) {
          console.log('直接传递现有模型数据给组合模型编辑器');
          this.$refs.compositeModelEditor.receiveModelData(this.modelData);
        }
      }
    },
    
    // 鎺ユ敹妯″瀷鏁版嵁骞朵紶閫掔粰缁勫悎妯″瀷缂栬緫鍣?
    receiveModelData(data) {
      if (this.$refs.compositeModelEditor) {
        this.$refs.compositeModelEditor.receiveModelData(data);
      }
    },
    
    // 鑾峰彇鍦烘櫙涓殑妯″瀷鍒楄〃
    getSceneModels() {
      if (!this.scene) return [];
      
      const models = [];
      this.scene.traverse((object) => {
        if (object.isGroup && object !== this.helperGroup) {
          models.push({
            id: object.uuid,
            name: object.name,
            position: {
              x: object.position.x,
              y: object.position.y,
              z: object.position.z
            },
            rotation: {
              x: object.rotation.x,
              y: object.rotation.y,
              z: object.rotation.z
            },
            scale: {
              x: object.scale.x,
              y: object.scale.y,
              z: object.scale.z
            }
          });
        }
      });
      return models;
    },
    
    // 鎵撳紑缁勫悎妯″瀷閫夋嫨鍣?
    openCompositeModelSelector() {
      this.isCompositeModelSelectorVisible = true;
    },
    
    // 鎵撳紑缁勫悎妯″瀷缂栬緫鍣?
    openCompositeModelEditor() {
      this.isCompositeEditorVisible = true;
      // 浣跨敤 $nextTick 纭繚缁勪欢宸叉寕杞藉悗鍐嶅姞杞芥暟鎹?
      this.$nextTick(() => {
        // 纭繚妯″瀷鏁版嵁宸插姞杞?
        if (this.modelData.length === 0) {
          this.loadModelData();
        } else {
          // 濡傛灉鏁版嵁宸茬粡瀛樺湪锛岀洿鎺ヤ紶閫掔粰缁勫悎妯″瀷缂栬緫鍣?
          if (this.$refs.compositeModelEditor) {
            console.log('直接传递现有模型数据给组合模型编辑器');
            this.$refs.compositeModelEditor.receiveModelData(this.modelData);
          }
        }
      });
    },
    
    // 鍔犺浇缁勫悎妯″瀷
    async loadCompositeModel(compositeModel) {
      try {
        // 鑾峰彇缁勫悎妯″瀷鐨勭粍浠?
        const response = await getCompositeModelComponents(compositeModel.id);
        const components = response.data || response;
        
        // 鍔犺浇姣忎釜缁勪欢瀵瑰簲鐨勬ā鍨?
        for (const component of components) {
          // 杩欓噷闇€瑕佹牴鎹甤omponent.modelId鑾峰彇瀹為檯鐨勬ā鍨嬩俊鎭?
          // 妫€鏌odelId鏄惁宸茬粡鏄畬鏁碪RL锛岄伩鍏嶉噸澶嶆坊鍔犲墠缂€
          let fileUrl = component.modelId;
          if (fileUrl && !fileUrl.startsWith('http')) {
            fileUrl = getFileUrl(component.modelId);
          }
          
          const modelData = {
            name: `妯″瀷_${component.modelId}`,
            file: fileUrl,
            position: { 
              x: component.position_x || 0, 
              y: component.position_y || 0, 
              z: component.position_z || 0 
            },
            scale: { 
              x: component.scale_x || 1, 
              y: component.scale_y || 1, 
              z: component.scale_z || 1 
            },
            rotation: { 
              x: component.rotation_x || 0, 
              y: component.rotation_y || 0, 
              z: component.rotation_z || 0 
            },
            boundData: null,
          };
          
          // 鍔犺浇妯″瀷
          this.loadModel(modelData);
        }
        
        this.$message.success(`组合模型 "${compositeModel.name}" 已加载`);
        this.isCompositeModelSelectorVisible = false;
      } catch (error) {
        console.error('鍔犺浇缁勫悎妯″瀷澶辫触:', error);
        this.$message.error('鍔犺浇缁勫悎妯″瀷澶辫触: ' + (error.message || '鏈煡閿欒'));
      }
    },
    
    // 淇濆瓨缁勫悎妯″瀷
    async saveCompositeModel(compositeModelData) {
      // 闃叉閲嶅鎻愪氦
      if (this.savingCompositeModel) {
        return;
      }
      
      try {
        this.savingCompositeModel = true;
        // 璋冪敤鍚庣API淇濆瓨缁勫悎妯″瀷
        const response = await createCompositeModel(compositeModelData);
        console.log('淇濆瓨缁勫悎妯″瀷鍒板悗绔?', response);
        this.$message.success('缁勫悎妯″瀷淇濆瓨鎴愬姛');
        
        // 鍏抽棴缂栬緫鍣?
        this.isCompositeEditorVisible = false;
      } catch (error) {
        console.error('淇濆瓨缁勫悎妯″瀷澶辫触:', error);
        this.$message.error('淇濆瓨澶辫触: ' + (error.message || '鏈煡閿欒'));
      } finally {
        this.savingCompositeModel = false;
      }
    },
    
    handleApplyTexture(model, textureData) {
      console.log('澶勭悊搴旂敤璐村浘璇锋眰:', { 
        modelId: model.uuid, 
        modelName: model.name,
        textureDataLength: textureData.length,
        textureDataPreview: textureData.substring(0, 100)
      });
      
      // 涓存椂瀛樺偍璐村浘淇℃伅
      this.textureCache.set(this.getModelStorageId(model), textureData);
      
      // 绔嬪嵆鍦ㄨ鍥句腑棰勮璐村浘鏁堟灉
      this.previewTextureOnModel(model, textureData);
      
      // 鏍囪鍦烘櫙鏈夊彉鍖?
      this.hasChanges = true;
    },
    
    handleRemoveTexture(model) {
      console.log('澶勭悊绉婚櫎璐村浘璇锋眰:', { 
        modelId: model.uuid, 
        modelName: model.name
      });
      
      // 绉婚櫎璐村浘缂撳瓨
      this.textureCache.delete(this.getModelStorageId(model));
      
      // 绉婚櫎妯″瀷涓婄殑璐村浘
      this.removeTextureFromModel(model);
      
      // 鏍囪鍦烘櫙鏈夊彉鍖?
      this.hasChanges = true;
    },
    
    previewTextureOnModel(model, textureData) {
      console.log('寮€濮嬮瑙堣创鍥?', { 
        modelId: model.uuid, 
        modelName: model.name,
        textureDataLength: textureData.length,
        textureDataPreview: textureData.substring(0, 100)
      });
      
      // 鍒涘缓鍥惧儚鍏冪礌鐢ㄤ簬鍔犺浇base64鏁版嵁
      const image = new Image();
      image.src = textureData;
      
      // 褰撳浘鍍忓姞杞藉畬鎴愬悗鍒涘缓璐村浘
      image.onload = () => {
        console.log('图像加载完成，开始创建贴图', {
          imageWidth: image.width,
          imageHeight: image.height
        });
        const texture = new THREE.CanvasTexture(image);
        // 璁剧疆绾圭悊鐨勮壊褰╃┖闂翠负sRGB
        texture.colorSpace = THREE.SRGBColorSpace;
        texture.needsUpdate = true;
        
        // 璁剧疆绾圭悊閲嶅妯″紡锛岀‘淇濊创鍥炬纭鐩栨暣涓ā鍨嬭〃闈?
        texture.wrapS = THREE.RepeatWrapping;
        texture.wrapT = THREE.RepeatWrapping;
        texture.repeat.set(1, 1);
        
        // 纭繚绾圭悊杩囨护鏂瑰紡姝ｇ‘
        texture.magFilter = THREE.LinearFilter;
        texture.minFilter = THREE.LinearFilter;
        
        // 寮哄埗璁剧疆UV鍙樻崲锛岀‘淇濊创鍥惧畬鏁磋鐩?
        texture.matrixAutoUpdate = false;
        texture.matrix.setUvTransform(0, 0, 1, 1, 0, 0, 0);
        
        console.log('鍒涘缓鐨勮创鍥?', texture);
        
        // 閬嶅巻妯″瀷鐨勬墍鏈夌綉鏍煎璞″苟搴旂敤璐村浘
        let meshCount = 0;
        model.traverse((child) => {
          if (child.isMesh) {
            meshCount++;
            console.log('澶勭悊缃戞牸瀵硅薄:', {
              name: child.name || child.uuid,
              materialType: child.material.type,
              hasMap: !!child.material.map
            });
            
            // 纭繚浣跨敤鏀寔璐村浘鐨勬潗璐ㄧ被鍨?
            let material;
            
            // 濡傛灉褰撳墠鏉愯川宸茬粡鏄悎閫傜殑绫诲瀷锛屽垯澶嶇敤骞舵洿鏂板叾灞炴€?
            if (child.material instanceof THREE.MeshStandardMaterial || 
                child.material instanceof THREE.MeshPhongMaterial ||
                child.material instanceof THREE.MeshLambertMaterial) {
              material = child.material.clone();
              material.map = texture;
              
              // 纭繚鏉愯川灞炴€ф纭缃互姝ｇ‘鏄剧ず璐村浘
              material.needsUpdate = true;
              
              // 娓呴櫎鍙兘褰卞搷璐村浘鏄剧ず鐨勫睘鎬?
              material.emissive = new THREE.Color(0x000000); // 娓呴櫎鑷彂鍏?
              material.emissiveIntensity = 0; // 娓呴櫎鑷彂鍏夊己搴?
              
              // 濡傛灉鏄疢eshStandardMaterial锛岃缃悎閫傜殑绮楃硻搴﹀拰閲戝睘搴︿互鏇村ソ鍦版樉绀鸿创鍥?
              if (material instanceof THREE.MeshStandardMaterial) {
                material.roughness = 0.7; // 涓瓑绮楃硻搴﹁〃闈㈡洿瀹规槗鐪嬪埌璐村浘缁嗚妭
                material.metalness = 0.2; // 杞诲井閲戝睘鎰?
              }
              
              // 寮哄埗鏇存柊UV鍙樻崲
              if (material.map) {
                material.map.matrixAutoUpdate = false;
                material.map.matrix.setUvTransform(0, 0, 1, 1, 0, 0, 0);
              }
              
              child.material = material;
              console.log('鏇存柊鐜版湁鏉愯川璐村浘');
            } 
            // 濡傛灉鏄熀纭€鏉愯川鎴栧叾浠栫被鍨嬶紝鍒涘缓鏂扮殑MeshPhongMaterial
            else {
              material = new THREE.MeshPhongMaterial({
                map: texture,
                emissive: new THREE.Color(0x000000), // 鏃犺嚜鍙戝厜
                emissiveIntensity: 0,
                specular: new THREE.Color(0x111111) // 杞诲井楂樺厜
              });
              
              // 淇濇寔鍘熷棰滆壊
              if (child.material.color) {
                material.color = child.material.color;
              }
              
              // 寮哄埗鏇存柊UV鍙樻崲
              if (material.map) {
                material.map.matrixAutoUpdate = false;
                material.map.matrix.setUvTransform(0, 0, 1, 1, 0, 0, 0);
              }
              
              // 鏇挎崲鏉愯川
              child.material = material;
              console.log('鍒涘缓鏂扮殑MeshPhongMaterial');
            }
            
            // 鍚屾鏇存柊鍘熷鏉愯川澶囦唤锛堝鏋滃瓨鍦級锛岀‘淇濆彇娑堥€変腑鏃朵篃鑳戒繚鐣欒创鍥?
            if (child.userData.originalMaterial) {
              // 鍏嬮殕褰撳墠鏉愯川浣滀负鏂扮殑鍘熷鏉愯川澶囦唤
              child.userData.originalMaterial = child.material.clone();
              console.log('鍚屾鏇存柊鍘熷鏉愯川澶囦唤');
            }
            
            console.log('搴旂敤璐村浘鍚庣殑鏉愯川:', {
              type: child.material.type,
              hasMap: !!child.material.map
            });
          }
        });
        
        console.log('共处理了', meshCount, '个网格对象');
        
        // 寮哄埗閲嶆柊娓叉煋鍦烘櫙
        if (this.renderer && this.scene && this.camera) {
          console.log('寮哄埗閲嶆柊娓叉煋鍦烘櫙');
          this.renderer.render(this.scene, this.camera);
        }
        
        // 鍐嶆妫€鏌ヨ创鍥炬槸鍚﹀簲鐢ㄦ垚鍔?
        setTimeout(() => {
          console.log('寤惰繜妫€鏌ヨ创鍥惧簲鐢ㄧ粨鏋?');
          let appliedCount = 0;
          model.traverse((child) => {
            if (child.isMesh) {
              console.log('缃戞牸瀵硅薄璐村浘鐘舵€?', {
                name: child.name || child.uuid,
                hasMap: !!child.material.map,
                mapType: child.material.map ? child.material.map.constructor.name : 'None'
              });
              if (child.material.map) appliedCount++;
            }
          });
          
          // 鏄剧ず璐村浘搴旂敤鎴愬姛鐨勬彁绀?
          if (appliedCount > 0) {
            this.$message.success(`贴图已成功应用于 ${appliedCount} 个网格对象`);
          } else {
            this.$message.warning('璐村浘搴旂敤鍙兘鏈垚鍔燂紝璇锋鏌ユ帶鍒跺彴杈撳嚭');
          }
        }, 100);
      };
      
      // 娣诲姞閿欒澶勭悊
      image.onerror = (err) => {
        console.error('璐村浘鍔犺浇澶辫触:', err);
        console.error('璐村浘鏁版嵁鍙兘鏈夐棶棰?', textureData.substring(0, 200));
        this.$message.error('贴图加载失败，请检查图片文件');
      };
    },
    
    removeTextureFromModel(model) {
      console.log('开始移除贴图');
      
      // 绉婚櫎璐村浘骞舵仮澶嶅師濮嬫棤璐村浘鏉愯川
      let restoredCount = 0;
      model.traverse((child) => {
        if (child.isMesh) {
          console.log('澶勭悊缃戞牸瀵硅薄:', {
            name: child.name || child.uuid,
            hasOriginalMaterial: !!child.userData.originalMaterial
          });
          
          // 妫€鏌ユ槸鍚﹀瓨鍦ㄥ師濮嬫潗璐ㄥ浠?
          if (child.userData.originalMaterial) {
            // 鎭㈠鍘熷鏉愯川锛堟棤璐村浘鐨勬潗璐級
            console.log('鎭㈠鍘熷鏉愯川');
            child.material = child.userData.originalMaterial;
            child.material.needsUpdate = true;
            
            // 娓呴櫎鏉愯川涓殑璐村浘
            if (child.material.map) {
              child.material.map = null;
            }
            
            // 鏇存柊鍘熷鏉愯川澶囦唤锛岀‘淇濆叾涓笉鍖呭惈璐村浘
            child.userData.originalMaterial = child.material.clone();
            
            restoredCount++;
          } else {
            // 濡傛灉娌℃湁鍘熷鏉愯川澶囦唤锛屽皾璇曟竻闄よ创鍥?
            console.log('清除当前材质的贴图');
            if (child.material.map) {
              child.material.map = null;
              child.material.needsUpdate = true;
              restoredCount++;
            }
          }
          
          // 纭繚绉婚櫎鎵€鏈変笌璐村浘鐩稿叧鐨勫睘鎬?
          if (child.material && child.material.map) {
            child.material.map = null;
            child.material.needsUpdate = true;
          }
        }
      });
      
      console.log('鍏辨仮澶嶄簡', restoredCount, '涓綉鏍煎璞＄殑鏉愯川');
      
      // 寮哄埗閲嶆柊娓叉煋鍦烘櫙
      if (this.renderer && this.scene && this.camera) {
        console.log('寮哄埗閲嶆柊娓叉煋鍦烘櫙');
        this.renderer.render(this.scene, this.camera);
      }
      
      // 鏄剧ず鎴愬姛娑堟伅
      if (restoredCount > 0) {
        this.$message.success(`宸茬Щ闄?${restoredCount} 涓綉鏍煎璞＄殑璐村浘`);
      }
    },
    
    // 淇濆瓨鍦烘櫙鏃惰皟鐢ㄦ鏂规硶鎸佷箙鍖栬创鍥句俊鎭?
    saveSceneTextures(sceneId) {
      // 灏嗚创鍥剧紦瀛樿浆鎹负鍙彂閫佸埌鍚庣鐨勬牸寮?
      const texturesToSave = [];
      for (let [modelId, textureData] of this.textureCache.entries()) {
        texturesToSave.push({
          modelId: modelId,
          textureData: textureData
        });
      }
      
      // 濡傛灉鏈夎创鍥句俊鎭渶瑕佷繚瀛橈紝鍒欏彂閫佸埌鍚庣
      if (texturesToSave.length > 0) {
        // 杩欓噷搴旇璋冪敤鍚庣API淇濆瓨璐村浘淇℃伅
        // 鐢变簬褰撳墠鍚庣娌℃湁涓撻棬鐨勮创鍥句繚瀛樻帴鍙ｏ紝鎴戜滑灏嗚创鍥句俊鎭繚瀛樺埌鍦烘櫙璧勪骇涓?
        console.log("闇€瑕佷繚瀛樼殑璐村浘淇℃伅:", texturesToSave);
        return texturesToSave;
      }
      
      return [];
    },
  },
};
</script>
<style scoped>
#three-scene {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #ffffff;
  overflow: hidden;
}
/* 宸ュ叿鏍忔牱寮?- 涓や晶甯冨眬 */
.toolbar {
  position: absolute;
  top: 5px;
  left: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  padding: 8px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  border: 1px solid #e8e8e8;
  min-height: 44px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.toolbar-center {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.toolbar-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex: 1;
}

.toolbar-left .el-button-group,
.toolbar-center .el-button-group,
.toolbar-right .el-button-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar .el-button {
  padding: 5px 12px;
  font-size: 12px;
  height: auto;
}

.toolbar .el-button i {
  font-size: 12px;
}

.loading-progress {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #409eff;
  z-index: 1001;
}

/* 涓诲唴瀹瑰尯鍩?- 鍏ㄥ睆鏄剧ず */
.main-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.main-content.with-toolbar {
  top: 70px; /* 涓哄伐鍏锋爮鐣欏嚭绌洪棿 */
  height: calc(100% - 64px);
}

/* 初始场景选择界面 */
.scene-options {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 16px 64px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.scene-options h2 {
  margin-bottom: 30px;
  color: #333;
  font-size: 28px;
  font-weight: 600;
}

.scene-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

/* 3D鍦烘櫙瀹瑰櫒 - 鍏ㄥ睆 */
.scene-container {
  width: 100%;
  height: 100%;
  position: relative;
  min-height: 400px; /* 娣诲姞鏈€灏忛珮搴︾‘淇濇湁灏哄 */
}

/* 闂哥珯鍦烘櫙瀹瑰櫒 - 鍏ㄥ睆 */
.gate-station-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: white;
  min-height: 400px; /* 娣诲姞鏈€灏忛珮搴︾‘淇濇湁灏哄 */
}

.gate-station-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: white;
}

/* 鍔犺浇鐘舵€佹寚绀?*/
.gate-station-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #666;
  z-index: 10001;
}

.gate-station-loading .el-icon {
  font-size: 24px;
  margin-bottom: 10px;
}

/* 鎿嶄綔鎻愮ず鏍峰紡 */
.operation-tips {
  position: absolute;
  top: 74px; /* 鍦ㄥ伐鍏锋爮涓嬫柟 */
  right: 10px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  z-index: 999;
  max-width: 280px;
  animation: slideIn 0.3s ease;
}

.tips-content h4 {
  margin-bottom: 10px;
  color: #333;
  font-size: 14px;
}
.tips-content ul {
  margin-bottom: 12px;
  padding-left: 16px;
}
.tips-content li {
  margin-bottom: 4px;
  color: #666;
  font-size: 12px;
  line-height: 1.4;
}
.tips-content strong {
  color: #409eff;
}
/* 甯姪鎸夐挳鏍峰紡 */
.help-button {
  position: absolute;
  bottom: 15px;
  right: 15px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 999;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.help-button:hover {
  background: rgba(255, 255, 255, 1);
  transform: scale(1.1);
}

/* 鍔ㄧ敾鏁堟灉 */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 鍝嶅簲寮忚璁?*/
@media (max-width: 1024px) {
  .toolbar {
    padding: 6px 12px;
  }

  .toolbar .el-button {
    padding: 4px 8px;
    font-size: 11px;
  }
  .main-content.with-toolbar {
    top: 56px;
    height: calc(100% - 56px);
  }
  .operation-tips {
    top: 66px;
  }
}
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 8px;
    padding: 8px;
    min-height: auto;
  }
  .toolbar-left,
  .toolbar-center,
  .toolbar-right {
    justify-content: center;
    width: 100%;
  }
  .toolbar-left {
    order: 1;
  }
  .toolbar-center {
    order: 3;
  }
  .toolbar-right {
    order: 2;
  }
  .main-content.with-toolbar {
    top: 120px;
    height: calc(100% - 120px);
  }
  .operation-tips {
    top: 130px;
    right: 5px;
    max-width: 250px;
  }
  .scene-options {
    padding: 20px;
    width: 90%;
  }
  .scene-options h2 {
    font-size: 20px;
    margin-bottom: 20px;
  }
  .scene-buttons {
    gap: 12px;
  }
  .scene-buttons .el-button {
    width: 200px;
  }
}
@media (max-width: 480px) {
  .toolbar {
    left: 5px;
    right: 5px;
  }
  .toolbar .el-button {
    padding: 3px 6px;
    font-size: 10px;
  }
  .scene-buttons {
    gap: 12px;
  }
  .scene-buttons .el-button {
    width: 200px;
  }
}
</style>
