<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// 根据当前角色生成可见菜单，后续会替换为后端返回的菜单数据
const menus = computed(() => {
  if (userStore.role !== 'ADMIN') {
    return []
  }

  return [
    {
      index: '/admin/dashboard',
      title: '后台首页'
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
    <el-aside width="220px" class="sidebar">
      <h2>拼省省后台</h2>

      <!-- 当前角色可访问的后台菜单 -->
      <el-menu
        router
        default-active="/admin/dashboard"
        background-color="#1f2937"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item v-for="menu in menus" :key="menu.index" :index="menu.index">
          {{ menu.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>当前用户：{{ userStore.username }}</span>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </el-header>

      <!-- 后台子页面在这里显示 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.sidebar {
  min-height: 100vh;
  color: #ffffff;
  background: #1f2937;
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
}
</style>
