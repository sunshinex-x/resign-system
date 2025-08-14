<template>
  <div class="profile-container">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="120" :src="userForm.avatar" icon="UserFilled" />
            <div class="avatar-overlay" @click="handleAvatarClick">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <input 
            ref="avatarInputRef" 
            type="file" 
            accept="image/*" 
            style="display: none" 
            @change="handleAvatarChange"
          />
        </div>
        
        <!-- 表单区域 -->
        <div class="form-section">
          <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="userForm.nickname" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="userForm.phone" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="updateLoading">
                保存修改
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
    
    <!-- 修改密码卡片 -->
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      
      <div class="password-content">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item>
            <el-button type="primary" @click="handlePasswordChange" :loading="passwordLoading">
              修改密码
            </el-button>
            <el-button @click="resetPasswordForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import request from '@/utils/request'

const authStore = useAuthStore()
const userFormRef = ref(null)
const passwordFormRef = ref(null)
const avatarInputRef = ref(null)
const updateLoading = ref(false)
const passwordLoading = ref(false)

// 用户信息表单
const userForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 初始化用户信息
const initUserInfo = () => {
  const userInfo = authStore.currentUser
  Object.assign(userForm, {
    username: userInfo.username || '',
    nickname: userInfo.nickname || '',
    email: userInfo.email || '',
    phone: userInfo.phone || '',
    avatar: userInfo.avatar || ''
  })
}

// 头像相关方法
const handleAvatarClick = () => {
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 验证文件大小（2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  const loading = ElLoading.service({
    lock: true,
    text: '正在上传头像...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const formData = new FormData()
    formData.append('avatar', file)
    
    const response = await request({
      url: '/api/user/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    userForm.avatar = response.data.avatarUrl
    ElMessage.success('头像更新成功')
    
    // 更新store中的用户信息
    const updatedUserInfo = { ...authStore.currentUser, avatar: userForm.avatar }
    localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
    authStore.userInfo = updatedUserInfo
  } catch (error) {
    ElMessage.error('头像上传失败: ' + (error.message || '未知错误'))
  } finally {
    loading.close()
    // 清空input值，允许重复选择同一文件
    event.target.value = ''
  }
}

// 保存用户信息修改
const handleUpdate = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      updateLoading.value = true
      try {
        const response = await request({
          url: '/api/user/profile',
          method: 'put',
          data: {
            nickname: userForm.nickname,
            email: userForm.email,
            phone: userForm.phone
          }
        })
        
        ElMessage.success('个人信息更新成功')
        
        // 更新store中的用户信息
        const updatedUserInfo = { ...authStore.currentUser, ...userForm }
        localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
        authStore.userInfo = updatedUserInfo
      } catch (error) {
        ElMessage.error('更新失败: ' + (error.message || '未知错误'))
      } finally {
        updateLoading.value = false
      }
    }
  })
}

// 重置用户信息表单
const resetForm = () => {
  initUserInfo()
}

// 修改密码
const handlePasswordChange = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await request({
          url: '/api/user/password',
          method: 'put',
          data: {
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
          }
        })
        
        ElMessage.success('密码修改成功')
        resetPasswordForm()
      } catch (error) {
        ElMessage.error('密码修改失败: ' + (error.message || '未知错误'))
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

onMounted(() => {
  initUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  margin-bottom: 24px;
  
  .profile-content {
    display: flex;
    gap: 40px;
    align-items: flex-start;
    
    .avatar-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      min-width: 160px;
      
      .avatar-wrapper {
        position: relative;
        cursor: pointer;
        
        &:hover .avatar-overlay {
          opacity: 1;
        }
        
        .avatar-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.6);
          border-radius: 50%;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          opacity: 0;
          transition: opacity 0.3s;
          color: white;
          font-size: 12px;
          
          .el-icon {
            font-size: 24px;
            margin-bottom: 4px;
          }
        }
      }
    }
    
    .form-section {
      flex: 1;
    }
  }
}

.password-card {
  .password-content {
    .el-form-item {
      margin-bottom: 24px;
    }
  }
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column !important;
    align-items: center;
    
    .form-section {
      width: 100%;
    }
  }
  
  .password-content {
    .el-row {
      .el-col {
        span: 24 !important;
      }
    }
  }
}
</style>