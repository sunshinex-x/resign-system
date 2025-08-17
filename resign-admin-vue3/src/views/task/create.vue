<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建重签名任务</span>
          <el-steps :active="currentStep" finish-status="success" style="margin-left: 20px;">
            <el-step title="上传文件" />
            <el-step title="解析包信息" />
            <el-step title="配置Profile" />
            <el-step title="完成创建" />
          </el-steps>
        </div>
        
  <!-- Profile上传对话框 -->
  <el-dialog v-model="uploadProfileDialog.visible" title="上传Profile文件" width="500px">
    <div class="dialog-content">
      <el-alert
        title="请上传与Bundle ID匹配的Profile文件"
        type="info"
        :description="`当前Bundle ID: ${uploadProfileDialog.bundleId}`"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />
      
      <el-upload
        ref="profileUploadRef"
        :auto-upload="false"
        :show-file-list="true"
        :limit="1"
        :on-change="handleProfileChange"
        :on-remove="handleProfileRemove"
        accept=".mobileprovision"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .mobileprovision 格式Profile文件
          </div>
        </template>
      </el-upload>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="uploadProfileDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="uploadProfileFile">上传</el-button>
      </div>
    </template>
  </el-dialog>
</template>
      
      <!-- 步骤1: 上传原始包文件 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-form :model="taskForm" :rules="rules" ref="taskFormRef" label-width="120px" class="create-form">
          <el-form-item label="原始包文件" prop="originalPackageFile">
            <el-upload
              ref="packageUploadRef"
              :auto-upload="false"
              :show-file-list="true"
              :limit="1"
              :on-change="handlePackageChange"
              :on-remove="handlePackageRemove"
              accept=".ipa,.apk,.app"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .ipa/.apk/.app 格式文件，系统将自动解析包信息
                </div>
              </template>
            </el-upload>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="nextStep" :disabled="!taskForm.originalPackageFile">下一步</el-button>
            <el-button @click="goBack">返回</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 步骤2: 包信息展示 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-alert
          title="包信息解析完成"
          type="success"
          :description="`检测到应用类型: ${getAppTypeLabel(taskForm.appType)}，发现 ${packageInfo.bundleInfos.length} 个Bundle ID`"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        />
        
        <el-descriptions title="应用基本信息" :column="2" border>
          <el-descriptions-item label="应用类型">
            <el-tag :type="getAppTypeTagType(taskForm.appType)" size="large">
              {{ getAppTypeLabel(taskForm.appType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="应用名称">{{ packageInfo.appName }}</el-descriptions-item>
          <el-descriptions-item label="版本号">{{ packageInfo.version }}</el-descriptions-item>
          <el-descriptions-item label="Bundle ID数量">{{ packageInfo.bundleInfos.length }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">Bundle ID详情</el-divider>
        
        <el-table :data="packageInfo.bundleInfos" style="width: 100%" border>
          <el-table-column prop="bundleId" label="Bundle ID" min-width="200" />
          <el-table-column prop="appName" label="应用名称" min-width="150" />
          <el-table-column prop="version" label="版本号" width="100" />
          <el-table-column prop="isMainApp" label="类型" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.isMainApp ? 'primary' : 'info'">
                {{ scope.row.isMainApp ? '主应用' : '插件' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep" v-if="taskForm.appType === 'IOS'">下一步</el-button>
          <el-button type="primary" @click="createTaskDirectly" v-else>创建任务</el-button>
        </div>
      </div>
      
      <!-- 步骤3: 选择证书和Profile文件 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-form :model="taskForm" :rules="rules" ref="taskFormRef" label-width="120px" class="create-form">
          <!-- 选择证书 -->
          <el-form-item label="选择证书" prop="selectedCertificate">
            <el-select v-model="taskForm.selectedCertificate" placeholder="请选择证书" style="width: 100%;" @change="handleCertificateChange">
              <el-option
                v-for="cert in availableCertificates"
                :key="cert.id"
                :label="cert.name"
                :value="cert.id"
              >
                <div style="display: flex; justify-content: space-between;">
                  <span>{{ cert.name }}</span>
                  <el-tag size="small" :type="cert.platform === 'IOS' ? 'primary' : 'success'">
                    {{ cert.platform }}
                  </el-tag>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip">
              <el-text size="small" type="info">
                没有合适的证书？<el-button type="text" @click="showUploadCertDialog">上传新证书</el-button>
              </el-text>
            </div>
          </el-form-item>
          
          <!-- iOS应用需要选择Profile文件 -->
          <div v-if="taskForm.appType === 'IOS'">
            <el-divider content-position="left">Profile文件配置</el-divider>
            <el-alert
              title="请为每个Bundle ID选择对应的Profile文件"
              type="info"
              description="Profile文件必须与Bundle ID匹配，否则重签名会失败"
              show-icon
              :closable="false"
              style="margin-bottom: 20px;"
            />
            
            <div v-for="(bundleInfo, index) in packageInfo.bundleInfos" :key="bundleInfo.bundleId" class="bundle-profile-item">
              <el-card shadow="hover">
                <template #header>
                  <div class="bundle-header">
                    <span class="bundle-id">{{ bundleInfo.bundleId }}</span>
                    <el-tag :type="bundleInfo.isMainApp ? 'primary' : 'info'" size="small">
                      {{ bundleInfo.isMainApp ? '主应用' : '插件' }}
                    </el-tag>
                  </div>
                </template>
                
                <div class="profile-status">
                  <div v-if="taskForm.profileFiles[index] && taskForm.profileFiles[index].uploaded" class="profile-uploaded">
                    <el-tag type="success" size="large">
                      <el-icon><Check /></el-icon>
                      Profile文件已上传
                    </el-tag>
                    <el-button type="primary" link @click="showUploadProfileDialog(bundleInfo.bundleId, index)">
                      重新上传
                    </el-button>
                  </div>
                  <div v-else class="profile-not-uploaded">
                    <el-button type="primary" @click="showUploadProfileDialog(bundleInfo.bundleId, index)">
                      <el-icon><Upload /></el-icon>
                      上传Profile文件
                    </el-button>
                    <div class="form-tip">
                      <el-text size="small" type="info">
                        请上传与Bundle ID匹配的Profile文件
                      </el-text>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
          
          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="nextStep" :disabled="!canGoToNextStep2">下一步</el-button>
          </div>
        </el-form>
      </div>
      
      <!-- 步骤4: 完成创建 -->
      <div v-if="currentStep === 3 || (currentStep === 2 && taskForm.appType !== 'IOS')" class="step-content">
        <el-form :model="taskForm" label-width="120px" class="create-form">
          <el-form-item label="任务描述">
            <el-input v-model="taskForm.description" type="textarea" :rows="4" placeholder="请输入任务描述信息（可选）" />
          </el-form-item>
          
          <el-form-item label="回调地址">
            <el-input v-model="taskForm.callbackUrl" placeholder="请输入回调地址（可选）" />
            <div class="form-tip">
              <el-text size="small" type="info">
                任务完成后将向此地址发送通知
              </el-text>
            </div>
          </el-form-item>
          
          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="submitForm" :loading="submitting">
              <el-icon v-if="!submitting"><Check /></el-icon>
              {{ submitting ? '创建中...' : '创建任务' }}
            </el-button>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import { UploadFilled, Check, Upload } from '@element-plus/icons-vue'
import { useTaskStore } from '@/store/task'
import request from '@/utils/request'
import { createTaskV2, addBundleProfile } from '@/api/task'

const router = useRouter()
const taskStore = useTaskStore()
const taskFormRef = ref(null)
const packageUploadRef = ref(null)
const certUploadRef = ref(null)
const profileUploadRefs = ref([])

// 当前步骤
const currentStep = ref(0)
const submitting = ref(false)

// 表单数据
const taskForm = reactive({
  appType: '',
  originalPackageFile: null,
  selectedCertificate: null,
  selectedProfiles: [],
  certificateFile: null,
  certificatePassword: '',
  profileFiles: [],
  description: '',
  callbackUrl: '',
  taskId: '' // 用于存储创建的任务ID（用于后续上传Profile文件）
})

// 包解析信息
const packageInfo = reactive({
  appName: '',
  version: '',
  bundleInfos: []
})

// 表单验证规则
const rules = {
  originalPackageFile: [
    { required: true, message: '请选择原始包文件', trigger: 'change' }
  ],
  selectedCertificate: [
    { required: true, message: '请选择证书', trigger: 'change' }
  ]
}

// 可用的证书和Profile文件
const availableCertificates = ref([])
const availableProfiles = ref([])

// 计算属性
const canGoToNextStep2 = computed(() => {
  if (!taskForm.selectedCertificate) return false
  if (taskForm.appType === 'IOS') {
    return packageInfo.bundleInfos.every((_, index) => taskForm.selectedProfiles[index])
  }
  return true
})

// 步骤控制
const nextStep = async () => {
  if (currentStep.value === 0) {
    // 第一步：解析包信息
    await parsePackageInfo(taskForm.originalPackageFile)
    currentStep.value = 1
  } else if (currentStep.value === 1) {
    // 第二步：如果是iOS，进入Profile配置；否则跳到最后一步
    if (taskForm.appType === 'IOS') {
      currentStep.value = 2
    } else {
      currentStep.value = 3
    }
  } else if (currentStep.value === 2) {
    // 第三步：进入最后配置
    currentStep.value = 3
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 文件处理方法
const handlePackageChange = (file) => {
  taskForm.originalPackageFile = file.raw
  
  // 根据文件扩展名自动设置应用类型
  const fileName = file.name.toLowerCase()
  if (fileName.endsWith('.ipa')) {
    taskForm.appType = 'IOS'
  } else if (fileName.endsWith('.apk')) {
    taskForm.appType = 'ANDROID'
  } else if (fileName.endsWith('.app')) {
    taskForm.appType = 'IOS' // macOS app也算iOS
  }
}

const handlePackageRemove = () => {
  taskForm.originalPackageFile = null
  taskForm.appType = ''
  // 清空包信息
  packageInfo.appName = ''
  packageInfo.version = ''
  packageInfo.bundleInfos = []
  taskForm.selectedProfiles = []
  // 重置到第一步
  currentStep.value = 0
}

// 获取可用证书列表
const loadAvailableCertificates = async () => {
  try {
    const response = await request({
      url: `/api/certificate/certificates/${taskForm.appType}`,
      method: 'get'
    })
    availableCertificates.value = response.data
  } catch (error) {
    console.error('获取证书列表失败:', error)
  }
}

// 获取可用Profile文件列表
const loadAvailableProfiles = async () => {
  try {
    const response = await request({
      url: '/api/certificate/profiles',
      method: 'get'
    })
    availableProfiles.value = response.data.records || []
  } catch (error) {
    console.error('获取Profile列表失败:', error)
  }
}

// 根据Bundle ID获取可用的Profile文件
const getAvailableProfiles = (bundleId) => {
  return availableProfiles.value.filter(profile => 
    profile.bundleId === bundleId || profile.bundleId === '*'
  )
}

// 处理证书选择
const handleCertificateChange = (certificateId) => {
  console.log('选择证书:', certificateId)
  // 获取证书详情
  const selectedCert = availableCertificates.value.find(cert => cert.id === certificateId)
  if (selectedCert) {
    // 设置证书文件和密码
    taskForm.certificateFile = selectedCert.file
    taskForm.certificatePassword = selectedCert.password || ''
  }
}

// 处理Profile选择
const handleProfileSelect = (index, profileId) => {
  console.log('选择Profile:', index, profileId)
}

// 显示上传证书对话框
const showUploadCertDialog = () => {
  // TODO: 实现上传证书对话框
  ElMessage.info('请前往证书管理页面上传证书')
}

// 上传Profile对话框数据
const uploadProfileDialog = reactive({
  visible: false,
  bundleId: '',
  bundleIndex: -1
})

// 上传Profile表单
const uploadProfileForm = reactive({
  file: null,
  bundleId: ''
})

const profileUploadRef = ref(null)

// 显示上传Profile对话框
const showUploadProfileDialog = (bundleId, index) => {
  uploadProfileDialog.visible = true
  uploadProfileDialog.bundleId = bundleId
  uploadProfileDialog.bundleIndex = index
  uploadProfileForm.bundleId = bundleId
  uploadProfileForm.file = null
  
  // 清空上传组件
  if (profileUploadRef.value) {
    profileUploadRef.value.clearFiles()
  }
}

const handleProfileChange = (file) => {
  uploadProfileForm.file = file.raw
}

const handleProfileRemove = () => {
  uploadProfileForm.file = null
}

// 上传Profile文件
const uploadProfileFile = async () => {
  if (!uploadProfileForm.file) {
    ElMessage.warning('请选择Profile文件')
    return
  }
  
  const loading = ElLoading.service({
    lock: true,
    text: '正在上传Profile文件...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const formData = new FormData()
    formData.append('bundleId', uploadProfileDialog.bundleId)
    formData.append('profileFile', uploadProfileForm.file)
    
    const response = await addBundleProfile(taskForm.taskId, formData)
    
    // 更新UI状态
    if (taskForm.profileFiles[uploadProfileDialog.bundleIndex]) {
      taskForm.profileFiles[uploadProfileDialog.bundleIndex] = null
    }
    
    taskForm.profileFiles[uploadProfileDialog.bundleIndex] = {
      file: uploadProfileForm.file,
      bundleId: uploadProfileDialog.bundleId,
      uploaded: true
    }
    
    ElMessage.success(`${uploadProfileDialog.bundleId} 的Profile文件上传成功`)
    uploadProfileDialog.visible = false
    
    // 检查是否所有Profile都已上传
    checkAllProfilesUploaded()
    
  } catch (error) {
    ElMessage.error('上传Profile文件失败: ' + (error.message || '未知错误'))
  } finally {
    loading.close()
  }
}

// 检查是否所有Profile都已上传
const checkAllProfilesUploaded = () => {
  if (taskForm.appType !== 'IOS') return
  
  const allUploaded = packageInfo.bundleInfos.every((_, index) => 
    taskForm.profileFiles[index] && taskForm.profileFiles[index].uploaded
  )
  
  if (allUploaded) {
    ElMessage.success('所有Profile文件已上传完成，任务将开始执行')
    // 跳转到任务列表页面
    router.push('/task/list')
  }
}

// 解析包信息
const parsePackageInfo = async (file) => {
  const loading = ElLoading.service({
    lock: true,
    text: '正在解析包信息...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await request({
      url: '/api/resign/parse-package-v2',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const { data } = response
    packageInfo.appName = data.appName || ''
    packageInfo.version = data.version || ''
    packageInfo.bundleInfos = data.bundleInfos || []
    
    // 更新应用类型（从解析结果中获取）
    if (data.appType) {
      taskForm.appType = data.appType
    }
    
    // 初始化选择的Profile文件数组
    taskForm.selectedProfiles = new Array(packageInfo.bundleInfos.length).fill(null)
    
    // 加载可用的证书和Profile文件
    await loadAvailableCertificates()
    if (taskForm.appType === 'IOS') {
      await loadAvailableProfiles()
    }
    
    ElMessage.success(`包信息解析成功，发现 ${packageInfo.bundleInfos.length} 个Bundle ID`)
  } catch (error) {
    ElMessage.error('包信息解析失败: ' + (error.message || '未知错误'))
    packageInfo.appName = ''
    packageInfo.version = ''
    packageInfo.bundleInfos = []
    taskForm.appType = ''
    throw error // 重新抛出错误，阻止进入下一步
  } finally {
    loading.close()
  }
}

// 直接创建任务（Android应用）
const createTaskDirectly = async () => {
  currentStep.value = 2 // 跳到最后一步
}

// 提交表单
const submitForm = async () => {
  submitting.value = true
  
  try {
    // 准备表单数据
    const formData = new FormData()
    formData.append('originalPackageFile', taskForm.originalPackageFile)
    formData.append('certificateFile', taskForm.certificateFile)
    formData.append('certificatePassword', taskForm.certificatePassword)
    formData.append('description', taskForm.description || '')
    formData.append('callbackUrl', taskForm.callbackUrl || '')
    
    // 使用新的API创建任务（不包含Profile文件）
    const response = await createTaskV2(formData)
    
    // 保存任务ID
    taskForm.taskId = response.data.taskId
    
    ElMessage.success('任务创建成功！')
    
    // 如果是iOS应用，需要上传Profile文件
    if (taskForm.appType === 'IOS' && packageInfo.bundleInfos.length > 0) {
      // 初始化profileFiles数组
      taskForm.profileFiles = new Array(packageInfo.bundleInfos.length).fill(null)
      
      // 显示提示，需要为每个Bundle ID上传Profile文件
      ElMessageBox.alert(
        `请为每个Bundle ID上传对应的Profile文件，共 ${packageInfo.bundleInfos.length} 个`,
        '上传Profile文件',
        {
          confirmButtonText: '开始上传',
          type: 'info',
          callback: () => {
            // 自动打开第一个Bundle ID的上传对话框
            if (packageInfo.bundleInfos.length > 0) {
              const firstBundle = packageInfo.bundleInfos[0]
              showUploadProfileDialog(firstBundle.bundleId, 0)
            }
          }
        }
      )
    } else {
      // 如果是Android应用，直接完成
      router.push('/task/list')
    }
    
  } catch (error) {
    ElMessage.error('创建任务失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  // 重置步骤
  currentStep.value = 0
  submitting.value = false
  
  // 清空表单数据
  taskForm.appType = ''
  taskForm.originalPackageFile = null
  taskForm.certificateFile = null
  taskForm.certificatePassword = ''
  taskForm.profileFiles = []
  taskForm.description = ''
  taskForm.callbackUrl = ''
  taskForm.taskId = ''
  taskForm.selectedCertificate = null
  taskForm.selectedProfiles = []
  
  // 清空包信息
  packageInfo.appName = ''
  packageInfo.version = ''
  packageInfo.bundleInfos = []
  
  // 清空上传组件
  if (packageUploadRef.value) {
    packageUploadRef.value.clearFiles()
  }
  if (certUploadRef.value) {
    certUploadRef.value.clearFiles()
  }
  if (profileUploadRef.value) {
    profileUploadRef.value.clearFiles()
  }
  
  // 重置表单验证
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
}

// 返回列表页
const goBack = () => {
  router.push('/task/list')
}

// 获取应用类型标签类型
const getAppTypeTagType = (appType) => {
  switch (appType) {
    case 'IOS':
      return 'primary'
    case 'ANDROID':
      return 'success'
    case 'HARMONY':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取应用类型标签文本
const getAppTypeLabel = (appType) => {
  switch (appType) {
    case 'IOS':
      return 'iOS应用'
    case 'ANDROID':
      return 'Android应用'
    case 'HARMONY':
      return '鸿蒙应用'
    default:
      return '未知类型'
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.el-card {
  width: 100%;
  max-width: 800px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.step-content {
  padding: 20px 0;
  min-height: 400px;
}

.step-actions {
  margin-top: 30px;
  text-align: center;
  
  .el-button {
    margin: 0 10px;
  }
}

.create-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .form-tip {
    margin-top: 8px;
    
    .el-text {
      font-size: 12px;
    }
  }
}

.bundle-profile-item {
  margin-bottom: 20px;
  
  .bundle-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .bundle-id {
      font-weight: 500;
      color: #303133;
    }
  }
  
  .profile-upload {
    margin-top: 10px;
  }
}

.el-steps {
  margin: 0 20px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .el-steps {
    margin: 10px 0;
    width: 100%;
  }
  
  .el-card {
    max-width: 100%;
  }
}
</style>