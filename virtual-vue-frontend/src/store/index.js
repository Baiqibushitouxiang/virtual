import { createStore } from 'vuex';
import { loginUser as loginApi } from '@/api'; // 从API中引入 loginUser 函数

export default createStore({
    state: {
        user: null,          // 保存用户信息
        token: localStorage.getItem('token') || null, // 尝试从localStorage获取现有token
    },
    mutations: {
        // 设置用户信息
        setUser(state, user) {
            state.user = user;
        },
        // 设置token
        setToken(state, token) {
            state.token = token;
            localStorage.setItem('token', token); // 保存 token 到 localStorage
        },
        // 清除用户信息和token
        logout(state) {
            state.user = null;
            state.token = null;
            localStorage.removeItem('token'); // 从localStorage中移除 token
        }
    },
    actions: {
        // 登录操作，调用API并保存token和用户信息
        async loginUser({ commit }, credentials) {
            try {
                const response = await loginApi(credentials); // 调用登录 API
                const userData = response.data.data; // 从响应中提取用户数据和 token
                console.log(userData)
                commit('setUser', userData);
                commit('setToken', userData.token);

                return response.data; // 返回响应数据
            } catch (error) {
                console.error('Login failed:', error);
                throw new Error('登录失败，请检查用户名和密码');
            }
        },
        // 退出登录
        logout({ commit }) {
            commit('logout');
        },
        // 自动登录：在应用加载时检查是否有 token，如果有则尝试自动登录
        async autoLogin({ commit, state }) {
            const token = state.token;
            if (token) {
                try {
                    // 根据实际需求，可以在这里调用获取用户信息的API
                    // const user = await getUserInfoApi();
                    // commit('setUser', user.data);

                    // 暂时只是假设token有效，将token存储在header中
                    console.log("Token存在，可以自动登录");
                } catch (error) {
                    console.error("自动登录失败，清除无效token");
                    commit('logout'); // 无效token则登出
                }
            }
        }
    },
    getters: {
        isAuthenticated: (state) => !!state.token, // 检查是否已登录
        getUser: (state) => state.user,            // 获取当前用户信息
        getToken: (state) => state.token           // 获取token
    }
});
