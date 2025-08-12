import { createRouter, createWebHistory } from 'vue-router'

// 布局组件
const Layout = () => import('@/views/layout/index.vue')

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '控制台', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/task',
    component: Layout,
    redirect: '/task/list',
    meta: { title: '任务管理', icon: 'Document' },
    children: [
      {
        path: 'list',
        name: 'TaskList',
        component: () => import('@/views/task/list.vue'),
        meta: { title: '任务列表', icon: 'List' }
      },
      {
        path: 'create',
        name: 'TaskCreate',
        component: () => import('@/views/task/create.vue'),
        meta: { title: '创建任务', icon: 'Plus' }
      },
      {
        path: 'detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/detail.vue'),
        meta: { title: '任务详情', hidden: true }
      }
    ]
  },
  {
    path: '/statistics',
    component: Layout,
    redirect: '/statistics/index',
    children: [
      {
        path: 'index',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/log',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/system/log.vue'),
        meta: { title: '日志管理', icon: 'Document' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/system/config.vue'),
        meta: { title: '系统配置', icon: 'Tools' }
      }
    ]
  },
  {
    path: '/404',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true }
  },
  { 
    path: '/:pathMatch(.*)*', 
    redirect: '/404', 
    meta: { hidden: true } 
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 应用重签名管理系统` : '应用重签名管理系统'
  
  // 这里可以添加登录验证等逻辑
  next()
})

export default router