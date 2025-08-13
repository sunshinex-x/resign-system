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
    meta: { hidden: true }
  },
  {
    path: '/dashboard',
    component: Layout,
    children: [
      {
        path: '',
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
    meta: { title: '任务管理', icon: 'Document', alwaysShow: true },
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
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    meta: { title: '用户管理', icon: 'User', alwaysShow: true },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户列表', icon: 'UserFilled' }
      },
      {
        path: 'role',
        name: 'RoleList',
        component: () => import('@/views/user/role.vue'),
        meta: { title: '角色管理', icon: 'Avatar' }
      },
      {
        path: 'permission',
        name: 'PermissionList',
        component: () => import('@/views/user/permission.vue'),
        meta: { title: '权限管理', icon: 'Key' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/log',
    meta: { title: '系统管理', icon: 'Setting', alwaysShow: true },
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
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 应用重签名管理系统` : '应用重签名管理系统'

  // 这里可以添加登录验证等逻辑
  next()
})

export default router