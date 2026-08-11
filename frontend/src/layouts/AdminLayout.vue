<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {useRoute, useRouter} from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const route = useRoute()
const isMobile = ref(false)
const mobileMenuVisible = ref(false)

function updateViewport() {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    mobileMenuVisible.value = false
  }
}

function toggleMobileMenu() {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

function closeMobileMenu() {
  if (isMobile.value) {
    mobileMenuVisible.value = false
  }
}

onMounted(() => {
  updateViewport()
  window.addEventListener('resize', updateViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewport)
})

watch(() => route.path, closeMobileMenu)

// 根据当前角色生成可见菜单，后续会替换为后端返回的菜单数据
const menus = computed(() => {
  if (userStore.role !== 'ADMIN') {
    return []
  }

  return [
    {
      index: '/admin/dashboard',
      title: '后台首页'
    },
    {
      index: '/admin/brands',
      title: '品牌管理'
    },
    {
      index: '/admin/categories',
      title: '分类管理'
    },
    {
      index: '/admin/products',
      title: '商品管理'
    },
    {
      index: '/admin/skus',
      title: 'SKU 与库存'
    },
    {
      index: '/admin/orders',
      title: '订单管理'
    },
    {
      index: '/admin/banners',
      title: '首页轮播图'
    }
  ]
})

// 退出时清理身份信息，避免浏览器继续保留旧 Token
function handleLogout() {
  userStore.clearLoginInfo()
  router.replace('/login')
}
</script>

<template>
  <el-container class="admin-layout">
    <div
      v-if="isMobile && mobileMenuVisible"
      class="sidebar-mask"
      @click="closeMobileMenu"
    />

    <el-aside
      width="220px"
      class="sidebar"
      :class="{ 'is-open': mobileMenuVisible }"
    >
      <h2>拼省省后台</h2>

      <!-- 当前角色可访问的后台菜单 -->
      <el-menu
        router
        :default-active="route.path"
        background-color="#1f2937"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item
          v-for="menu in menus"
          :key="menu.index"
          :index="menu.index"
          @click="closeMobileMenu"
        >
          {{ menu.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-button
          v-if="isMobile"
          class="mobile-menu-button"
          text
          @click="toggleMobileMenu"
        >
          ☰
        </el-button>
        <span>当前用户：{{ userStore.username }}</span>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </el-header>

      <!-- 后台子页面在这里显示 -->
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  position: relative;
}

.sidebar {
  min-height: 100vh;
  color: #ffffff;
  background: #1f2937;
  flex-shrink: 0;
}

.sidebar h2 {
  padding: 0 20px;
  font-size: 20px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
  gap: 10px;
}

.mobile-menu-button {
  display: none;
  padding: 0;
  font-size: 22px;
}

.sidebar-mask {
  display: none;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 20;
    top: 0;
    bottom: 0;
    left: 0;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
  }

  .sidebar.is-open {
    transform: translateX(0);
  }

  .sidebar-mask {
    display: block;
    position: fixed;
    z-index: 10;
    inset: 0;
    background: rgba(15, 23, 42, 0.42);
  }

  .mobile-menu-button {
    display: inline-flex;
  }

  .header {
    padding: 0 14px;
  }

  .admin-main {
    padding: 14px !important;
  }
}
</style>
