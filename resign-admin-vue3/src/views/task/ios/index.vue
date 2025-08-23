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
      <el-button type="primary" @click="showCreateDialog" icon="Plus">创建iOS重签名任务</el-button>
      <el-button type="success" @click="processPendingTasks" icon="Refresh">处理待处理任务</el-button>
      <el-button type="warning" @click="showCleanupDialog" icon="Delete">清理过期任务</el-button>
    </el-card>

    <!-- 任务列表 -->
    <el-card>
      <el-table v-loading="loading" :data="taskList" style="width: 100%">
        <el-table-column prop="taskId" label="任务ID" width="120" />
        <el-table-column prop="appName" label="应用名称" width="150" />
        <el-table-column prop="appVersion" label="应用版本" width="120" />
        <el-table-column prop="buildVersion" label="构建版本" width="120" />
        <el-table-column label="任务状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="processingTime" label="处理耗时" width="120">
          <template #default="scope">
            {{ scope.row.processingTime ? scope.row.processingTime + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" type="success" size="small" @click="executeTask(scope.row)">执行</el-button>
            <el-button v-if="scope.row.status === 'FAILED'" type="warning" size="small" @click="retryTask(scope.row)">重试</el-button>
            <el-button v-if="scope.row.status === 'SUCCESS'" type="info" size="small" @click="downloadFile(scope.row)">下载</el-button>
            <el-button type="danger" size="small" @click="deleteTask(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建iOS重签名任务" width="800px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="120px">
        <!-- 第一步：上传IPA文件 -->
        <div v-if="createStep === 1">
          <h3>第一步：上传IPA文件</h3>
          <el-form-item label="IPA文件" prop="ipaFile">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :before-upload="beforeUpload"
              accept=".ipa"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">将IPA文件拖到此处，或<em>点击上传</em></div>
              <template #tip>
                <div class="el-upload__tip">只能上传.ipa文件，且不超过500MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="parseIpaFile" :loading="parseLoading" :disabled="!createForm.ipaFile">解析IPA文件</el-button>
          </el-form-item>
        </div>

        <!-- 第二步：配置重签名信息 -->
        <div v-if="createStep === 2">
          <h3>第二步：配置重签名信息</h3>
          
          <!-- IPA解析信息展示 -->
          <el-card class="ipa-info-card" v-if="ipaInfo">
            <template #header>
              <span>IPA文件信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="应用名称">{{ ipaInfo.appName }}</el-descriptions-item>
              <el-descriptions-item label="应用版本">{{ ipaInfo.appVersion }}</el-descriptions-item>
              <el-descriptions-item label="构建版本">{{ ipaInfo.buildVersion }}</el-descriptions-item>
              <el-descriptions-item label="主Bundle ID">{{ ipaInfo.mainBundleId }}</el-descriptions-item>
              <el-descriptions-item label="Bundle ID数量">{{ ipaInfo.bundleIdList ? ipaInfo.bundleIdList.length : 0 }}</el-descriptions-item>
              <el-descriptions-item label="文件大小">{{ formatFileSize(ipaInfo.fileSize) }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 证书选择 -->
          <el-form-item label="选择证书" prop="certificateId">
            <el-select v-model="createForm.certificateId" placeholder="请选择iOS证书" style="width: 100%">
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

          <!-- Bundle ID配置 -->
          <el-form-item label="Bundle ID配置">
            <el-card class="bundle-config-card">
              <template #header>
                <span>配置每个Bundle ID对应的描述文件</span>
              </template>
              <div v-if="bundleIdConfigs.length > 0">
                <div v-for="(config, index) in bundleIdConfigs" :key="index" class="bundle-config-item">
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <el-form-item :label="'Bundle ID ' + (index + 1)">
                        <el-input v-model="config.bundleId" readonly />
                      </el-form-item>
                    </el-col>
                    <el-col :span="16">
                      <el-form-item label="描述文件">
                        <el-select v-model="config.profileId" placeholder="请选择描述文件" style="width: 100%">
                          <el-option
                            v-for="profile in profileList"
                            :key="profile.id"
                            :label="profile.name"
                            :value="profile.id"
                          >
                            <span>{{ profile.name }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ profile.bundleId }}</span>
                          </el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>
              </div>
              <div v-else class="no-bundle-ids">
                <el-empty description="暂无Bundle ID信息" />
              </div>
            </el-card>
          </el-form-item>

          <!-- 可选配置 -->
          <el-form-item label="应用名称">
            <el-input v-model="createForm.appName" placeholder="留空则使用原应用名称" />
          </el-form-item>
          <el-form-item label="应用版本">
            <el-input v-model="createForm.appVersion" placeholder="留空则使用原版本" />
          </el-form-item>
          <el-form-item label="构建版本">
            <el-input v-model="createForm.buildVersion" placeholder="留空则使用原构建版本" />
          </el-form-item>
          <el-form-item label="回调URL">
            <el-input v-model="createForm.callbackUrl" placeholder="任务完成后的回调地址" />
          </el-form-item>
          <el-form-item label="任务描述">
            <el-input v-model="createForm.description" type="textarea" placeholder="任务描述信息" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="createStep === 2" @click="createStep = 1">上一步</el-button>
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button v-if="createStep === 2" type="primary" @click="submitCreate" :loading="createLoading">创建任务</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import * as iosTaskApi from '@/api/iosTask'
import * as certificateApi from '@/api/certificate'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const taskList = ref([])
const total = ref(0)
const searchForm = reactive({
  taskId: '',
  status: '',
  appName: ''
})
const dateRange = ref([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 创建任务相关
const createDialogVisible = ref(false)
const createStep = ref(1)
const createLoading = ref(false)
const parseLoading = ref(false)
const createFormRef = ref()
const uploadRef = ref()
const createForm = reactive({
  ipaFile: null,
  certificateId: '',
  appName: '',
  appVersion: '',
  buildVersion: '',
  callbackUrl: '',
  description: ''
})
const createRules = {
  ipaFile: [{ required: true, message: '请上传IPA文件', trigger: 'change' }],
  certificateId: [{ required: true, message: '请选择证书', trigger: 'change' }]
}

// IPA解析信息
const ipaInfo = ref(null)
const bundleIdConfigs = ref([])
const certificateList = ref([])
const profileList = ref([])

// 清理任务相关
const cleanupDialogVisible = ref(false)
const cleanupLoading = ref(false)
const cleanupForm = reactive({
  daysToKeep: 30
})

// 获取任务列表
const getTaskList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await iosTaskApi.getTaskList(params)
    if (response.code === 200) {
      taskList.value = response.data.list || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error('获取任务列表失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getTaskList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    taskId: '',
    status: '',
    appName: ''
  })
  dateRange.value = []
  pagination.page = 1
  getTaskList()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  getTaskList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  getTaskList()
}

// 显示创建对话框
const showCreateDialog = async () => {
  createDialogVisible.value = true
  createStep.value = 1
  resetCreateForm()
  await loadCertificateList()
  await loadProfileList()
}

// 重置创建表单
const resetCreateForm = () => {
  Object.assign(createForm, {
    ipaFile: null,
    certificateId: '',
    appName: '',
    appVersion: '',
    buildVersion: '',
    callbackUrl: '',
    description: ''
  })
  ipaInfo.value = null
  bundleIdConfigs.value = []
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 文件上传处理
const handleFileChange = (file) => {
  createForm.ipaFile = file.raw
}

const beforeUpload = (file) => {
  const isIpa = file.name.toLowerCase().endsWith('.ipa')
  const isLt500M = file.size / 1024 / 1024 < 500

  if (!isIpa) {
    ElMessage.error('只能上传.ipa文件!')
    return false
  }
  if (!isLt500M) {
    ElMessage.error('上传文件大小不能超过500MB!')
    return false
  }
  return true
}

// 解析IPA文件
const parseIpaFile = async () => {
  if (!createForm.ipaFile) {
    ElMessage.error('请先上传IPA文件')
    return
  }

  parseLoading.value = true
  try {
    const formData = new FormData()
    formData.append('ipaFile', createForm.ipaFile)
    
    const response = await iosTaskApi.parseIpaInfo(formData)
    if (response.code === 200 && response.data) {
      ipaInfo.value = response.data
      
      // 初始化Bundle ID配置
      if (response.data.bundleIdList && response.data.bundleIdList.length > 0) {
        bundleIdConfigs.value = response.data.bundleIdList.map(bundleId => ({
          bundleId,
          profileId: ''
        }))
      }
      
      createStep.value = 2
      ElMessage.success('IPA文件解析成功')
    } else {
      ElMessage.error('IPA文件解析失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('解析IPA文件失败')
  } finally {
    parseLoading.value = false
  }
}

// 加载证书列表
const loadCertificateList = async () => {
  try {
    const response = await certificateApi.getList({ platform: 'IOS' })
    if (response.code === 200 && response.data) {
      certificateList.value = response.data
    } else {
      console.error('加载证书列表失败:', response.message || '未知错误')
    }
  } catch (error) {
    console.error('加载证书列表失败', error)
  }
}

// 加载描述文件列表
const loadProfileList = async () => {
  try {
    const response = await certificateApi.getIosProfiles()
    if (response.code === 200 && response.data) {
      profileList.value = response.data
    } else {
      console.error('加载描述文件列表失败:', response.message || '未知错误')
    }
  } catch (error) {
    console.error('加载描述文件列表失败', error)
  }
}

// 提交创建任务
const submitCreate = async () => {
  try {
    await createFormRef.value.validate()
    
    // 验证Bundle ID配置
    const hasEmptyProfile = bundleIdConfigs.value.some(config => !config.profileId)
    if (hasEmptyProfile) {
      ElMessage.error('请为所有Bundle ID配置对应的描述文件')
      return
    }

    createLoading.value = true
    
    const formData = new FormData()
    formData.append('ipaFile', createForm.ipaFile)
    formData.append('certificateId', createForm.certificateId)
    
    // 构建Bundle ID配置JSON
    const bundleConfig = {}
    bundleIdConfigs.value.forEach(config => {
      bundleConfig[config.bundleId] = config.profileId
    })
    formData.append('bundleIdConfig', JSON.stringify(bundleConfig))
    
    if (createForm.appName) formData.append('appName', createForm.appName)
    if (createForm.appVersion) formData.append('appVersion', createForm.appVersion)
    if (createForm.buildVersion) formData.append('buildVersion', createForm.buildVersion)
    if (createForm.callbackUrl) formData.append('callbackUrl', createForm.callbackUrl)
    if (createForm.description) formData.append('description', createForm.description)
    
    const response = await iosTaskApi.createTask(formData)
    if (response.code === 200) {
      ElMessage.success('任务创建成功')
      createDialogVisible.value = false
      getTaskList()
    } else {
      ElMessage.error('创建任务失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('创建任务失败', error)
  } finally {
    createLoading.value = false
  }
}

// 执行任务
const executeTask = async (task) => {
  try {
    const response = await iosTaskApi.executeTask(task.id)
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
    const response = await iosTaskApi.retryTask(task.id)
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
    
    const response = await iosTaskApi.deleteTask(task.id)
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
  router.push({ name: 'IosTaskDetail', params: { id: task.id } })
}

// 下载文件
const downloadFile = async (task) => {
  try {
    const response = await iosTaskApi.downloadFile(task.id)
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${task.appName || 'resigned'}_${task.id}.ipa`
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
    const response = await iosTaskApi.processPendingTasks()
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
    const response = await iosTaskApi.cleanupExpiredTasks(cleanupForm.daysToKeep)
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
    PENDING: '',
    PROCESSING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return statusMap[status] || ''
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

// 生命周期
onMounted(() => {
  getTaskList()
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

.ipa-info-card {
  margin-bottom: 20px;
}

.bundle-config-card {
  border: 1px solid #dcdfe6;
}

.bundle-config-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.bundle-config-item:last-child {
  border-bottom: none;
}

.no-bundle-ids {
  text-align: center;
  padding: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>