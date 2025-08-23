<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="日志级别">
              <el-select v-model="searchForm.level" placeholder="请选择日志级别" clearable style="width: 100%">
                <el-option label="INFO" value="INFO" />
                <el-option label="WARN" value="WARN" />
                <el-option label="ERROR" value="ERROR" />
                <el-option label="DEBUG" value="DEBUG" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
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
          <el-col :span="6">
            <el-form-item label=" " class="search-buttons">
              <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
              <el-button @click="resetSearch" icon="Refresh">重置</el-button>
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
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="logList"
        stripe
        class="log-table"
        max-height="600"
      >
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{row}">
            <el-tag
              :type="row.level === 'ERROR' ? 'danger' : (row.level === 'WARN' ? 'warning' : (row.level === 'INFO' ? 'success' : 'info'))"
            >
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logger" label="记录器" min-width="200" show-overflow-tooltip />
        <el-table-column prop="message" label="消息" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{row}">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{row}">
            <div class="table-actions">
              <el-button
                type="primary"
                size="small"
                @click="showLogDetail(row)"
              >
                详情
              </el-button>
            </div>
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
    </el-card>
    
    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="70%">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="时间">{{ formatDateTime(currentLog.createTime) }}</el-descriptions-item>
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
import { getLogList, clearLogs as clearLogsApi } from '@/api/log'

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



// 获取日志列表
const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      level: searchForm.level,
      keyword: searchForm.keyword
    }
    
    // 添加日期范围参数
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const response = await getLogList(params)
    console.log('日志API响应:', response)
    
    // 确保数据格式正确
    let data = response.data
    if (response.code === 200 && response.data) {
      data = response.data
    }
    
    // 验证数据结构
    if (data && typeof data === 'object') {
      logList.value = Array.isArray(data.records) ? data.records : (Array.isArray(data.list) ? data.list : [])
      total.value = data.total || data.count || 0
    } else {
      console.warn('日志API返回的数据格式不正确:', data)
      logList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取日志列表失败:', error)
    ElMessage.error('获取日志列表失败')
    logList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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
  }).then(async () => {
    try {
      await clearLogsApi()
      ElMessage.success('日志已清空')
      fetchLogs() // 重新加载日志列表
    } catch (error) {
      console.error('清空日志失败:', error)
      ElMessage.error('清空日志失败')
    }
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

.search-card {
  margin-bottom: 20px;
  
  .search-form {
    .el-form-item {
      margin-bottom: 0;
    }
    
    .search-buttons {
      .el-form-item__content {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        
        .el-button {
          margin-left: 12px;
          
          &:first-child {
            margin-left: 0;
          }
        }
      }
    }
  }
}

.table-card {
  .log-table {
    width: 100%;
    
    .el-table__header {
      th {
        background: #fafbfc !important;
        color: #374151;
        font-weight: 600;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
  }
}

.log-content {
  padding: 20px 0;
  text-align: center;
}
</style>