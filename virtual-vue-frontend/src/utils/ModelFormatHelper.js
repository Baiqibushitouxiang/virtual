/**
 * 模型格式帮助工具
 * 提供格式转换建议和兼容性检查
 */

export class ModelFormatHelper {
  constructor() {
    this.supportedFormats = {
      gltf: {
        name: 'GLTF',
        description: 'GL Transmission Format (推荐)',
        supported: true,
        priority: 1
      },
      glb: {
        name: 'GLB',
        description: 'GL Transmission Format Binary (推荐)',
        supported: true,
        priority: 1
      },
      fbx: {
        name: 'FBX',
        description: 'Autodesk FBX (部分支持)',
        supported: true,
        priority: 2
      },
      obj: {
        name: 'OBJ',
        description: 'Wavefront OBJ (基础支持)',
        supported: true,
        priority: 3
      }
    };
  }

  /**
   * 检查文件格式是否支持
   * @param {string} fileName - 文件名
   * @returns {Object} - 格式信息
   */
  checkFormat(fileName) {
    const extension = fileName.toLowerCase().split('.').pop();
    const format = this.supportedFormats[extension];
    
    if (format) {
      return {
        ...format,
        extension: extension,
        fileName: fileName
      };
    }
    
    return {
      name: 'Unknown',
      description: '未知格式',
      supported: false,
      priority: 999,
      extension: extension,
      fileName: fileName
    };
  }

  /**
   * 获取格式转换建议
   * @param {string} fileName - 文件名
   * @returns {Array} - 转换建议列表
   */
  getConversionSuggestions(fileName) {
    const format = this.checkFormat(fileName);
    const suggestions = [];

    if (!format.supported) {
      suggestions.push({
        type: 'error',
        message: `不支持的格式: ${format.extension}`,
        action: '请转换为支持的格式'
      });
      return suggestions;
    }

    if (format.extension === 'fbx') {
      suggestions.push({
        type: 'warning',
        message: 'FBX格式可能存在版本兼容性问题',
        action: '建议转换为GLTF/GLB格式以获得更好的兼容性'
      });
    }

    if (format.extension === 'obj') {
      suggestions.push({
        type: 'info',
        message: 'OBJ格式功能有限',
        action: '建议转换为GLTF/GLB格式以获得更好的功能支持'
      });
    }

    if (format.extension === 'gltf' || format.extension === 'glb') {
      suggestions.push({
        type: 'success',
        message: 'GLTF/GLB格式是最佳选择',
        action: '无需转换，直接使用'
      });
    }

    return suggestions;
  }

  /**
   * 获取推荐的转换工具
   * @param {string} fromFormat - 源格式
   * @param {string} toFormat - 目标格式
   * @returns {Array} - 推荐工具列表
   */
  getConversionTools(fromFormat, toFormat) {
    const tools = [];

    if (fromFormat === 'fbx' && (toFormat === 'gltf' || toFormat === 'glb')) {
      tools.push({
        name: 'Blender',
        description: '免费3D软件，支持FBX到GLTF转换',
        url: 'https://www.blender.org/',
        steps: [
          '导入FBX文件',
          '选择导出 > glTF 2.0 (.glb/.gltf)',
          '调整导出设置',
          '导出文件'
        ]
      });

      tools.push({
        name: 'FBX2glTF',
        description: 'Facebook开发的命令行转换工具',
        url: 'https://github.com/facebookincubator/FBX2glTF',
        steps: [
          '安装FBX2glTF',
          '运行转换命令',
          '获得GLTF文件'
        ]
      });

      tools.push({
        name: 'Online Converters',
        description: '在线转换工具',
        url: 'https://www.online-convert.com/',
        steps: [
          '上传FBX文件',
          '选择目标格式',
          '下载转换后的文件'
        ]
      });
    }

    return tools;
  }

  /**
   * 创建格式转换指南
   * @param {string} fileName - 文件名
   * @returns {Object} - 转换指南
   */
  createConversionGuide(fileName) {
    const format = this.checkFormat(fileName);
    const suggestions = this.getConversionSuggestions(fileName);
    const tools = this.getConversionTools(format.extension, 'glb');

    return {
      fileName: fileName,
      currentFormat: format,
      suggestions: suggestions,
      conversionTools: tools,
      recommendedAction: this.getRecommendedAction(format, suggestions)
    };
  }

  /**
   * 获取推荐操作
   * @param {Object} format - 格式信息
   * @param {Array} suggestions - 建议列表
   * @returns {string} - 推荐操作
   */
  getRecommendedAction(format, suggestions) {
    if (!format.supported) {
      return '立即转换为GLTF/GLB格式';
    }

    if (format.extension === 'gltf' || format.extension === 'glb') {
      return '无需转换，直接使用';
    }

    if (format.extension === 'fbx') {
      return '建议转换为GLTF/GLB格式以提高兼容性';
    }

    if (format.extension === 'obj') {
      return '建议转换为GLTF/GLB格式以获得更好功能';
    }

    return '检查文件格式';
  }

  /**
   * 生成错误信息
   * @param {string} fileName - 文件名
   * @param {Error} error - 错误对象
   * @returns {Object} - 错误信息
   */
  generateErrorMessage(fileName, error) {
    const format = this.checkFormat(fileName);
    const guide = this.createConversionGuide(fileName);

    let errorType = 'unknown';
    let errorMessage = error.message;

    if (error.message.includes('FBX version not supported')) {
      errorType = 'fbx_version';
      errorMessage = 'FBX版本不支持，请转换为GLTF/GLB格式';
    } else if (error.message.includes('JSON Parse error')) {
      errorType = 'format_mismatch';
      errorMessage = '文件格式不匹配，请检查文件类型';
    } else if (error.message.includes('404')) {
      errorType = 'file_not_found';
      errorMessage = '文件未找到，请检查文件路径';
    }

    return {
      fileName: fileName,
      errorType: errorType,
      errorMessage: errorMessage,
      originalError: error.message,
      format: format,
      conversionGuide: guide,
      timestamp: new Date().toISOString()
    };
  }
}

// 导出单例实例
export const modelFormatHelper = new ModelFormatHelper(); 