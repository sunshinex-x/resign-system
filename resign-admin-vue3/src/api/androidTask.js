import request from '@/utils/request'

// 获取任务列表
export function getTaskList(params) {
  return request({
    url: '/api/android/resign/tasks',
    method: 'get',
    params
  })
}

// 创建任务
export function createTask(data) {
  return request({
    url: '/api/android/resign/tasks',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 解析APK文件
export function parseApkInfo(file) {
  return request({
    url: '/api/android/resign/parse-apk',
    method: 'post',
    data: file,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 验证包名
export function validatePackageName(packageName) {
  return request({
    url: '/api/android/resign/validate-package',
    method: 'post',
    data: { packageName }
  })
}

// 执行任务
export function executeTask(taskId) {
  return request({
    url: `/api/android/resign/tasks/${taskId}/execute`,
    method: 'post'
  })
}

// 重试任务
export function retryTask(taskId) {
  return request({
    url: `/api/android/resign/tasks/${taskId}/retry`,
    method: 'post'
  })
}

// 取消任务
export function cancelTask(taskId) {
  return request({
    url: `/api/android/resign/tasks/${taskId}/cancel`,
    method: 'post'
  })
}

// 删除任务
export function deleteTask(taskId) {
  return request({
    url: `/api/android/resign/tasks/${taskId}`,
    method: 'delete'
  })
}

// 获取任务详情
export function getTaskDetail(taskId) {
  return request({
    url: `/api/android/resign/tasks/${taskId}`,
    method: 'get'
  })
}

// 下载重签名文件
export function downloadFile(taskId) {
  return request({
    url: `/api/android/resign/tasks/${taskId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取任务统计
export function getTaskStats() {
  return request({
    url: '/api/android/resign/tasks/stats',
    method: 'get'
  })
}

// 获取任务进度
export function getTaskProgress(taskId) {
  return request({
    url: `/api/android/resign/tasks/${taskId}/progress`,
    method: 'get'
  })
}

// 处理待处理任务
export function processPendingTasks() {
  return request({
    url: '/api/android/resign/tasks/process-pending',
    method: 'post'
  })
}

// 清理过期任务
export function cleanupExpiredTasks(daysToKeep) {
  return request({
    url: '/api/android/resign/tasks/cleanup',
    method: 'post',
    data: { daysToKeep }
  })
}

// 默认导出所有方法
export default {
  getTaskList,
  createTask,
  parseApkInfo,
  validatePackageName,
  executeTask,
  retryTask,
  cancelTask,
  deleteTask,
  getTaskDetail,
  downloadFile,
  getTaskStats,
  getTaskProgress,
  processPendingTasks,
  cleanupExpiredTasks
}