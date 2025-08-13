<template>
  <div class="app-container">
    <!-- 操作按钮 -->
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">新增权限</el-button>
      <el-button type="success" @click="refreshList">刷新</el-button>
      <el-button type="info" @click="expandAll">展开所有</el-button>
      <el-button type="info" @click="collapseAll">收起所有</el-button>
    </div>
    
    <!-- 权限表格 -->
    <el-table
      ref="permissionTableRef"
      v-loading="loading"
      :data="permissionList"
      border
      stripe
      style="width: 100%"
      row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      :default-expand-all="false"
    >
      <el-table-column prop="name" label="权限名称" width="200" />
      <el-table-column prop="code" label="权限编码" width="200" />
      <el-table-column prop="type" label="权限类型" width="100">
        <template #default="{row}">
          <el-tag :type="row.type === 'menu' ? 'primary' : (row.type === 'button' ? 'success' : 'info')">
            {{ row.type === 'menu' ? '菜单' : (row.type === 'button' ? '按钮' : '接口') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径/接口" show-overflow-tooltip />
      <el-table-column prop="icon" label="图标" width="80">
        <template #default="{row}">
          <el-icon v-if="row.icon">
            <component :is="row.icon" />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
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
      <el-table-column label="操作" width="200" fixed="right">
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
            type="success"
            size="small"
            link
            @click="handleAddChild(row)"
            v-if="row.type === 'menu'"
          >
            添加子权限
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
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 权限编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="permissionForm" :rules="rules" ref="permissionFormRef" label-width="100px">
        <el-form-item label="上级权限" v-if="parentPermission">
          <el-input :value="parentPermission.name" disabled />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="permissionForm.code" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-select v-model="permissionForm.type" placeholder="请选择权限类型" style="width: 100%">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="接口" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径/接口" prop="path" v-if="permissionForm.type !== 'button'">
          <el-input v-model="permissionForm.path" placeholder="请输入路径或接口地址" />
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="permissionForm.type === 'menu'">
          <el-select v-model="permissionForm.icon" placeholder="请选择图标" style="width: 100%">
            <el-option label="Document" value="Document" />
            <el-option label="User" value="User" />
            <el-option label="Setting" value="Setting" />
            <el-option label="List" value="List" />
            <el-option label="Plus" value="Plus" />
            <el-option label="Edit" value="Edit" />
            <el-option label="Delete" value="Delete" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="permissionForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="permissionForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="permissionForm.description" type="textarea" rows="3" placeholder="请输入权限描述" />
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

// 数据
const permissionList = ref([])
const loading = ref(false)
const permissionTableRef = ref(null)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const permissionFormRef = ref(null)
const parentPermission = ref(null)

// 权限表单
const permissionForm = reactive({
  id: null,
  parentId: null,
  name: '',
  code: '',
  type: 'menu',
  path: '',
  icon: '',
  sort: 0,
  status: 1,
  description: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[A-Z_:]+$/, message: '权限编码只能包含大写字母、下划线和冒号', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ]
}

// 模拟权限数据
const mockPermissions = [
  {
    id: 1,
    parentId: null,
    name: '控制台',
    code: 'DASHBOARD',
    type: 'menu',
    path: '/dashboard',
    icon: 'Odometer',
    sort: 1,
    status: 1,
    description: '系统控制台',
    createTime: new Date('2023-01-01'),
    children: [
      {
        id: 11,
        parentId: 1,
        name: '查看控制台',
        code: 'DASHBOARD:VIEW',
        type: 'button',
        path: '',
        icon: '',
        sort: 1,
        status: 1,
        description: '查看控制台权限',
        createTime: new Date('2023-01-01')
      }
    ]
  },
  {
    id: 2,
    parentId: null,
    name: '任务管理',
    code: 'TASK',
    type: 'menu',
    path: '/task',
    icon: 'Document',
    sort: 2,
    status: 1,
    description: '任务管理模块',
    createTime: new Date('2023-01-01'),
    children: [
      {
        id: 21,
        parentId: 2,
        name: '任务列表',
        code: 'TASK:LIST',
        type: 'menu',
        path: '/task/list',
        icon: 'List',
        sort: 1,
        status: 1,
        description: '任务列表页面',
        createTime: new Date('2023-01-01')
      },
      {
        id: 22,
        parentId: 2,
        name: '创建任务',
        code: 'TASK:CREATE',
        type: 'button',
        path: '',
        icon: '',
        sort: 2,
        status: 1,
        description: '创建任务权限',
        createTime: new Date('2023-01-01')
      },
      {
        id: 23,
        parentId: 2,
        name: '编辑任务',
        code: 'TASK:EDIT',
        type: 'button',
        path: '',
        icon: '',
        sort: 3,
        status: 1,
        description: '编辑任务权限',
        createTime: new Date('2023-01-01')
      },
      {
        id: 24,
        parentId: 2,
        name: '删除任务',
        code: 'TASK:DELETE',
        type: 'button',
        path: '',
        icon: '',
        sort: 4,
        status: 1,
        description: '删除任务权限',
        createTime: new Date('2023-01-01')
      }
    ]
  },
  {
    id: 3,
    parentId: null,
    name: '用户管理',
    code: 'USER',
    type: 'menu',
    path: '/user',
    icon: 'User',
    sort: 3,
    status: 1,
    description: '用户管理模块',
    createTime: new Date('2023-01-01'),
    children: [
      {
        id: 31,
        parentId: 3,
        name: '用户列表',
        code: 'USER:LIST',
        type: 'menu',
        path: '/user/list',
        icon: 'UserFilled',
        sort: 1,
        status: 1,
        description: '用户列表页面',
        createTime: new Date('2023-01-01')
      },
      {
        id: 32,
        parentId: 3,
        name: '角色管理',
        code: 'USER:ROLE',
        type: 'menu',
        path: '/user/role',
        icon: 'Avatar',
        sort: 2,
        status: 1,
        description: '角色管理页面',
        createTime: new Date('2023-01-01')
      }
    ]
  }
]

// 获取权限列表
const fetchPermissionList = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    permissionList.value = [...mockPermissions]
    loading.value = false
  }, 500)
}

