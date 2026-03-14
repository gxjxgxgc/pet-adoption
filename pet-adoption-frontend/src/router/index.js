import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { isLoggedIn } from '@/utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/Index.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/pets',
    name: 'PetList',
    component: () => import('@/views/pet/List.vue'),
    meta: { title: '宠物列表' }
  },
  {
    path: '/pets/:id',
    name: 'PetDetail',
    component: () => import('@/views/pet/Detail.vue'),
    meta: { title: '宠物详情' }
  },
  {
    path: '/pets/publish',
    name: 'PetPublish',
    component: () => import('@/views/pet/Publish.vue'),
    meta: { title: '发布宠物', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/user/Favorites.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/adoptions',
    name: 'MyAdoptions',
    component: () => import('@/views/adoption/MyAdoptions.vue'),
    meta: { title: '我的领养', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 宠物领养系统` : '宠物领养系统'
  
  const loggedIn = isLoggedIn()
  
  // 需要登录但未登录
  if (to.meta.requiresAuth && !loggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  // 需要管理员权限
  if (to.meta.requiresAdmin) {
    const userStore = useUserStore()
    if (!userStore.isAdmin) {
      next({ name: 'Home' })
      return
    }
  }
  
  next()
})

export default router