<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建重签名任务</span>
        </div>
      </template>
      
      <el-form :model="taskForm" :rules="rules" ref="taskFormRef" label-width="120px">
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="taskForm.appType" placeholder="请选择应用类型" style="width: 100%">
            <el-option label="iOS" value="IOS" />
            <el-option label="Android" value="ANDROID" />
            <el-option label="HarmonyOS" value="HARMONY" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="原始包地址" prop="originalPackageUrl">
          <el-input v-model="taskForm.originalPackageUrl" placeholder="请输入原始包地址" />
        </el-form-item>
        
        <el-form-item label="证书地址" prop="certificateUrl">
          <el-input v-model="taskForm.certificateUrl" placeholder="请输入证书地址" />
        </el-form-item>
        
        <el-form-item label="证书密码" prop="certificatePassword">
          <el-input v-model="taskForm.certificatePassword" type="password" placeholder="请输入证书密码" />
        </el-form-item>
        
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
import { ElMessage } from 'element-plus'
import { useTaskStore } from '@/store/task'

const router = useRouter()
const taskStore = useTaskStore()
const taskFormRef = ref(null)

// 表单数据
const taskForm = reactive({
  appType: '',
  originalPackageUrl: '',
  certificateUrl: '',
  certificatePassword: '',
  callbackUrl: ''
})

// 表单验证规则
const rules = {
  appType: [
    { required: true, message: '请选择应用类型', trigger: 'change' }
  ],
  originalPackageUrl: [
    { required: true, message: '请输入原始包地址', trigger: 'blur' }
  ],
  certificateUrl: [
    { required: true, message: '请输入证书地址', trigger: 'blur' }
  ],
  certificatePassword: [
    { required: true, message: '请输入证书密码', trigger: 'blur' }
  ]
}

// 提交表单
const submitForm = async () => {
  if (!taskFormRef.value) return
  
  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await taskStore.createNewTask(taskForm)
        ElMessage.success('任务创建成功')
        router.push('/task/list')
      } catch (error) {
        console.error('创建任务失败:', error)
      }
    } else {
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
}

// 返回列表页
const goBack = () => {
  router.push('/task/list')
}
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
</style>