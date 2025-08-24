<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="证书名称">
              <el-input v-model="searchForm.name" placeholder="请输入证书名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="密钥别名">
              <el-input v-model="searchForm.alias" placeholder="请输入密钥别名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
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
    <el-card class="operation-card">
      <el-button type="primary" @click="showUploadDialog" icon="Upload">上传证书</el-button>
      <el-button type="success" @click="refreshList" icon="Refresh">刷新</el-button>
    </el-card>

    <!-- 证书列表 -->
    <el-card>
      <DataTable
        :data="certificateList"
        :columns="certificateColumns"
        :loading="loading"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      >
        <template #expiryDate="{ row }">
          <span :class="{ 'text-danger': isExpiringSoon(row.expireDate) }">
            {{ formatDate(row.expireDate) || '未设置' }}
          </span>
        </template>
        <template #actions="{ row }">
          <div class="table-actions">
            <el-button type="primary" size="small" @click="viewCertDetail(row)">详情</el-button>
            <el-button type="warning" size="small" @click="verifyCertificate(row)">验证</el-button>
            <el-button type="danger" size="small" @click="deleteCertificate(row)">删除</el-button>
          </div>
        </template>
      </DataTable>
    </el-card>

    <!-- 上传证书对话框 -->
    <el-dialog v-model="uploadDialog.visible" title="上传Android证书" width="600px" @close="resetUploadForm">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="证书名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入证书名称" />
        </el-form-item>
        <el-form-item label="密钥库密码" prop="storePassword">
          <el-input v-model="uploadForm.storePassword" type="password" placeholder="请输入密钥库密码" show-password />
        </el-form-item>
        <el-form-item label="密钥别名" prop="alias">
          <el-input v-model="uploadForm.alias" placeholder="请输入密钥别名" />
        </el-form-item>
        <el-form-item label="密钥密码" prop="keyPassword">
          <el-input v-model="uploadForm.keyPassword" type="password" placeholder="请输入密钥密码" show-password />
        </el-form-item>
        <el-form-item label="包名">
          <el-input v-model="uploadForm.bundleId" placeholder="请输入主应用包名（可选）" />
        </el-form-item>
        <el-form-item label="证书文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            accept=".jks,.keystore"
            @change="handleFileChange"
            @remove="handleFileRemove"
          >
            <el-button type="primary" icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 .jks/.keystore 格式证书文件</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入证书描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleUpload" :loading="uploadLoading">上传</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 证书详情对话框 -->
    <el-dialog v-model="detailDialog.visible" title="证书详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="证书名称">{{ certDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="密钥别名">{{ certDetail.keyAlias }}</el-descriptions-item>
        <el-descriptions-item label="包名">{{ certDetail.bundleId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ certDetail.status }}</el-descriptions-item>
        <el-descriptions-item label="证书主题">{{ certDetail.subject || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="签发者">{{ certDetail.issuer || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ certDetail.serialNumber || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">
          <span :class="{ 'text-danger': isExpiringSoon(certDetail.expireDate) }">
            {{ formatDate(certDetail.expireDate) || '未设置' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(certDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(certDetail.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ certDetail.createBy }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ certDetail.description || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Upload } from '@element-plus/icons-vue'
import { 
  getAndroidCertificateList, 
  uploadAndroidCertificate, 
  deleteAndroidCertificate, 
  validateAndroidCertificate,
  getAndroidCertificateDetail
} from '@/api/certificate'
import DataTable from '@/components/DataTable.vue'
import { createListHandler } from '@/utils/listHandler'

// 创建列表处理器
const { loading, list: certificateList, total, pagination, listHandler } = createListHandler({
  pagination: {
    page: 1,
    size: 10
  },
  messages: {
    success: '获取Android证书列表成功',
    error: '获取Android证书列表失败'
  }
})

// 其他响应式数据
const uploadLoading = ref(false)
const certDetail = ref({})

// 搜索表单
const searchForm = reactive({
  name: '',
  alias: '',
  createTimeStart: '',
  createTimeEnd: ''
})

const dateRange = ref([])

// 上传对话框
const uploadDialog = reactive({
  visible: false
})

// 详情对话框
const detailDialog = reactive({
  visible: false
})

// 上传表单
const uploadForm = reactive({
  name: '',
  storePassword: '',
  alias: '',
  keyPassword: '',
  bundleId: '',
  file: null,
  description: ''
})

// 表单引用
const uploadFormRef = ref(null)
const uploadRef = ref(null)

// 表单验证规则
const uploadRules = {
  name: [
    { required: true, message: '请输入证书名称', trigger: 'blur' }
  ],
  storePassword: [
    { required: true, message: '请输入密钥库密码', trigger: 'blur' }
  ],
  alias: [
    { required: true, message: '请输入密钥别名', trigger: 'blur' }
  ],
  keyPassword: [
    { required: true, message: '请输入密钥密码', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择证书文件', trigger: 'change' }
  ]
}

// 获取证书列表的请求函数
const fetchCertificateList = async (params) => {
  const requestParams = {
    ...params,
    ...searchForm
  }
  if (dateRange.value && dateRange.value.length === 2) {
    requestParams.createTimeStart = dateRange.value[0]
    requestParams.createTimeEnd = dateRange.value[1]
  }
  
  console.log('Android Certificate API Request:', requestParams)
  const response = await getAndroidCertificateList(requestParams)
  console.log('Android Certificate API Response:', response)
  
  // 直接返回原始API响应，让listHandler处理数据结构
  return response
}

// 获取证书列表（兼容原有调用）
const getCertificateList = async () => {
  if (pagination.page === 1) {
    await listHandler.init(fetchCertificateList)
  } else {
    await listHandler.refresh(fetchCertificateList)
  }
}

// 搜索
const handleSearch = async () => {
  await listHandler.search(fetchCertificateList)
}

// 重置搜索
const resetSearch = async () => {
  Object.assign(searchForm, {
    name: '',
    alias: '',
    createTimeStart: '',
    createTimeEnd: ''
  })
  dateRange.value = []
  await listHandler.reset(fetchCertificateList)
}

// 刷新列表
const refreshList = async () => {
  await listHandler.refresh(fetchCertificateList)
}

// 分页处理
const handleSizeChange = async (size) => {
  await listHandler.handleSizeChange(size, fetchCertificateList)
}

const handlePageChange = async (page) => {
  await listHandler.handlePageChange(page, fetchCertificateList)
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadDialog.visible = true
  resetUploadForm()
}

// 重置上传表单
const resetUploadForm = () => {
  uploadForm.name = ''
  uploadForm.storePassword = ''
  uploadForm.alias = ''
  uploadForm.keyPassword = ''
  uploadForm.bundleId = ''
  uploadForm.file = null
  uploadForm.description = ''
  
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 文件选择处理
const handleFileChange = (file) => {
  uploadForm.file = file.raw
}

// 文件移除处理
const handleFileRemove = () => {
  uploadForm.file = null
}

// 处理上传
const handleUpload = async () => {
  if (!uploadFormRef.value) return
  
  await uploadFormRef.value.validate(async (valid) => {
    if (valid) {
      uploadLoading.value = true
      try {
        const formData = new FormData()
        formData.append('name', uploadForm.name)
        formData.append('storePassword', uploadForm.storePassword)
        formData.append('alias', uploadForm.alias)
        formData.append('keyPassword', uploadForm.keyPassword)
        formData.append('bundleId', uploadForm.bundleId || '')
        formData.append('file', uploadForm.file)
        formData.append('description', uploadForm.description || '')
        
        await uploadAndroidCertificate(formData)
        
        ElMessage.success('证书上传成功')
        uploadDialog.visible = false
        getCertificateList()
      } catch (error) {
        ElMessage.error('证书上传失败')
      } finally {
        uploadLoading.value = false
      }
    }
  })
}

// 查看证书详情
const viewCertDetail = async (cert) => {
  try {
    const response = await getAndroidCertificateDetail(cert.id)
    certDetail.value = response.data
    detailDialog.visible = true
  } catch (error) {
    ElMessage.error('获取证书详情失败')
  }
}

// 验证证书
const verifyCertificate = async (cert) => {
  try {
    const { value: passwords } = await ElMessageBox.prompt(
      '请输入密钥库密码和密钥密码（用逗号分隔）', 
      '验证证书', 
      {
        confirmButtonText: '验证',
        cancelButtonText: '取消',
        inputPlaceholder: '密钥库密码,密钥密码'
      }
    )
    
    const [storePassword, keyPassword] = passwords.split(',')
    if (!storePassword || !keyPassword) {
      ElMessage.error('请输入正确格式的密码')
      return
    }
    
    await validateAndroidCertificate(cert.id, { 
      storePassword: storePassword.trim(), 
      keyPassword: keyPassword.trim() 
    })
    ElMessage.success('证书验证成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('证书验证失败')
    }
  }
}

// 删除证书
const deleteCertificate = async (cert) => {
  try {
    await ElMessageBox.confirm('确定要删除这个证书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAndroidCertificate(cert.id)
    ElMessage.success('删除成功')
    getCertificateList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 检查证书是否即将过期
const isExpiringSoon = (expireDate) => {
  if (!expireDate) return false
  const expiry = new Date(expireDate)
  const now = new Date()
  const diffTime = expiry.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 30 && diffDays >= 0
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return ''
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 表格列配置
const certificateColumns = computed(() => [
  {
    prop: 'name',
    label: '证书名称',
    minWidth: 150,
    showOverflowTooltip: true
  },
  {
    prop: 'keyAlias',
    label: '密钥别名',
    width: 120
  },
  {
    prop: 'bundleId',
    label: '包名',
    minWidth: 180,
    showOverflowTooltip: true
  },
  {
    prop: 'expireDate',
    label: '过期时间',
    width: 120,
    slot: 'expiryDate'
  },
  {
    prop: 'description',
    label: '描述',
    minWidth: 150,
    showOverflowTooltip: true
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180
  },
  {
    label: '操作',
    width: 180,
    fixed: 'right',
    slot: 'actions'
  }
])

// 组件挂载时获取数据
onMounted(async () => {
  await listHandler.init(fetchCertificateList)
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.operation-card {
  margin-bottom: 20px;
}

.search-buttons {
  text-align: right;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

.text-danger {
  color: #F56C6C;
}

.dialog-footer {
  text-align: right;
}
</style>