<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon">
              <el-icon>
                <Document />
              </el-icon>
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
              <el-icon>
                <SuccessFilled />
              </el-icon>
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
              <el-icon>
                <Loading />
              </el-icon>
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
              <el-icon>
                <CircleCloseFilled />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">失败任务</div>
              <div class="stat-value">{{ taskStats.failedCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>任务类型分布</span>
            </div>
          </template>
          <div class="chart-container" ref="pieChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近7天任务趋势</span>
            </div>
          </template>
          <div class="chart-container" ref="lineChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近任务列表 -->
    <el-card class="recent-tasks">
      <template #header>
        <div class="card-header">
          <span>最近任务</span>
          <el-button type="primary" link @click="goToTaskList">查看更多</el-button>
        </div>
      </template>
      <el-table :data="recentTasks" style="width: 100%">
        <el-table-column prop="taskId" label="任务ID" width="220" show-overflow-tooltip />
        <el-table-column prop="appType" label="应用类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.appType === 'IOS' ? 'primary' : (row.appType === 'ANDROID' ? 'success' : 'warning')">
              {{ row.appType === 'IOS' ? 'iOS' : (row.appType === 'ANDROID' ? 'Android' : 'HarmonyOS') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'SUCCESS' ? 'success' : (row.status === 'FAILED' ? 'danger' : (row.status === 'PROCESSING' ? 'warning' : 'info'))">
              {{ row.status === 'PENDING' ? '等待中' : (row.status === 'PROCESSING' ? '处理中' : (row.status === 'SUCCESS' ?
                '成功' : '失败')) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="goToTaskDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '@/store/task'
import { formatDateTime } from '@/utils/validate'
import * as echarts from 'echarts/core'
import { PieChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必须的组件
echarts.use([
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
])

const router = useRouter()
const taskStore = useTaskStore()

// 图表引用
const pieChartRef = ref(null)
const lineChartRef = ref(null)

// 图表实例
let pieChart = null
let lineChart = null

// 任务统计数据
const taskStats = reactive({
  totalCount: 0,
  pendingCount: 0,
  processingCount: 0,
  successCount: 0,
  failedCount: 0
})

// 最近任务
const recentTasks = ref([])

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: ['iOS', 'Android', 'HarmonyOS']
    },
    series: [
      {
        name: '任务类型',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 40, name: 'iOS' },
          { value: 35, name: 'Android' },
          { value: 25, name: 'HarmonyOS' }
        ]
      }
    ]
  }

  pieChart.setOption(option)
}

// 初始化折线图
const initLineChart = () => {
  if (!lineChartRef.value) return

  lineChart = echarts.init(lineChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['总任务', '成功', '失败']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: getLast7Days()
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '总任务',
        type: 'line',
        stack: 'Total',
        data: [12, 15, 18, 20, 22, 25, 30]
      },
      {
        name: '成功',
        type: 'line',
        stack: 'Total',
        data: [10, 12, 15, 16, 18, 20, 25]
      },
      {
        name: '失败',
        type: 'line',
        stack: 'Total',
        data: [2, 3, 3, 4, 4, 5, 5]
      }
    ]
  }

  lineChart.setOption(option)
}

// 获取最近7天的日期
const getLast7Days = () => {
  const result = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    result.push(formatDateTime(date, 'MM-DD'))
  }
  return result
}

// 获取任务统计数据
const fetchTaskStats = async () => {
  try {
    const data = await taskStore.fetchTaskStats()
    if (data) {
      Object.assign(taskStats, data)
    }
  } catch (error) {
    console.error('获取任务统计数据失败:', error)
  }
}

// 获取最近任务
const fetchRecentTasks = async () => {
  try {
    const data = await taskStore.fetchTaskList({ current: 1, size: 5 })
    if (data && data.records) {
      recentTasks.value = data.records
    } else {
      recentTasks.value = []
    }
  } catch (error) {
    console.error('获取最近任务失败:', error)
    recentTasks.value = []
  }
}

// 跳转到任务列表
const goToTaskList = () => {
  router.push('/task/list')
}

// 跳转到用户管理
const goToUserManagement = () => {
  router.push('/user/list')
}

// 跳转到任务详情
const goToTaskDetail = (row) => {
  router.push(`/task/detail/${row.taskId}`)
}

// 窗口大小变化时重新调整图表大小
const handleResize = () => {
  pieChart && pieChart.resize()
  lineChart && lineChart.resize()
}

onMounted(() => {
  // 获取数据
  fetchTaskStats()
  fetchRecentTasks()

  // 初始化图表
  setTimeout(() => {
    initPieChart()
    initLineChart()
  }, 0)

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  pieChart && pieChart.dispose()
  lineChart && lineChart.dispose()

  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  height: 120px;

  &.success {
    .stat-icon {
      background-color: rgba(103, 194, 58, 0.2);
      color: #67c23a;
    }
  }

  &.warning {
    .stat-icon {
      background-color: rgba(230, 162, 60, 0.2);
      color: #e6a23c;
    }
  }

  &.danger {
    .stat-icon {
      background-color: rgba(245, 108, 108, 0.2);
      color: #f56c6c;
    }
  }

  .stat-card-content {
    display: flex;
    align-items: center;
    height: 100%;
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: rgba(64, 158, 255, 0.2);
    color: #409eff;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 30px;
    margin-right: 20px;
  }

  .stat-info {
    flex: 1;
  }

  .stat-title {
    font-size: 16px;
    color: #909399;
    margin-bottom: 10px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
  }
}

.chart-row {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
}

.recent-tasks {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>