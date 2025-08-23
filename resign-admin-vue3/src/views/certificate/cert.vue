<template>
  <div class="app-container">
    <!-- 证书管理导航 -->
    <el-card class="navigation-card">
      <div class="navigation-header">
        <h2>证书管理</h2>
        <p>选择平台进行证书管理</p>
      </div>
      
      <el-row :gutter="20" class="platform-cards">
        <el-col :span="12">
          <el-card class="platform-card ios-card" shadow="hover" @click="goToIosCert">
            <div class="platform-content">
              <div class="platform-icon">
                <el-icon size="48"><Apple /></el-icon>
              </div>
              <div class="platform-info">
                <h3>iOS 证书管理</h3>
                <p>管理 iOS 开发和发布证书</p>
                <ul class="feature-list">
                  <li>开发证书管理</li>
                  <li>发布证书管理</li>
                  <li>描述文件关联</li>
                  <li>证书有效性验证</li>
                </ul>
              </div>
            </div>
            <div class="platform-footer">
              <el-button type="primary" size="large">进入 iOS 证书管理</el-button>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="platform-card android-card" shadow="hover" @click="goToAndroidCert">
            <div class="platform-content">
              <div class="platform-icon">
                <el-icon size="48"><Android /></el-icon>
              </div>
              <div class="platform-info">
                <h3>Android 证书管理</h3>
                <p>管理 Android 签名证书</p>
                <ul class="feature-list">
                  <li>签名证书管理</li>
                  <li>密钥库管理</li>
                  <li>证书信息查看</li>
                  <li>证书有效性验证</li>
                </ul>
              </div>
            </div>
            <div class="platform-footer">
              <el-button type="success" size="large">进入 Android 证书管理</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 统计信息 -->
    <el-card class="stats-card">
      <div class="stats-header">
        <h3>证书统计</h3>
      </div>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总证书数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value ios">{{ stats.ios }}</div>
            <div class="stat-label">iOS 证书</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value android">{{ stats.android }}</div>
            <div class="stat-label">Android 证书</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value expired">{{ stats.expired }}</div>
            <div class="stat-label">即将过期</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近证书 -->
    <el-card class="recent-certs-card">
      <template #header>
        <div class="card-header">
          <span>最近证书</span>
          <el-button type="text" @click="refreshRecentCerts">刷新</el-button>
        </div>
      </template>
      
      <el-table v-loading="recentCertsLoading" :data="recentCerts" style="width: 100%">
        <el-table-column prop="name" label="证书名称" width="200" />
        <el-table-column label="平台" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.platform === 'IOS' ? 'primary' : 'success'">
              {{ scope.row.platform === 'IOS' ? 'iOS' : 'Android' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="expiryDate" label="过期时间" width="120">
          <template #default="scope">
            <span :class="{ 'text-danger': isExpiringSoon(scope.row.expiryDate) }">
              {{ scope.row.expiryDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewCertDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Apple, Android } from '@element-plus/icons-vue'
import { getCertificateStats, getRecentCertificates } from '@/api/certificate'

const router = useRouter()

// 响应式数据
const stats = ref({
  total: 0,
  ios: 0,
  android: 0,
  expired: 0
})

const recentCerts = ref([])
const recentCertsLoading = ref(false)

// 导航到iOS证书管理
const goToIosCert = () => {
  router.push('/certificate/ios')
}

// 导航到Android证书管理
const goToAndroidCert = () => {
  router.push('/certificate/android')
}

// 获取证书统计
const getCertStats = async () => {
  try {
    const response = await getCertificateStats()
    stats.value = response.data
  } catch (error) {
    console.error('获取证书统计失败:', error)
  }
}

// 获取最近证书
const getRecentCerts = async () => {
  recentCertsLoading.value = true
  try {
    const response = await getRecentCertificates({ size: 10 })
    recentCerts.value = response.data
  } catch (error) {
    console.error('获取最近证书失败:', error)
  } finally {
    recentCertsLoading.value = false
  }
}

// 刷新最近证书
const refreshRecentCerts = () => {
  getRecentCerts()
}

// 查看证书详情
const viewCertDetail = (cert) => {
  const platform = cert.platform === 'IOS' ? 'ios' : 'android'
  router.push(`/certificate/${platform}?id=${cert.id}`)
}

// 检查证书是否即将过期
const isExpiringSoon = (expiryDate) => {
  if (!expiryDate) return false
  const expiry = new Date(expiryDate)
  const now = new Date()
  const diffTime = expiry.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 30 && diffDays >= 0
}

// 组件挂载时获取数据
onMounted(() => {
  getCertStats()
  getRecentCerts()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.navigation-card {
  margin-bottom: 20px;
}

.navigation-header {
  text-align: center;
  margin-bottom: 30px;
}

.navigation-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
}

.navigation-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.platform-cards {
  margin-top: 20px;
}

.platform-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 280px;
}

.platform-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.ios-card {
  border-left: 4px solid #007AFF;
}

.android-card {
  border-left: 4px solid #3DDC84;
}

.platform-content {
  display: flex;
  flex-direction: column;
  height: 200px;
}

.platform-icon {
  text-align: center;
  margin-bottom: 20px;
}

.ios-card .platform-icon {
  color: #007AFF;
}

.android-card .platform-icon {
  color: #3DDC84;
}

.platform-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 18px;
  text-align: center;
}

.platform-info p {
  margin: 0 0 15px 0;
  color: #909399;
  font-size: 14px;
  text-align: center;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  padding: 5px 0;
  color: #606266;
  font-size: 13px;
  position: relative;
  padding-left: 15px;
}

.feature-list li:before {
  content: '•';
  position: absolute;
  left: 0;
  color: #409EFF;
}

.platform-footer {
  text-align: center;
  margin-top: auto;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-header {
  margin-bottom: 20px;
}

.stats-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: #e9ecef;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-value.ios {
  color: #007AFF;
}

.stat-value.android {
  color: #3DDC84;
}

.stat-value.expired {
  color: #F56C6C;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.recent-certs-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-danger {
  color: #F56C6C;
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