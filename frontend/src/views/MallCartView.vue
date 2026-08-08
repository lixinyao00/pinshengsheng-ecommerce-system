<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  clearCart,
  deleteCartItem,
  getCart,
  updateAllCartSelected,
  updateCartQuantity,
  updateCartSelected
} from '../api/cart'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const updating = ref(false)
const cartSummary = ref({
  items: [],
  selectedCount: 0,
  selectedTotalAmount: 0
})

const allSelected = computed(() => {
  const items = cartSummary.value.items || []
  return items.length > 0 && items.every((item) => item.selected)
})

function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}

function goHome() {
  router.push('/mall/home')
}

async function loadCart() {
  loading.value = true

  try {
    const result = await getCart()

    if (result.code !== 200) {
      throw new Error(result.message)
    }

    cartSummary.value = result.data
  } catch (error) {
    ElMessage.error(error.message || '购物车加载失败')
  } finally {
    loading.value = false
  }
}

async function changeQuantity(item, quantity) {
  if (!quantity || quantity < 1) {
    return
  }

  await updateItem(() => updateCartQuantity(item.id, quantity), '数量修改失败')
}

async function changeSelected(item, selected) {
  await updateItem(() => updateCartSelected(item.id, selected), '选中状态修改失败')
}

async function changeAllSelected(selected) {
  await updateItem(() => updateAllCartSelected(selected), '全选状态修改失败')
}

async function removeItem(item) {
  await updateItem(() => deleteCartItem(item.id), '删除商品失败')
}

async function handleClearCart() {
  await updateItem(clearCart, '清空购物车失败')
}

async function updateItem(action, errorMessage) {
  if (updating.value) {
    return
  }

  updating.value = true

  try {
    const result = await action()

    if (result.code !== 200) {
      throw new Error(result.message)
    }

    await loadCart()
  } catch (error) {
    ElMessage.error(error.message || errorMessage)
  } finally {
    updating.value = false
  }
}

onMounted(loadCart)
</script>

<template>
  <main class="cart-page">
    <header class="cart-header">
      <div>
        <div class="mall-logo">拼省省</div>
        <p>购物车</p>
      </div>
      <div class="header-actions">
        <span>你好，{{ userStore.username }}</span>
        <el-button link type="primary" @click="goHome">继续购物</el-button>
      </div>
    </header>

    <el-card v-loading="loading" class="cart-card" shadow="never">
      <template v-if="cartSummary.items.length > 0">
        <div class="cart-toolbar">
          <el-checkbox
            :model-value="allSelected"
            :disabled="updating"
            @change="changeAllSelected"
          >
            全选
          </el-checkbox>
          <el-button link type="danger" :disabled="updating" @click="handleClearCart">
            清空购物车
          </el-button>
        </div>

        <section class="cart-list">
          <article v-for="item in cartSummary.items" :key="item.id" class="cart-item">
            <el-checkbox
              :model-value="item.selected"
              :disabled="updating"
              @change="(selected) => changeSelected(item, selected)"
            />

            <div class="item-image">
              <img v-if="item.mainImage" :src="item.mainImage" :alt="item.productName">
              <span v-else>暂无图片</span>
            </div>

            <div class="item-info">
              <h2>{{ item.productName }}</h2>
              <p>{{ item.skuName }}</p>
              <strong>¥{{ formatPrice(item.price) }}</strong>
            </div>

            <el-input-number
              :model-value="item.quantity"
              :min="1"
              :disabled="updating"
              @change="(quantity) => changeQuantity(item, quantity)"
            />

            <strong class="subtotal">¥{{ formatPrice(item.subtotal) }}</strong>

            <el-button link type="danger" :disabled="updating" @click="removeItem(item)">
              删除
            </el-button>
          </article>
        </section>

        <footer class="cart-summary">
          <span>已选 {{ cartSummary.selectedCount }} 件商品</span>
          <div>
            <span>合计：</span>
            <strong>¥{{ formatPrice(cartSummary.selectedTotalAmount) }}</strong>
            <el-button type="primary" disabled>去结算</el-button>
          </div>
        </footer>
      </template>

      <el-empty v-else description="购物车还是空的">
        <el-button type="primary" @click="goHome">去挑选商品</el-button>
      </el-empty>
    </el-card>
  </main>
</template>

<style scoped>
.cart-page {
  min-height: 100vh;
  padding: 24px;
  background: #f5f7fa;
}

.cart-header,
.cart-card {
  max-width: 1100px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 18px 28px;
  background: #ffffff;
  border-radius: 8px;
}

.mall-logo {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}

.cart-header p {
  margin: 4px 0 0;
  color: #909399;
}

.header-actions,
.cart-toolbar,
.cart-summary,
.cart-summary div {
  display: flex;
  align-items: center;
}

.header-actions {
  gap: 12px;
}

.cart-toolbar {
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.cart-list {
  display: grid;
  gap: 14px;
  padding: 18px 0;
}

.cart-item {
  display: grid;
  grid-template-columns: auto 86px minmax(220px, 1fr) auto 110px auto;
  align-items: center;
  gap: 18px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.item-image {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 86px;
  height: 86px;
  overflow: hidden;
  color: #909399;
  font-size: 13px;
  background: #f5f7fa;
  border-radius: 6px;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.item-info h2 {
  margin: 0 0 8px;
  font-size: 16px;
}

.item-info p {
  margin: 0 0 10px;
  color: #909399;
  font-size: 13px;
}

.item-info strong,
.subtotal,
.cart-summary strong {
  color: #e64545;
}

.cart-summary {
  justify-content: space-between;
  padding-top: 18px;
  border-top: 1px solid #ebeef5;
}

.cart-summary div {
  gap: 14px;
}

.cart-summary strong {
  font-size: 24px;
}

@media (max-width: 760px) {
  .cart-page {
    padding: 12px;
  }

  .cart-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .cart-item {
    grid-template-columns: auto 70px 1fr;
    gap: 12px;
  }

  .item-image {
    width: 70px;
    height: 70px;
  }

  .cart-item :deep(.el-input-number),
  .subtotal,
  .cart-item > .el-button {
    grid-column: 3;
  }

  .cart-summary {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
  }
}
</style>
