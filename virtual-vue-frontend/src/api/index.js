import axios from "axios";
import { API_CONFIG, EXTERNAL_CONFIG, getModelUrl, getSceneUrl } from '../config/apiconfig';
import { ElMessage } from 'element-plus'


const apiClient = axios.create({
    baseURL: API_CONFIG.baseURL,
    timeout: API_CONFIG.timeout
});

apiClient.interceptors.request.use(
    config => config,
    error => Promise.reject(error)
)

apiClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response) {
            const status = error.response.status;
            switch (status) {
                case 401:
                    ElMessage.error('用户名或密码错误');
                    break;
                case 403:
                    ElMessage.error('您的权限不足');
                    break;
                case 404:
                    ElMessage.error('资源不存在');
                    break;
                case 409:
                    ElMessage.error('用户名已存在');
                    break;
                case 500:
                    ElMessage.error('服务器错误');
                    break;
                default:
                    ElMessage.error('发生未知错误');
            }
        }
        return Promise.reject(error);
    }
);

const API_PREFIX = '/api';

export const getUserById = (id) => apiClient.get(`${API_PREFIX}/users/${id}`);
export const updateUser = (id, data) => apiClient.put(`${API_PREFIX}/users/${id}`, data);
export const deleteUser = (id) => apiClient.delete(`${API_PREFIX}/users/${id}`);
export const getAllUsers = () => apiClient.get(`${API_PREFIX}/users`);
export const createUser = (data) => apiClient.post(`${API_PREFIX}/users`, data);
export const registerUser = (data) => apiClient.post(`${API_PREFIX}/users/register`, data);
export const loginUser = (data) => apiClient.post(`${API_PREFIX}/users/login`, data);
export const getUsersPage = () => apiClient.get(`${API_PREFIX}/users/page`);

export const getAllUserData = () => apiClient.get(`${API_PREFIX}/userdata`);
export const getUserDataById = (id) => apiClient.get(`${API_PREFIX}/userdata/${id}`);
export const addUserData = (data) => apiClient.post(`${API_PREFIX}/userdata/save`, data)


export const getAllModels = () => apiClient.get(`${API_PREFIX}/models/`);
export const getModelById = (id) => apiClient.get(`${API_PREFIX}/models/${id}`);
export const getModelByName = (name) => apiClient.get(`${API_PREFIX}/models/name/${name}`);
export const createModel = (modelData) => apiClient.post(`${API_PREFIX}/models/`, modelData);
export const updateModel = (id, modelData) => apiClient.put(`${API_PREFIX}/models/${id}`, modelData);
export const deleteModel = (id) => apiClient.delete(`${API_PREFIX}/models/${id}`);
export const importModels = (modelsData) => apiClient.post(`${API_PREFIX}/models/import`, modelsData);
export const getModelMenu = () => apiClient.get(`${API_PREFIX}/models/menu`);

export const getModelCategoryCount = () => apiClient.get(`${API_PREFIX}/models/categoryCount`);
export const downloadModelFile = (name) => apiClient.get(`${API_PREFIX}/files/${name}`, { responseType: 'blob' });

export const uploadModel = async (category, name, modelFile) => {
    const formData = new FormData();
    formData.append('category', category);
    formData.append('name', name);
    formData.append('modelFile', modelFile);

    try {
        const response = await fetch(`${API_CONFIG.baseURL}${API_PREFIX}/models/upload`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error('文件上传失败');
        }

        const result = await response.json();
        console.log('上传成功:', result);
        return result;
    } catch (error) {
        console.error('上传失败:', error);
        throw error;
    }
};

export const createCompositeModel = (data) => apiClient.post(`${API_PREFIX}/composite-models`, data);
export const getCompositeModels = () => apiClient.get(`${API_PREFIX}/composite-models`);
export const getCompositeModel = (id) => apiClient.get(`${API_PREFIX}/composite-models/${id}`);
export const updateCompositeModel = (id, data) => apiClient.put(`${API_PREFIX}/composite-models/${id}`, data);
export const deleteCompositeModel = (id) => apiClient.delete(`${API_PREFIX}/composite-models/${id}`);
export const getCompositeModelComponents = (id) => apiClient.get(`${API_PREFIX}/composite-models/${id}/components`);
export const saveCompositeModelComponents = (id, components) => apiClient.post(`${API_PREFIX}/composite-models/${id}/components`, components);

export const getOpcUaStatus = () => apiClient.get(`${API_PREFIX}/opcua/status`);
export const syncOpcUaDevices = () => apiClient.post(`${API_PREFIX}/opcua/sync-devices`);
export const createOpcUaDevice = (data) => apiClient.post(`${API_PREFIX}/opcua/device`, data);
export const deleteOpcUaDevice = (name) => apiClient.delete(`${API_PREFIX}/opcua/device/${name}`);

export const getFileUrl = (name) => `${API_CONFIG.baseURL}${API_PREFIX}/files/${name}`;
export const getModelStaticUrl = (name) => getModelUrl(`models/${name}.glb`);
export const getModelPathUrl = (path) => {
    const cleanPath = path.startsWith('/') ? path.slice(1) : path;
    return getModelUrl(cleanPath.startsWith('models/') ? cleanPath : `models/${cleanPath}`);
};
export const getScenePathUrl = (path) => getSceneUrl(path);
export const getGateStationUrl = () => EXTERNAL_CONFIG.gateStationURL;

export default {
    getUserById,
    updateUser,
    deleteUser,
    getAllUsers,
    createUser,
    registerUser,
    loginUser,
    getUsersPage,
    getAllUserData,
    getUserDataById,
    getAllModels,
    createModel,
    getModelById,
    getModelByName,
    updateModel,
    deleteModel,
    importModels,
    getModelMenu,
    downloadModelFile,
    uploadModel,
    createCompositeModel,
    getCompositeModels,
    getCompositeModel,
    updateCompositeModel,
    deleteCompositeModel,
    getCompositeModelComponents,
    saveCompositeModelComponents,
    getOpcUaStatus,
    syncOpcUaDevices,
    createOpcUaDevice,
    deleteOpcUaDevice,
    getFileUrl,
    getModelStaticUrl,
    getModelPathUrl,
    getScenePathUrl,
    getGateStationUrl
};
