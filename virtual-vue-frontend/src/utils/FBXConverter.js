/**
 * FBX文件转换和兼容性处理工具
 * 用于解决FBX版本不支持的问题
 */

export class FBXConverter {
  constructor() {
    // 根据Three.js FBXLoader的实际支持情况更新版本列表
    // 6100版本在某些Three.js版本中可能不被支持
    this.supportedVersions = [7400, 7300, 7200, 7100, 7000, 6000, 5800, 5700];
  }

  /**
   * 检查FBX文件版本是否支持
   * @param {ArrayBuffer} arrayBuffer - FBX文件的ArrayBuffer
   * @returns {boolean} - 是否支持
   */
  checkFBXVersion(arrayBuffer) {
    try {
      const uint8Array = new Uint8Array(arrayBuffer);
      const textDecoder = new TextDecoder('utf-8');
      const header = textDecoder.decode(uint8Array.slice(0, 23));
      
      if (header.startsWith('Kaydara FBX Binary')) {
        // 二进制FBX文件
        const version = new DataView(arrayBuffer).getUint32(23, true);
        return this.supportedVersions.includes(version);
      } else if (header.startsWith('; FBX')) {
        // ASCII FBX文件
        return true; // ASCII格式通常兼容性更好
      }
      
      return false;
    } catch (error) {
      console.error('检查FBX版本时出错:', error);
      return false;
    }
  }

  /**
   * 获取FBX文件版本信息
   * @param {ArrayBuffer} arrayBuffer - FBX文件的ArrayBuffer
   * @returns {Object} - 版本信息
   */
  getFBXVersionInfo(arrayBuffer) {
    try {
      const uint8Array = new Uint8Array(arrayBuffer);
      const textDecoder = new TextDecoder('utf-8');
      const header = textDecoder.decode(uint8Array.slice(0, 23));
      
      if (header.startsWith('Kaydara FBX Binary')) {
        const version = new DataView(arrayBuffer).getUint32(23, true);
        return {
          type: 'binary',
          version: version,
          supported: this.supportedVersions.includes(version)
        };
      } else if (header.startsWith('; FBX')) {
        return {
          type: 'ascii',
          version: 'unknown',
          supported: true
        };
      }
      
      return {
        type: 'unknown',
        version: 'unknown',
        supported: false
      };
    } catch (error) {
      console.error('获取FBX版本信息时出错:', error);
      return {
        type: 'error',
        version: 'error',
        supported: false
      };
    }
  }

  /**
   * 尝试转换FBX文件为GLTF格式
   * @param {ArrayBuffer} arrayBuffer - FBX文件的ArrayBuffer
   * @returns {Promise<ArrayBuffer>} - 转换后的GLTF文件
   */
  async convertFBXToGLTF(arrayBuffer) {
    try {
      // 这里可以集成第三方转换库，如FBX2glTF
      // 目前返回原始数据，建议使用外部工具转换
      console.warn('FBX转GLTF功能需要集成第三方转换库');
      return arrayBuffer;
    } catch (error) {
      console.error('FBX转GLTF失败:', error);
      throw error;
    }
  }

  /**
   * 提供FBX版本兼容性建议
   * @param {number} version - FBX版本号
   * @returns {string} - 建议信息
   */
  getCompatibilityAdvice(version) {
    const versionMap = {
      7400: 'FBX 7.4 (2016)',
      7300: 'FBX 7.3 (2013)',
      7200: 'FBX 7.2 (2012)',
      7100: 'FBX 7.1 (2011)',
      7000: 'FBX 7.0 (2010)',
      6100: 'FBX 6.1 (2009)',
      6000: 'FBX 6.0 (2008)',
      5800: 'FBX 5.8 (2007)',
      5700: 'FBX 5.7 (2006)'
    };

    const versionName = versionMap[version] || `未知版本 (${version})`;
    
    if (this.supportedVersions.includes(version)) {
      return `FBX版本 ${versionName} 应该被支持，如果仍有问题，请检查文件完整性。`;
    } else {
      return `FBX版本 ${versionName} 不被Three.js支持。建议使用以下方法：
1. 在3D软件中重新导出为FBX 2013或更早版本
2. 转换为GLTF/GLB格式
3. 使用在线转换工具`;
    }
  }

  /**
   * 创建FBX加载错误处理函数
   * @param {string} modelName - 模型名称
   * @returns {Function} - 错误处理函数
   */
  createErrorHandler(modelName) {
    return (error) => {
      console.error(`FBX模型加载失败: ${modelName}`, error);
      
      // 分析错误信息
      if (error.message.includes('FBX version not supported')) {
        const versionMatch = error.message.match(/FileVersion: (\d+)/);
        if (versionMatch) {
          const version = parseInt(versionMatch[1]);
          const advice = this.getCompatibilityAdvice(version);
          console.warn(`版本兼容性建议: ${advice}`);
        }
      }
      
      // 返回错误信息供上层处理
      return {
        error: true,
        message: error.message,
        modelName: modelName,
        suggestion: '建议转换为GLTF/GLB格式或使用兼容的FBX版本'
      };
    };
  }
}

// 导出单例实例
export const fbxConverter = new FBXConverter(); 