// 刷新列表
const refreshList = () => {
  fetchPermissionList()
}

// 展开所有
const expandAll = () => {
  // 递归展开所有节点
  const expandNode = (data) => {
    data.forEach(item => {
      permissionTableRef.value.toggleRowExpansion(item, true)
      if (item.children && item.children.length > 0) {
        expandNode(item.children)
      }
    })
  }
  expandNode(permissionList.value)
}

// 收起所有
const collapseAll = () => {
  // 递归收起所有节点
  const collapseNode = (data) => {
    data.forEach(item => {
      permissionTableRef.value.toggleRowExpansion(item, false)
      if (item.children && item.children.length > 0) {
        collapseNode(item.children)
      }
    })
  }
  collapseNode(permissionList.value)
}

// 新增权限
const handleAdd = () => {
  dialogTitle.value = '新增权限'
  isEdit.value = false
  parentPermission.value = null
  resetPermissionForm()
  dialogVisible.value = true
}

// 添加子权限
const handleAddChild = (row) => {
  dialogTitle.value = '新增子权限'
  isEdit.value = false
  parentPermission.value = row
  resetPermissionForm()
  permissionForm.parentId = row.id
  dialogVisible.value = true
}

// 编辑权限
const handleEdit = (row) => {
  dialogTitle.value = '编辑权限'
  isEdit.value = true
  parentPermission.value = null
  Object.assign(permissionForm, row)
  dialogVisible.value = true
}

// 切换权限状态
const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}权限 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  }).catch(() => {})
}

// 删除权限
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除权限 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 递归删除权限
    const deletePermission = (list, id) => {
      for (let i = 0; i < list.length; i++) {
        if (list[i].id === id) {
          list.splice(i, 1)
          return true
        }
        if (list[i].children && list[i].children.length > 0) {
          if (deletePermission(list[i].children, id)) {
            return true
          }
        }
      }
      return false
    }
    
    deletePermission(permissionList.value, row.id)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!permissionFormRef.value) return
  
  await permissionFormRef.value.validate(async (valid) => {
    if (valid) {
      if (isEdit.value) {
        // 编辑权限
        const updatePermission = (list, form) => {
          for (let item of list) {
            if (item.id === form.id) {
              Object.assign(item, form)
              return true
            }
            if (item.children && item.children.length > 0) {
              if (updatePermission(item.children, form)) {
                return true
              }
            }
          }
          return false
        }
        
        updatePermission(permissionList.value, permissionForm)
        ElMessage.success('编辑成功')
      } else {
        // 新增权限
        const newPermission = {
          ...permissionForm,
          id: Date.now(),
          createTime: new Date(),
          children: []
        }
        
        if (permissionForm.parentId) {
          // 添加到父权限的children中
          const addToParent = (list, parentId, newItem) => {
            for (let item of list) {
              if (item.id === parentId) {
                if (!item.children) {
                  item.children = []
                }
                item.children.push(newItem)
                return true
              }
              if (item.children && item.children.length > 0) {
                if (addToParent(item.children, parentId, newItem)) {
                  return true
                }
              }
            }
            return false
          }
          
          addToParent(permissionList.value, permissionForm.parentId, newPermission)
        } else {
          // 添加为顶级权限
          permissionList.value.push(newPermission)
        }
        
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
    }
  })
}

// 重置权限表单
const resetPermissionForm = () => {
  Object.assign(permissionForm, {
    id: null,
    parentId: null,
    name: '',
    code: '',
    type: 'menu',
    path: '',
    icon: '',
    sort: 0,
    status: 1,
    description: ''
  })
  if (permissionFormRef.value) {
    permissionFormRef.value.resetFields()
  }
}

// 初始化
onMounted(() => {
  fetchPermissionList()
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