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
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
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
              <el-avatar :size="30" icon="UserFilled" />
              <span class="username">管理员</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
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
import SidebarItem from './components/SidebarItem.vue'
import Breadcrumb from './components/Breadcrumb.vue'

const router = useRouter()
const route = useRoute()

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

// 退出登录
const logout = () => {
  // 清除token等登录信息
  // localStorage.removeItem('token')
  
  // 跳转到登录页
  router.push('/login')
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
  width: 210px;
  height: 100%;
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;
  
  &.is-collapse {
    width: 64px;
  }
  
  .logo-container {
    height: 50px;
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #2b3649;
    
    .logo {
      width: 32px;
      height: 32px;
    }
    
    .title {
      margin-left: 10px;
      color: #fff;
      font-weight: 600;
      font-size: 16px;
      white-space: nowrap;
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
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  background-color: #fff;
  
  .left-menu {
    display: flex;
    align-items: center;
    
    .hamburger {
      padding: 0 15px;
      cursor: pointer;
      font-size: 20px;
    }
  }
  
  .right-menu {
    display: flex;
    align-items: center;
    
    .avatar-container {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .username {
        margin: 0 5px;
      }
    }
  }
}

.app-main {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background-color: #f5f7fa;
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
</style>