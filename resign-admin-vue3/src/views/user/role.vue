<template>
  <div class="app-container">
    <!-- 操作按钮 -->
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
      <el-button type="success" @click="refreshList">刷新</el-button>
    </div>
    
    <!-- 角色表格 -->
    <el-table
      v-loading="loading"
      :data="roleList"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="角色ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="roleCode" label="角色编码" width="150" />
      <el-table-column prop="description" label="角色描述" show-overflow-tooltip />
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
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{row}">
          <el-button
            type="primary"
            size="small"
            link
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            type="info"
            size="small"
            link
            @click="handlePermission(row)"
          >
            权限配置
          </el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            size="small"
            link
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            type="danger"
            size="small"
            link
            @click="handleDelete(row)"
            :disabled="row.id === 1"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
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

// 模拟角色数据
const mockRoles = [
  {
    id: 1,
    roleName: '超级管理员',
    roleCode: 'SUPER_ADMIN',
    description: '系统超级管理员，拥有所有权限',
    status: 1,
    createTime: new Date('2023-01-01')
  },
  {
    id: 2,
    roleName: '普通用户',
    roleCode: 'USER',
    description: '普通用户，只能查看和操作自己的数据',
    status: 1,
    createTime: new Date('2023-02-01')
  },
  {
    id: 3,
    roleName: '审核员',
    roleCode: 'AUDITOR',
    description: '负责审核任务和数据',
    status: 1,
    createTime: new Date('2023-03-01')
  }
]

// 模拟权限树数据
const permissionTree = [
  {
    id: 1,
    name: '控制台',
    children: [
      { id: 11, name: '查看控制台' }
    ]
  },
  {
    id: 2,
    name: '任务管理',
    children: [
      { id: 21, name: '查看任务列表' },
      { id: 22, name: '创建任务' },
      { id: 23, name: '编辑任务' },
      { id: 24, name: '删除任务' },
      { id: 25, name: '重试任务' }
    ]
  },
  {
    id: 3,
    name: '用户管理',
    children: [
      { id: 31, name: '查看用户列表' },
      { id: 32, name: '创建用户' },
      { id: 33, name: '编辑用户' },
      { id: 34, name: '删除用户' },
      { id: 35, name: '重置密码' }
    ]
  },
  {
    id: 4,
    name: '系统管理',
    children: [
      { id: 41, name: '查看日志' },
      { id: 42, name: '系统配置' }
    ]
  }
]

// 获取角色列表
const fetchRoleList = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    roleList.value = [...mockRoles]
    loading.value = false
  }, 500)
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
const handlePermission = (row) => {
  currentRole.value = row
  // 模拟获取角色已有权限
  if (row.id === 1) {
    // 超级管理员拥有所有权限
    checkedPermissions.value = [11, 21, 22, 23, 24, 25, 31, 32, 33, 34, 35, 41, 42]
  } else if (row.id === 2) {
    // 普通用户只有查看权限
    checkedPermissions.value = [11, 21]
  } else {
    // 审核员有部分权限
    checkedPermissions.value = [11, 21, 22, 25, 41]
  }
  permissionDialogVisible.value = true
}

// 切换角色状态
const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  }).catch(() => {})
}

// 删除角色
const handleDelete = (row) => {
  if (row.id === 1) {
    ElMessage.warning('超级管理员角色不能删除')
    return
  }
  
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = roleList.value.findIndex(role => role.id === row.id)
    if (index > -1) {
      roleList.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      if (isEdit.value) {
        // 编辑角色
        const index = roleList.value.findIndex(role => role.id === roleForm.id)
        if (index > -1) {
          Object.assign(roleList.value[index], roleForm)
        }
        ElMessage.success('编辑成功')
      } else {
        // 新增角色
        const newRole = {
          ...roleForm,
          id: Date.now(),
          createTime: new Date()
        }
        roleList.value.push(newRole)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
    }
  })
}

// 权限选择变化
const handlePermissionCheck = (data, checked) => {
  // 处理权限选择逻辑
}

// 保存权限配置
const handleSavePermission = () => {
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
  const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]
  
  ElMessage.success(`已为角色 "${currentRole.value.roleName}" 配置 ${allCheckedKeys.length} 个权限`)
  permissionDialogVisible.value = false
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

.table-operations {
  margin-bottom: 15px;
}
</style>