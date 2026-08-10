<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.clearLoginInfo()
  router.replace('/mall/login')
}
</script>

<template>
  <header class="mall-header">
    <router-link class="mall-logo" to="/mall/home">拼省省</router-link>

    <nav class="mall-nav">
      <router-link to="/mall/home" exact-active-class="active">首页</router-link>
      <router-link to="/mall/products" exact-active-class="active">全部商品</router-link>
      <router-link to="/mall/sign" exact-active-class="active">每日签到</router-link>
      <router-link to="/mall/cart" exact-active-class="active">购物车</router-link>
      <router-link to="/mall/orders" exact-active-class="active">我的订单</router-link>
      <router-link to="/mall/addresses" exact-active-class="active">收货地址</router-link>
    </nav>

    <div class="mall-user">
      <span>你好，{{ userStore.username }}</span>
      <el-button link type="danger" @click="handleLogout">退出登录</el-button>
    </div>
  </header>
</template>

<style scoped>
.mall-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 28px;
  max-width: 1200px;
  margin: 0 auto 20px;
  padding: 16px 28px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #ebeef5;
  border-radius: 10px;
  box-shadow: 0 4px 14px rgba(31, 35, 41, 0.06);
  backdrop-filter: blur(8px);
}

.mall-logo {
  flex: 0 0 auto;
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
  text-decoration: none;
}

.mall-nav {
  display: flex;
  flex: 1;
  gap: 22px;
  align-items: center;
}

.mall-nav a {
  color: #303133;
  font-size: 16px;
  text-decoration: none;
  white-space: nowrap;
}

.mall-nav a:hover,
.mall-nav a.active {
  color: #f56c6c;
  font-weight: 600;
}

.mall-user {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .mall-header {
    flex-wrap: wrap;
    gap: 12px 20px;
  }

  .mall-nav {
    order: 3;
    flex-basis: 100%;
    overflow-x: auto;
    padding-bottom: 2px;
  }
}
</style>
