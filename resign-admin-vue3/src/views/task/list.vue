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
            <el-form-item label="任务ID">
              <el-input v-model="searchForm.taskId" placeholder="请输入任务ID" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="应用类型">
              <el-select v-model="searchForm.appType" placeholder="请选择应用类型" clearable style="width: 100%">
                <el-option label="iOS" value="IOS" />
                <el-option label="Android" value="ANDROID" />
                <el-option label="HarmonyOS" value="HARMONY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="任务状态">
              <el-select v-model="searchForm.status" placeholder="请选择任务状态" clearable style="width: 100%">
                <el-option label="等待中" value="PENDING" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAILED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="创建时间">
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
      <el-button type="primary" @click="handleCreate">创建任务</el-button>
      <el-button type="danger" :disabled="selectedTasks.length === 0" @click="handleBatchDelete">批量删除</el-button>
      <el-button type="success" @click="refreshList">刷新</el-button>
    </div>
    
    <!-- 表格 -->
    <el-table
      v-loading="taskStore.loading"
      :data="taskStore.taskList"
      border
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
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
      <el-table-column prop="originalPackageUrl" label="原始包" show-overflow-tooltip />
      <el-table-column prop="resignedPackageUrl" label="重签名包" show-overflow-tooltip>
        <template #default="{row}">
          <span v-if="row.resignedPackageUrl">{{ row.resignedPackageUrl }}</span>
          <span v-else class="text-muted">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{row}">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{row}">
          <el-button
            v-if="row.status === 'FAILED'"
            type="primary"
            size="small"
            @click="handleRetry(row)"
          >
            重试
          </el-button>
          <el-button
            type="primary"
            size="small"
            link
            @click="handleDetail(row)"
          >
            详情
          </el-button>
          <el-button
            type="danger"
            size="small"
            link
            @click="handleDelete(row)"
          >
            删除
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
        :total="taskStore.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useTaskStore } from '@/store/task'
import { formatDateTime } from '@/utils/validate'

const router = useRouter()
const taskStore = useTaskStore()

// 搜索表单
const searchForm = reactive({
  taskId: '',
  appType: '',
  status: ''
})

// 日期范围
const dateRange = ref([])

// 分页参数
const pagination = reactive({
  current: 1,
  size: 10
})

// 选中的任务
const selectedTasks = ref([])

// 获取任务列表
const fetchTaskList = async () => {
  const params = {
    current: pagination.current,
    size: pagination.size,
    ...searchForm
  }
  
  // 处理日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0] + ' 00:00:00'
    params.endTime = dateRange.value[1] + ' 23:59:59'
  }
  
  await taskStore.fetchTaskList(params)
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchTaskList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  dateRange.value = []
  pagination.current = 1
  fetchTaskList()
}

// 刷新列表
const refreshList = () => {
  fetchTaskList()
}

// 创建任务
const handleCreate = () => {
  router.push('/task/create')
}

// 查看详情
const handleDetail = (row) => {
  router.push(`/task/detail/${row.taskId}`)
}

// 重试任务
const handleRetry = async (row) => {
  try {
    await taskStore.retryFailedTask(row.taskId)
    ElMessage.success('任务重试已提交')
    fetchTaskList()
  } catch (error) {
    console.error('重试任务失败:', error)
  }
}

// 删除任务
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await taskStore.batchDeleteTasks([row.taskId])
      ElMessage.success('删除成功')
      fetchTaskList()
    } catch (error) {
      console.error('删除任务失败:', error)
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请选择要删除的任务')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedTasks.value.length} 个任务吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const taskIds = selectedTasks.value.map(task => task.taskId)
      await taskStore.batchDeleteTasks(taskIds)
      ElMessage.success('批量删除成功')
      fetchTaskList()
    } catch (error) {
      console.error('批量删除任务失败:', error)
    }
  }).catch(() => {})
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

// 分页大小变化
const handleSizeChange = (val) => {
  pagination.size = val
  fetchTaskList()
}

// 当前页变化
const handleCurrentChange = (val) => {
  pagination.current = val
  fetchTaskList()
}

// 初始化
onMounted(() => {
  fetchTaskList()
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

.table-operations {
  margin: 15px 0;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
}
</style>