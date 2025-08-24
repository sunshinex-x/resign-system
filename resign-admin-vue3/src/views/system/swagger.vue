<template>
  <div class="swagger-container">    
    <div class="swagger-content">
      <div class="swagger-toolbar">
        <el-button type="primary" @click="openInNewTab">
          <el-icon><Link /></el-icon>
          在新窗口打开
        </el-button>
        <el-button @click="refreshDoc">
          <el-icon><Refresh /></el-icon>
          刷新文档
        </el-button>
      </div>
      
      <div class="swagger-iframe-container">
        <iframe 
          ref="swaggerFrame"
          :src="swaggerUrl" 
          frameborder="0"
          width="100%"
          height="100%"
          @load="onIframeLoad"
        >
        </iframe>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-overlay">
          <el-loading 
            element-loading-text="正在加载API文档..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.8)"
          />
        </div>
        
        <!-- 错误状态 -->
        <div v-if="error" class="error-overlay">
          <el-result
            icon="error"
            title="加载失败"
            :sub-title="errorMessage"
          >
            <template #extra>
              <el-button type="primary" @click="refreshDoc">重新加载</el-button>
            </template>
          </el-result>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Link, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const swaggerFrame = ref(null)

// Swagger UI URL
const swaggerUrl = computed(() => {
  // 获取当前后端API地址
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return `${baseUrl}/swagger-ui/index.html`
})

// iframe加载完成
const onIframeLoad = () => {
  loading.value = false
  error.value = false
  
  // 检查iframe是否加载成功
  try {
    const iframe = swaggerFrame.value
    if (iframe && iframe.contentWindow) {
      // 监听iframe内的错误
      iframe.contentWindow.addEventListener('error', () => {
        handleLoadError('API文档加载失败，请检查后端服务是否正常运行')
      })
    }
  } catch (e) {
    console.warn('无法访问iframe内容，可能是跨域限制')
  }
}

// 处理加载错误
const handleLoadError = (message) => {
  loading.value = false
  error.value = true
  errorMessage.value = message
}

// 在新窗口打开
const openInNewTab = () => {
  window.open(swaggerUrl.value, '_blank')
}

// 刷新文档
const refreshDoc = () => {
  loading.value = true
  error.value = false
  
  if (swaggerFrame.value) {
    swaggerFrame.value.src = swaggerUrl.value
  }
  
  ElMessage.success('正在刷新API文档...')
}

// 组件挂载时的处理
onMounted(() => {
  // 设置iframe错误处理
  if (swaggerFrame.value) {
    swaggerFrame.value.addEventListener('error', () => {
      handleLoadError('无法连接到API文档服务')
    })
  }
  
  // 设置超时检查
  setTimeout(() => {
    if (loading.value) {
      handleLoadError('API文档加载超时，请检查网络连接')
    }
  }, 10000) // 10秒超时
})
</script>

<style lang="scss" scoped>
.swagger-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.swagger-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.swagger-toolbar {
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
  display: flex;
  gap: 12px;
  
  .el-button {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.swagger-iframe-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  
  iframe {
    border: none;
    background: #fff;
  }
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  
  .el-result {
    padding: 40px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .swagger-header {
    padding: 16px 20px;
    
    h2 {
      font-size: 18px;
    }
  }
  
  .swagger-toolbar {
    padding: 12px 20px;
    flex-wrap: wrap;
  }
}
</style>