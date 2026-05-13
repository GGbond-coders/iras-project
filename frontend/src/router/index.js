/**
 * @file index.js
 * @description Vue Router 路由配置文件。
 *              定义应用的路由规则、路由守卫（权限控制）。
 *              包含登录页和主布局（侧边栏 + 内容区）两个顶级路由，
 *              主布局下嵌套三个子路由：职位检索、职能画像、智能诊断。
 *
 * @author IRAS Team
 * @since 1.0
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

/**
 * 路由配置数组。
 * <p>
 * 路由结构：
 * <ul>
 *   <li>/login - 登录页（无需认证）</li>
 *   <li>/ - 主布局（需要认证，默认重定向到 /jobs）
 *     <ul>
 *       <li>/jobs - 职位检索</li>
 *       <li>/job-profile - 职能画像</li>
 *       <li>/diagnosis - 智能诊断</li>
 *     </ul>
 *   </li>
 * </ul>
 * 所有页面组件均使用懒加载（() => import(...)）以优化首屏加载速度。
 * </p>
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),  // 懒加载登录页组件
    meta: { requiresAuth: false }                    // 无需认证即可访问
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),  // 懒加载主布局组件
    redirect: '/jobs',                                // 默认重定向到职位检索页
    meta: { requiresAuth: true },                     // 需要认证才能访问
    children: [
      {
        path: 'jobs',
        name: 'Jobs',
        component: () => import('../views/Jobs.vue'),  // 职位检索页
        meta: { title: '职位检索' }                      // 页面标题（用于面包屑）
      },
      {
        path: 'job-profile',
        name: 'JobProfile',
        component: () => import('../views/JobProfile.vue'),  // 职能画像页
        meta: { title: '职能画像' }
      },
      {
        path: 'diagnosis',
        name: 'Diagnosis',
        component: () => import('../views/Diagnosis.vue'),  // 智能诊断页
        meta: { title: '智能诊断' }
      }
    ]
  }
]

/**
 * 创建路由实例。
 * <p>
 * 使用 HTML5 History 模式（无 hash # 号），需要服务端配合处理前端路由。
 * </p>
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置路由守卫。
 * <p>
 * 权限控制逻辑：
 * <ul>
 *   <li>如果目标页面需要认证（requiresAuth !== false）且用户未登录（无 Token），则重定向到登录页</li>
 *   <li>如果用户已登录却访问登录页，则重定向到首页</li>
 *   <li>其他情况正常放行</li>
 * </ul>
 * </p>
 *
 * @param {Object} to - 即将进入的目标路由
 * @param {Object} from - 当前正要离开的路由
 * @param {Function} next - 放行函数，调用 next() 继续导航，next('/path') 重定向
 */
router.beforeEach((to, from, next) => {
  // 获取用户状态 store
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false && !userStore.token) {
    // 需要认证但未登录 -> 跳转登录页
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    // 已登录却访问登录页 -> 跳转首页
    next('/')
  } else {
    // 正常放行
    next()
  }
})

export default router
