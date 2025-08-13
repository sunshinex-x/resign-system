<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>任务详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <div v-if="taskDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID" :span="2">{{ taskDetail.taskId }}</el-descriptions-item>
          <el-descriptions-item label="应用类型">
            <el-tag
              :type="taskDetail.appType === 'IOS' ? 'primary' : (taskDetail.appType === 'ANDROID' ? 'success' : 'warning')"
            >
              {{ taskDetail.appType === 'IOS' ? 'iOS' : (taskDetail.appType === 'ANDROID' ? 'Android' : 'HarmonyOS') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag
              :type="taskDetail.status === 'SUCCESS' ? 'success' : (taskDetail.status === 'FAILED' ? 'danger' : (taskDetail.status === 'PROCESSING' ? 'warning' : 'info'))"
            >
              {{ taskDetail.status === 'PENDING' ? '等待中' : (taskDetail.status === 'PROCESSING' ? '处理中' : (taskDetail.status === 'SUCCESS' ? '成功' : '失败')) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="原始包地址" :span="2">
            <el-link type="primary" :href="taskDetail.originalPackageUrl" target="_blank" :underline="false">
              {{ taskDetail.originalPackageUrl }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="重签名包地址" :span="2">
            <el-link v-if="taskDetail.resignedPackageUrl" type="success" :href="taskDetail.resignedPackageUrl" target="_blank" :underline="false">
              {{ taskDetail.resignedPackageUrl }}
            </el-link>
            <span v-else class="text-muted">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="证书ID">{{ taskDetail.certificateId }}</el-descriptions-item>
          <el-descriptions-item label="回调地址">{{ taskDetail.callbackUrl || '无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(taskDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(taskDetail.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="描述信息" :span="2">{{ taskDetail.description || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">处理日志</el-divider>
        
        <el-card shadow="never" class="log-card">
          <pre v-if="taskDetail.processLog" class="process-log">{{ taskDetail.processLog }}</pre>
          <div v-else class="text-muted">暂无处理日志</div>
        </el-card>
        
        <div class="action-buttons" v-if="taskDetail.status === 'FAILED'">
          <el-button type="primary" @click="handleRetry">重试任务</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTaskStore } from '@/store/task'
import { formatDateTime } from '@/utils/validate'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()

const taskId = route.params.id
const taskDetail = ref(null)
const loading = ref(false)

// 获取任务详情
const fetchTaskDetail = async () => {
  loading.value = true
  try {
    const data = await taskStore.fetchTaskDetail(taskId)
    taskDetail.value = data
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 重试任务
const handleRetry = async () => {
  try {
    await taskStore.retryFailedTask(taskId)
    ElMessage.success('任务重试已提交')
    fetchTaskDetail() // 刷新详情
  } catch (error) {
    console.error('重试任务失败:', error)
  }
}

// 返回列表页
const goBack = () => {
  router.push('/task/list')
}

onMounted(() => {
  fetchTaskDetail()
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

.log-card {
  margin-top: 10px;
  background-color: #f5f7fa;
}

.process-log {
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.5;
  padding: 10px;
  margin: 0;
  max-height: 400px;
  overflow-y: auto;
}

.text-muted {
  color: #909399;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>