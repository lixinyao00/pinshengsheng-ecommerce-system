<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

// 当前阶段使用后端提供的普通用户测试账号
const loginForm = reactive({
  username: 'user',
  password: '123456'
})

const loading = ref(false)
const router = useRouter()
const userStore = useUserStore()

// 登录成功后保存 Token，并进入商城首页
async function handleLogin() {
  loading.value = true
  try {
    const result = await login(loginForm)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    if (result.data.role !== 'USER') {
      ElMessage.warning('请使用普通用户账号进入商城')
      return
    }

    userStore.setLoginInfo(result.data)
    ElMessage.success('登录成功')
    router.replace('/mall/home')
  } catch {
    ElMessage.error('登录失败，请检查 Gateway 和认证服务')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="mall-login-page">
    <el-card class="mall-login-card">
      <div class="mall-brand">拼省省</div>
      <h1>欢迎来到拼省省</h1>
      <p class="login-subtitle">登录后开始挑选商品</p>

      <el-form :model="loginForm" @keyup.enter="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">
          登录商城
        </el-button>
      </el-form>

      <el-link class="admin-link" type="info" href="/login">进入管理后台</el-link>
    </el-card>
  </main>
</template>

<style scoped>
.mall-login-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.mall-login-card {
  width: 390px;
  padding: 12px 10px;
}

.mall-brand {
  color: #f56c6c;
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}

h1 {
  margin: 12px 0 6px;
  text-align: center;
}

.login-subtitle {
  margin: 0 0 26px;
  color: #909399;
  text-align: center;
}

.login-button {
  width: 100%;
}

.admin-link {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
</style>
