<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>搜索条件</span>
          <div>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
      </template>

      <el-form :model="searchForm" label-width="100px" class="compact-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="用户名">
              <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="邮箱">
              <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="启用" value="1" />
                <el-option label="禁用" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
      <el-button type="danger" :disabled="selectedUsers.length === 0" @click="handleBatchDelete">批量删除</el-button>
      <el-button type="success" @click="refreshList">刷新</el-button>
    </div>

    <!-- 用户表格 -->
    <el-table v-loading="loading" :data="userList" border stripe style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="id" label="用户ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="roleName" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.roleName === '管理员' ? 'danger' : 'primary'">
            {{ row.roleName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="160">
        <template #default="{ row }">
          {{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '从未登录' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" link
            @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 用户编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option 
              v-for="role in roleOptions" 
              :key="role.id" 
              :label="role.roleName" 
              :value="role.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/validate'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  username: '',
  email: '',
  status: ''
})

// 分页参数
const pagination = reactive({
  current: 1,
  size: 10
})

// 数据
const userList = ref([])
const total = ref(0)
const loading = ref(false)
const selectedUsers = ref([])
const roleOptions = ref([])

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const userFormRef = ref(null)

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  roleId: '',
  password: '',
  status: 1
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }

    const result = await userStore.fetchUserList(params)
    // 确保result和result.data存在
    if (result && result.data) {
      userList.value = result.data.records || []
      total.value = result.data.total || 0
    } else if (result && result.records !== undefined) {
      // 如果直接返回了分页数据
      userList.value = result.records || []
      total.value = result.total || 0
    } else {
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败: ' + (error.message || '未知错误'))
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.current = 1
  fetchUserList()
}

// 刷新列表
const refreshList = () => {
  fetchUserList()
}

// 新增用户
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  resetUserForm()
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  Object.assign(userForm, row)
  dialogVisible.value = true
}

// 切换用户状态
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const newStatus = row.status === 1 ? 0 : 1
      await userStore.toggleUserStatus(row.id, newStatus)
      row.status = newStatus
      ElMessage.success(`${action}成功`)
    } catch (error) {
      console.error(`${action}用户失败:`, error)
    }
  }).catch(() => { })
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await userStore.deleteUserById(row.id)
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error) {
      console.error('删除用户失败:', error)
    }
  }).catch(() => { })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  ElMessageBox.confirm(`确定要删除选中的 ${selectedUsers.value.length} 个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const selectedIds = selectedUsers.value.map(user => user.id)
      await userStore.batchDeleteUsers(selectedIds)
      ElMessage.success('批量删除成功')
      fetchUserList()
    } catch (error) {
      console.error('批量删除用户失败:', error)
    }
  }).catch(() => { })
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑用户
          await userStore.updateUserInfo(userForm.id, userForm)
          ElMessage.success('编辑成功')
        } else {
          // 新增用户
          await userStore.createNewUser(userForm)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchUserList()
      } catch (error) {
        console.error('操作失败:', error)
      }
    }
  })
}

// 重置用户表单
const resetUserForm = () => {
  Object.assign(userForm, {
    id: null,
    username: '',
    nickname: '',
    email: '',
    phone: '',
    roleId: '',
    password: '',
    status: 1
  })
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pagination.size = val
  fetchUserList()
}

// 当前页变化
const handleCurrentChange = (val) => {
  pagination.current = val
  fetchUserList()
}

// 获取角色选项
const fetchRoleOptions = async () => {
  try {
    // 使用获取所有角色的API
    const response = await fetch('/api/role/all')
    const result = await response.json()
    if (result.code === 200) {
      roleOptions.value = result.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  fetchUserList()
  fetchRoleOptions()
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

.table-operations {
  margin: 15px 0;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.compact-form {
  .el-form-item {
    margin-bottom: 15px;
  }
}
</style>