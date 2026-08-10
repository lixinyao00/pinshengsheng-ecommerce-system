<script setup>
// 引入 Vue 的响应式工具
import { reactive, ref } from 'vue'

// 引入 Element Plus 的提示消息
import { ElMessage } from 'element-plus'

// 引入路由，用于登录成功后跳转
import { useRouter } from 'vue-router'

// 引入刚才封装的登录接口
import { login, register } from '../api/auth'

// 引入用户状态管理
import { useUserStore } from '../stores/user'

// 保存登录表单中的用户名和密码
const loginForm = reactive({
  username: 'user',
  password: '123456'
})

// 保存登录按钮的加载状态
const loading = ref(false)

const registerVisible = ref(false)
const registerLoading = ref(false)
const registerFormRef = ref()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

// 创建路由对象
const router = useRouter()

// 获取用户状态管理对象
const userStore = useUserStore()

function openRegisterDialog() {
  Object.assign(registerForm, {
    username: '',
    password: '',
    confirmPassword: '',
    nickname: ''
  })
  registerVisible.value = true
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  registerLoading.value = true
  try {
    const result = await register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname || registerForm.username
    })

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success('注册成功，请登录')
    registerVisible.value = false
    loginForm.username = registerForm.username
    loginForm.password = ''
  } catch {
    ElMessage.error('注册失败，请检查认证服务是否启动')
  } finally {
    registerLoading.value = false
  }
}

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
          label-width="56px"
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
        <div class="login-actions">
          <el-button
            class="login-button"
            type="primary"
            :loading="loading"
            @click="handleLogin">
            登录商城
          </el-button>
          <el-button link type="primary" @click="openRegisterDialog">
            注册账号
          </el-button>
        </div>
        <el-link
          class="admin-link"
          type="info"
          href="/login">
          进入管理后台
        </el-link>

      </el-form>
    </el-card>

    <el-dialog v-model="registerVisible" title="注册商城账号" width="460px">
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="90px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="registerForm.nickname" placeholder="不填则使用用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">
          注册
        </el-button>
      </template>
    </el-dialog>
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
  min-width: 96px;
}

.login-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.mall-login-card :deep(.el-input) {
  width: 100%;
}

.admin-link {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
</style>
