<template>
  <div v-if="item && (!item.meta || !item.meta.hidden)">
    <!-- 判断是否显示为单个菜单项还是子菜单 -->
    <template v-if="isShowAsMenuItem">
      <app-link :to="menuItemPath">
        <el-menu-item :index="menuItemPath">
          <el-icon v-if="menuItemIcon">
            <component :is="menuItemIcon" />
          </el-icon>
          <template #title>
            <span>{{ menuItemTitle }}</span>
          </template>
        </el-menu-item>
      </app-link>
    </template>
    
    <!-- 多级菜单 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta && item.meta.title }}</span>
      </template>
      
      <!-- 递归渲染子菜单 -->
      <sidebar-item
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { isExternal } from '@/utils/validate'
import AppLink from './Link.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

// 获取可见的子菜单
const visibleChildren = computed(() => {
  if (!props.item.children || !Array.isArray(props.item.children)) {
    return []
  }
  return props.item.children.filter(child => {
    return child && (!child.meta || !child.meta.hidden)
  })
})

// 判断是否显示为单个菜单项
const isShowAsMenuItem = computed(() => {
  // 如果有 alwaysShow 标记，强制显示为子菜单
  if (props.item.meta && props.item.meta.alwaysShow) {
    return false
  }
  
  // 如果没有可见子菜单，显示为菜单项
  if (visibleChildren.value.length === 0) {
    return true
  }
  
  // 如果只有一个可见子菜单，显示为菜单项
  if (visibleChildren.value.length === 1) {
    return true
  }
  
  return false
})

// 菜单项路径
const menuItemPath = computed(() => {
  if (visibleChildren.value.length === 1) {
    return resolvePath(visibleChildren.value[0].path)
  }
  return resolvePath(props.item.path)
})

// 菜单项图标
const menuItemIcon = computed(() => {
  if (visibleChildren.value.length === 1 && visibleChildren.value[0].meta && visibleChildren.value[0].meta.icon) {
    return visibleChildren.value[0].meta.icon
  }
  return props.item.meta && props.item.meta.icon
})

// 菜单项标题
const menuItemTitle = computed(() => {
  if (visibleChildren.value.length === 1 && visibleChildren.value[0].meta && visibleChildren.value[0].meta.title) {
    return visibleChildren.value[0].meta.title
  }
  return props.item.meta && props.item.meta.title
})

// 解析路径
const resolvePath = (routePath) => {
  if (!routePath) {
    return props.basePath || '/'
  }
  
  if (isExternal(routePath)) {
    return routePath
  }
  
  if (isExternal(props.basePath)) {
    return props.basePath
  }
  
  // 如果路径以 / 开头，说明是绝对路径，直接返回
  if (routePath.startsWith('/')) {
    return routePath
  }
  
  // 处理相对路径
  if (props.basePath) {
    const cleanBasePath = props.basePath.replace(/\/$/, '')
    const normalizedBasePath = cleanBasePath.startsWith('/') ? cleanBasePath : '/' + cleanBasePath
    return normalizedBasePath + '/' + routePath
  }
  
  return '/' + routePath
}
</script>