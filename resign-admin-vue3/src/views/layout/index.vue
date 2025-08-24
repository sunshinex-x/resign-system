<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
      <div class="logo-container">
        <img src="@/assets/logo.svg" alt="logo" class="logo" />
        <h1 class="title" v-show="!isCollapse">重签名管理系统</h1>
      </div>
      
      <!-- 菜单 -->
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          :collapse-transition="false"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.8)"
          active-text-color="#ffffff"
          router
          class="sidebar-menu"
        >
          <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="''" />
        </el-menu>
      </el-scrollbar>
    </div>
    
    <!-- 主区域 -->
    <div class="main-container">
      <!-- 头部 -->
      <div class="navbar">
        <div class="left-menu">
          <el-icon class="hamburger" @click="toggleSidebar">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <breadcrumb />
        </div>
        
        <div class="right-menu">
          <el-dropdown trigger="click">
            <div class="avatar-container">
              <el-avatar :size="30" :src="userInfo.avatar" icon="UserFilled" />
              <span class="username">{{ userInfo.nickname || userInfo.username || '管理员' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import SidebarItem from './components/SidebarItem.vue'
import Breadcrumb from './components/Breadcrumb.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 获取路由
const routes = computed(() => {
  return router.options.routes.filter(route => !route.meta?.hidden)
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 获取用户信息
const userInfo = computed(() => authStore.currentUser)

// 跳转到个人中心
const goToProfile = () => {
  router.push('/profile')
}

// 退出登录
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    authStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  display: flex;
}

.sidebar-container {
  width: 220px;
  height: 100%;
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  transition: width 0.28s;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  
  &.is-collapse {
    width: 64px;
  }
  
  .logo-container {
    height: 60px;
    padding: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.15);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    .logo {
      width: 32px;
      height: 32px;
      border-radius: 6px;
    }
    
    .title {
      margin-left: 12px;
      color: #fff;
      font-weight: 600;
      font-size: 16px;
      white-space: nowrap;
      letter-spacing: 0.5px;
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid #f0f0f0;
  
  .left-menu {
    display: flex;
    align-items: center;
    
    .hamburger {
      padding: 8px;
      cursor: pointer;
      font-size: 18px;
      color: #606266;
      border-radius: 6px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
        color: #409eff;
      }
    }
  }
  
  .right-menu {
    display: flex;
    align-items: center;
    
    .avatar-container {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .username {
        margin: 0 8px;
        font-weight: 500;
        color: #303133;
      }
      
      .el-icon {
        color: #909399;
        transition: transform 0.3s;
      }
    }
  }
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #f8fafc 100%);
  min-height: calc(100vh - 60px);
}

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 自定义滚动条 */
:deep(.el-scrollbar__bar) {
  opacity: 0.3;
}

:deep(.el-scrollbar__thumb) {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

:deep(.el-scrollbar__thumb:hover) {
  background-color: rgba(255, 255, 255, 0.5);
}

/* 菜单样式优化 */
.sidebar-menu {
  border: none;
  
  :deep(.el-menu-item) {
    margin: 6px 12px;
    border-radius: 10px;
    transition: all 0.3s;
    color: rgba(255, 255, 255, 0.85) !important;
    text-decoration: none !important;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.12) !important;
      color: #ffffff !important;
      transform: translateX(4px);
      text-decoration: none !important;
    }
    
    &.is-active {
      background: rgba(255, 255, 255, 0.15) !important;
      color: #ffffff !important;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      transform: translateX(4px);
      text-decoration: none !important;
      
      &::before {
        content: '';
        position: absolute;
        left: -12px;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 20px;
        background: #ffffff;
        border-radius: 2px;
      }
    }
  }
  
  :deep(.el-sub-menu__title) {
    margin: 6px 12px;
    border-radius: 10px;
    transition: all 0.3s;
    color: rgba(255, 255, 255, 0.85) !important;
    text-decoration: none !important;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.12) !important;
      color: #ffffff !important;
      transform: translateX(4px);
      text-decoration: none !important;
    }
  }
  
  :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
    background: rgba(255, 255, 255, 0.15) !important;
    color: #ffffff !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    transform: translateX(4px);
    text-decoration: none !important;
  }
  
  :deep(.el-menu-item-group__title) {
    padding: 7px 0 7px 20px;
    color: rgba(255, 255, 255, 0.6);
    font-size: 12px;
  }
  
  // 修复子菜单弹出层样式
  :deep(.el-sub-menu__icon-arrow) {
    color: rgba(255, 255, 255, 0.85) !important;
  }
}

/* 折叠状态下的样式修复 */
.sidebar-container.is-collapse {
  .sidebar-menu {
    :deep(.el-menu-item) {
      span {
        display: none !important;
      }
    }
    
    :deep(.el-sub-menu__title) {
      span {
        display: none !important;
      }
    }
  }
}

/* 修复子菜单弹出层的样式问题 */
:deep(.el-menu--popup),
:deep(.el-popper),
:deep(.el-menu--popup-container) {
  background-color: #2c3e50 !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3) !important;
  
  .el-menu-item {
    background-color: transparent !important;
    color: rgba(255, 255, 255, 0.85) !important;
    margin: 0 !important;
    border-radius: 0 !important;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.12) !important;
      color: #ffffff !important;
    }
    
    &.is-active {
      background: rgba(255, 255, 255, 0.15) !important;
      color: #ffffff !important;
    }
  }
  
  .el-sub-menu__title {
    background-color: transparent !important;
    color: rgba(255, 255, 255, 0.85) !important;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.12) !important;
      color: #ffffff !important;
    }
  }
}

/* 下拉菜单样式优化 */
:deep(.el-dropdown-menu) {
  background: #ffffff !important;
  border: 2px solid transparent !important;
  background-image: linear-gradient(white, white), linear-gradient(135deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%) !important;
  background-origin: border-box !important;
  background-clip: content-box, border-box !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 25px rgba(209, 12, 12, 0.15) !important;
  padding: 8px 0 !important;
  
  .el-dropdown-menu__item {
    color: #374151 !important;
    padding: 12px 20px !important;
    transition: all 0.3s ease !important;
    
    &:hover {
      background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%) !important;
      color: #1f2937 !important;
    }
    
    &.is-divided {
      border-top: 1px solid #e5e7eb !important;
      margin-top: 4px !important;
      padding-top: 12px !important;
    }
  }
}
</style>