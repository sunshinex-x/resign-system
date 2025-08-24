import request from '@/utils/request'

/**
 * 分页查询签名配置
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getSignConfigPage(params) {
  return request({
    url: '/api/sign-config/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取签名配置
 * @param {number} id 配置ID
 * @returns {Promise}
 */
export function getSignConfigById(id) {
  return request({
    url: `/api/sign-config/${id}`,
    method: 'get'
  })
}

/**
 * 创建签名配置
 * @param {Object} data 配置数据
 * @returns {Promise}
 */
export function createSignConfig(data) {
  return request({
    url: '/api/sign-config',
    method: 'post',
    data
  })
}

/**
 * 更新签名配置
 * @param {number} id 配置ID
 * @param {Object} data 配置数据
 * @returns {Promise}
 */
export function updateSignConfig(id, data) {
  return request({
    url: `/api/sign-config/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除签名配置
 * @param {number} id 配置ID
 * @returns {Promise}
 */
export function deleteSignConfig(id) {
  return request({
    url: `/api/sign-config/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除签名配置
 * @param {Array} ids 配置ID数组
 * @returns {Promise}
 */
export function batchDeleteSignConfig(ids) {
  return request({
    url: '/api/sign-config/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 切换签名配置状态
 * @param {number} id 配置ID
 * @param {number} status 状态值 (0: 禁用, 1: 启用)
 * @returns {Promise}
 */
export function toggleSignConfigStatus(id, status) {
  return request({
    url: `/api/sign-config/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 根据包名和应用类型获取签名配置
 * @param {string} packageName 包名
 * @param {string} appType 应用类型
 * @returns {Promise}
 */
export function getSignConfigByPackage(packageName, appType) {
  return request({
    url: '/api/sign-config/package',
    method: 'get',
    params: {
      packageName,
      appType
    }
  })
}

/**
 * 根据应用类型获取所有启用的签名配置
 * @param {string} appType 应用类型
 * @returns {Promise}
 */
export function getEnabledSignConfigsByType(appType) {
  return request({
    url: '/api/sign-config/enabled',
    method: 'get',
    params: { appType }
  })
}