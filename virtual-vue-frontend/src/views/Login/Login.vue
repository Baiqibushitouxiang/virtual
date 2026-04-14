<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="project-title">数字孪生平台</h1>
        <h2 class="login-title">欢迎回来</h2>
        <p class="login-subtitle">请登录您的账号</p>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            class="custom-input"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            class="custom-input"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-button">
            <span class="button-text">登录</span>
          </el-button>
        </el-form-item>
        <el-form-item class="register-link">
          <el-button type="text" @click="goRegister">
            <span>没有账号？</span>
            <span class="link-highlight">立即注册</span>
          </el-button>
        </el-form-item>
      </el-form>
      <p v-if="error" class="error-message">{{ error }}</p>
    </div>
    <!-- 装饰元素 -->
    <div class="decorative-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

export default {
  name: 'Login',
  setup() {
    const store = useStore();
    const router = useRouter();
    const loginFormRef = ref(null);
    const loginForm = reactive({
      username: '',
      password: ''
    });
    const error = ref(null);

    const loginRules = reactive({
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    });

    const handleLogin = async () => {
      error.value = null;
      if (!loginFormRef.value) return;
      
      try {
        await loginFormRef.value.validate();
      } catch (validateError) {
        return;
      }

      try {
        await store.dispatch('loginUser', {
          username: loginForm.username,
          password: loginForm.password
        });
        router.push('/');
      } catch (err) {
        error.value = '登录失败，请检查用户名和密码';
        console.error('Login failed:', err);
      }
    };

    const goRegister = () => {
      router.push('/register');
    };

    return {
      loginFormRef,
      loginForm,
      loginRules,
      error,
      handleLogin,
      goRegister
    };
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  padding: 20px;
}

.login-card {
  max-width: 420px;
  width: 100%;
  padding: 48px 36px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(226, 232, 240, 0.5);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #64b5f6 0%, #42a5f5 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.login-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.12);
}

.login-card:hover::before {
  opacity: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.project-title {
  font-size: 32px;
  font-weight: 700;
  color: #64b5f6;
  margin: 0 0 12px 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e88e5;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  font-size: 14px;
  color: #90caf9;
  margin: 0;
  font-weight: 400;
}

.login-form {
  width: 100%;
}

.custom-input {
  width: 100%;
  margin-bottom: 8px;
}

.custom-input .el-input__wrapper {
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1.5px solid rgba(226, 232, 240, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 12px 16px;
}

.custom-input .el-input__wrapper:hover {
  border-color: rgba(100, 181, 246, 0.4);
  box-shadow: 0 4px 12px rgba(100, 181, 246, 0.1);
}

.custom-input .el-input__wrapper.is-focus {
  border-color: #64b5f6;
  box-shadow: 0 0 0 3px rgba(100, 181, 246, 0.1);
  background-color: #ffffff;
}

.login-button {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
  border: none;
  color: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(100, 181, 246, 0.3);
  background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
}

.login-button:active {
  transform: translateY(0);
}

.button-text {
  position: relative;
  z-index: 1;
}

.register-link {
  margin-top: 20px;
  text-align: center;
}

.register-link .el-button {
  width: auto;
  padding: 0;
  font-size: 14px;
  color: #868e96;
  background: transparent;
  border: none;
  transition: all 0.2s ease;
}

.register-link .el-button:hover {
  color: #495057;
  background: transparent;
}

.link-highlight {
  color: #64b5f6;
  font-weight: 600;
  transition: all 0.2s ease;
  position: relative;
}

.link-highlight::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #64b5f6;
  transform: scaleX(0);
  transform-origin: right;
  transition: transform 0.3s ease;
}

.register-link .el-button:hover .link-highlight::after {
  transform: scaleX(1);
  transform-origin: left;
}

.error-message {
  color: #e53e3e;
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  padding: 10px 16px;
  background-color: rgba(237, 204, 204, 0.3);
  border-radius: 8px;
  border-left: 4px solid #e53e3e;
  animation: slideIn 0.4s ease;
}

.decorative-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #90caf9 0%, #64b5f6 100%);
  bottom: -75px;
  left: -75px;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #bbdefb 0%, #90caf9 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
