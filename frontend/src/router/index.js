import { createRouter, createWebHistory } from 'vue-router'

const LoginView = () => import('../views/LoginView.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const BrandView = () => import('../views/BrandView.vue')
const CategoryView = () => import('../views/CategoryView.vue')
const ProductView = () => import('../views/ProductView.vue')
const SkuView = () => import('../views/SkuView.vue')
const OrderView = () => import('../views/OrderView.vue')
const HomeBannerView = () => import('../views/HomeBannerView.vue')
const MallLoginView = () => import('../views/MallLoginView.vue')
const MallHomeView = () => import('../views/MallHomeView.vue')
const MallProductListView = () => import('../views/MallProductListView.vue')
const MallProductDetailView = () => import('../views/MallProductDetailView.vue')
const MallSignView = () => import('../views/MallSignView.vue')
const MallCartView = () => import('../views/MallCartView.vue')
const MallOrderView = () => import('../views/MallOrderView.vue')
const MallAddressView = () => import('../views/MallAddressView.vue')

// 定义页面地址、布局和页面组件之间的对应关系
const routes = [
  {
    path: '/',
    redirect: '/mall/login'
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/mall/login',
    name: 'mall-login',
    component: MallLoginView
  },
  {
    path: '/mall/home',
    name: 'mall-home',
    component: MallHomeView,
    meta: {
      requiresAuth: true,
      userOnly: true
    }
  },
  {
    path: '/mall/product/:id',
    name: 'mall-product-detail',
    component: MallProductDetailView,
    meta: {
      requiresAuth: true,
      userOnly: true
    }
  },
  {
    path: '/mall/products',
    name: 'mall-products',
    component: MallProductListView,
    meta: {
      requiresAuth: true,
      userOnly: true
    }
  },
  {
    path: '/mall/sign',
    name: 'mall-sign',
    component: MallSignView,
    meta: {
      requiresAuth: true,
      userOnly: true
    }
  },
  {
    path: '/mall/cart',
    name: 'mall-cart',
    component: MallCartView,
    meta: {
      requiresAuth: true,
      userOnly: true
    }
  },
  {
    path: '/mall/orders',
    name: 'mall-orders',
    component: MallOrderView,
    meta: {
      requiresAuth: true,
      userOnly: true
    }
  },
  {
    path: '/mall/addresses',
    name: 'mall-addresses',
    component: MallAddressView,
    meta: {
      requiresAuth: true,
      userOnly: true
    }
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
      },
      {
        path: 'orders',
        name: 'order',
        component: OrderView
      },
      {
        path: 'banners',
        name: 'banner',
        component: HomeBannerView
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

  // 商城页面只允许普通用户访问
  if (to.meta.userOnly && (!token || role !== 'USER')) {
    return { name: 'mall-login' }
  }

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
