import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import BrandView from "../views/BrandView.vue";
import CategoryView from "../views/CategoryView.vue";
import ProductView from "../views/ProductView.vue";
import SkuView from '../views/SkuView.vue'

// 定义页面地址、布局和页面组件之间的对应关系
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      requiresAuth: true,
      adminOnly: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView
      },
      {
        path: 'brands',
        name: 'brand',
        component: BrandView
      },
      {
        path: 'categories',
        name: 'category',
        component: CategoryView
      },
      {
        path: 'products',
        name: 'product',
        component: ProductView
      },
      {
        path: 'skus',
        name: 'sku',
        component: SkuView
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 进入后台页面前，校验是否已经登录且拥有管理员角色
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }

  if (to.meta.adminOnly && role !== 'ADMIN') {
    return { name: 'login' }
  }

  if (to.name === 'login' && token && role === 'ADMIN') {
    return { name: 'dashboard' }
  }

  return true
})

export default router
