<template>
  <div class="minio-container">
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>MinIO状态</span>
          <el-button type="primary" size="small" @click="checkHealth" :loading="healthLoading">
            刷新状态
          </el-button>
        </div>
      </template>
      
      <div class="status-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">连接状态</div>
              <div class="status-value" :class="healthData.connected ? 'success' : 'error'">
                <el-icon><Connection /></el-icon>
                {{ healthData.connected ? '已连接' : '未连接' }}
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">服务地址</div>
              <div class="status-value">{{ healthData.endpoint || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">存储桶</div>
              <div class="status-value" :class="healthData.bucketExists ? 'success' : 'warning'">
                <el-icon><Folder /></el-icon>
                {{ healthData.bucketName || '-' }}
                <el-tag v-if="healthData.bucketExists" type="success" size="small">存在</el-tag>
                <el-tag v-else type="warning" size="small">不存在</el-tag>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">文件数量</div>
              <div class="status-value">
                <el-icon><Document /></el-icon>
                {{ healthData.fileCount || 0 }} 个文件
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div v-if="healthData.error" class="error-message">
          <el-alert :title="healthData.error" type="error" show-icon />
        </div>
      </div>
    </el-card>

    <el-card class="action-card">
      <template #header>
        <div class="card-header">
          <span>操作面板</span>
        </div>
      </template>
      
      <div class="action-content">
        <el-space wrap>
          <el-button 
            type="primary" 
            @click="createBucket" 
            :loading="createLoading"
            :disabled="healthData.bucketExists"
          >
            <el-icon><Plus /></el-icon>
            创建存储桶
          </el-button>
          
          <el-button type="info" @click="openMinioConsole">
            <el-icon><Link /></el-icon>
            打开MinIO控制台
          </el-button>
          
          <el-button type="success" @click="loadFiles" :loading="filesLoading">
            <el-icon><Refresh /></el-icon>
            刷新文件列表
          </el-button>
          
          <el-button type="warning" @click="testUpload">
            <el-icon><Upload /></el-icon>
            测试上传
          </el-button>
        </el-space>
      </div>
    </el-card>

    <el-card class="files-card">
      <template #header>
        <div class="card-header">
          <span>文件列表</span>
          <el-tag type="info">{{ files.length }} 个文件</el-tag>
        </div>
      </template>
      
      <el-table :data="files" v-loading="filesLoading" empty-text="暂无文件">
        <el-table-column prop="objectName" label="文件名" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.objectName" placement="top">
              <span class="file-name">{{ row.objectName }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastModified" label="修改时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.lastModified) }}
          </template>
        </el-table-column>
        <el-table-column prop="etag" label="ETag" width="120">
          <template #default="{ row }">
            <el-tooltip :content="row.etag" placement="top">
              <span class="etag">{{ row.etag?.substring(0, 8) }}...</span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 测试上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="测试文件上传" width="500px">
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        drag
        multiple
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持任意格式文件，用于测试MinIO上传功能
          </div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Connection, 
  Folder, 
  Document, 
  Plus, 
  Link, 
  Refresh, 
  Upload,
  UploadFilled 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const healthLoading = ref(false)
const createLoading = ref(false)
const filesLoading = ref(false)
const uploadDialogVisible = ref(false)
const uploadRef = ref(null)

const healthData = reactive({
  connected: false,
  endpoint: '',
  bucketName: '',
  bucketExists: false,
  fileCount: 0,
  error: ''
})

const files = ref([])

const uploadUrl = '/api/file/upload'
const uploadHeaders = {
  'Authorization': `Bearer ${localStorage.getItem('token')}`
}

// 检查MinIO健康状态
const checkHealth = async () => {
  healthLoading.value = true
  try {
    const response = await request.get('/api/minio/health')
    Object.assign(healthData, response.data)
    healthData.error = ''
    
    if (healthData.connected) {
      ElMessage.success('MinIO连接正常')
    } else {
      ElMessage.warning('MinIO连接异常')
    }
  } catch (error) {
    healthData.connected = false
    healthData.error = error.message || '连接失败'
    ElMessage.error('检查MinIO状态失败: ' + error.message)
  } finally {
    healthLoading.value = false
  }
}

// 创建存储桶
const createBucket = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要创建MinIO存储桶吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    createLoading.value = true
    const response = await request.post('/api/minio/create-bucket')
    ElMessage.success(response.data)
    
    // 刷新状态
    await checkHealth()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('创建存储桶失败: ' + error.message)
    }
  } finally {
    createLoading.value = false
  }
}

// 打开MinIO控制台
const openMinioConsole = () => {
  const consoleUrl = healthData.endpoint?.replace(':9000', ':9001') || 'http://localhost:9001'
  window.open(consoleUrl, '_blank')
}

// 加载文件列表
const loadFiles = async () => {
  filesLoading.value = true
  try {
    const response = await request.get('/api/minio/files')
    files.value = response.data || []
    ElMessage.success(`加载了 ${files.value.length} 个文件`)
  } catch (error) {
    ElMessage.error('加载文件列表失败: ' + error.message)
    files.value = []
  } finally {
    filesLoading.value = false
  }
}

// 测试上传
const testUpload = () => {
  uploadDialogVisible.value = true
}

// 上传前检查
const beforeUpload = (file) => {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response, file) => {
  ElMessage.success(`文件 ${file.name} 上传成功`)
  loadFiles() // 刷新文件列表
}

// 上传失败
const handleUploadError = (error, file) => {
  ElMessage.error(`文件 ${file.name} 上传失败: ${error.message}`)
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  checkHealth()
  loadFiles()
})
</script>

<style lang="scss" scoped>
.minio-container {
  padding: 20px;
}

.status-card,
.action-card,
.files-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.status-content {
  .status-item {
    text-align: center;
    padding: 20px 0;
    
    .status-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .status-value {
      font-size: 16px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      &.success {
        color: #67c23a;
      }
      
      &.warning {
        color: #e6a23c;
      }
      
      &.error {
        color: #f56c6c;
      }
    }
  }
  
  .error-message {
    margin-top: 20px;
  }
}

.action-content {
  padding: 10px 0;
}

.file-name {
  font-family: monospace;
  font-size: 13px;
}

.etag {
  font-family: monospace;
  font-size: 12px;
  color: #909399;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>