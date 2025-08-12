<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
        </div>
      </template>
      
      <div class="config-content">
        <h3>系统参数配置</h3>
        <p>此页面将展示系统配置参数，包括重签名配置、通知配置等。</p>
        <el-empty description="系统配置功能开发中，敬请期待" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 系统配置页面
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

.config-content {
  padding: 20px 0;
  text-align: center;
}
</style>