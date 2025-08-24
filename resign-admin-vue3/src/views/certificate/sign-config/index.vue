<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="应用名称">
              <el-input
                v-model="searchForm.appName"
                placeholder="请输入应用名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="包名">
              <el-input
                v-model="searchForm.packageName"
                placeholder="请输入包名/Bundle ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="应用类型">
              <el-select v-model="searchForm.appType" placeholder="请选择应用类型" clearable style="width: 100%">
                <el-option label="iOS" value="iOS" />
                <el-option label="Android" value="Android" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label=" " class="search-buttons">
              <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
              <el-button @click="resetSearch" icon="Refresh">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="operation-card">
      <el-button type="primary" @click="showAddDialog" icon="Plus">新增配置</el-button>
      <el-button type="success" @click="refreshList" icon="Refresh">刷新</el-button>
      <el-button 
        type="danger" 
        @click="handleBatchDelete" 
        :disabled="selectedRows.length === 0"
        icon="Delete"
      >
        批量删除
      </el-button>
    </el-card>

    <!-- 签名配置列表 -->
    <el-card>
      <DataTable
        :data="configList"
        :columns="configColumns"
        :loading="loading"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #appType="{ row }">
          <el-tag :type="row.appType === 'iOS' ? 'primary' : 'success'">
            {{ row.appType }}
          </el-tag>
        </template>
        <template #configJson="{ row }">
          <el-button type="primary" link @click="viewConfig(row)">查看配置</el-button>
        </template>
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
        <template #actions="{ row }">
          <div class="table-actions">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </DataTable>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="formData.appName" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="包名/Bundle ID" prop="packageName">
          <el-input v-model="formData.packageName" placeholder="请输入包名或Bundle ID" />
        </el-form-item>
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="formData.appType" placeholder="请选择应用类型">
            <el-option label="iOS" value="iOS" />
            <el-option label="Android" value="Android" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置信息" prop="configJson">
          <el-input
            v-model="formData.configJson"
            type="textarea"
            :rows="6"
            placeholder="请输入JSON格式的配置信息"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述信息"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 配置详情对话框 -->
    <el-dialog
      v-model="configDetailVisible"
      title="配置详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应用名称">{{ configDetail.appName }}</el-descriptions-item>
        <el-descriptions-item label="包名/Bundle ID">{{ configDetail.packageName }}</el-descriptions-item>
        <el-descriptions-item label="应用类型">
          <el-tag :type="configDetail.appType === 'iOS' ? 'primary' : 'success'">
            {{ configDetail.appType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="configDetail.status === 1 ? 'success' : 'danger'">
            {{ configDetail.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ configDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ configDetail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ configDetail.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
          <h4 style="margin: 0;">配置信息 (JSON)</h4>
          <div v-if="!isEditingConfig">
            <el-button type="primary" size="small" @click="startEditConfig">编辑配置</el-button>
          </div>
          <div v-else>
            <el-button type="success" size="small" @click="saveConfig">保存</el-button>
            <el-button size="small" @click="cancelEditConfig">取消</el-button>
          </div>
        </div>
        
        <div style="border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden;">
           <Vue3JsonEditor
             ref="jsonEditorRef"
             v-model="editableConfigJson"
             :show-btns="false"
             :expandedOnStart="true"
             :mode="isEditingConfig ? 'code' : 'view'"
             :modes="['code', 'view']"
             :lang="'zh'"
             height="400px"
           />
         </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="configDetailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Vue3JsonEditor } from 'vue3-json-editor'
import DataTable from '@/components/DataTable.vue'
import { 
  getSignConfigPage, 
  createSignConfig, 
  updateSignConfig, 
  deleteSignConfig, 
  batchDeleteSignConfig,
  toggleSignConfigStatus
} from '@/api/sign-config'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const configList = ref([])
const selectedRows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const configDetailVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const jsonEditorRef = ref()
const editableConfigJson = ref({})
const isEditingConfig = ref(false)

// 表格列配置
const configColumns = [
  {
    type: 'selection',
    width: 55
  },
  {
    prop: 'appName',
    label: '应用名称',
    minWidth: 120
  },
  {
    prop: 'packageName',
    label: '包名/Bundle ID',
    minWidth: 200
  },
  {
    prop: 'appType',
    label: '应用类型',
    width: 100,
    slot: 'appType'
  },
  {
    prop: 'configJson',
    label: '配置信息',
    width: 120,
    slot: 'configJson'
  },
  {
    prop: 'status',
    label: '状态',
    width: 80,
    slot: 'status'
  },
  {
    prop: 'description',
    label: '描述',
    minWidth: 150,
    showOverflowTooltip: true
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 160
  },
  {
    prop: 'updateTime',
    label: '更新时间',
    width: 160
  },
  {
    label: '操作',
    width: 200,
    fixed: 'right',
    slot: 'actions'
  }
]

// 搜索表单
const searchForm = reactive({
  appName: '',
  packageName: '',
  appType: '',
  status: null
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10
})

// 表单数据
const formData = reactive({
  id: null,
  appName: '',
  packageName: '',
  appType: '',
  configJson: '',
  description: '',
  status: 1
})

// 配置详情
const configDetail = reactive({
  appName: '',
  packageName: '',
  appType: '',
  configJson: '',
  description: '',
  status: 1,
  createTime: '',
  updateTime: ''
})

// 表单验证规则
const formRules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' }
  ],
  packageName: [
    { required: true, message: '请输入包名/Bundle ID', trigger: 'blur' }
  ],
  appType: [
    { required: true, message: '请选择应用类型', trigger: 'change' }
  ],
  configJson: [
    { required: true, message: '请输入配置信息', trigger: 'blur' },
    { validator: validateJSON, trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑签名配置' : '新增签名配置')

// JSON验证器
function validateJSON(rule, value, callback) {
  if (!value) {
    callback(new Error('请输入配置信息'))
    return
  }
  try {
    JSON.parse(value)
    callback()
  } catch (error) {
    callback(new Error('请输入有效的JSON格式'))
  }
}

// 获取配置列表
const getConfigList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await getSignConfigPage(params)
    if (response.code === 200) {
      configList.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取配置列表失败')
    }
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getConfigList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    appName: '',
    packageName: '',
    appType: '',
    status: null
  })
  pagination.current = 1
  getConfigList()
}

// 刷新列表
const refreshList = () => {
  getConfigList()
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    appName: row.appName,
    packageName: row.packageName,
    appType: row.appType,
    configJson: row.configJson,
    description: row.description || '',
    status: row.status
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const apiCall = isEdit.value ? updateSignConfig : createSignConfig
    const params = isEdit.value ? { ...formData } : { ...formData }
    delete params.id // 新增时不需要id
    
    const response = await apiCall(isEdit.value ? formData.id : undefined, params)
    
    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      getConfigList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置 "${row.appName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await deleteSignConfig(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      getConfigList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的配置')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个配置吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    const response = await batchDeleteSignConfig(ids)
    
    if (response.code === 200) {
      ElMessage.success('批量删除成功')
      getConfigList()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}配置 "${row.appName}" 吗？`,
      `确认${action}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await toggleSignConfigStatus(row.id, newStatus)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      getConfigList()
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 查看配置详情
const viewConfig = (row) => {
  Object.assign(configDetail, row)
  try {
    editableConfigJson.value = JSON.parse(row.configJson || '{}')
  } catch (error) {
    editableConfigJson.value = {}
    ElMessage.warning('JSON格式不正确，已重置为空对象')
  }
  isEditingConfig.value = false
  configDetailVisible.value = true
}

// 开始编辑配置
const startEditConfig = () => {
  isEditingConfig.value = true
}

// 取消编辑配置
const cancelEditConfig = () => {
  try {
    editableConfigJson.value = JSON.parse(configDetail.configJson || '{}')
  } catch (error) {
    editableConfigJson.value = {}
  }
  isEditingConfig.value = false
}

// 保存配置
const saveConfig = async () => {
  try {
    const jsonString = JSON.stringify(editableConfigJson.value, null, 2)
    const response = await updateSignConfig(configDetail.id, {
      ...configDetail,
      configJson: jsonString
    })
    
    if (response.code === 200) {
      ElMessage.success('配置保存成功')
      configDetail.configJson = jsonString
      isEditingConfig.value = false
      refreshList()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存失败')
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  getConfigList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.current = current
  getConfigList()
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    id: null,
    appName: '',
    packageName: '',
    appType: '',
    configJson: '',
    description: '',
    status: 1
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getConfigList()
})
</script>

<style scoped>
.sign-config-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.operation-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table .cell) {
  padding: 8px 12px;
}
</style>