import request from '@/utils/request'

/**
 * 看板相关API
 */

// 获取任务统计数据
export function getTaskStats() {
  return request({
    url: '/api/dashboard/stats',
    method: 'get'
  })
}

// 获取最近任务列表
export function getRecentTasks(params = {}) {
  return request({
    url: '/api/dashboard/recent-tasks',
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 5,
      ...params
    }
  })
}

// 获取任务类型分布
export function getTaskTypeDistribution() {
  return request({
    url: '/api/dashboard/task-type-distribution',
    method: 'get'
  })
}

// 获取近7天任务趋势
export function getTaskTrend() {
  return request({
    url: '/api/dashboard/task-trend',
    method: 'get'
  })
}

// 兼容性方法：为了平滑过渡，保留原有方法名
export function getTaskList(params = {}) {
  return getRecentTasks(params)
}