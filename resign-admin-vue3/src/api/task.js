import request from '@/utils/request'

// 获取任务列表（分页）
export function getTaskList(params) {
  return request({
    url: '/api/resign/tasks',
    method: 'get',
    params
  })
}

// 获取任务详情
export function getTaskDetail(taskId) {
  return request({
    url: `/api/resign/tasks/${taskId}`,
    method: 'get'
  })
}

// 创建任务
export function createTask(data) {
  return request({
    url: '/api/resign/tasks',
    method: 'post',
    data
  })
}

// 重试任务
export function retryTask(taskId) {
  return request({
    url: `/api/resign/tasks/${taskId}/retry`,
    method: 'post'
  })
}

// 高级搜索
export function searchTasks(data) {
  return request({
    url: '/api/resign/tasks/search',
    method: 'post',
    data
  })
}

// 获取任务统计数据
export function getTaskStats() {
  return request({
    url: '/api/resign/tasks/stats',
    method: 'get'
  })
}

// 批量删除任务
export function deleteTaskBatch(taskIds) {
  return request({
    url: '/api/resign/tasks/batch',
    method: 'delete',
    data: taskIds
  })
}

// 创建任务V2（支持多Bundle ID和Profile文件映射）
export function createTaskV2(formData) {
  return request({
    url: '/api/resign/create-v2',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 为任务添加Bundle ID和Profile文件映射
export function addBundleProfile(taskId, formData) {
  return request({
    url: `/api/resign/tasks/${taskId}/add-bundle-profile`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}