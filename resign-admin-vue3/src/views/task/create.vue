<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建重签名任务</span>
        </div>
      </template>
      
      <el-form :model="taskForm" :rules="rules" ref="taskFormRef" label-width="120px" class="create-form">
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="taskForm.appType" placeholder="请选择应用类型" style="width: 100%">
            <el-option label="iOS" value="IOS" />
            <el-option label="Android" value="ANDROID" />
            <el-option label="HarmonyOS" value="HARMONY" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="原始包文件" prop="originalPackageFile">
          <el-upload
            ref="packageUploadRef"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            :on-change="handlePackageChange"
            :on-remove="handlePackageRemove"
            accept=".ipa,.apk,.app,.hap"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .ipa/.apk/.app/.hap 格式文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="证书文件" prop="certificateFile">
          <el-upload
            ref="certUploadRef"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            :on-change="handleCertChange"
            :on-remove="handleCertRemove"
            accept=".p12,.pfx,.jks"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将证书文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .p12/.pfx/.jks 格式证书文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="证书密码" prop="certificatePassword">
          <el-input v-model="taskForm.certificatePassword" type="password" placeholder="请输入证书密码" show-password />
        </el-form-item>
        
        <!-- 包解析信息 -->
        <el-form-item v-if="packageInfo.bundleIds.length > 0" label="包信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="应用名称">{{ packageInfo.appName }}</el-descriptions-item>
            <el-descriptions-item label="版本号">{{ packageInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="Bundle ID" :span="2">
              <el-tag v-for="bundleId in packageInfo.bundleIds" :key="bundleId" style="margin-right: 8px;">
                {{ bundleId }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        
        <!-- iOS和HarmonyOS的Profile文件 -->
        <div v-if="(taskForm.appType === 'IOS' || taskForm.appType === 'HARMONY') && packageInfo.bundleIds.length > 0">
          <el-form-item 
            v-for="(bundleId, index) in packageInfo.bundleIds" 
            :key="bundleId"
            :label="`Profile文件 (${bundleId})`"
            :prop="`profileFiles.${index}`"
          >
            <el-upload
              :ref="el => profileUploadRefs[index] = el"
              :auto-upload="false"
              :show-file-list="true"
              :limit="1"
              :on-change="(file) => handleProfileChange(file, index)"
              :on-remove="() => handleProfileRemove(index)"
              :accept="taskForm.appType === 'IOS' ? '.mobileprovision' : '.p7b'"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                选择 {{ taskForm.appType === 'IOS' ? '.mobileprovision' : '.p7b' }} 文件
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  为 {{ bundleId }} 选择对应的Profile文件
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </div>
        
        <el-form-item label="描述信息">
          <el-input v-model="taskForm.description" type="textarea" :rows="4" placeholder="请输入任务描述信息" />
        </el-form-item>
        
        <el-form-item label="回调地址">
          <el-input v-model="taskForm.callbackUrl" placeholder="请输入回调地址" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useTaskStore } from '@/store/task'
import request from '@/utils/request'

const router = useRouter()
const taskStore = useTaskStore()
const taskFormRef = ref(null)
const packageUploadRef = ref(null)
const certUploadRef = ref(null)
const profileUploadRefs = ref([])

// 表单数据
const taskForm = reactive({
  appType: '',
  originalPackageFile: null,
  certificateFile: null,
  certificatePassword: '',
  profileFiles: [],
  description: '',
  callbackUrl: ''
})

// 包解析信息
const packageInfo = reactive({
  appName: '',
  version: '',
  bundleIds: []
})

// 表单验证规则
const rules = {
  appType: [
    { required: true, message: '请选择应用类型', trigger: 'change' }
  ],
  originalPackageFile: [
    { required: true, message: '请选择原始包文件', trigger: 'change' }
  ],
  certificateFile: [
    { required: true, message: '请选择证书文件', trigger: 'change' }
  ],
  certificatePassword: [
    { required: true, message: '请输入证书密码', trigger: 'blur' }
  ]
}

// 文件处理方法
const handlePackageChange = async (file) => {
  taskForm.originalPackageFile = file.raw
  
  // 根据文件扩展名自动设置应用类型
  const fileName = file.name.toLowerCase()
  if (fileName.endsWith('.ipa')) {
    taskForm.appType = 'IOS'
  } else if (fileName.endsWith('.apk')) {
    taskForm.appType = 'ANDROID'
  } else if (fileName.endsWith('.hap') || fileName.endsWith('.app')) {
    taskForm.appType = 'HARMONY'
  }
  
  // 解析包信息
  if (taskForm.appType === 'IOS' || taskForm.appType === 'HARMONY') {
    await parsePackageInfo(file.raw)
  }
}

const handlePackageRemove = () => {
  taskForm.originalPackageFile = null
  // 清空包信息
  packageInfo.appName = ''
  packageInfo.version = ''
  packageInfo.bundleIds = []
  taskForm.profileFiles = []
}

const handleCertChange = (file) => {
  taskForm.certificateFile = file.raw
}

const handleCertRemove = () => {
  taskForm.certificateFile = null
}

const handleProfileChange = (file, index) => {
  if (!taskForm.profileFiles[index]) {
    taskForm.profileFiles[index] = {}
  }
  taskForm.profileFiles[index].file = file.raw
  taskForm.profileFiles[index].bundleId = packageInfo.bundleIds[index]
}

const handleProfileRemove = (index) => {
  if (taskForm.profileFiles[index]) {
    taskForm.profileFiles[index] = null
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
    formData.append('appType', taskForm.appType)
    
    const response = await request({
      url: '/api/resign/parse-package',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const { data } = response
    packageInfo.appName = data.appName || ''
    packageInfo.version = data.version || ''
    packageInfo.bundleIds = data.bundleIds || []
    
    // 初始化profile文件数组
    taskForm.profileFiles = new Array(packageInfo.bundleIds.length).fill(null)
    
    ElMessage.success('包信息解析成功')
  } catch (error) {
    ElMessage.error('包信息解析失败: ' + (error.message || '未知错误'))
    packageInfo.appName = ''
    packageInfo.version = ''
    packageInfo.bundleIds = []
  } finally {
    loading.close()
  }
}

// 提交表单
const submitForm = async () => {
  if (!taskFormRef.value) return
  
  // 验证Profile文件
  if ((taskForm.appType === 'IOS' || taskForm.appType === 'HARMONY') && packageInfo.bundleIds.length > 0) {
    const missingProfiles = packageInfo.bundleIds.filter((bundleId, index) => 
      !taskForm.profileFiles[index] || !taskForm.profileFiles[index].file
    )
    
    if (missingProfiles.length > 0) {
      ElMessage.error(`请为以下Bundle ID选择Profile文件: ${missingProfiles.join(', ')}`)
      return
    }
  }
  
  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      const loading = ElLoading.service({
        lock: true,
        text: '正在创建任务...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      
      try {
        const formData = new FormData()
        formData.append('appType', taskForm.appType)
        formData.append('originalPackageFile', taskForm.originalPackageFile)
        formData.append('certificateFile', taskForm.certificateFile)
        formData.append('certificatePassword', taskForm.certificatePassword)
        formData.append('description', taskForm.description || '')
        formData.append('callbackUrl', taskForm.callbackUrl || '')
        
        // 添加Profile文件
        if (taskForm.profileFiles && taskForm.profileFiles.length > 0) {
          taskForm.profileFiles.forEach((profileFile, index) => {
            if (profileFile && profileFile.file) {
              formData.append(`profileFiles`, profileFile.file)
              formData.append(`bundleIds`, profileFile.bundleId)
            }
          })
        }
        
        const response = await request({
          url: '/api/resign/create',
          method: 'post',
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        ElMessage.success('任务创建成功')
        router.push('/task/list')
      } catch (error) {
        ElMessage.error('创建任务失败: ' + (error.message || '未知错误'))
      } finally {
        loading.close()
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
  
  // 清空文件
  taskForm.originalPackageFile = null
  taskForm.certificateFile = null
  taskForm.profileFiles = []
  
  // 清空包信息
  packageInfo.appName = ''
  packageInfo.version = ''
  packageInfo.bundleIds = []
  
  // 清空上传组件
  if (packageUploadRef.value) {
    packageUploadRef.value.clearFiles()
  }
  if (certUploadRef.value) {
    certUploadRef.value.clearFiles()
  }
  profileUploadRefs.value.forEach(ref => {
    if (ref) {
      ref.clearFiles()
    }
  })
}

// 返回列表页
const goBack = () => {
  router.push('/task/list')
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
  max-width: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
</style>