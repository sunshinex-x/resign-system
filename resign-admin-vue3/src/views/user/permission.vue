<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="权限名称">
              <el-input v-model="searchForm.name" placeholder="请输入权限名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="权限编码">
              <el-input v-model="searchForm.code" placeholder="请输入权限编码" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="权限类型">
              <el-select v-model="searchForm.type" placeholder="请选择权限类型" clearable style="width: 100%">
                <el-option label="菜单" value="menu" />
                <el-option label="按钮" value="button" />
                <el-option label="接口" value="api" />
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
      <el-button type="primary" @click="handleAdd">新增权限</el-button>
      <el-button type="success" @click="refreshList">刷新</el-button>
      <el-button type="info" @click="expandAll">展开所有</el-button>
      <el-button type="info" @click="collapseAll">收起所有</el-button>
    </div>
    
    <!-- 权限表格 -->
    <el-card class="table-card">
      <el-table
        ref="permissionTableRef"
        v-loading="loading"
        :data="permissionList"
        stripe
        class="permission-table"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      >
        <el-table-column prop="name" label="权限名称" min-width="200" />
        <el-table-column prop="code" label="权限编码" min-width="200" />
        <el-table-column prop="type" label="权限类型" width="100">
          <template #default="{row}">
            <el-tag :type="row.type === 'menu' ? 'primary' : (row.type === 'button' ? 'success' : 'info')">
              {{ row.type === 'menu' ? '菜单' : (row.type === 'button' ? '按钮' : '接口') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径/接口" min-width="150" show-overflow-tooltip />
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
        <el-table-column label="操作" width="220" fixed="right">
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
                type="success"
                size="small"
                @click="handleAddChild(row)"
                v-if="row.type === 'menu'"
              >
                添加子权限
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
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
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
import { getPermissionList, createPermission, updatePermission, deletePermission } from '@/api/user'
import { formatDateTime } from '@/utils/validate'

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  type: ''
})

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



// 搜索
const handleSearch = () => {
  fetchPermissionList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  fetchPermissionList()
}

// 获取权限列表
const fetchPermissionList = async () => {
  loading.value = true
  
  try {
    const response = await getPermissionList()
    console.log('权限API响应:', response)
    
    // 确保数据格式正确
    let data = response.data
    if (response.code === 200 && response.data) {
      data = response.data.list || response.data
    }
    
    // 验证数据是否为数组
    if (Array.isArray(data)) {
      permissionList.value = data
    } else {
      console.warn('权限API返回的数据不是数组格式:', data)
      permissionList.value = []
    }
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败')
    permissionList.value = []
  } finally {
    loading.value = false
  }
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
  }).then(async () => {
    try {
      const newStatus = row.status === 1 ? 0 : 1
      await updatePermission(row.id, { ...row, status: newStatus })
      row.status = newStatus
      ElMessage.success(`${action}成功`)
    } catch (error) {
      console.error('更新权限状态失败:', error)
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

// 删除权限
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除权限 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePermission(row.id)
      ElMessage.success('删除成功')
      fetchPermissionList() // 重新加载列表
    } catch (error) {
      console.error('删除权限失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!permissionFormRef.value) return
  
  await permissionFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑权限
          await updatePermission(permissionForm.id, permissionForm)
          ElMessage.success('编辑成功')
        } else {
          // 新增权限
          await createPermission(permissionForm)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchPermissionList() // 重新加载列表
      } catch (error) {
        console.error('保存权限失败:', error)
        ElMessage.error('保存失败')
      }
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
  .permission-table {
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