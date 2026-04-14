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
          <el-button @click="redo" size="small" title="清除所有模型">
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
          <el-button @click="exportSceneToGLB" size="small" title="导出为GLB格式">
            <i class="el-icon-download"></i> 导出GLB
          </el-button>
          <el-button @click="exportSceneToJSON" size="small" title="导出为JSON格式">
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
            <i class="el-icon-plus"></i> 创建新场景
          </el-button>
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
        <!-- Three.js场景将渲染到这里 -->
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
          @bind-data="bindData"
          @delete-model="handleDeleteModel"
          @environment-change="handleEnvironmentChange"
          @apply-texture="handleApplyTexture"
          @remove-texture="handleRemoveTexture"
      />

      <!-- WebSocket数据监控面板 - 多展板支持 -->
      <DeviceDataPanel
          v-for="panel in openedPanels"
          :key="panel.id"
          :visible="true"
          :panelConfig="panel"
          :boundModel="boundModels[panel.id]"
          :bindMode="bindMode && bindingPanelId === panel.id"
          @close="closePanel(panel)"
          @startBind="startBindMode"
          @cancelBind="cancelBind"
          @updatePanel="refreshPanels"
          @deletePanel="handleDeletePanel"
          @unbindModel="handleUnbindModel"
      />

      <!-- 展板管理弹窗 -->
      <DataPanelManager
          v-model:visible="showPanelManager"
          :openedPanelIds="openedPanelIds"
          @openPanel="openPanel"
          @closePanel="closePanel"
          @openAll="openAllPanels"
          @closeAll="closeAllPanels"
          @refresh="refreshPanels"
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
            知道了
          </el-button>
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
import { getDataPanels, createDataPanel, deleteDataPanel, bindModel as bindModelApi } from '@/api/dataPanel';
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
    boundPanelsCount() {
      return this.allPanels.filter(p => p.deviceId || p.modelId).length;
    }
  },

  mounted() {
    const sceneId = this.$route?.query?.sceneId
    if (sceneId) {
      this.loadFromSceneLibrary(sceneId)
    }
    
    this.loadAllPanels();
    
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
        if (v) this.loadFromSceneLibrary(v)
      }
    }
  },

  methods: {
    // 添加测试贴图功能的方法
    testTextureApplication() {
      console.log('=== 测试贴图功能 ===');
      if (!this.selectedModel) {
        console.log('没有选中的模型');
        this.$message.warning('请先选择一个模型');
        return;
      }
      
      console.log('选中的模型:', this.selectedModel);
      
      // 创建一个明显的测试贴图
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
      
      console.log('选中的模型:', this.selectedModel);
      this.removeTextureFromModel(this.selectedModel);
      this.$message.info("测试移除贴图已完成");
    },
    
    async createNewScene() {
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

      // 检查容器尺寸
      const width = container.clientWidth;
      const height = container.clientHeight;
      
      if (width === 0 || height === 0) {
        console.warn('Container has zero dimensions, waiting for layout...');
        // 可以添加重试逻辑或使用默认尺寸
        setTimeout(() => {
          this.initScene();
        }, 100);
        return;
      }

      console.log('Initializing scene with dimensions:', width, height);

      this.scene = new THREE.Scene();
      this.scene.background = new THREE.Color(0xeeeeee);

      this.camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 10000); // 增加远平面距离
      this.camera.position.set(0, 9, 4);
      this.camera.updateProjectionMatrix();

      this.renderer = new THREE.WebGLRenderer({ antialias: true });
      this.renderer.setSize(width, height);
      this.renderer.setPixelRatio(window.devicePixelRatio); // 添加像素比例支持
      container.appendChild(this.renderer.domElement);

      this.controls = new OrbitControls(this.camera, this.renderer.domElement);
      this.controls.enableDamping = true;

      this.initLoaders();
      this.addLighting();

      this.helperGroup = new THREE.Group();
      // 创建更大的网格以确保完整显示
      const gridHelper = new THREE.GridHelper(2000, 2000, 0x888888, 0xcccccc);
      gridHelper.material.opacity = 0.3;
      gridHelper.material.transparent = true;
      this.helperGroup.add(gridHelper);
      this.scene.add(this.helperGroup);

      // 确保绑定正确的 this 上下文
      this.boundOnWindowResize = this.onWindowResize.bind(this);
      window.addEventListener("resize", this.boundOnWindowResize);
      
      this.renderer.domElement.addEventListener("click", this.onClick.bind(this));
      document.addEventListener("keydown", this.handleKeyDown.bind(this));

      this.raycaster = new THREE.Raycaster();
      this.mouse = new THREE.Vector2();

      this.animate();

      // 统一设置窗口引用
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
      
      // 添加额外的光源以确保网格各部分都能被照亮
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

      this.saveForm.name = `场景_${new Date().toLocaleDateString()}_${modelCount}个模型`;
      this.saveForm.description = '';
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

        const sceneAsset = await uploadScene(file, this.saveForm.name.trim(), this.saveForm.description.trim());
        
        // 保存贴图信息
        if (sceneAsset && sceneAsset.id) {
          const texturesToSave = this.saveSceneTextures(sceneAsset.id);
          if (texturesToSave.length > 0) {
            const textureInfo = JSON.stringify(Object.fromEntries(this.textureCache));
            try {
              await saveSceneTextures(sceneAsset.id, textureInfo);
              console.log('贴图信息保存成功');
            } catch (err) {
              console.error('保存贴图信息失败:', err);
            }
          }
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
        this.$message.warning('正在保存，请稍候...');
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
      console.log(`加载模型: ${modelData}`);
      this.gltfLoader.load(modelData.file, (gltf) => {
        const model = gltf.scene;
        model.name = modelData.name;
        model.file = modelData.file;
        model.position.set(modelData.position.x, modelData.position.y, modelData.position.z);
        model.scale.set(modelData.scale.x, modelData.scale.y, modelData.scale.z);
        model.rotation.set(modelData.rotation.x, modelData.rotation.y, modelData.rotation.z);
        model.boundData = modelData.boundData || null;
        
        // 将模型包装在一个Group中，以便更好地管理
        const modelGroup = new THREE.Group();
        modelGroup.add(model);
        modelGroup.name = modelData.name;
        modelGroup.position.copy(model.position);
        modelGroup.scale.copy(model.scale);
        modelGroup.rotation.copy(model.rotation);
        modelGroup.boundData = modelData.boundData || null;
        modelGroup.userData = { 
          file: modelData.file,
          originalModel: model
        };
        
        this.scene.add(modelGroup);
        window.threeScene.scene = this.scene;
        console.log(`成功加载模型: ${modelData.name}`);
      }, undefined, (error) => {
        console.error('模型加载失败:', error);
        this.$message.error(`模型加载失败: ${modelData.name}`);
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
      console.log("可点击对象数量:", clickableObjects.length);

      const intersects = this.raycaster.intersectObjects(clickableObjects, true);
      console.log("射线检测结果:", intersects);

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
          console.log("选中的模型:", this.selectedModel);
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
      console.log("selectedModel 已设置:", this.selectedModel);

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
          // 保存原始材质（如果尚未保存）
          if (!child.userData.originalMaterial) {
            child.userData.originalMaterial = child.material.clone();
          }

          // 创建高亮材质（基于当前材质）
          const highlightMaterial = child.material.clone();
          
          // 添加轮廓光效果而不是改变颜色
          // 通过添加轻微的自发光来表示选中状态
          if (child.material.map) {
            // 对于有贴图的材质，添加非常轻微的发光效果
            highlightMaterial.emissive = new THREE.Color(0x333333);
            highlightMaterial.emissiveIntensity = 0.4;
          } else {
            // 对于没有贴图的材质，使用绿色高亮
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
          // 恢复原始材质（带贴图的材质）
          const originalMaterial = child.userData.originalMaterial;
          child.material = originalMaterial;
          
          // 注意：不要删除originalMaterial引用，因为我们可能还会再次选中这个模型
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
          console.log('模型数据加载成功:', this.modelData);
          // 通知组合模型编辑器数据已加载
          if (this.isCompositeEditorVisible && this.$refs.compositeModelEditor) {
            console.log('通知组合模型编辑器数据已加载');
            this.$refs.compositeModelEditor.receiveModelData(this.modelData);
          }
        }
        return this.modelData; // 返回数据
      } catch (error) {
        console.error('加载模型数据失败:', error);
        this.$message.error('加载模型数据失败: ' + (error.message || '未知错误'));
        throw error;
      } finally {
        this.loading = false;
      }
    },

    handleModelSelection(modelData) {
      console.log('选择的模型数据:', modelData);

      if (modelData && modelData.file) {
        const modelInfo = {
          name: modelData.name || '未命名模型',
          file: modelData.file,
          position: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
          rotation: { x: 0, y: 0, z: 0 },
          boundData: null
        };

        this.loadModel(modelInfo);

        this.recordOperation({
          type: 'add_model',
          modelId: modelInfo.name,
          modelData: modelInfo
        });

        this.$message.success(`模型 ${modelInfo.name} 已添加到场景`);
      } else {
        console.error('无效的模型数据:', modelData);
        this.$message.error('模型数据无效');
      }
    },

    handleDeleteModel(model) {
      if (model && model.parent) {
        model.parent.remove(model);
      }

      this.selectedModel = null;
      console.log(`模型 ${model.name} 已被删除`);
    },

    // 闸站场景方法
    openGateStationInterface() {
      this.showGateStation = true;
      this.gateStationLoading = true;

      // 清理3D场景
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
      console.log('闸站场景加载完成');
    },

    cleanupThreeScene() {
      // 暂停动画
      if (this.animateId) {
        cancelAnimationFrame(this.animateId);
        this.animateId = null;
      }

      // 移除渲染器
      if (this.renderer) {
        const container = document.querySelector('.scene-container');
        if (container && this.renderer.domElement) {
          container.removeChild(this.renderer.domElement);
        }
        this.renderer.dispose();
        this.renderer = null;
      }

      // 清理其他资源
      this.scene = null;
      this.camera = null;
      this.controls = null;
      this.helperGroup = null;
      this.selectedModel = null;
    },

    async importScene() {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.json,.glb,.gltf';
      input.onchange = (event) => {
        const file = event.target.files[0];
        if (!file) return;
        const ext = file.name.toLowerCase().split('.').pop();

        if (ext === 'json') {
          const reader = new FileReader();
          reader.onload = () => {
            try {
              const sceneData = JSON.parse(reader.result);
              this.sceneLoaded = true;
              this.$nextTick(() => {
                this.initScene();
                if (sceneData.models) {
                  this.loadModels(sceneData.models);
                }
                if (sceneData.skyboxEnabled) {
                  this.skyboxEnabled = true;
                  this.setSkyboxBackground();
                }
                this.$message.success('场景导入成功');
              });
            } catch (error) {
              console.error('导入场景失败:', error);
              this.$message.error('导入场景失败');
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
              model.position.set(0, 0, 0);
              model.scale.set(1, 1, 1);
              model.rotation.set(0, 0, 0);
              this.scene.add(model);
              window.threeScene.scene = this.scene;
              this.$message.success('GLB/GLTF 导入成功');
              URL.revokeObjectURL(url);
            }, undefined, (err) => {
              console.error('GLB/GLTF 导入失败:', err);
              this.$message.error('GLB/GLTF 导入失败');
              URL.revokeObjectURL(url);
            });
          });
          return;
        }

        this.$message.warning('不支持的文件类型');
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
              this.$message.success('GLB导出成功');
            }
          },
          (error) => {
            console.error('GLB导出失败:', error);
            this.$message.error('GLB导出失败');
          },
          { binary: true }
      );

      this.scene.add(this.helperGroup);
    },

    async exportSceneToGLBBlob() {
      return new Promise((resolve, reject) => {
        if (!this.scene || !this.helperGroup) {
          reject(new Error('场景未初始化'));
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

      const sceneData = {
        models: this.scene.children
            .filter(child => child !== this.helperGroup)
            .map(model => ({
              name: model.name,
              file: model.file,
              position: {
                x: model.position.x,
                y: model.position.y,
                z: model.position.z
              },
              scale: {
                x: model.scale.x,
                y: model.scale.y,
                z: model.scale.z
              },
              rotation: {
                x: model.rotation.x,
                y: model.rotation.y,
                z: model.rotation.z
              },
              boundData: model.boundData
            }))
      };

      const jsonString = JSON.stringify(sceneData, null, 2);
      const blob = new Blob([jsonString], { type: 'application/json' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = 'scene.json';
      link.click();
      URL.revokeObjectURL(link.href);

      this.scene.add(this.helperGroup);
      this.$message.success('JSON导出成功');
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
        this.$message.success('撤销操作');
      }
    },

    redo() {
      this.clearAllModels();
    },
    clearAllModels() {
      if (!this.scene) return;
      this.$confirm('确定要清除场景中的所有模型吗？此操作不可撤销。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 获取所有模型（排除辅助对象）
        const models = this.scene.children.filter(child =>
            child !== this.helperGroup && child.isGroup
        );
        models.forEach(model => {
          this.scene.remove(model);
        });

        this.deselectModel();

        // 重置操作历史
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
      if (this.showGateStation) return; // 闸站场景中不响应快捷键

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
      this.$message.success(`背景颜色已设置为 ${color}`);
    },

    setDynamicSky() {
      // 简化的动态天空实现
      const skyColor = new THREE.Color(0x87CEEB);
      this.scene.background = skyColor;
      this.skyboxEnabled = false;
      this.$message.success('动态天空已启用（基础版）');
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

    async loadAllPanels() {
      try {
        const res = await getDataPanels();
        this.allPanels = res.data || res || [];
      } catch (e) {
        console.error('加载展板列表失败:', e);
        this.allPanels = [];
      }
    },

    openPanel(panel) {
      const exists = this.openedPanels.find(p => p.id === panel.id);
      if (!exists) {
        this.openedPanels.push(panel);
        if (panel.modelId) {
          this.findAndBindModel(panel);
        }
      }
    },

    closePanel(panel) {
      const index = this.openedPanels.findIndex(p => p.id === panel.id);
      if (index > -1) {
        this.openedPanels.splice(index, 1);
        delete this.boundModels[panel.id];
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
      this.openedPanels = this.openedPanels.map(opened => {
        const updated = this.allPanels.find(p => p.id === opened.id);
        return updated || opened;
      });
    },

    async handleDeletePanel(panel) {
      try {
        await this.$confirm(`确定要删除展板 "${panel.name}" 吗？`, '提示', { type: 'warning' });
        await deleteDataPanel(panel.id);
        this.closePanel(panel);
        await this.refreshPanels();
        this.$message.success('展板已删除');
      } catch (e) {
        if (e !== 'cancel') {
          console.error('删除展板失败:', e);
        }
      }
    },

    openAllBoundPanels() {
      const boundPanels = this.allPanels.filter(p => p.deviceId || p.modelId);
      if (boundPanels.length === 0) {
        this.$message.warning('没有已绑定的展板');
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
          if (object.uuid === panel.modelId || object.name === panel.modelName) {
            foundModel = object;
          }
        }
      });

      if (foundModel) {
        this.boundModels[panel.id] = {
          name: foundModel.name || panel.modelName,
          object: foundModel,
          screenPosition: this.getScreenPosition(foundModel)
        };
      }
    },

    startBindMode(panelConfig) {
      this.bindMode = true;
      this.bindingPanelId = panelConfig.id;
      this.$message.info('请点击场景中的模型进行绑定');
    },

    cancelBind() {
      this.bindMode = false;
      this.bindingPanelId = null;
    },

    handleUnbindModel(panelId) {
      if (this.boundModels[panelId]) {
        delete this.boundModels[panelId];
      }
      const panel = this.openedPanels.find(p => p.id === panelId);
      if (panel) {
        panel.modelId = null;
        panel.modelName = null;
        panel.modelType = null;
      }
      this.$message.success('已解除模型绑定');
    },

    async bindModelToPanel(model) {
      if (!this.bindingPanelId) return;
      
      const panel = this.openedPanels.find(p => p.id === this.bindingPanelId);
      if (!panel) return;

      try {
        await bindModelApi(panel.id, model.uuid, model.name || '未命名模型', 'scene');
        this.boundModels[panel.id] = {
          name: model.name || '未命名模型',
          object: model,
          screenPosition: this.getScreenPosition(model)
        };
        await this.refreshPanels();
        const updatedPanel = this.allPanels.find(p => p.id === panel.id);
        if (updatedPanel) {
          const idx = this.openedPanels.findIndex(p => p.id === panel.id);
          if (idx > -1) {
            this.openedPanels[idx] = updatedPanel;
          }
        }
        this.$message.success(`已绑定到模型: ${model.name || '未命名模型'}`);
      } catch (e) {
        console.error('绑定模型失败:', e);
        this.$message.error('绑定模型失败');
      }
      
      this.bindMode = false;
      this.bindingPanelId = null;
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
            console.error('天空盒背景加载失败:', error);
            this.scene.background = new THREE.Color(0x87CEEB);
            this.skyboxEnabled = false;
            this.$message.error('天空背景加载失败，使用默认背景');
          }
      );
    },

    async loadFromSceneLibrary(id) {
      try {
        const { getScene } = await import('@/api/scenes');
        const sceneData = await getScene(id)
        const { fileType, path, name } = sceneData

        this.sceneLoaded = true
        this.$nextTick(() => {
          this.initScene()

          if (fileType === 'json') {
            const sceneUrl = getScenePathUrl(path.startsWith('/') ? path.slice(1) : path)
            fetch(sceneUrl)
                .then(resp => resp.json())
                .then(jsonData => {
                  if (jsonData.models) this.loadModels(jsonData.models)
                  if (jsonData.skyboxEnabled) {
                    this.skyboxEnabled = true
                    this.setSkyboxBackground()
                  }
                  this.$message.success('JSON 场景已加载')
                })
            return
          }

          if (fileType === 'glb' || fileType === 'gltf') {
            const loader = new GLTFLoader()
            const sceneUrl = getScenePathUrl(path.startsWith('/') ? path.slice(1) : path)
            loader.load(
                sceneUrl,
                (gltf) => {
                  const model = gltf.scene
                  model.name = name
                  model.position.set(0,0,0)
                  model.scale.set(1,1,1)
                  this.scene.add(model)
                  window.threeScene.scene = this.scene
                  this.$message.success('GLB/GLTF 场景已加载')
                },
                undefined,
                (err) => {
                  console.error(err)
                  this.$message.error('GLB/GLTF 加载失败')
                }
            )
          }
          
          // 加载贴图信息
          this.loadSceneTextures(id);
        })
      } catch (e) {
        console.error('加载场景库失败', e)
        this.$message.error('加载场景失败')
      }
    },
    
    // 加载场景贴图信息
    async loadSceneTextures(sceneId) {
      try {
        const sceneAsset = await getScene(sceneId);
        console.log('加载的场景资产:', sceneAsset);
        if (sceneAsset && sceneAsset.textureInfo) {
          const textures = JSON.parse(sceneAsset.textureInfo);
          this.modelTextures = new Map(Object.entries(textures));
          this.applySavedTextures();
        }
      } catch (error) {
        console.error('加载场景贴图信息失败:', error);
      }
    },
    
    // 应用已保存的贴图到模型
    applySavedTextures() {
      // 遍历场景中的所有模型
      this.scene.traverse((object) => {
        if (object.isGroup && object !== this.helperGroup) {
          const textureData = this.modelTextures.get(object.uuid);
          if (textureData) {
            this.previewTextureOnModel(object, textureData);
          }
        }
      });
    },
    
    // 提供模型数据给组合模型编辑器
    provideModelData() {
      console.log('提供模型数据给组合模型编辑器');
      // 直接传递当前的模型数据，如果为空则先加载
      if (this.modelData.length === 0) {
        this.loadModelData();
      } else {
        // 如果数据已经存在，直接传递给组合模型编辑器
        if (this.$refs.compositeModelEditor) {
          console.log('直接传递现有模型数据给组合模型编辑器');
          this.$refs.compositeModelEditor.receiveModelData(this.modelData);
        }
      }
    },
    
    // 接收模型数据并传递给组合模型编辑器
    receiveModelData(data) {
      if (this.$refs.compositeModelEditor) {
        this.$refs.compositeModelEditor.receiveModelData(data);
      }
    },
    
    // 获取场景中的模型列表
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
    
    // 打开组合模型选择器
    openCompositeModelSelector() {
      this.isCompositeModelSelectorVisible = true;
    },
    
    // 打开组合模型编辑器
    openCompositeModelEditor() {
      this.isCompositeEditorVisible = true;
      // 使用 $nextTick 确保组件已挂载后再加载数据
      this.$nextTick(() => {
        // 确保模型数据已加载
        if (this.modelData.length === 0) {
          this.loadModelData();
        } else {
          // 如果数据已经存在，直接传递给组合模型编辑器
          if (this.$refs.compositeModelEditor) {
            console.log('直接传递现有模型数据给组合模型编辑器');
            this.$refs.compositeModelEditor.receiveModelData(this.modelData);
          }
        }
      });
    },
    
    // 加载组合模型
    async loadCompositeModel(compositeModel) {
      try {
        // 获取组合模型的组件
        const response = await getCompositeModelComponents(compositeModel.id);
        const components = response.data || response;
        
        // 加载每个组件对应的模型
        for (const component of components) {
          // 这里需要根据component.modelId获取实际的模型信息
          // 检查modelId是否已经是完整URL，避免重复添加前缀
          let fileUrl = component.modelId;
          if (fileUrl && !fileUrl.startsWith('http')) {
            fileUrl = getFileUrl(component.modelId);
          }
          
          const modelData = {
            name: `模型_${component.modelId}`,
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
          
          // 加载模型
          this.loadModel(modelData);
        }
        
        this.$message.success(`组合模型 "${compositeModel.name}" 已加载`);
        this.isCompositeModelSelectorVisible = false;
      } catch (error) {
        console.error('加载组合模型失败:', error);
        this.$message.error('加载组合模型失败: ' + (error.message || '未知错误'));
      }
    },
    
    // 保存组合模型
    async saveCompositeModel(compositeModelData) {
      // 防止重复提交
      if (this.savingCompositeModel) {
        return;
      }
      
      try {
        this.savingCompositeModel = true;
        // 调用后端API保存组合模型
        const response = await createCompositeModel(compositeModelData);
        console.log('保存组合模型到后端:', response);
        this.$message.success('组合模型保存成功');
        
        // 关闭编辑器
        this.isCompositeEditorVisible = false;
      } catch (error) {
        console.error('保存组合模型失败:', error);
        this.$message.error('保存失败: ' + (error.message || '未知错误'));
      } finally {
        this.savingCompositeModel = false;
      }
    },
    
    handleApplyTexture(model, textureData) {
      console.log('处理应用贴图请求:', { 
        modelId: model.uuid, 
        modelName: model.name,
        textureDataLength: textureData.length,
        textureDataPreview: textureData.substring(0, 100)
      });
      
      // 临时存储贴图信息
      this.textureCache.set(model.uuid, textureData);
      
      // 立即在视图中预览贴图效果
      this.previewTextureOnModel(model, textureData);
      
      // 标记场景有变化
      this.hasChanges = true;
    },
    
    handleRemoveTexture(model) {
      console.log('处理移除贴图请求:', { 
        modelId: model.uuid, 
        modelName: model.name
      });
      
      // 移除贴图缓存
      this.textureCache.delete(model.uuid);
      
      // 移除模型上的贴图
      this.removeTextureFromModel(model);
      
      // 标记场景有变化
      this.hasChanges = true;
    },
    
    previewTextureOnModel(model, textureData) {
      console.log('开始预览贴图:', { 
        modelId: model.uuid, 
        modelName: model.name,
        textureDataLength: textureData.length,
        textureDataPreview: textureData.substring(0, 100)
      });
      
      // 创建图像元素用于加载base64数据
      const image = new Image();
      image.src = textureData;
      
      // 当图像加载完成后创建贴图
      image.onload = () => {
        console.log('图像加载完成，开始创建贴图', {
          imageWidth: image.width,
          imageHeight: image.height
        });
        const texture = new THREE.CanvasTexture(image);
        // 设置纹理的色彩空间为sRGB
        texture.colorSpace = THREE.SRGBColorSpace;
        texture.needsUpdate = true;
        
        // 设置纹理重复模式，确保贴图正确覆盖整个模型表面
        texture.wrapS = THREE.RepeatWrapping;
        texture.wrapT = THREE.RepeatWrapping;
        texture.repeat.set(1, 1);
        
        // 确保纹理过滤方式正确
        texture.magFilter = THREE.LinearFilter;
        texture.minFilter = THREE.LinearFilter;
        
        // 强制设置UV变换，确保贴图完整覆盖
        texture.matrixAutoUpdate = false;
        texture.matrix.setUvTransform(0, 0, 1, 1, 0, 0, 0);
        
        console.log('创建的贴图:', texture);
        
        // 遍历模型的所有网格对象并应用贴图
        let meshCount = 0;
        model.traverse((child) => {
          if (child.isMesh) {
            meshCount++;
            console.log('处理网格对象:', {
              name: child.name || child.uuid,
              materialType: child.material.type,
              hasMap: !!child.material.map
            });
            
            // 确保使用支持贴图的材质类型
            let material;
            
            // 如果当前材质已经是合适的类型，则复用并更新其属性
            if (child.material instanceof THREE.MeshStandardMaterial || 
                child.material instanceof THREE.MeshPhongMaterial ||
                child.material instanceof THREE.MeshLambertMaterial) {
              material = child.material.clone();
              material.map = texture;
              
              // 确保材质属性正确设置以正确显示贴图
              material.needsUpdate = true;
              
              // 清除可能影响贴图显示的属性
              material.emissive = new THREE.Color(0x000000); // 清除自发光
              material.emissiveIntensity = 0; // 清除自发光强度
              
              // 如果是MeshStandardMaterial，设置合适的粗糙度和金属度以更好地显示贴图
              if (material instanceof THREE.MeshStandardMaterial) {
                material.roughness = 0.7; // 中等粗糙度表面更容易看到贴图细节
                material.metalness = 0.2; // 轻微金属感
              }
              
              // 强制更新UV变换
              if (material.map) {
                material.map.matrixAutoUpdate = false;
                material.map.matrix.setUvTransform(0, 0, 1, 1, 0, 0, 0);
              }
              
              child.material = material;
              console.log('更新现有材质贴图');
            } 
            // 如果是基础材质或其他类型，创建新的MeshPhongMaterial
            else {
              material = new THREE.MeshPhongMaterial({
                map: texture,
                emissive: new THREE.Color(0x000000), // 无自发光
                emissiveIntensity: 0,
                specular: new THREE.Color(0x111111) // 轻微高光
              });
              
              // 保持原始颜色
              if (child.material.color) {
                material.color = child.material.color;
              }
              
              // 强制更新UV变换
              if (material.map) {
                material.map.matrixAutoUpdate = false;
                material.map.matrix.setUvTransform(0, 0, 1, 1, 0, 0, 0);
              }
              
              // 替换材质
              child.material = material;
              console.log('创建新的MeshPhongMaterial');
            }
            
            // 同步更新原始材质备份（如果存在），确保取消选中时也能保留贴图
            if (child.userData.originalMaterial) {
              // 克隆当前材质作为新的原始材质备份
              child.userData.originalMaterial = child.material.clone();
              console.log('同步更新原始材质备份');
            }
            
            console.log('应用贴图后的材质:', {
              type: child.material.type,
              hasMap: !!child.material.map
            });
          }
        });
        
        console.log('共处理了', meshCount, '个网格对象');
        
        // 强制重新渲染场景
        if (this.renderer && this.scene && this.camera) {
          console.log('强制重新渲染场景');
          this.renderer.render(this.scene, this.camera);
        }
        
        // 再次检查贴图是否应用成功
        setTimeout(() => {
          console.log('延迟检查贴图应用结果:');
          let appliedCount = 0;
          model.traverse((child) => {
            if (child.isMesh) {
              console.log('网格对象贴图状态:', {
                name: child.name || child.uuid,
                hasMap: !!child.material.map,
                mapType: child.material.map ? child.material.map.constructor.name : 'None'
              });
              if (child.material.map) appliedCount++;
            }
          });
          
          // 显示贴图应用成功的提示
          if (appliedCount > 0) {
            this.$message.success(`贴图已成功应用于 ${appliedCount} 个网格对象`);
          } else {
            this.$message.warning('贴图应用可能未成功，请检查控制台输出');
          }
        }, 100);
      };
      
      // 添加错误处理
      image.onerror = (err) => {
        console.error('贴图加载失败:', err);
        console.error('贴图数据可能有问题:', textureData.substring(0, 200));
        this.$message.error('贴图加载失败，请检查图片文件');
      };
    },
    
    removeTextureFromModel(model) {
      console.log('开始移除贴图');
      
      // 移除贴图并恢复原始无贴图材质
      let restoredCount = 0;
      model.traverse((child) => {
        if (child.isMesh) {
          console.log('处理网格对象:', {
            name: child.name || child.uuid,
            hasOriginalMaterial: !!child.userData.originalMaterial
          });
          
          // 检查是否存在原始材质备份
          if (child.userData.originalMaterial) {
            // 恢复原始材质（无贴图的材质）
            console.log('恢复原始材质');
            child.material = child.userData.originalMaterial;
            child.material.needsUpdate = true;
            
            // 清除材质中的贴图
            if (child.material.map) {
              child.material.map = null;
            }
            
            // 更新原始材质备份，确保其中不包含贴图
            child.userData.originalMaterial = child.material.clone();
            
            restoredCount++;
          } else {
            // 如果没有原始材质备份，尝试清除贴图
            console.log('清除当前材质的贴图');
            if (child.material.map) {
              child.material.map = null;
              child.material.needsUpdate = true;
              restoredCount++;
            }
          }
          
          // 确保移除所有与贴图相关的属性
          if (child.material && child.material.map) {
            child.material.map = null;
            child.material.needsUpdate = true;
          }
        }
      });
      
      console.log('共恢复了', restoredCount, '个网格对象的材质');
      
      // 强制重新渲染场景
      if (this.renderer && this.scene && this.camera) {
        console.log('强制重新渲染场景');
        this.renderer.render(this.scene, this.camera);
      }
      
      // 显示成功消息
      if (restoredCount > 0) {
        this.$message.success(`已移除 ${restoredCount} 个网格对象的贴图`);
      }
    },
    
    // 保存场景时调用此方法持久化贴图信息
    saveSceneTextures(sceneId) {
      // 将贴图缓存转换为可发送到后端的格式
      const texturesToSave = [];
      for (let [modelId, textureData] of this.textureCache.entries()) {
        texturesToSave.push({
          modelId: modelId,
          textureData: textureData
        });
      }
      
      // 如果有贴图信息需要保存，则发送到后端
      if (texturesToSave.length > 0) {
        // 这里应该调用后端API保存贴图信息
        // 由于当前后端没有专门的贴图保存接口，我们将贴图信息保存到场景资产中
        console.log("需要保存的贴图信息:", texturesToSave);
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
/* 工具栏样式 - 两侧布局 */
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

/* 主内容区域 - 全屏显示 */
.main-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.main-content.with-toolbar {
  top: 70px; /* 为工具栏留出空间 */
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

/* 3D场景容器 - 全屏 */
.scene-container {
  width: 100%;
  height: 100%;
  position: relative;
  min-height: 400px; /* 添加最小高度确保有尺寸 */
}

/* 闸站场景容器 - 全屏 */
.gate-station-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: white;
  min-height: 400px; /* 添加最小高度确保有尺寸 */
}

.gate-station-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: white;
}

/* 加载状态指示 */
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

/* 操作提示样式 */
.operation-tips {
  position: absolute;
  top: 74px; /* 在工具栏下方 */
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
/* 帮助按钮样式 */
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

/* 动画效果 */
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

/* 响应式设计 */
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