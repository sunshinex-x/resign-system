<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="邮箱">
              <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="启用" value="1" />
                <el-option label="禁用" value="0" />
              </el-select>
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
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
      <el-button type="danger" :disabled="selectedUsers.length === 0" @click="handleBatchDelete">批量删除</el-button>
      <el-button type="success" @click="refreshList">刷新</el-button>
    </div>

    <!-- 用户表格 -->
    <el-card class="table-card">
      <DataTable
        v-loading="loading"
        :data="userList"
        :columns="columns"
        :pagination="{ page: pagination.current, size: pagination.size, total }"
        :show-selection="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #roleName="{ row }">
          <el-tag :type="row.roleName === '管理员' ? 'danger' : 'primary'">
            {{ row.roleName }}
          </el-tag>
        </template>
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
        <template #createTime="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
        <template #lastLoginTime="{ row }">
          {{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '从未登录' }}
        </template>
        <template #actions="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small"
            @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </DataTable>
    </el-card>

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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/validate'
import { getUserList, createUser, updateUser, deleteUser, batchDeleteUsers, toggleUserStatus } from '@/api/user'
import DataTable from '@/components/DataTable.vue'
import { createListHandler } from '@/utils/listHandler'

// 创建列表处理器
const { loading, list: userList, total, pagination, listHandler } = createListHandler({
  pagination: {
    page: 1,
    size: 10
  },
  messages: {
    success: '获取用户列表成功',
    error: '获取用户列表失败'
  }
})

// 搜索表单
const searchForm = reactive({
  username: '',
  email: '',
  status: ''
})

// 其他数据
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

// 获取用户列表的请求函数
const fetchUserList = async (params) => {
  const requestParams = {
    current: params.page || 1,
    size: params.size || 10,
    ...searchForm
  }
  
  console.log('User API Request:', requestParams)
  const result = await getUserList(requestParams)
  console.log('User API Response:', result)
  
  // 直接返回原始API响应，让listHandler处理数据结构
  return result
}

// 获取用户列表（兼容原有调用）
const loadUserList = async () => {
  if (pagination.page === 1) {
    await listHandler.init(fetchUserList)
  } else {
    await listHandler.refresh(fetchUserList)
  }
}

// 搜索
const handleSearch = async () => {
  await listHandler.search(fetchUserList)
}

// 重置搜索
const resetSearch = async () => {
  Object.assign(searchForm, {
    username: '',
    email: '',
    status: ''
  })
  await listHandler.reset(fetchUserList)
}

// 刷新列表
const refreshList = async () => {
  await listHandler.refresh(fetchUserList)
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
      await toggleUserStatus(row.id, newStatus)
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
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      await listHandler.refresh(fetchUserList)
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
      await batchDeleteUsers(selectedIds)
      ElMessage.success('批量删除成功')
      await listHandler.refresh(fetchUserList)
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
          await updateUser(userForm.id, userForm)
          ElMessage.success('编辑成功')
        } else {
          // 新增用户
          await createUser(userForm)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        await listHandler.refresh(fetchUserList)
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
const handleSizeChange = async (val) => {
  await listHandler.handleSizeChange(val, fetchUserList)
}

// 当前页变化
const handleCurrentChange = async (val) => {
  await listHandler.handlePageChange(val, fetchUserList)
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

// 列配置
const columns = computed(() => [
  { prop: 'id', label: '用户ID', width: 80 },
  { prop: 'username', label: '用户名', minWidth: 120 },
  { prop: 'nickname', label: '昵称', minWidth: 120 },
  { prop: 'email', label: '邮箱', minWidth: 200 },
  { prop: 'phone', label: '手机号', minWidth: 120 },
  { prop: 'roleName', label: '角色', width: 100, slot: 'roleName' },
  { prop: 'status', label: '状态', width: 80, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 160, slot: 'createTime' },
  { prop: 'lastLoginTime', label: '最后登录', width: 160, slot: 'lastLoginTime' },
  { prop: 'actions', label: '操作', width: 220, slot: 'actions', fixed: 'right' }
])

// 初始化
onMounted(async () => {
  await listHandler.init(fetchUserList)
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

.search-card {
  margin-bottom: 20px;
  
  .search-form {
    .el-form-item {
      margin-bottom: 0;
    }
    
    .search-buttons {
      .el-form-item__content {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        
        .el-button {
          margin-left: 12px;
          
          &:first-child {
            margin-left: 0;
          }
        }
      }
    }
  }
}

.table-operations {
  margin-bottom: 20px;
  
  .el-button {
    margin-right: 12px;
    
    &:last-child {
      margin-right: 0;
    }
  }
}

.table-card {
  .user-table {
    width: 100%;
    
    .el-table__header {
      th {
        background: #fafbfc !important;
        color: #374151;
        font-weight: 600;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
  }
}
</style>