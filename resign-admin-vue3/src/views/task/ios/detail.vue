<template>
  <div class="task-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>iOS任务详情</span>
          <el-button type="primary" @click="goBack">返回列表</el-button>
        </div>
      </template>
      
      <div v-loading="loading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">
            {{ taskDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="应用名称">
            {{ taskDetail.appName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="应用版本">
            {{ taskDetail.appVersion || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="构建版本">
            {{ taskDetail.buildVersion || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(taskDetail.status)">{{ getStatusText(taskDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="证书ID">
            {{ taskDetail.certificateId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="Bundle ID配置">
            <div v-if="taskDetail.bundleIdConfig">
              <div v-for="(profileId, bundleId) in JSON.parse(taskDetail.bundleIdConfig || '{}')" :key="bundleId" class="bundle-config">
                <span class="bundle-id">{{ bundleId }}</span>: {{ profileId }}
              </div>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="回调地址">
            {{ taskDetail.callbackUrl || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ taskDetail.createTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="完成时间">
            {{ taskDetail.finishTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="下载地址" :span="2">
            <el-link v-if="taskDetail.downloadUrl" :href="taskDetail.downloadUrl" type="primary" target="_blank">
              {{ taskDetail.downloadUrl }}
            </el-link>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="2">
            {{ taskDetail.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 操作按钮 -->
        <div class="action-buttons" v-if="taskDetail.status === 'FAILED'">
          <el-button type="warning" @click="handleRetry" :loading="retryLoading">
            重试任务
          </el-button>
        </div>
        
        <!-- 处理日志 -->
        <el-card class="log-card" v-if="taskDetail.processLog">
          <template #header>
            <span>处理日志</span>
          </template>
          <pre class="log-content">{{ taskDetail.processLog }}</pre>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as iosTaskApi from '@/api/iosTask'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const retryLoading = ref(false)
const taskDetail = ref({})

// 获取任务详情
const fetchTaskDetail = async () => {
  loading.value = true
  try {
    const response = await iosTaskApi.getTaskDetail(route.params.id)
    if (response.code === 200 && response.data) {
      taskDetail.value = response.data
    } else {
      ElMessage.error('获取任务详情失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 重试任务
const handleRetry = async () => {
  retryLoading.value = true
  try {
    const response = await iosTaskApi.retryTask(taskDetail.value.id)
    if (response.code === 200) {
      ElMessage.success('任务重试成功')
      fetchTaskDetail() // 重新获取任务详情
    } else {
      ElMessage.error('任务重试失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('任务重试失败:', error)
    ElMessage.error('任务重试失败')
  } finally {
    retryLoading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/task/ios')
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'info',
    'PROCESSING': 'warning',
    'SUCCESS': 'success',
    'FAILED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '等待中',
    'PROCESSING': '处理中',
    'SUCCESS': '成功',
    'FAILED': '失败'
  }
  return statusMap[status] || status
}

onMounted(() => {
  fetchTaskDetail()
})
</script>

<style scoped>
.task-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bundle-config {
  margin-bottom: 4px;
}

.bundle-id {
  font-weight: bold;
  color: #409eff;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}

.log-card {
  margin-top: 20px;
}

.log-content {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>