<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

// 暂存用户输入的账号和密码
const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

// 控制登录按钮的加载状态，避免重复点击
const loading = ref(false)
// 获取用户状态仓库
const userStore = useUserStore()
const router = useRouter()

// 调用 Gateway 的登录接口，并保存登录结果
async function handleLogin() {
  loading.value = true

  try {
    const response = await axios.post('/api/auth/login', loginForm)
    const result = response.data

    // 后端业务校验未通过时，显示错误提示
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    // 将登录信息交给 Pinia 统一保存
    userStore.setLoginInfo(result.data)

    // 当前页面是后台管理端，普通用户后续会进入商城用户端
    if (result.data.role !== 'ADMIN') {
      userStore.clearLoginInfo()
      ElMessage.warning('普通用户请在商城用户端登录')
      return
    }

    ElMessage.success('登录成功，正在进入管理后台')
    router.replace('/admin/dashboard')
  } catch {
    // 网络异常或后端未启动时的提示
    ElMessage.error('登录请求失败，请检查 Gateway 和认证服务')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <!-- 后台登录页的整体容器 -->
  <main class="login-page">
    <el-card class="login-card">
      <!-- 登录页标题 -->
      <h1>拼省省管理后台</h1>
      <p>使用管理员账号登录</p>

      <!-- 登录表单 -->
      <el-form :model="loginForm" @keyup.enter="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" show-password />
        </el-form-item>

        <!-- 点击后调用后端登录接口 -->
        <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
    </el-card>
  </main>
</template>

<style scoped>
/* 让登录卡片在页面中水平、垂直居中 */
.login-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background: #f4f6f9;
}

.login-card {
  width: 380px;
}

.login-card h1 {
  margin: 0 0 8px;
  text-align: center;
}

.login-card p {
  margin: 0 0 24px;
  color: #909399;
  text-align: center;
}

.login-button {
  width: 100%;
}
</style>
