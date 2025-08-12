import { defineStore } from 'pinia'
import { getTaskList, getTaskDetail, createTask, retryTask, deleteTaskBatch, getTaskStats } from '@/api/task'

export const useTaskStore = defineStore('task', {
  state: () => ({
    taskList: [],
    total: 0,
    loading: false,
    currentTask: null,
    taskStats: {
      totalCount: 0,
      pendingCount: 0,
      processingCount: 0,
      successCount: 0,
      failedCount: 0
    }
  }),
  
  actions: {
    // 获取任务列表
    async fetchTaskList(params) {
      this.loading = true
      try {
        const { data } = await getTaskList(params)
        this.taskList = data.records
        this.total = data.total
        return data
      } catch (error) {
        console.error('获取任务列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 获取任务详情
    async fetchTaskDetail(taskId) {
      try {
        const { data } = await getTaskDetail(taskId)
        this.currentTask = data
        return data
      } catch (error) {
        console.error('获取任务详情失败:', error)
        throw error
      }
    },
    
    // 创建任务
    async createNewTask(taskData) {
      try {
        const { data } = await createTask(taskData)
        return data
      } catch (error) {
        console.error('创建任务失败:', error)
        throw error
      }
    },
    
    // 重试任务
    async retryFailedTask(taskId) {
      try {
        const { data } = await retryTask(taskId)
        return data
      } catch (error) {
        console.error('重试任务失败:', error)
        throw error
      }
    },
    
    // 批量删除任务
    async batchDeleteTasks(taskIds) {
      try {
        await deleteTaskBatch(taskIds)
        // 删除成功后更新列表
        this.taskList = this.taskList.filter(task => !taskIds.includes(task.id))
      } catch (error) {
        console.error('批量删除任务失败:', error)
        throw error
      }
    },
    
    // 获取任务统计数据
    async fetchTaskStats() {
      try {
        const { data } = await getTaskStats()
        this.taskStats = data
        return data
      } catch (error) {
        console.error('获取任务统计数据失败:', error)
        throw error
      }
    },
    
    // 清除当前任务
    clearCurrentTask() {
      this.currentTask = null
    }
  }
})