import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/jobs',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'jobs',
        name: 'Jobs',
        component: () => import('../views/Jobs.vue'),
        meta: { title: '职位检索' }
      },
      {
        path: 'job-profile',
        name: 'JobProfile',
        component: () => import('../views/JobProfile.vue'),
        meta: { title: '职能画像' }
      },
      {
        path: 'diagnosis',
        name: 'Diagnosis',
        component: () => import('../views/Diagnosis.vue'),
        meta: { title: '智能诊断' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else {
    next()
  }
})

export default router
