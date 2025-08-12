<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>日志管理</span>
        </div>
      </template>
      
      <div class="log-content">
        <h3>系统日志</h3>
        <p>此页面将展示系统日志，包括操作日志、错误日志等。</p>
        <el-empty description="日志管理功能开发中，敬请期待" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 日志管理页面
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

.log-content {
  padding: 20px 0;
  text-align: center;
}
</style>