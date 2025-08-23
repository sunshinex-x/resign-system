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
            <el-form-item label="证书类型">
              <el-select v-model="searchForm.type" placeholder="请选择证书类型" clearable style="width: 100%">
                <el-option label="开发证书" value="DEVELOPMENT" />
                <el-option label="发布证书" value="DISTRIBUTION" />
              </el-select>
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
      <el-table v-loading="loading" :data="certificateList" style="width: 100%">
        <el-table-column prop="name" label="证书名称" width="200" />
        <el-table-column label="证书类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'DEVELOPMENT' ? 'primary' : 'success'">
              {{ scope.row.type === 'DEVELOPMENT' ? '开发证书' : '发布证书' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teamId" label="Team ID" width="120" />
        <el-table-column prop="bundleId" label="Bundle ID" width="200" show-overflow-tooltip />
        <el-table-column label="关联Profile" width="120">
          <template #default="scope">
            <el-button 
              v-if="scope.row.profileCount > 0" 
              type="info" 
              size="small" 
              @click="viewProfiles(scope.row)"
            >
              {{ scope.row.profileCount }}个
            </el-button>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="过期时间" width="120">
          <template #default="scope">
            <span :class="{ 'text-danger': isExpiringSoon(scope.row.expiryDate) }">
              {{ scope.row.expiryDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewCertDetail(scope.row)">详情</el-button>
            <el-button type="warning" size="small" @click="verifyCertificate(scope.row)">验证</el-button>
            <el-button type="danger" size="small" @click="deleteCertificate(scope.row)">删除</el-button>
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

    <!-- 上传证书对话框 -->
    <el-dialog v-model="uploadDialog.visible" title="上传iOS证书" width="600px" @close="resetUploadForm">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="证书名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入证书名称" />
        </el-form-item>
        <el-form-item label="证书类型" prop="type">
          <el-select v-model="uploadForm.type" placeholder="请选择证书类型" style="width: 100%">
            <el-option label="开发证书" value="DEVELOPMENT" />
            <el-option label="发布证书" value="DISTRIBUTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="证书密码" prop="password">
          <el-input v-model="uploadForm.password" type="password" placeholder="请输入证书密码" show-password />
        </el-form-item>
        <el-form-item label="证书文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            accept=".p12,.pfx"
            @change="handleFileChange"
            @remove="handleFileRemove"
          >
            <el-button type="primary" icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 .p12/.pfx 格式证书文件</div>
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

    <!-- Profile详情对话框 -->
    <el-dialog v-model="profileDialog.visible" title="关联的描述文件" width="800px">
      <el-table v-loading="profileLoading" :data="profileList" style="width: 100%">
        <el-table-column prop="name" label="Profile名称" width="200" />
        <el-table-column prop="bundleId" label="Bundle ID" width="200" />
        <el-table-column label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'DEVELOPMENT' ? 'primary' : 'success'">
              {{ scope.row.type === 'DEVELOPMENT' ? '开发' : '发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teamId" label="Team ID" width="120" />
        <el-table-column prop="expiryDate" label="过期时间" width="120">
          <template #default="scope">
            <span :class="{ 'text-danger': isExpiringSoon(scope.row.expiryDate) }">
              {{ scope.row.expiryDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleDownloadProfile(scope.row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 证书详情对话框 -->
    <el-dialog v-model="detailDialog.visible" title="证书详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="证书名称">{{ certDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="证书类型">
          <el-tag :type="certDetail.type === 'DEVELOPMENT' ? 'primary' : 'success'">
            {{ certDetail.type === 'DEVELOPMENT' ? '开发证书' : '发布证书' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Team ID">{{ certDetail.teamId }}</el-descriptions-item>
        <el-descriptions-item label="Bundle ID">{{ certDetail.bundleId }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ certDetail.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="指纹">{{ certDetail.fingerprint }}</el-descriptions-item>
        <el-descriptions-item label="颁发者">{{ certDetail.issuer }}</el-descriptions-item>
        <el-descriptions-item label="主题">{{ certDetail.subject }}</el-descriptions-item>
        <el-descriptions-item label="生效时间">{{ certDetail.validFrom }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">
          <span :class="{ 'text-danger': isExpiringSoon(certDetail.expiryDate) }">
            {{ certDetail.expiryDate }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ certDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ certDetail.description || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Upload } from '@element-plus/icons-vue'
import { 
  getIosCertificateList, 
  uploadIosCertificate, 
  deleteIosCertificate, 
  validateIosCertificate,
  getIosCertificateDetail,
  getCertificateProfiles,
  downloadProfile
} from '@/api/certificate'

// 响应式数据
const loading = ref(false)
const uploadLoading = ref(false)
const profileLoading = ref(false)
const certificateList = ref([])
const profileList = ref([])
const certDetail = ref({})

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  createTimeStart: '',
  createTimeEnd: ''
})

const dateRange = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 上传对话框
const uploadDialog = reactive({
  visible: false
})

// Profile对话框
const profileDialog = reactive({
  visible: false
})

// 详情对话框
const detailDialog = reactive({
  visible: false
})

// 上传表单
const uploadForm = reactive({
  name: '',
  type: '',
  password: '',
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
  type: [
    { required: true, message: '请选择证书类型', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入证书密码', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择证书文件', trigger: 'change' }
  ]
}

// 获取证书列表
const getCertificateList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name,
      type: searchForm.type,
      createTimeStart: searchForm.createTimeStart,
      createTimeEnd: searchForm.createTimeEnd
    }
    
    const response = await getIosCertificateList(params)
    if (response.code === 200 && response.data) {
      certificateList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      certificateList.value = []
      pagination.total = 0
      console.error('获取iOS证书列表失败:', response.message)
    }
  } catch (error) {
    ElMessage.error('获取证书列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.createTimeStart = dateRange.value[0]
    searchForm.createTimeEnd = dateRange.value[1]
  } else {
    searchForm.createTimeStart = ''
    searchForm.createTimeEnd = ''
  }
  pagination.page = 1
  getCertificateList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.createTimeStart = ''
  searchForm.createTimeEnd = ''
  dateRange.value = []
  pagination.page = 1
  getCertificateList()
}

// 刷新列表
const refreshList = () => {
  getCertificateList()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  getCertificateList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  getCertificateList()
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadDialog.visible = true
  resetUploadForm()
}

// 重置上传表单
const resetUploadForm = () => {
  uploadForm.name = ''
  uploadForm.type = ''
  uploadForm.password = ''
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
        formData.append('type', uploadForm.type)
        formData.append('password', uploadForm.password)
        formData.append('file', uploadForm.file)
        formData.append('description', uploadForm.description || '')
        
        await uploadIosCertificate(formData)
        
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

// 查看关联的Profile
const viewProfiles = async (cert) => {
  profileLoading.value = true
  profileDialog.visible = true
  
  try {
    const response = await getCertificateProfiles(cert.id)
    profileList.value = response.data
  } catch (error) {
    ElMessage.error('获取Profile列表失败')
  } finally {
    profileLoading.value = false
  }
}

// 下载Profile
const handleDownloadProfile = async (profile) => {
  try {
    await downloadProfile(profile.id)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 查看证书详情
const viewCertDetail = async (cert) => {
  try {
    const response = await getIosCertificateDetail(cert.id)
    certDetail.value = response.data
    detailDialog.visible = true
  } catch (error) {
    ElMessage.error('获取证书详情失败')
  }
}

// 验证证书
const verifyCertificate = async (cert) => {
  try {
    const { value: password } = await ElMessageBox.prompt('请输入证书密码', '验证证书', {
      confirmButtonText: '验证',
      cancelButtonText: '取消',
      inputType: 'password'
    })
    
    await validateIosCertificate(cert.id, { password })
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
    await ElMessageBox.confirm('确定要删除这个证书吗？删除后关联的描述文件也会被删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteIosCertificate(cert.id)
    ElMessage.success('删除成功')
    getCertificateList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 检查证书是否即将过期
const isExpiringSoon = (expiryDate) => {
  if (!expiryDate) return false
  const expiry = new Date(expiryDate)
  const now = new Date()
  const diffTime = expiry.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 30 && diffDays >= 0
}

// 组件挂载时获取数据
onMounted(() => {
  getCertificateList()
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

.text-muted {
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>