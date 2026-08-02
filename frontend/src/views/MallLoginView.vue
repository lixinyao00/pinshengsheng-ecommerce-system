<script setup>
// 引入 Vue 的响应式工具
import { reactive, ref } from 'vue'

// 引入 Element Plus 的提示消息
import { ElMessage } from 'element-plus'

// 引入路由，用于登录成功后跳转
import { useRouter } from 'vue-router'

// 引入刚才封装的登录接口
import { login } from '../api/auth'

// 引入用户状态管理
import { useUserStore } from '../stores/user'

// 保存登录表单中的用户名和密码
const loginForm = reactive({
  username: 'user',
  password: '123456'
})

// 保存登录按钮的加载状态
const loading = ref(false)

// 创建路由对象
const router = useRouter()

// 获取用户状态管理对象
const userStore = useUserStore()
//处理商城用户登录
async function handleLogin() {
  loading.value = true

  try {
    //调用后端登录接口
    const result = await login(loginForm)

    //判断后端返回的业务代码
    if (result.code !== 200) {
      ElMessage.error(result.message)
      result
    }

    //保存登录成功后的Token、用户名和角色
    userStore.setLoginInfo(result.data)

    //提示用户登录成功
    ElMessage.success('登录成功')

    // 跳转到商城首页
    router.replace('/mall/home')
  } catch {
    // 请求失败时的提示
    ElMessage.error('登录失败，请检查 Gateway 和认证服务')
  } finally {
    // 无论成功还是失败，都结束加载状态
    loading.value = false
  }
}
</script>
<template>
  <!-- 商城登录页面的最外层容器 -->
  <main class="mall-login-page">
    <!-- 登录内容卡片 -->
    <el-card class="mall-login-card">
      <!-- 项目名称 -->
      <div class="mall-brand">拼省省</div>
      <!-- 登录页面标题 -->
      <h1>欢迎来到拼省省</h1>
      <p class="login-subtitle">登录后开始挑选商品</p>
      <!-- 登录表单，绑定登录数据 -->
      <el-form
          :model="loginForm"
          @keyup.enter="handleLogin"
      >
        <!-- 用户名输入项 -->
        <el-form-item label="用户名">
          <el-input
              v-model="loginForm.username"
              autocomplete="username"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
              v-model="loginForm.password"
              type="password"
              show-password/>
        </el-form-item>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleLogin">
          登录商城
        </el-button>
        <el-link
          class="admin-link"
          type="info"
          href="/login">
          进入管理后台
        </el-link>

      </el-form>
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