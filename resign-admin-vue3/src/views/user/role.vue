<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="角色名称">
              <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="角色编码">
              <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" clearable />
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
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
      <el-button type="success" @click="refreshList">刷新</el-button>
    </div>
    
    <!-- 角色表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="roleList"
        stripe
        class="role-table"
      >
        <el-table-column prop="id" label="角色ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="150" />
        <el-table-column prop="description" label="角色描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{row}">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <div class="table-actions">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="handlePermission(row)"
              >
                权限配置
              </el-button>
              <el-button
                :type="row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                :disabled="row.id === 1"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 角色编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="roleForm" :rules="rules" ref="roleFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
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
    
    <!-- 权限配置对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="权限配置" width="600px">
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="treeProps"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermissions"
        @check="handlePermissionCheck"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSavePermission">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/validate'
import { 
  getRoleList, 
  createRole, 
  updateRole, 
  deleteRole, 
  getRolePermissions, 
  setRolePermissions,
  getPermissionTree 
} from '@/api/user'

// 搜索表单
const searchForm = reactive({
  roleName: '',
  roleCode: '',
  status: ''
})

// 数据
const roleList = ref([])
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const roleFormRef = ref(null)

// 权限配置对话框
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref(null)
const currentRole = ref(null)
const checkedPermissions = ref([])

// 角色表单
const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 权限树配置
const treeProps = {
  children: 'children',
  label: 'name'
}



// 权限树数据
const permissionTree = ref([])

// 搜索
const handleSearch = () => {
  fetchRoleList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  fetchRoleList()
}

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    const params = {
      roleName: searchForm.roleName,
      roleCode: searchForm.roleCode,
      status: searchForm.status
    }
    const response = await getRoleList(params)
    console.log('API响应:', response)
    
    // 确保数据是数组格式
    let data = response.data
    if (response.code === 200 && response.data) {
      data = response.data.records || response.data
    }
    
    // 验证数据是否为数组
    if (Array.isArray(data)) {
      roleList.value = data
    } else {
      console.warn('API返回的数据不是数组格式:', data)
      roleList.value = []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
    roleList.value = []
  } finally {
    loading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  fetchRoleList()
}

// 新增角色
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  isEdit.value = false
  resetRoleForm()
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  Object.assign(roleForm, row)
  dialogVisible.value = true
}

// 权限配置
const handlePermission = async (row) => {
  currentRole.value = row
  try {
    // 获取权限树
    const { data: treeData } = await getPermissionTree()
    permissionTree.value = treeData || []
    
    // 获取角色已有权限
    const { data: permissions } = await getRolePermissions(row.id)
    checkedPermissions.value = permissions || []
    
    permissionDialogVisible.value = true
  } catch (error) {
    console.error('获取权限数据失败:', error)
    ElMessage.error('获取权限数据失败')
  }
}

// 切换角色状态
const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const newStatus = row.status === 1 ? 0 : 1
      await updateRole(row.id, { ...row, status: newStatus })
      row.status = newStatus
      ElMessage.success(`${action}成功`)
    } catch (error) {
      console.error(`${action}角色失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

// 删除角色
const handleDelete = (row) => {
  if (row.roleCode === 'SUPER_ADMIN') {
    ElMessage.warning('超级管理员角色不能删除')
    return
  }
  
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      fetchRoleList() // 重新加载列表
    } catch (error) {
      console.error('删除角色失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑角色
          await updateRole(roleForm.id, roleForm)
          ElMessage.success('编辑成功')
        } else {
          // 新增角色
          await createRole(roleForm)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchRoleList() // 重新加载列表
      } catch (error) {
        console.error('保存角色失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

// 权限选择变化
const handlePermissionCheck = (data, checked) => {
  // 处理权限选择逻辑
}

// 保存权限配置
const handleSavePermission = async () => {
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]
    
    await setRolePermissions(currentRole.value.id, allCheckedKeys)
    ElMessage.success(`已为角色 "${currentRole.value.roleName}" 配置 ${allCheckedKeys.length} 个权限`)
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('保存权限配置失败:', error)
    ElMessage.error('保存权限配置失败')
  }
}

// 重置角色表单
const resetRoleForm = () => {
  Object.assign(roleForm, {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  })
  if (roleFormRef.value) {
    roleFormRef.value.resetFields()
  }
}

// 初始化
onMounted(() => {
  fetchRoleList()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
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
  .role-table {
    width: 100%;
    
    .el-table__header {
      th {
        background: #fafbfc !important;
        color: #374151;
        font-weight: 600;
      }
    }
  }
}
</style>