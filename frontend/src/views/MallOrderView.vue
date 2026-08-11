<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  cancelOrder,
  completeOrder,
  getMyOrderDetail,
  getMyOrderList,
  payOrder
} from '../api/order'
import { useUserStore } from '../stores/user'
import MallHeader from '../components/MallHeader.vue'

const router = useRouter()
const userStore = useUserStore()

const orderList = ref([])
const loading = ref(true)
const selectedStatus = ref('')
const detailVisible = ref(false)
const detailLoading = ref(false)
const orderDetail = ref(null)

const statusOptions = [
  { value: '', label: '全部订单' },
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已发货' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' }
]

const visibleOrders = computed(() => {
  if (selectedStatus.value === '') {
    return orderList.value
  }
  return orderList.value.filter((order) => order.status === selectedStatus.value)
})

function goHome() {
  router.push('/mall/home')
}

function goCart() {
  router.push('/mall/cart')
}

function goAddresses() {
  router.push('/mall/addresses')
}

function statusText(status) {
  return statusOptions.find((item) => item.value === status)?.label || '未知状态'
}

function statusType(status) {
  if (status === 1) return 'warning'
  if (status === 2) return 'primary'
  if (status === 3) return 'success'
  if (status === 4) return 'info'
  return 'danger'
}

function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}

async function loadOrders() {
  loading.value = true
  try {
    const result = await getMyOrderList()
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    orderList.value = result.data || []
  } catch {
    ElMessage.error('订单列表加载失败，请检查订单服务')
  } finally {
    loading.value = false
  }
}

async function openDetail(order) {
  detailVisible.value = true
  detailLoading.value = true
  orderDetail.value = null

  try {
    const result = await getMyOrderDetail(order.id)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      detailVisible.value = false
      return
    }
    orderDetail.value = result.data
  } catch {
    ElMessage.error('订单详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleOrderAction(order, action, confirmText, successText) {
  try {
    await ElMessageBox.confirm(confirmText, '操作确认', { type: 'warning' })
    const result = await action(order.id)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success(successText)
    await loadOrders()
  } catch {
    // 用户取消操作时不提示错误
  }
}

function handlePay(order) {
  return handleOrderAction(order, payOrder, '确定模拟支付这笔订单吗？', '订单支付成功')
}

function handleCancel(order) {
  return handleOrderAction(order, cancelOrder, '确定取消这笔订单吗？', '订单已取消')
}

function handleComplete(order) {
  return handleOrderAction(order, completeOrder, '确认已经收到商品了吗？', '已确认收货')
}

function handleLogout() {
  userStore.clearLoginInfo()
  router.replace('/mall/login')
}

onMounted(loadOrders)
</script>

<template>
  <main class="order-page">
    <MallHeader />

    <el-card v-loading="loading" class="order-card" shadow="never">
      <div class="order-toolbar">
        <h1>我的订单</h1>
        <el-select v-model="selectedStatus" style="width: 140px">
          <el-option
            v-for="item in statusOptions"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>

      <el-empty v-if="visibleOrders.length === 0" description="暂无订单" />

      <section v-else class="order-list">
        <article v-for="order in visibleOrders" :key="order.id" class="order-item">
          <div class="order-item-header">
            <span>订单号：{{ order.orderNo }}</span>
            <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
          </div>

          <div class="order-item-content">
            <div>
              <p>创建时间：{{ order.createTime }}</p>
              <p>订单金额：<strong>¥{{ formatPrice(order.payAmount) }}</strong></p>
            </div>
            <div class="order-actions">
              <el-button link type="primary" @click="openDetail(order)">查看详情</el-button>
              <el-button v-if="order.status === 0" link type="success" @click="handlePay(order)">
                去支付
              </el-button>
              <el-button v-if="order.status === 0" link type="danger" @click="handleCancel(order)">
                取消订单
              </el-button>
              <el-button v-if="order.status === 2" link type="success" @click="handleComplete(order)">
                确认收货
              </el-button>
            </div>
          </div>
        </article>
      </section>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="720px">
      <el-skeleton v-if="detailLoading" :rows="5" animated />
      <template v-else-if="orderDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusType(orderDetail.order.status)">
              {{ statusText(orderDetail.order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ formatPrice(orderDetail.order.payAmount) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ orderDetail.order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderDetail.order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetail.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">
            {{ orderDetail.order.province }}{{ orderDetail.order.city }}{{ orderDetail.order.district }}{{ orderDetail.order.detailAddress }}
          </el-descriptions-item>
        </el-descriptions>

        <h3 class="item-title">商品明细</h3>
        <el-table :data="orderDetail.items" border>
          <el-table-column prop="productName" label="商品" min-width="180" />
          <el-table-column prop="skuName" label="规格" min-width="180" />
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalAmount" label="小计" width="100" />
        </el-table>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.order-page {
  min-height: 100vh;
  max-width: 1200px;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 24px;
  background: #f5f7fa;
}

.order-card {
  max-width: 1200px;
  margin: 0 auto;
}

.order-toolbar,
.order-item-header,
.order-item-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-toolbar {
  margin-bottom: 20px;
}

.order-toolbar h1 {
  margin: 0;
  font-size: 22px;
}

.order-list {
  display: grid;
  gap: 14px;
}

.order-item {
  padding: 18px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.order-item-header {
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  font-size: 13px;
}

.order-item-content {
  padding-top: 14px;
}

.order-item-content p {
  margin: 6px 0;
  color: #909399;
}

.order-item-content strong {
  color: #e64545;
  font-size: 18px;
}

.order-actions {
  display: flex;
  gap: 4px;
}

.item-title {
  margin: 22px 0 12px;
  font-size: 16px;
}

@media (max-width: 760px) {
  .order-page {
    padding: 12px;
  }

  .order-item-content {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .order-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .order-toolbar .el-select {
    width: 100% !important;
  }

  .order-item-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .order-actions {
    flex-wrap: wrap;
  }

  .order-actions .el-button {
    margin-left: 0;
  }

  .order-card :deep(.el-descriptions__body),
  .order-card :deep(.el-descriptions__table) {
    width: 100%;
  }

  .order-card :deep(.el-table) {
    overflow-x: auto;
  }
}
</style>
