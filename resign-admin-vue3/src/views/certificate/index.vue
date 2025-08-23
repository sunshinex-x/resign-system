<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>证书和Profile文件管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 证书管理 -->
        <el-tab-pane label="证书管理" name="certificates">
          <div class="tab-header">
            <el-button type="primary" @click="showUploadCertDialog">
              <el-icon><Plus /></el-icon>
              上传证书
            </el-button>
          </div>
          
          <!-- 证书搜索条件 -->
          <div class="filter-container">
            <el-form :inline="true" :model="certQueryParams" class="demo-form-inline">
              <el-form-item label="平台">
                <el-select v-model="certQueryParams.platform" placeholder="选择平台" clearable>
                  <el-option label="iOS" value="IOS" />
                  <el-option label="Android" value="ANDROID" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="getCertificateList">查询</el-button>
                <el-button @click="resetCertQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 证书表格 -->
          <el-table v-loading="certLoading" :data="certificateList" style="width: 100%">
            <el-table-column prop="name" label="证书名称" min-width="150" />
            <el-table-column prop="platform" label="平台" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.platform === 'IOS' ? 'primary' : 'success'">
                  {{ scope.row.platform }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="validateCertificate(scope.row)">
                  验证
                </el-button>
                <el-button type="danger" size="small" @click="deleteCertificate(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 证书分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="certQueryParams.current"
              v-model:page-size="certQueryParams.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="certTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="getCertificateList"
              @current-change="getCertificateList"
            />
          </div>
        </el-tab-pane>
        
        <!-- Profile文件管理 -->
        <el-tab-pane label="Profile文件管理" name="profiles">
          <div class="tab-header">
            <el-button type="success" @click="showUploadProfileDialog">
              <el-icon><Plus /></el-icon>
              上传Profile
            </el-button>
          </div>
          
          <!-- Profile搜索条件 -->
          <div class="filter-container">
            <el-form :inline="true" :model="profileQueryParams" class="demo-form-inline">
              <el-form-item label="Bundle ID">
                <el-input v-model="profileQueryParams.bundleId" placeholder="请输入Bundle ID" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="getProfileList">查询</el-button>
                <el-button @click="resetProfileQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- Profile表格 -->
          <el-table v-loading="profileLoading" :data="profileList" style="width: 100%">
            <el-table-column prop="name" label="Profile名称" min-width="150" />
            <el-table-column prop="bundleId" label="Bundle ID" min-width="200" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="validateProfile(scope.row)">
                  验证
                </el-button>
                <el-button type="danger" size="small" @click="deleteProfile(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- Profile分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="profileQueryParams.current"
              v-model:page-size="profileQueryParams.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="profileTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="getProfileList"
              @current-change="getProfileList"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>
    
    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialog.visible" :title="uploadDialog.title" width="600px">
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadFormRef" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入名称" />
        </el-form-item>
        
        <el-form-item label="平台" prop="platform">
          <el-select v-model="uploadForm.platform" placeholder="选择平台">
            <el-option label="iOS" value="IOS" />
            <el-option label="Android" value="ANDROID" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="uploadDialog.type === 'PROFILE'" label="Bundle ID" prop="bundleId">
          <el-input v-model="uploadForm.bundleId" placeholder="请输入Bundle ID" />
        </el-form-item>
        
        <el-form-item v-if="uploadDialog.type === 'CERTIFICATE'" label="证书密码" prop="password">
          <el-input v-model="uploadForm.password" type="password" placeholder="请输入证书密码" show-password />
        </el-form-item>
        
        <el-form-item label="文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :accept="getAcceptTypes()"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                {{ getUploadTip() }}
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入描述信息" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleUpload">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 当前激活的标签页
const activeTab = ref('certificates')

// 证书相关数据
const certLoading = ref(false)
const certificateList = ref([])
const certTotal = ref(0)

// Profile相关数据
const profileLoading = ref(false)
const profileList = ref([])
const profileTotal = ref(0)

// 证书查询参数
const certQueryParams = reactive({
  current: 1,
  size: 10,
  platform: ''
})

// Profile查询参数
const profileQueryParams = reactive({
  current: 1,
  size: 10,
  bundleId: ''
})

// 上传对话框
const uploadDialog = reactive({
  visible: false,
  title: '',
  type: ''
})

// 上传表单
const uploadForm = reactive({
  name: '',
  platform: '',
  bundleId: '',
  password: '',
  file: null,
  description: ''
})

// 表单验证规则
const uploadRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
  ],
  platform: [
    { required: true, message: '请选择平台', trigger: 'change' }
  ],
  bundleId: [
    { required: true, message: '请输入Bundle ID', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入证书密码', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择文件', trigger: 'change' }
  ]
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'certificates') {
    getCertificateList()
  } else if (tabName === 'profiles') {
    getProfileList()
  }
}

