<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getSignCount, getSignStatus, signIn } from '../api/sign'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// 当前认证服务还没有提供真实用户编号，先使用普通用户的演示编号
const userId = 777
const today = new Date().getDate()
const days = Array.from({ length: 31 }, (_, index) => index + 1)

const loading = ref(true)
const signing = ref(false)
const signCount = ref(0)
const signedDays = ref([])

const currentMonthText = computed(() => {
  const now = new Date()
  return `${now.getFullYear()} 年 ${now.getMonth() + 1} 月`
})

function isSigned(day) {
  return signedDays.value.includes(day)
}

function backToHome() {
  router.push('/mall/home')
}

async function loadSignData() {
  loading.value = true

  try {
    // 统计本月签到次数，同时读取每一天的签到状态
    const [countResult, statusResults] = await Promise.all([
      getSignCount(userId),
      Promise.all(days.map((day) => getSignStatus(userId, day)))
    ])

    if (countResult.code !== 200) {
      throw new Error(countResult.message)
    }

    signCount.value = countResult.data.signCount
    signedDays.value = statusResults
      .filter((result) => result.code === 200 && result.data.signed)
      .map((result) => result.data.day)
  } catch (error) {
    ElMessage.error(error.message || '签到数据加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSign() {
  if (isSigned(today)) {
    ElMessage.info('今天已经签到过了')
    return
  }

  signing.value = true

  try {
    const result = await signIn(userId, today)

    if (result.code !== 200) {
      ElMessage.warning(result.message)
      return
    }

    signedDays.value.push(today)
    signCount.value += 1
    ElMessage.success('签到成功，明天也要记得来哦')
  } catch {
    ElMessage.error('签到失败，请检查认证服务是否启动')
  } finally {
    signing.value = false
  }
}

onMounted(loadSignData)
</script>

<template>
  <main class="sign-page">
    <header class="sign-header">
      <div>
        <div class="mall-logo">拼省省</div>
        <p>每日签到</p>
      </div>
      <div class="user-actions">
        <span>你好，{{ userStore.username }}</span>
        <el-button link type="primary" @click="backToHome">返回商城</el-button>
      </div>
    </header>

    <el-card v-loading="loading" class="sign-card" shadow="never">
      <section class="sign-summary">
        <div>
          <p class="month-text">{{ currentMonthText }}</p>
          <h1>本月已签到 <strong>{{ signCount }}</strong> 天</h1>
          <span>绿色日期表示已经签到，只有当天可以进行签到。</span>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="signing"
          :disabled="isSigned(today)"
          @click="handleSign"
        >
          {{ isSigned(today) ? '今日已签到' : '立即签到' }}
        </el-button>
      </section>

      <section class="calendar-section">
        <h2>签到日历</h2>
        <div class="weekday-row">
          <span v-for="weekday in ['一', '二', '三', '四', '五', '六', '日']" :key="weekday">
            周{{ weekday }}
          </span>
        </div>
        <div class="day-grid">
          <el-button
            v-for="day in days"
            :key="day"
            class="sign-day"
            :type="isSigned(day) ? 'success' : day === today ? 'primary' : 'default'"
            :plain="!isSigned(day)"
            :disabled="day !== today || isSigned(day) || signing"
            @click="handleSign"
          >
            {{ day }} 日
          </el-button>
        </div>
      </section>
    </el-card>
  </main>
</template>

<style scoped>
.sign-page {
  min-height: 100vh;
  padding: 24px;
  background: #f5f7fa;
}

.sign-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 960px;
  margin: 0 auto 20px;
  padding: 18px 28px;
  background: #ffffff;
  border-radius: 8px;
}

.mall-logo {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}

.sign-header p {
  margin: 4px 0 0;
  color: #909399;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sign-card {
  max-width: 960px;
  margin: 0 auto;
}

.sign-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px;
  border-radius: 8px;
  background: linear-gradient(120deg, #fff1f0, #fff8f2);
}

.month-text {
  margin: 0;
  color: #909399;
}

.sign-summary h1 {
  margin: 8px 0;
  font-size: 28px;
}

.sign-summary strong {
  color: #f56c6c;
}

.sign-summary span {
  color: #606266;
}

.calendar-section {
  margin-top: 28px;
}

.calendar-section h2 {
  margin-bottom: 16px;
}

.weekday-row,
.day-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
}

.weekday-row {
  margin-bottom: 10px;
  color: #909399;
  text-align: center;
}

.sign-day {
  width: 100%;
  height: 44px;
  margin: 0;
}

@media (max-width: 640px) {
  .sign-page {
    padding: 12px;
  }

  .sign-header,
  .sign-summary {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
  }

  .weekday-row,
  .day-grid {
    gap: 6px;
  }

  .sign-day {
    padding: 8px 2px;
  }
}
</style>
