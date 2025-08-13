<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">总任务数</div>
              <div class="stat-value">{{ taskStats.totalCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-card-content">
            <div class="stat-icon">
              <el-icon><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">成功任务</div>
              <div class="stat-value">{{ taskStats.successCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-card-content">
            <div class="stat-icon">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">处理中任务</div>
              <div class="stat-value">{{ taskStats.processingCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-card-content">
            <div class="stat-icon">
              <el-icon><CircleCloseFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">失败任务</div>
              <div class="stat-value">{{ taskStats.failedCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 成功率统计 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>任务成功率</span>
            </div>
          </template>
          <div class="success-rate">
            <div class="rate-value">{{ successRate }}%</div>
            <div class="rate-desc">总体成功率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>应用类型分布</span>
            </div>
          </template>
          <div class="app-type-stats">
            <div class="type-item">
              <span class="type-label">iOS:</span>
              <span class="type-count">{{ appTypeStats.IOS || 0 }}</span>
            </div>
            <div class="type-item">
              <span class="type-label">Android:</span>
              <span class="type-count">{{ appTypeStats.ANDROID || 0 }}</span>
            </div>
            <div class="type-item">
              <span class="type-label">HarmonyOS:</span>
              <span class="type-count">{{ appTypeStats.HARMONY || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近任务列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>最近任务</span>
          <el-button type="primary" link @click="goToTaskList">查看更多</el-button>
        </div>
      </template>
      <el-table :data="recentTasks" style="width: 100%">
        <el-table-column prop="taskId" label="任务ID" width="220" show-overflow-tooltip />
        <el-table-column prop="appType" label="应用类型" width="100">
          <template #default="{row}">
            <el-tag
              :type="row.appType === 'IOS' ? 'primary' : (row.appType === 'ANDROID' ? 'success' : 'warning')"
            >
              {{ row.appType === 'IOS' ? 'iOS' : (row.appType === 'ANDROID' ? 'Android' : 'HarmonyOS') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag
              :type="row.status === 'SUCCESS' ? 'success' : (row.status === 'FAILED' ? 'danger' : (row.status === 'PROCESSING' ? 'warning' : 'info'))"
            >
              {{ row.status === 'PENDING' ? '等待中' : (row.status === 'PROCESSING' ? '处理中' : (row.status === 'SUCCESS' ? '成功' : '失败')) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{row}">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '@/store/task'
import { formatDateTime } from '@/utils/validate'

const router = useRouter()
const taskStore = useTaskStore()

// 任务统计数据
const taskStats = reactive({
  totalCount: 0,
  pendingCount: 0,
  processingCount: 0,
  successCount: 0,
  failedCount: 0
})

// 应用类型统计
const appTypeStats = reactive({
  IOS: 0,
  ANDROID: 0,
  HARMONY: 0
})

// 最近任务
const recentTasks = ref([])

// 计算成功率
const successRate = computed(() => {
  if (taskStats.totalCount === 0) return 0
  return Math.round((taskStats.successCount / taskStats.totalCount) * 100)
})

// 获取统计数据
const fetchStats = async () => {
  try {
    const data = await taskStore.fetchTaskStats()
    Object.assign(taskStats, data)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取最近任务
const fetchRecentTasks = async () => {
  try {
    const data = await taskStore.fetchTaskList({ current: 1, size: 10 })
    if (data && data.records) {
      recentTasks.value = data.records
      
      // 统计应用类型
      const typeCount = { IOS: 0, ANDROID: 0, HARMONY: 0 }
      data.records.forEach(task => {
        if (typeCount[task.appType] !== undefined) {
          typeCount[task.appType]++
        }
      })
      Object.assign(appTypeStats, typeCount)
    }
  } catch (error) {
    console.error('获取最近任务失败:', error)
  }
}

// 跳转到任务列表
const goToTaskList = () => {
  router.push('/task/list')
}

onMounted(() => {
  fetchStats()
  fetchRecentTasks()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-content {
  padding: 20px 0;
  text-align: center;
}
</style>