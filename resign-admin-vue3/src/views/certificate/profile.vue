<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <span>描述文件管理</span>
            <el-tag v-if="certificateName" type="info" style="margin-left: 10px;">
              证书：{{ certificateName }}
            </el-tag>
          </div>
          <div>
            <el-button @click="goBack">返回证书列表</el-button>
            <el-button type="primary" @click="showUploadDialog">
              <el-icon><Plus /></el-icon>
              上传描述文件
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="证书">
            <el-select v-model="queryParams.certificateId" placeholder="选择证书" clearable @change="getList">
              <el-option
                v-for="cert in certificateList"
                :key="cert.id"
                :label="cert.name"
                :value="cert.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Bundle ID">
            <el-input v-model="queryParams.bundleId" placeholder="请输入Bundle ID" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="getList">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="list" style="width: 100%">
        <el-table-column prop="name" label="Profile名称" min-width="150" />
        <el-table-column prop="certificateName" label="关联证书" min-width="150" />
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
    <el-dialog v-model="uploadDialog.visible" title="上传描述文件" width="600px">
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadFormRef" label-width="120px">
        <el-form-item label="Profile名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入Profile名称" />
        </el-form-item>
        
        <el-form-item label="选择证书" prop="certificateId">
          <el-select v-model="uploadForm.certificateId" placeholder="请选择证书" style="width: 100%;">
            <el-option
              v-for="cert in iosCertificateList"
              :key="cert.id"
              :label="cert.name"
              :value="cert.id"
            >
              <div style="display: flex; justify-content: space-between;">
                <span>{{ cert.name }}</span>
                <el-tag size="small" type="primary">{{ cert.platform }}</el-tag>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">
            <el-text size="small" type="info">
              没有合适的证书？<el-button type="text" @click="goToCertManage">去上传证书</el-button>
            </el-text>
          </div>
        </el-form-item>
        
        <el-form-item label="Bundle ID" prop="bundleId">
          <el-input v-model="uploadForm.bundleId" placeholder="请输入Bundle ID，如：com.example.app" />
        </el-form-item>
        
        <el-form-item label="Profile文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const list = ref([])
const total = ref(0)
const certificateList = ref([])
const uploadFormRef = ref(null)
const uploadRef = ref(null)

// 从路由参数获取证书信息
const certificateId = ref(route.query.certificateId)
const certificateName = ref(route.query.certificateName)

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  certificateId: certificateId.value || '',
  bundleId: ''
})

// 上传对话框
const uploadDialog = reactive({
  visible: false
})

// 上传表单
const uploadForm = reactive({
  name: '',
  certificateId: certificateId.value || '',
  bundleId: '',
  file: null,
  description: ''
})

// 表单验证规则
const uploadRules = {
  name: [
    { required: true, message: '请输入Profile名称', trigger: 'blur' }
  ],
  certificateId: [
    { required: true, message: '请选择证书', trigger: 'change' }
  ],
  bundleId: [
    { required: true, message: '请输入Bundle ID', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择Profile文件', trigger: 'change' }
  ]
}

// 计算属性：只显示iOS证书
const iosCertificateList = computed(() => {
  return certificateList.value.filter(cert => cert.platform === 'IOS')
})

// 获取证书列表
const getCertificateList = async () => {
  try {
    const response = await request({
      url: '/api/certificate/certificates/IOS',
      method: 'get'
    })
    if (response.code === 200 && response.data) {
      certificateList.value = response.data.records || response.data || []
    } else {
      certificateList.value = []
      console.error('获取证书列表失败:', response.message)
    }
  } catch (error) {
    certificateList.value = []
    console.error('获取证书列表失败:', error)
  }
}

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    let url = '/api/certificate/profiles'
    const params = {
      current: queryParams.current,
      size: queryParams.size
    }
    
    // 如果指定了证书ID，则按证书ID查询
    if (queryParams.certificateId) {
      url = `/api/certificate/profiles/certificate/${queryParams.certificateId}`
    } else if (queryParams.bundleId) {
      params.bundleId = queryParams.bundleId
    }
    
    const response = await request({
      url: url,
      method: 'get',
      params: params
    })
    
    // 处理响应数据
    if (response.code === 200 && response.data) {
      if (Array.isArray(response.data)) {
        list.value = response.data
        total.value = response.data.length
      } else {
        list.value = response.data.records || []
        total.value = response.data.total || 0
      }
    } else {
      list.value = []
      total.value = 0
      console.error('获取Profile列表失败:', response.message)
    }
    
    // 为每个Profile添加证书名称
    list.value.forEach(profile => {
      const cert = certificateList.value.find(c => c.id === profile.certificateId)
      profile.certificateName = cert ? cert.name : '未知证书'
    })
    
  } catch (error) {
    ElMessage.error('获取Profile列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.certificateId = certificateId.value || ''
  queryParams.bundleId = ''
  queryParams.current = 1
  getList()
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadDialog.visible = true
  resetUploadForm()
}

// 重置上传表单
const resetUploadForm = () => {
  uploadForm.name = ''
  uploadForm.certificateId = certificateId.value || ''
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
      try {
        const formData = new FormData()
        formData.append('name', uploadForm.name)
        formData.append('certificateId', uploadForm.certificateId)
        formData.append('bundleId', uploadForm.bundleId)
        formData.append('file', uploadForm.file)
        formData.append('description', uploadForm.description || '')
        
        await request({
          url: '/api/certificate/upload-profile',
          method: 'post',
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        ElMessage.success('Profile文件上传成功')
        uploadDialog.visible = false
        getList()
      } catch (error) {
        ElMessage.error('Profile文件上传失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 验证Profile
const validateProfile = async (row) => {
  try {
    const { value: bundleId } = await ElMessageBox.prompt('请输入Bundle ID', '验证Profile', {
      confirmButtonText: '验证',
      cancelButtonText: '取消',
      inputValue: row.bundleId
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
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 返回证书列表
const goBack = () => {
  router.push('/certificate/cert')
}

// 跳转到证书管理
const goToCertManage = () => {
  router.push('/certificate/cert')
}

// 组件挂载时获取数据
onMounted(async () => {
  await getCertificateList()
  await getList()
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

.form-tip {
  margin-top: 8px;
}
</style>