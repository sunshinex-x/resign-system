import { ElMessage } from 'element-plus'
import { ref } from 'vue'

/**
 * 通用列表请求处理工具
 * 提供统一的数据请求、加载状态管理和成功提示功能
 */
export class ListHandler {
  constructor(options = {}) {
    this.loading = ref(false)
    this.list = ref([])
    this.total = ref(0)
    this.pagination = ref({
      page: 1,
      size: 10,
      ...options.pagination
    })
    
    // 配置选项
    this.options = {
      successMessage: '刷新成功',
      errorMessage: '获取数据失败',
      showSuccessOnManual: true, // 是否在手动操作时显示成功提示
      ...options
    }
  }

  /**
   * 执行数据请求
   * @param {Function} requestFn - 请求函数
   * @param {Object} params - 请求参数
   * @param {Boolean} showSuccessMessage - 是否显示成功提示
   * @returns {Promise}
   */
  async fetchData(requestFn, params = {}, showSuccessMessage = false) {
    this.loading.value = true
    
    try {
      const response = await requestFn(params)
      
      // 检查响应是否有效
      if (!response) {
        throw new Error('API响应为空')
      }
      
      if (response.code === 200 && response.data) {
        // 处理分页列表数据结构
        // API响应格式: { code, data: { records, total, ... }, message }
        let listData = []
        let totalCount = 0
        
        // data 应该是对象，包含 records 和分页信息
        if (response.data && typeof response.data === 'object' && !Array.isArray(response.data)) {
          listData = response.data.records || response.data.list || []
          totalCount = response.data.total || response.data.totalElements || 0
        } else {
          // 兼容旧的数组格式（如果存在）
          console.warn('意外的数据格式，期望对象但收到:', typeof response.data, response.data)
          listData = Array.isArray(response.data) ? response.data : []
          totalCount = listData.length
        }
        
        this.list.value = Array.isArray(listData) ? listData : []
        this.total.value = totalCount
        
        // 显示成功提示
        if (showSuccessMessage && this.options.showSuccessOnManual) {
          ElMessage.success(this.options.successMessage)
        }
        
        return { success: true, data: this.list.value, total: this.total.value }
      } else {
        this.list.value = []
        this.total.value = 0
        const errorMsg = response.message || response.msg || `请求失败 (状态码: ${response.code})`
        console.warn('API返回非200状态:', {
          code: response.code,
          message: errorMsg,
          data: response.data
        })
        // 不显示错误提示，让调用方决定是否需要提示
        return { success: false, error: errorMsg }
      }
    } catch (error) {
      this.list.value = []
      this.total.value = 0
      const errorMsg = error.message || this.options.errorMessage
      console.warn('API请求失败:', {
        error: error.message,
        stack: error.stack,
        options: this.options
      })
      return { success: false, error: errorMsg }
    } finally {
      this.loading.value = false
    }
  }

  /**
   * 搜索数据（手动操作，显示成功提示）
   * @param {Function} requestFn - 请求函数
   * @param {Object} params - 请求参数
   * @returns {Promise}
   */
  async search(requestFn, params = {}) {
    this.pagination.value.page = 1
    return await this.fetchData(requestFn, {
      ...params,
      page: this.pagination.value.page,
      size: this.pagination.value.size
    }, true)
  }

  /**
   * 重置查询（手动操作，显示成功提示）
   * @param {Function} requestFn - 请求函数
   * @param {Object} defaultParams - 默认参数
   * @returns {Promise}
   */
  async reset(requestFn, defaultParams = {}) {
    this.pagination.value.page = 1
    return await this.fetchData(requestFn, {
      ...defaultParams,
      page: this.pagination.value.page,
      size: this.pagination.value.size
    }, true)
  }

  /**
   * 刷新数据（手动操作，显示成功提示）
   * @param {Function} requestFn - 请求函数
   * @param {Object} params - 请求参数
   * @returns {Promise}
   */
  async refresh(requestFn, params = {}) {
    return await this.fetchData(requestFn, {
      ...params,
      page: this.pagination.value.page,
      size: this.pagination.value.size
    }, true)
  }

  /**
   * 分页大小变化
   * @param {Number} size - 新的分页大小
   * @param {Function} requestFn - 请求函数
   * @param {Object} params - 请求参数
   * @returns {Promise}
   */
  async handleSizeChange(size, requestFn, params = {}) {
    this.pagination.value.size = size
    this.pagination.value.page = 1
    return await this.fetchData(requestFn, {
      ...params,
      page: this.pagination.value.page,
      size: this.pagination.value.size
    }, false)
  }

  /**
   * 页码变化
   * @param {Number} page - 新的页码
   * @param {Function} requestFn - 请求函数
   * @param {Object} params - 请求参数
   * @returns {Promise}
   */
  async handlePageChange(page, requestFn, params = {}) {
    this.pagination.value.page = page
    return await this.fetchData(requestFn, {
      ...params,
      page: this.pagination.value.page,
      size: this.pagination.value.size
    }, false)
  }

  /**
   * 初始化加载数据（不显示成功提示）
   * @param {Function} requestFn - 请求函数
   * @param {Object} params - 请求参数
   * @returns {Promise}
   */
  async init(requestFn, params = {}) {
    return await this.fetchData(requestFn, {
      ...params,
      page: this.pagination.value.page,
      size: this.pagination.value.size
    }, false)
  }

  /**
   * 获取当前状态
   * @returns {Object}
   */
  getState() {
    return {
      loading: this.loading,
      list: this.list,
      total: this.total,
      pagination: this.pagination
    }
  }

  /**
   * 更新配置
   * @param {Object} newOptions - 新的配置选项
   */
  updateOptions(newOptions) {
    this.options = { ...this.options, ...newOptions }
  }
}

/**
 * 创建列表处理器的便捷函数
 * @param {Object} options - 配置选项
 * @returns {Object} 包含loading、list、pagination和listHandler的对象
 */
export function createListHandler(options = {}) {
  const handler = new ListHandler(options)
  return {
    loading: handler.loading,
    list: handler.list,
    total: handler.total,
    pagination: handler.pagination,
    listHandler: handler
  }
}

/**
 * 默认导出
 */
export default {
  ListHandler,
  createListHandler
}