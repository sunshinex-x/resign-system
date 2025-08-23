import request from '@/utils/request'

// 获取日志列表
export function getLogList(params) {
  return request({
    url: '/api/system/logs',
    method: 'get',
    params
  })
}

// 获取日志详情
export function getLogDetail(logId) {
  return request({
    url: `/api/system/logs/${logId}`,
    method: 'get'
  })
}

// 清空日志
export function clearLogs() {
  return request({
    url: '/api/system/logs/clear',
    method: 'delete'
  })
}

// 导出日志
export function exportLogs(params) {
  return request({
    url: '/api/system/logs/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取日志统计信息
export function getLogStats() {
  return request({
    url: '/api/system/logs/stats',
    method: 'get'
  })
}