// 获取证书列表
const getCertificateList = async () => {
  certLoading.value = true
  try {
    const response = await request({
      url: '/api/certificate/certificates',
      method: 'get',
      params: certQueryParams
    })
    
    if (response.code === 200 && response.data) {
      certificateList.value = response.data.records || []
      certTotal.value = response.data.total || 0
    } else {
      certificateList.value = []
      certTotal.value = 0
      ElMessage.error('获取证书列表失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    certificateList.value = []
    certTotal.value = 0
    ElMessage.error('获取证书列表失败: ' + (error.message || '未知错误'))
  } finally {
    certLoading.value = false
  }
}

// 获取Profile列表
const getProfileList = async () => {
  profileLoading.value = true
  try {
    const response = await request({
      url: '/api/certificate/profiles',
      method: 'get',
      params: profileQueryParams
    })
    
    if (response.code === 200 && response.data) {
      profileList.value = response.data.records || []
      profileTotal.value = response.data.total || 0
    } else {
      profileList.value = []
      profileTotal.value = 0
      ElMessage.error('获取Profile列表失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    profileList.value = []
    profileTotal.value = 0
    ElMessage.error('获取Profile列表失败: ' + (error.message || '未知错误'))
  } finally {
    profileLoading.value = false
  }
}

// 重置证书查询
const resetCertQuery = () => {
  certQueryParams.platform = ''
  certQueryParams.current = 1
  getCertificateList()
}

// 重置Profile查询
const resetProfileQuery = () => {
  profileQueryParams.bundleId = ''
  profileQueryParams.current = 1
  getProfileList()
}

// 显示上传证书对话框
const showUploadCertDialog = () => {
  uploadDialog.type = 'CERTIFICATE'
  uploadDialog.title = '上传证书文件'
  uploadDialog.visible = true
  resetUploadForm()
}

// 显示上传Profile对话框
const showUploadProfileDialog = () => {
  uploadDialog.type = 'PROFILE'
  uploadDialog.title = '上传Profile文件'
  uploadDialog.visible = true
  resetUploadForm()
}

// 重置上传表单
const resetUploadForm = () => {
  uploadForm.name = ''
  uploadForm.platform = ''
  uploadForm.bundleId = ''
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

// 获取接受的文件类型
const getAcceptTypes = () => {
  if (uploadDialog.type === 'CERTIFICATE') {
    return uploadForm.platform === 'IOS' ? '.p12,.pfx' : '.jks,.keystore'
  } else {
    return uploadForm.platform === 'IOS' ? '.mobileprovision' : '.p7b'
  }
}

// 获取上传提示
const getUploadTip = () => {
  if (uploadDialog.type === 'CERTIFICATE') {
    return uploadForm.platform === 'IOS' ? '支持 .p12/.pfx 格式证书文件' : '支持 .jks/.keystore 格式证书文件'
  } else {
    return uploadForm.platform === 'IOS' ? '支持 .mobileprovision 格式Profile文件' : '支持 .p7b 格式Profile文件'
  }
}

// 处理上传
const handleUpload = async () => {
  if (!uploadFormRef.value) return
  
  await uploadFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = new FormData()
        formData.append('name', uploadForm.name)
        formData.append('platform', uploadForm.platform)
        formData.append('file', uploadForm.file)
        formData.append('description', uploadForm.description || '')
        
        let url = ''
        if (uploadDialog.type === 'CERTIFICATE') {
          formData.append('password', uploadForm.password)
          url = '/api/certificate/upload-certificate'
        } else {
          formData.append('bundleId', uploadForm.bundleId)
          url = '/api/certificate/upload-profile'
        }
        
        await request({
          url: url,
          method: 'post',
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        ElMessage.success('上传成功')
        uploadDialog.visible = false
        
        // 根据类型刷新对应列表
        if (uploadDialog.type === 'CERTIFICATE') {
          getCertificateList()
        } else {
          getProfileList()
        }
      } catch (error) {
        ElMessage.error('上传失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 验证证书
const validateCertificate = async (row) => {
  try {
    const { value: password } = await ElMessageBox.prompt('请输入证书密码', '验证证书', {
      confirmButtonText: '验证',
      cancelButtonText: '取消',
      inputType: 'password'
    })
    
    const response = await request({
      url: `/api/certificate/validate-certificate/${row.id}`,
      method: 'post',
      params: { password }
    })
    
    if (response.data) {
      ElMessage.success('证书验证成功')
    } else {
      ElMessage.error('证书验证失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('验证失败: ' + (error.message || '未知错误'))
    }
  }
}

// 验证Profile
const validateProfile = async (row) => {
  try {
    const { value: bundleId } = await ElMessageBox.prompt('请输入Bundle ID', '验证Profile', {
      confirmButtonText: '验证',
      cancelButtonText: '取消'
    })
    
    const response = await request({
      url: `/api/certificate/validate-profile/${row.id}`,
      method: 'post',
      params: { bundleId }
    })
    
    if (response.data) {
      ElMessage.success('Profile验证成功')
    } else {
      ElMessage.error('Profile验证失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('验证失败: ' + (error.message || '未知错误'))
    }
  }
}

// 删除证书
const deleteCertificate = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个证书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({
      url: `/api/certificate/certificates/${row.id}`,
      method: 'delete'
    })
    
    ElMessage.success('删除成功')
    getCertificateList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 删除Profile
const deleteProfile = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个Profile文件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({
      url: `/api/certificate/profiles/${row.id}`,
      method: 'delete'
    })
    
    ElMessage.success('删除成功')
    getProfileList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getCertificateList()
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

.tab-header {
  margin-bottom: 20px;
  text-align: right;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.el-tabs {
  :deep(.el-tab-pane) {
    padding-top: 20px;
  }
}
</style>