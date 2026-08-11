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
    <router-link class="mall-logo" to="/mall/home">
      <span class="logo-mark">拼</span>
      <span>拼省省</span>
    </router-link>

    <nav class="mall-nav">
      <router-link to="/mall/home" exact-active-class="active"><span>⌂</span>首页</router-link>
      <router-link to="/mall/products" exact-active-class="active"><span>▦</span>全部商品</router-link>
      <router-link to="/mall/sign" exact-active-class="active"><span>✦</span>每日签到</router-link>
      <router-link to="/mall/cart" exact-active-class="active"><span>🛒</span>购物车</router-link>
      <router-link to="/mall/orders" exact-active-class="active"><span>▤</span>我的订单</router-link>
      <router-link to="/mall/addresses" exact-active-class="active"><span>⌖</span>收货地址</router-link>
    </nav>

    <div class="mall-user">
      <span class="user-avatar">{{ (userStore.username || 'U').slice(0, 1).toUpperCase() }}</span>
      <span class="user-name">你好，{{ userStore.username }}</span>
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
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto 20px;
  padding: 13px 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--mall-border);
  border-radius: 14px;
  box-shadow: var(--mall-shadow);
  backdrop-filter: blur(8px);
}

.mall-logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  color: var(--mall-text);
  font-size: 24px;
  font-weight: 700;
  text-decoration: none;
}

.logo-mark {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  color: #fff;
  font-size: 17px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--mall-primary), #ff9474);
  box-shadow: 0 6px 12px rgba(232, 93, 74, 0.25);
}

.mall-nav {
  display: flex;
  flex: 1;
  gap: 4px;
  align-items: center;
}

.mall-nav a {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 9px 11px;
  color: #5e6570;
  font-size: 16px;
  border-radius: 9px;
  text-decoration: none;
  white-space: nowrap;
  transition: color 0.2s ease, background-color 0.2s ease, transform 0.2s ease;
}

.mall-nav a:hover,
.mall-nav a.active {
  color: var(--mall-primary);
  background: var(--mall-primary-light);
  font-weight: 600;
}

.mall-nav a:hover {
  transform: translateY(-1px);
}

.mall-user {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.user-avatar {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  color: var(--mall-primary-dark);
  font-size: 13px;
  font-weight: 700;
  border-radius: 50%;
  background: var(--mall-primary-light);
}

.user-name {
  color: #5e6570;
  font-size: 14px;
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

  .user-name {
    display: none;
  }
}
</style>
