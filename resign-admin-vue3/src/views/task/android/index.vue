<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="5">
            <el-form-item label="任务ID">
              <el-input v-model="searchForm.taskId" placeholder="请输入任务ID" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="任务状态">
              <el-select v-model="searchForm.status" placeholder="请选择任务状态" clearable style="width: 100%">
                <el-option label="等待中" value="PENDING" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAILED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="应用名称">
              <el-input v-model="searchForm.appName" placeholder="请输入应用名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5">
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
          <el-col :span="4">
            <el-form-item label=" " class="search-buttons">
              <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
              <el-button @click="resetSearch" icon="Refresh">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="operation-card">
      <el-button type="primary" @click="showCreateDialog" icon="Plus">创建Android重签名任务</el-button>
      <el-button type="success" @click="processPendingTasks" icon="Refresh">处理待处理任务</el-button>
      <el-button type="warning" @click="showCleanupDialog" icon="Delete">清理过期任务</el-button>
    </el-card>

    <!-- 任务列表 -->
    <el-card>
      <DataTable
        v-loading="loading"
        :data="taskList"
        :columns="columns"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #status="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
        <template #processingTime="{ row }">
          {{ row.processingTime ? row.processingTime + 's' : '-' }}
        </template>
        <template #actions="{ row }">
          <el-button type="primary" size="small" @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="executeTask(row)">执行</el-button>
          <el-button v-if="row.status === 'FAILED'" type="warning" size="small" @click="retryTask(row)">重试</el-button>
          <el-button v-if="row.status === 'SUCCESS'" type="info" size="small" @click="downloadFile(row)">下载</el-button>
          <el-button type="danger" size="small" @click="deleteTask(row)">删除</el-button>
        </template>
      </DataTable>
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建Android重签名任务" width="600px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="120px">
        <!-- APK文件上传 -->
        <el-form-item label="APK文件" prop="apkFile">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept=".apk"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">将APK文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">只能上传.apk文件，且不超过200MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- APK信息展示 -->
        <el-card class="apk-info-card" v-if="apkInfo">
          <template #header>
            <span>APK文件信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="应用名称">{{ apkInfo.appName }}</el-descriptions-item>
            <el-descriptions-item label="包名">{{ apkInfo.packageName }}</el-descriptions-item>
            <el-descriptions-item label="版本名称">{{ apkInfo.versionName }}</el-descriptions-item>
            <el-descriptions-item label="版本号">{{ apkInfo.versionCode }}</el-descriptions-item>
            <el-descriptions-item label="文件大小">{{ formatFileSize(apkInfo.fileSize) }}</el-descriptions-item>
            <el-descriptions-item label="最小SDK版本">{{ apkInfo.minSdkVersion }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 证书选择 -->
        <el-form-item label="选择证书" prop="certificateId">
          <el-select v-model="createForm.certificateId" placeholder="请选择Android证书" style="width: 100%">
            <el-option
              v-for="cert in certificateList"
              :key="cert.id"
              :label="cert.name"
              :value="cert.id"
            >
              <span>{{ cert.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ cert.description }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 签名配置 -->
        <el-form-item label="签名版本">
          <el-radio-group v-model="createForm.signatureVersion">
            <el-radio label="v1">V1签名</el-radio>
            <el-radio label="v2">V2签名</el-radio>
            <el-radio label="v1+v2">V1+V2签名</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 可选配置 -->
        <el-form-item label="应用名称">
          <el-input v-model="createForm.appName" placeholder="留空则使用原应用名称" />
        </el-form-item>
        <el-form-item label="包名">
          <el-input v-model="createForm.packageName" placeholder="留空则使用原包名" />
        </el-form-item>
        <el-form-item label="版本名称">
          <el-input v-model="createForm.versionName" placeholder="留空则使用原版本名称" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input-number v-model="createForm.versionCode" :min="1" placeholder="留空则使用原版本号" style="width: 100%" />
        </el-form-item>
        <el-form-item label="回调URL">
          <el-input v-model="createForm.callbackUrl" placeholder="任务完成后的回调地址" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="createForm.description" type="textarea" placeholder="任务描述信息" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate" :loading="createLoading">创建任务</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 清理过期任务对话框 -->
    <el-dialog v-model="cleanupDialogVisible" title="清理过期任务" width="400px">
      <el-form :model="cleanupForm" label-width="120px">
        <el-form-item label="保留天数">
          <el-input-number v-model="cleanupForm.daysToKeep" :min="1" :max="365" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cleanupDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCleanup" :loading="cleanupLoading">确认清理</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import * as androidTaskApi from '@/api/androidTask'
import * as certificateApi from '@/api/certificate'
import DataTable from '@/components/DataTable.vue'
import { createListHandler } from '@/utils/listHandler'

const router = useRouter()

// 响应式数据
const { loading, list: taskList, total, pagination, listHandler } = createListHandler({
  pagination: {
    page: 1,
    size: 10
  },
  messages: {
    success: '获取任务列表成功',
    error: '获取任务列表失败'
  }
})

const searchForm = reactive({
  taskId: '',
  status: '',
  appName: ''
})
const dateRange = ref([])

// 创建任务相关
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref()
const uploadRef = ref()
const createForm = reactive({
  apkFile: null,
  certificateId: '',
  signatureVersion: 'v1+v2',
  appName: '',
  packageName: '',
  versionName: '',
  versionCode: null,
  callbackUrl: '',
  description: ''
})
const createRules = {
  apkFile: [{ required: true, message: '请上传APK文件', trigger: 'change' }],
  certificateId: [{ required: true, message: '请选择证书', trigger: 'change' }]
}

// APK解析信息
const apkInfo = ref(null)
const certificateList = ref([])

// 清理任务相关
const cleanupDialogVisible = ref(false)
const cleanupLoading = ref(false)
const cleanupForm = reactive({
  daysToKeep: 30
})

// 获取任务列表数据的函数
const fetchTaskList = async (params) => {
  const requestParams = {
    ...params,
    ...searchForm
  }
  if (dateRange.value && dateRange.value.length === 2) {
    requestParams.startDate = dateRange.value[0]
    requestParams.endDate = dateRange.value[1]
  }
  
  const response = await androidTaskApi.getTaskList(requestParams)
  console.log('Android Task API Response:', response)
  
  // 直接返回原始API响应，让listHandler处理数据结构
  return response
}

// 兼容原有调用的函数
const getTaskList = async () => {
  if (pagination.page === 1) {
    await listHandler.init(fetchTaskList)
  } else {
    await listHandler.refresh(fetchTaskList)
  }
}

// 搜索
const handleSearch = async () => {
  await listHandler.search(fetchTaskList)
}

// 重置搜索
const resetSearch = async () => {
  Object.assign(searchForm, {
    taskId: '',
    status: '',
    appName: ''
  })
  dateRange.value = []
  await listHandler.reset(fetchTaskList)
}

// 分页处理
const handleSizeChange = async (size) => {
  await listHandler.handleSizeChange(size, fetchTaskList)
}

const handleCurrentChange = async (page) => {
  await listHandler.handlePageChange(page, fetchTaskList)
}

// 显示创建对话框
const showCreateDialog = async () => {
  createDialogVisible.value = true
  resetCreateForm()
  await loadCertificateList()
}

// 重置创建表单
const resetCreateForm = () => {
  Object.assign(createForm, {
    apkFile: null,
    certificateId: '',
    signatureVersion: 'v1+v2',
    appName: '',
    packageName: '',
    versionName: '',
    versionCode: null,
    callbackUrl: '',
    description: ''
  })
  apkInfo.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 文件上传处理
const handleFileChange = async (file) => {
  createForm.apkFile = file.raw
  // 自动解析APK信息
  await parseApkInfo()
}

const beforeUpload = (file) => {
  const isApk = file.name.toLowerCase().endsWith('.apk')
  const isLt200M = file.size / 1024 / 1024 < 200

  if (!isApk) {
    ElMessage.error('只能上传.apk文件!')
    return false
  }
  if (!isLt200M) {
    ElMessage.error('上传文件大小不能超过200MB!')
    return false
  }
  return true
}

// 解析APK信息
const parseApkInfo = async () => {
  if (!createForm.apkFile) {
    return
  }

  try {
    const formData = new FormData()
    formData.append('apkFile', createForm.apkFile)
    
    const response = await androidTaskApi.parseApkInfo(formData)
    if (response.code === 200 && response.data) {
      apkInfo.value = response.data
      
      // 自动填充表单信息
      if (response.data.appName) {
        createForm.appName = response.data.appName
      }
      if (response.data.packageName) {
        createForm.packageName = response.data.packageName
      }
      if (response.data.versionName) {
        createForm.versionName = response.data.versionName
      }
      if (response.data.versionCode) {
        createForm.versionCode = response.data.versionCode
      }
      
      ElMessage.success('APK文件解析成功')
    } else {
      ElMessage.warning('APK文件解析失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('解析APK文件失败', error)
    ElMessage.warning('APK文件解析失败，请手动填写信息')
  }
}

// 加载证书列表
const loadCertificateList = async () => {
  try {
    const response = await certificateApi.getList({ platform: 'ANDROID' })
    if (response.code === 200 && response.data) {
      certificateList.value = response.data
    } else {
      console.error('加载证书列表失败:', response.message || '未知错误')
    }
  } catch (error) {
    console.error('加载证书列表失败', error)
  }
}

// 提交创建任务
const submitCreate = async () => {
  try {
    await createFormRef.value.validate()

    createLoading.value = true
    
    const formData = new FormData()
    formData.append('apkFile', createForm.apkFile)
    formData.append('certificateId', createForm.certificateId)
    formData.append('signatureVersion', createForm.signatureVersion)
    
    if (createForm.appName) formData.append('appName', createForm.appName)
    if (createForm.packageName) formData.append('packageName', createForm.packageName)
    if (createForm.versionName) formData.append('versionName', createForm.versionName)
    if (createForm.versionCode) formData.append('versionCode', createForm.versionCode)
    if (createForm.callbackUrl) formData.append('callbackUrl', createForm.callbackUrl)
    if (createForm.description) formData.append('description', createForm.description)
    
    const response = await androidTaskApi.createTask(formData)
    if (response.code === 200) {
      ElMessage.success('任务创建成功')
      createDialogVisible.value = false
      getTaskList()
    } else {
      ElMessage.error('任务创建失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('创建任务失败', error)
    ElMessage.error('任务创建失败')
  } finally {
    createLoading.value = false
  }
}

// 执行任务
const executeTask = async (task) => {
  try {
    const response = await androidTaskApi.executeTask(task.id)
    if (response.code === 200) {
      ElMessage.success('任务已开始执行')
      getTaskList()
    } else {
      ElMessage.error('执行任务失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('执行任务失败')
  }
}

// 重试任务
const retryTask = async (task) => {
  try {
    const response = await androidTaskApi.retryTask(task.id)
    if (response.code === 200) {
      ElMessage.success('任务重试成功')
      getTaskList()
    } else {
      ElMessage.error('重试任务失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('重试任务失败')
  }
}

// 删除任务
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await androidTaskApi.deleteTask(task.id)
    if (response.code === 200) {
      ElMessage.success('任务删除成功')
      getTaskList()
    } else {
      ElMessage.error('删除任务失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除任务失败')
    }
  }
}

// 查看详情
const viewDetail = (task) => {
  router.push({ name: 'AndroidTaskDetail', params: { id: task.id } })
}

// 下载文件
const downloadFile = async (task) => {
  try {
    const response = await androidTaskApi.downloadFile(task.id)
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${task.appName || 'resigned'}_${task.id}.apk`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('文件下载成功')
  } catch (error) {
    ElMessage.error('下载文件失败')
  }
}

// 处理待处理任务
const processPendingTasks = async () => {
  try {
    const response = await androidTaskApi.processPendingTasks()
    if (response.code === 200) {
      ElMessage.success('开始处理待处理任务')
      getTaskList()
    } else {
      ElMessage.error('处理待处理任务失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('处理待处理任务失败')
  }
}

// 显示清理对话框
const showCleanupDialog = () => {
  cleanupDialogVisible.value = true
}

// 确认清理
const confirmCleanup = async () => {
  cleanupLoading.value = true
  try {
    const response = await androidTaskApi.cleanupExpiredTasks(cleanupForm.daysToKeep)
    if (response.code === 200) {
      ElMessage.success('清理过期任务完成')
      cleanupDialogVisible.value = false
      getTaskList()
    } else {
      ElMessage.error('清理过期任务失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('清理过期任务失败')
  } finally {
    cleanupLoading.value = false
  }
}

// 工具函数
const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'info',
    PROCESSING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '等待中',
    PROCESSING: '处理中',
    SUCCESS: '成功',
    FAILED: '失败'
  }
  return statusMap[status] || status
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

// 列配置
const columns = computed(() => [
  { prop: 'taskId', label: '任务ID', width: 200 },
  { prop: 'appName', label: '应用名称', minWidth: 150 },
  { prop: 'appVersion', label: '应用版本', width: 120 },
  { prop: 'versionCode', label: '构建版本', width: 120 },
  { prop: 'packageName', label: '包名', minWidth: 200 },
  { prop: 'status', label: '任务状态', width: 120, slot: 'status' },
  { prop: 'processingTime', label: '处理耗时', width: 120, slot: 'processingTime' },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'actions', label: '操作', width: 200, slot: 'actions', fixed: 'right' }
])

// 生命周期
onMounted(async () => {
  await getTaskList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-card,
.operation-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.search-buttons {
  text-align: right;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.apk-info-card {
  margin-bottom: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>