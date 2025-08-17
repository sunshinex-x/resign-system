<template>
  <div class="test-container">
    <h1>菜单测试页面</h1>
    <p>当前路由: {{ $route.path }}</p>
    <p>如果你能看到这个页面，说明路由配置是正确的。</p>
    
    <div class="route-info">
      <h3>路由信息:</h3>
      <pre>{{ JSON.stringify($route, null, 2) }}</pre>
    </div>
    
    <div class="navigation-test">
      <h3>导航测试:</h3>
      <el-button @click="goToCert">跳转到证书管理</el-button>
      <el-button @click="goToProfile">跳转到描述文件管理</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToCert = () => {
  router.push('/certificate/cert')
}

const goToProfile = () => {
  router.push('/certificate/profile')
}
</script>

<style scoped>
.test-container {
  padding: 20px;
}

.route-info, .navigation-test {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
}
</style>