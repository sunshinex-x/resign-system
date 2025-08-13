<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>搜索条件</span>
          <div>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="searchForm" label-width="100px" class="compact-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="日志级别">
              <el-select v-model="searchForm.level" placeholder="请选择日志级别" clearable style="width: 100%">
                <el-option label="INFO" value="INFO" />
                <el-option label="WARN" value="WARN" />
                <el-option label="ERROR" value="ERROR" />
                <el-option label="DEBUG" value="DEBUG" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 操作按钮 -->
    <div class="table-operations">
      <el-button type="success" @click="refreshLogs">刷新</el-button>
      <el-button type="danger" @click="clearLogs">清空日志</el-button>
    </div>
    
    <!-- 日志表格 -->
    <el-table
      v-loading="loading"
      :data="logList"
      border
      stripe
      style="width: 100%"
      max-height="600"
    >
      <el-table-column prop="timestamp" label="时间" width="180">
        <template #default="{row}">
          {{ formatDateTime(row.timestamp) }}
        </template>
      </el-table-column>
      <el-table-column prop="level" label="级别" width="80">
        <template #default="{row}">
          <el-tag
            :type="row.level === 'ERROR' ? 'danger' : (row.level === 'WARN' ? 'warning' : (row.level === 'INFO' ? 'success' : 'info'))"
          >
            {{ row.level }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="logger" label="记录器" width="200" show-overflow-tooltip />
      <el-table-column prop="message" label="消息" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{row}">
          <el-button
            type="primary"
            size="small"
            link
            @click="showLogDetail(row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="70%">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="时间">{{ formatDateTime(currentLog.timestamp) }}</el-descriptions-item>
        <el-descriptions-item label="级别">
          <el-tag
            :type="currentLog.level === 'ERROR' ? 'danger' : (currentLog.level === 'WARN' ? 'warning' : (currentLog.level === 'INFO' ? 'success' : 'info'))"
          >
            {{ currentLog.level }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="记录器">{{ currentLog.logger }}</el-descriptions-item>
        <el-descriptions-item label="消息">{{ currentLog.message }}</el-descriptions-item>
        <el-descriptions-item label="异常堆栈" v-if="currentLog.exception">
          <pre class="exception-stack">{{ currentLog.exception }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/validate'

// 搜索表单
const searchForm = reactive({
  level: '',
  keyword: ''
})

// 日期范围
const dateRange = ref([])

// 分页参数
const pagination = reactive({
  current: 1,
  size: 20
})

// 数据
const logList = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const currentLog = ref(null)

// 模拟日志数据
const mockLogs = [
  {
    id: 1,
    timestamp: new Date(),
    level: 'INFO',
    logger: 'com.example.resign.service.ResignTaskService',
    message: '任务创建成功，任务ID: cb157501dace4139b24ff0793b82f318',
    exception: null
  },
  {
    id: 2,
    timestamp: new Date(Date.now() - 60000),
    level: 'WARN',
    logger: 'com.example.resign.mq.ResignTaskConsumer',
    message: '任务处理超时，将进行重试',
    exception: null
  },
  {
    id: 3,
    timestamp: new Date(Date.now() - 120000),
    level: 'ERROR',
    logger: 'com.example.resign.service.FileService',
    message: '文件下载失败',
    exception: 'java.io.IOException: Connection timeout\n\tat java.net.SocketInputStream.read(SocketInputStream.java:116)\n\tat java.io.BufferedInputStream.fill(BufferedInputStream.java:246)'
  }
]

// 获取日志列表
const fetchLogs = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    let filteredLogs = [...mockLogs]
    
    // 应用搜索条件
    if (searchForm.level) {
      filteredLogs = filteredLogs.filter(log => log.level === searchForm.level)
    }
    if (searchForm.keyword) {
      filteredLogs = filteredLogs.filter(log => 
        log.message.toLowerCase().includes(searchForm.keyword.toLowerCase()) ||
        log.logger.toLowerCase().includes(searchForm.keyword.toLowerCase())
      )
    }
    
    logList.value = filteredLogs
    total.value = filteredLogs.length
    loading.value = false
  }, 500)
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchLogs()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  dateRange.value = []
  pagination.current = 1
  fetchLogs()
}

// 刷新日志
const refreshLogs = () => {
  fetchLogs()
}

// 清空日志
const clearLogs = () => {
  ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    logList.value = []
    total.value = 0
    ElMessage.success('日志已清空')
  }).catch(() => {})
}

// 显示日志详情
const showLogDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

// 分页大小变化
const handleSizeChange = (val) => {
  pagination.size = val
  fetchLogs()
}

// 当前页变化
const handleCurrentChange = (val) => {
  pagination.current = val
  fetchLogs()
}

// 初始化
onMounted(() => {
  fetchLogs()
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

.log-content {
  padding: 20px 0;
  text-align: center;
}
</style>