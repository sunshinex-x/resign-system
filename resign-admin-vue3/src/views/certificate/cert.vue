<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>证书管理</span>
          <el-button type="primary" @click="showUploadDialog">
            <el-icon><Plus /></el-icon>
            上传证书
          </el-button>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="平台">
            <el-select v-model="queryParams.platform" placeholder="选择平台" clearable>
              <el-option label="iOS" value="IOS" />
              <el-option label="Android" value="ANDROID" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="getList">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="list" style="width: 100%">
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
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button type="info" size="small" @click="viewProfiles(scope.row)">
              查看描述文件
            </el-button>
            <el-button type="primary" size="small" @click="validateCertificate(scope.row)">
              验证
            </el-button>
            <el-button type="danger" size="small" @click="deleteCertificate(scope.row)">
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
    <el-dialog v-model="uploadDialog.visible" title="上传证书" width="600px">
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadFormRef" label-width="120px">
        <el-form-item label="证书名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入证书名称" />
        </el-form-item>
        
        <el-form-item label="平台" prop="platform">
          <el-select v-model="uploadForm.platform" placeholder="选择平台">
            <el-option label="iOS" value="IOS" />
            <el-option label="Android" value="ANDROID" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const list = ref([])
const total = ref(0)
const uploadFormRef = ref(null)
const uploadRef = ref(null)

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  platform: ''
})

// 上传对话框
const uploadDialog = reactive({
  visible: false
})

// 上传表单
const uploadForm = reactive({
  name: '',
  platform: '',
  password: '',
  file: null,
  description: ''
})

// 表单验证规则
const uploadRules = {
  name: [
    { required: true, message: '请输入证书名称', trigger: 'blur' }
  ],
  platform: [
    { required: true, message: '请选择平台', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入证书密码', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择证书文件', trigger: 'change' }
  ]
}

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/api/certificate/certificates',
      method: 'get',
      params: queryParams
    })
    
    list.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('获取证书列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.platform = ''
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
  uploadForm.platform = ''
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
  return uploadForm.platform === 'IOS' ? '.p12,.pfx' : '.jks,.keystore'
}

// 获取上传提示
const getUploadTip = () => {
  return uploadForm.platform === 'IOS' ? '支持 .p12/.pfx 格式证书文件' : '支持 .jks/.keystore 格式证书文件'
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
        formData.append('password', uploadForm.password)
        formData.append('file', uploadForm.file)
        formData.append('description', uploadForm.description || '')
        
        await request({
          url: '/api/certificate/upload-certificate',
          method: 'post',
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        ElMessage.success('证书上传成功')
        uploadDialog.visible = false
        getList()
      } catch (error) {
        ElMessage.error('证书上传失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 查看描述文件
const viewProfiles = (row) => {
  router.push({
    path: '/certificate/profile',
    query: { certificateId: row.id, certificateName: row.name }
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

// 删除证书
const deleteCertificate = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个证书吗？删除后关联的描述文件也会被删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request({
      url: `/api/certificate/certificates/${row.id}`,
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

// 组件挂载时获取数据
onMounted(() => {
  getList()
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
</style>