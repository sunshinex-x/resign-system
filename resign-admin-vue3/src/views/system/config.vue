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
        <el-form-item label="HarmonyOS签名工具路径">
          <el-input v-model="resignConfig.harmonySignTool" placeholder="请输入HarmonyOS签名工具路径" />
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
        <el-descriptions-item label="系统版本">{{ systemInfo.version }}</el-descriptions-item>
        <el-descriptions-item label="Java版本">{{ systemInfo.javaVersion }}</el-descriptions-item>
        <el-descriptions-item label="启动时间">{{ formatDateTime(systemInfo.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="运行时长">{{ systemInfo.uptime }}</el-descriptions-item>
        <el-descriptions-item label="内存使用">{{ systemInfo.memoryUsage }}</el-descriptions-item>
        <el-descriptions-item label="CPU使用率">{{ systemInfo.cpuUsage }}</el-descriptions-item>
        <el-descriptions-item label="磁盘使用">{{ systemInfo.diskUsage }}</el-descriptions-item>
        <el-descriptions-item label="活跃线程数">{{ systemInfo.activeThreads }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/validate'

// 重签名配置
const resignConfig = reactive({
  tempDir: '/tmp/resign',
  retryCount: 3,
  timeoutSeconds: 1800,
  iosSignTool: '/usr/bin/codesign',
  androidSignTool: '/usr/local/bin/apksigner',
  harmonySignTool: '/usr/local/bin/hapsigntool'
})

// MinIO配置
const minioConfig = reactive({
  endpoint: 'http://localhost:9000',
  accessKey: 'minioadmin',
  secretKey: 'minioadmin',
  bucketName: 'resign-apps'
})

// RabbitMQ配置
const rabbitConfig = reactive({
  host: 'localhost',
  port: 5672,
  username: 'guest',
  password: 'guest',
  virtualHost: '/'
})

// 系统信息
const systemInfo = reactive({
  version: '1.0.0',
  javaVersion: 'Java 17.0.15',
  startTime: new Date(Date.now() - 3600000),
  uptime: '1小时23分钟',
  memoryUsage: '512MB / 2GB (25%)',
  cpuUsage: '15%',
  diskUsage: '45GB / 100GB (45%)',
  activeThreads: 25
})

// 刷新重签名配置
const refreshResignConfig = () => {
  // 模拟刷新配置
  ElMessage.success('重签名配置刷新成功')
}

// 保存重签名配置
const saveResignConfig = () => {
  // 模拟保存配置
  ElMessage.success('重签名配置保存成功')
}

// 刷新MinIO配置
const refreshMinioConfig = () => {
  // 模拟刷新配置
  ElMessage.success('MinIO配置刷新成功')
}

// 保存MinIO配置
const saveMinioConfig = () => {
  // 模拟保存配置
  ElMessage.success('MinIO配置保存成功')
}

// 刷新RabbitMQ配置
const refreshRabbitConfig = () => {
  // 模拟刷新配置
  ElMessage.success('RabbitMQ配置刷新成功')
}

// 保存RabbitMQ配置
const saveRabbitConfig = () => {
  // 模拟保存配置
  ElMessage.success('RabbitMQ配置保存成功')
}

// 测试MinIO连接
const testMinioConnection = () => {
  // 模拟测试连接
  ElMessage.success('MinIO连接测试成功')
}

// 测试RabbitMQ连接
const testRabbitConnection = () => {
  // 模拟测试连接
  ElMessage.success('RabbitMQ连接测试成功')
}

// 刷新系统信息
const refreshSystemInfo = () => {
  // 模拟刷新系统信息
  systemInfo.uptime = '1小时25分钟'
  systemInfo.memoryUsage = '520MB / 2GB (26%)'
  systemInfo.cpuUsage = '12%'
  ElMessage.success('系统信息已刷新')
}

// 初始化
onMounted(() => {
  // 加载配置
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