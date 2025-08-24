<template>
  <div class="app-container">
    <!-- 重签名配置 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>重签名配置</span>
          <div class="header-buttons">
            <el-button type="success" @click="refreshResignConfig" icon="Refresh">刷新</el-button>
            <el-button type="primary" @click="saveResignConfig" icon="Check">保存配置</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="resignConfig" label-width="150px" class="config-form">
        <el-form-item label="临时文件目录">
          <el-input v-model="resignConfig.tempDir" placeholder="请输入临时文件存储目录" />
        </el-form-item>
        <el-form-item label="任务重试次数">
          <el-input-number v-model="resignConfig.retryCount" :min="0" :max="10" />
        </el-form-item>
        <el-form-item label="任务超时时间(秒)">
          <el-input-number v-model="resignConfig.timeoutSeconds" :min="60" :max="7200" />
        </el-form-item>
        <el-form-item label="iOS签名工具路径">
          <el-input v-model="resignConfig.iosSignTool" placeholder="请输入iOS签名工具路径" />
        </el-form-item>
        <el-form-item label="Android签名工具路径">
          <el-input v-model="resignConfig.androidSignTool" placeholder="请输入Android签名工具路径" />
        </el-form-item>

      </el-form>
    </el-card>
    
    <!-- MinIO配置 -->
    <el-card class="config-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>MinIO存储配置</span>
          <div class="header-buttons">
            <el-button type="success" @click="refreshMinioConfig" icon="Refresh">刷新</el-button>
            <el-button type="primary" @click="saveMinioConfig" icon="Check">保存配置</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="minioConfig" label-width="150px" class="config-form">
        <el-form-item label="服务端点">
          <el-input v-model="minioConfig.endpoint" placeholder="请输入MinIO服务端点" />
        </el-form-item>
        <el-form-item label="访问密钥">
          <el-input v-model="minioConfig.accessKey" placeholder="请输入访问密钥" />
        </el-form-item>
        <el-form-item label="秘密密钥">
          <el-input v-model="minioConfig.secretKey" type="password" placeholder="请输入秘密密钥" />
        </el-form-item>
        <el-form-item label="存储桶名称">
          <el-input v-model="minioConfig.bucketName" placeholder="请输入存储桶名称" />
        </el-form-item>
        <el-form-item label="连接测试">
          <el-button type="success" @click="testMinioConnection">测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- RabbitMQ配置 -->
    <el-card class="config-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>RabbitMQ消息队列配置</span>
          <div class="header-buttons">
            <el-button type="success" @click="refreshRabbitConfig" icon="Refresh">刷新</el-button>
            <el-button type="primary" @click="saveRabbitConfig" icon="Check">保存配置</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="rabbitConfig" label-width="150px" class="config-form">
        <el-form-item label="服务器地址">
          <el-input v-model="rabbitConfig.host" placeholder="请输入RabbitMQ服务器地址" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input-number v-model="rabbitConfig.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="rabbitConfig.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="rabbitConfig.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="虚拟主机">
          <el-input v-model="rabbitConfig.virtualHost" placeholder="请输入虚拟主机" />
        </el-form-item>
        <el-form-item label="连接测试">
          <el-button type="success" @click="testRabbitConnection">测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 系统信息 -->
    <el-card class="config-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>系统信息</span>
          <el-button type="success" @click="refreshSystemInfo">刷新</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应用版本">{{ systemInfo.version }}</el-descriptions-item>
        <el-descriptions-item label="Java版本">{{ systemInfo.javaVersion }}</el-descriptions-item>
        <el-descriptions-item label="启动时间">{{ systemInfo.startTime }}</el-descriptions-item>
        <el-descriptions-item label="当前时间">{{ systemInfo.currentTime }}</el-descriptions-item>
        <el-descriptions-item label="运行时长">{{ systemInfo.uptime }}</el-descriptions-item>
        <el-descriptions-item label="内存使用率">{{ systemInfo.memoryUsage }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ systemInfo.osName }} {{ systemInfo.osVersion }}</el-descriptions-item>
        <el-descriptions-item label="处理器核心数">{{ systemInfo.processors }}</el-descriptions-item>
        <el-descriptions-item label="JVM名称">{{ systemInfo.vmName }}</el-descriptions-item>
        <el-descriptions-item label="JVM版本">{{ systemInfo.vmVersion }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/validate'
import {
  getResignConfig,
  saveResignConfig as saveResignConfigApi,
  getMinioConfig,
  saveMinioConfig as saveMinioConfigApi,
  testMinioConnection as testMinioConnectionApi,
  getRabbitConfig,
  saveRabbitConfig as saveRabbitConfigApi,
  testRabbitConnection as testRabbitConnectionApi,
  getSystemInfo
} from '@/api/config'

// 重签名配置
const resignConfig = reactive({
  tempDir: '',
  retryCount: 3,
  timeoutSeconds: 1800,
  iosSignTool: '',
  androidSignTool: '',

})

// MinIO配置
const minioConfig = reactive({
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucketName: ''
})

// RabbitMQ配置
const rabbitConfig = reactive({
  host: '',
  port: 5672,
  username: '',
  password: '',
  virtualHost: '/'
})

// 系统信息
const systemInfo = reactive({
  version: '',
  javaVersion: '',
  startTime: null,
  uptime: '',
  memoryUsage: '',
  cpuUsage: '',
  diskUsage: '',
  activeThreads: 0,
  vmName: '',
  vmVersion: '',
  osName: '',
  osVersion: '',
  processors: 0,
  currentTime: ''
})

// 刷新重签名配置
const refreshResignConfig = async (showSuccessMessage = true) => {
  try {
    const response = await getResignConfig()
    const data = response.data
    
    // 处理后端返回的嵌套数据结构
    resignConfig.tempDir = data.tempDir || ''
    resignConfig.retryCount = data.task?.retryCount || 3
    resignConfig.timeoutSeconds = data.task?.timeoutSeconds || 1800
    resignConfig.iosSignTool = data.tools?.ios?.signTool || ''
    resignConfig.androidSignTool = data.tools?.android?.signTool || ''

    
    if (showSuccessMessage) {
      ElMessage.success('重签名配置刷新成功')
    }
  } catch (error) {
    console.error('获取重签名配置失败:', error)
    ElMessage.error('获取重签名配置失败')
  }
}

// 保存重签名配置
const saveResignConfig = async () => {
  try {
    await saveResignConfigApi(resignConfig)
    ElMessage.success('重签名配置保存成功')
  } catch (error) {
    console.error('保存重签名配置失败:', error)
    ElMessage.error('保存重签名配置失败')
  }
}

// 刷新MinIO配置
const refreshMinioConfig = async (showSuccessMessage = true) => {
  try {
    const response = await getMinioConfig()
    const data = response.data
    
    // 处理后端返回的数据结构
    minioConfig.endpoint = data.endpoint || ''
    minioConfig.accessKey = data.accessKey || ''
    minioConfig.secretKey = data.secretKey || '' // 出于安全考虑，后端可能不返回密钥
    minioConfig.bucketName = data.bucketName || ''
    
    if (showSuccessMessage) {
      ElMessage.success('MinIO配置刷新成功')
    }
  } catch (error) {
    console.error('获取MinIO配置失败:', error)
    ElMessage.error('获取MinIO配置失败')
  }
}

// 保存MinIO配置
const saveMinioConfig = async () => {
  try {
    await saveMinioConfigApi(minioConfig)
    ElMessage.success('MinIO配置保存成功')
  } catch (error) {
    console.error('保存MinIO配置失败:', error)
    ElMessage.error('保存MinIO配置失败')
  }
}

// 刷新RabbitMQ配置
const refreshRabbitConfig = async (showSuccessMessage = true) => {
  try {
    const response = await getRabbitConfig()
    const data = response.data
    
    // 处理后端返回的数据结构
    rabbitConfig.host = data.host || ''
    rabbitConfig.port = data.port || 5672
    rabbitConfig.username = data.username || ''
    rabbitConfig.password = data.password || '' // 出于安全考虑，后端可能不返回密码
    rabbitConfig.virtualHost = data.virtualHost || '/'
    
    if (showSuccessMessage) {
      ElMessage.success('RabbitMQ配置刷新成功')
    }
  } catch (error) {
    console.error('获取RabbitMQ配置失败:', error)
    ElMessage.error('获取RabbitMQ配置失败')
  }
}

// 保存RabbitMQ配置
const saveRabbitConfig = async () => {
  try {
    await saveRabbitConfigApi(rabbitConfig)
    ElMessage.success('RabbitMQ配置保存成功')
  } catch (error) {
    console.error('保存RabbitMQ配置失败:', error)
    ElMessage.error('保存RabbitMQ配置失败')
  }
}

// 测试MinIO连接
const testMinioConnection = async () => {
  try {
    await testMinioConnectionApi(minioConfig)
    ElMessage.success('MinIO连接测试成功')
  } catch (error) {
    console.error('MinIO连接测试失败:', error)
    ElMessage.error('MinIO连接测试失败')
  }
}

// 测试RabbitMQ连接
const testRabbitConnection = async () => {
  try {
    await testRabbitConnectionApi(rabbitConfig)
    ElMessage.success('RabbitMQ连接测试成功')
  } catch (error) {
    console.error('RabbitMQ连接测试失败:', error)
    ElMessage.error('RabbitMQ连接测试失败')
  }
}

// 刷新系统信息
const refreshSystemInfo = async (showSuccessMessage = true) => {
  try {
    const response = await getSystemInfo()
    const data = response.data
    
    // 处理后端返回的嵌套数据结构
    systemInfo.version = data.application?.version || ''
    systemInfo.javaVersion = data.java?.version || ''
    systemInfo.startTime = data.application?.startTime || null
    systemInfo.uptime = data.runtime?.uptime || ''
    systemInfo.memoryUsage = data.memory?.heapUsedPercent || ''
    systemInfo.cpuUsage = 'N/A' // 后端暂未提供CPU使用率
    systemInfo.diskUsage = 'N/A' // 后端暂未提供磁盘使用率
    systemInfo.activeThreads = 0 // 后端暂未提供活跃线程数
    systemInfo.vmName = data.runtime?.vmName || ''
    systemInfo.vmVersion = data.runtime?.vmVersion || ''
    systemInfo.osName = data.os?.name || ''
    systemInfo.osVersion = data.os?.version || ''
    systemInfo.processors = data.runtime?.processors || 0
    systemInfo.currentTime = data.application?.currentTime || ''
    
    if (showSuccessMessage) {
      ElMessage.success('系统信息已刷新')
    }
  } catch (error) {
    console.error('获取系统信息失败:', error)
    ElMessage.error('获取系统信息失败')
  }
}

// 初始化
onMounted(() => {
  // 加载所有配置，初始加载时不显示成功提示
  refreshResignConfig(false)
  refreshMinioConfig(false)
  refreshRabbitConfig(false)
  refreshSystemInfo(false)
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.config-card {
  width: 100%;
  max-width: 800px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-buttons {
      .el-button {
        margin-left: 12px;
      }
    }
  }
  
  .config-form {
    max-width: 600px;
    margin: 0 auto;
    
    .el-form-item {
      margin-bottom: 24px;
    }
  }
}

.config-content {
  padding: 20px 0;
  text-align: center;
}
</style>