<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminOrderDetail,
  getAdminOrderList,
  shipAdminOrder
} from '../api/order'

const orderList = ref([])
const loading = ref(false)
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

async function loadOrderList() {
  loading.value = true
  try {
    const result = await getAdminOrderList(selectedStatus.value)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    orderList.value = result.data
  } catch {
    ElMessage.error('订单列表加载失败，请检查订单服务和网关')
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  orderDetail.value = null
  try {
    const result = await getAdminOrderDetail(row.id)
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

async function shipOrder(row) {
  try {
    await ElMessageBox.confirm(`确定要发货订单 ${row.orderNo} 吗？`, '操作确认', {
      type: 'warning'
    })
    const result = await shipAdminOrder(row.id)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('订单发货成功')
    await loadOrderList()
  } catch {
    // 用户取消操作时不提示错误
  }
}

onMounted(loadOrderList)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-header">
      <h1>订单管理</h1>
      <div class="admin-page-actions">
        <el-select v-model="selectedStatus" placeholder="订单状态" style="width: 140px" @change="loadOrderList">
          <el-option
            v-for="item in statusOptions"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button :loading="loading" @click="loadOrderList">刷新列表</el-button>
      </div>
    </div>

    <div class="admin-table-wrap">
    <el-table v-loading="loading" :data="orderList" border>
      <el-table-column prop="id" label="订单ID" width="90" />
      <el-table-column prop="orderNo" label="订单号" min-width="190" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="payAmount" label="实付金额" width="110" />
      <el-table-column label="订单状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 1"
            link
            type="success"
            @click="shipOrder(row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="760px">
      <el-skeleton v-if="detailLoading" :rows="5" animated />
      <template v-else-if="orderDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusType(orderDetail.order.status)">
              {{ statusText(orderDetail.order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ orderDetail.order.userId }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">{{ orderDetail.order.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderDetail.order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetail.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">
            {{ orderDetail.order.province }}{{ orderDetail.order.city }}{{ orderDetail.order.district }}{{ orderDetail.order.detailAddress }}
          </el-descriptions-item>
        </el-descriptions>

        <h3 class="item-title">商品明细</h3>
        <el-table :data="orderDetail.items" border>
          <el-table-column prop="productName" label="商品" min-width="180" />
          <el-table-column prop="skuName" label="SKU" min-width="180" />
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalAmount" label="小计" width="100" />
        </el-table>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 12px;
}

.item-title {
  margin: 22px 0 12px;
  font-size: 16px;
}
</style>
