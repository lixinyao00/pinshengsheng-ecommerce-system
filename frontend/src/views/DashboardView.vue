<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const dashboardMessage = ref('正在加载后台数据...')
const loading = ref(true)

// 通过 Gateway 调用管理员接口，验证前端确实携带了登录 Token
async function loadDashboard() {
  try {
    const response = await axios.get('/api/auth/admin/dashboard', {
      headers: {
        Authorization: `Bearer ${userStore.token}`
      }
    })

    const result = response.data
    dashboardMessage.value = result.code === 200
      ? result.data.message
      : result.message
  } catch {
    dashboardMessage.value = '后台数据加载失败，请检查认证服务是否启动'
  } finally {
    loading.value = false
  }
}

// 页面首次显示时加载管理员概览信息
onMounted(loadDashboard)
</script>

<template>
  <section class="admin-page">
    <h1>后台首页</h1>
    <p>欢迎你，{{ userStore.username }}！</p>

    <!-- 展示管理员接口的实际响应结果 -->
    <el-alert
      :title="dashboardMessage"
      :type="loading ? 'info' : 'success'"
      :closable="false"
      show-icon
    />
  </section>
</template>
