<script setup>
// 引入 Vue 的响应式和生命周期工具
import { computed, onMounted, ref } from 'vue'

// 获取当前路由中的商品 ID
import { useRoute, useRouter } from 'vue-router'

// 引入商品详情接口
import { getMallProductDetail } from '../api/mall'
import { addCartItem } from '../api/cart'
// 引入 Element Plus 提示消息
import { ElMessage } from 'element-plus'
import MallHeader from '../components/MallHeader.vue'

// 获取当前路由对象
const route = useRoute()
// 获取路由跳转对象
const router = useRouter()

// 保存商品详情数据
const productDetail = ref(null)

// 页面加载状态
const loading = ref(true)

// 页面错误信息
const errorMessage = ref('')
// 保存当前选中的 SKU 编号
const selectedSkuId = ref(null)

// 保存购买数量
const quantity = ref(1)

// 根据当前编号计算选中的 SKU
const selectedSku = computed(() => {
  const skuList = productDetail.value?.skuList || []

  return skuList.find((item) => {
    return item.sku.id === selectedSkuId.value
  }) || null
})

// 加载商品详情
async function loadProductDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    // 路由参数默认是字符串，后端接口可以直接接收这个商品编号
    const productId = route.params.id

    // 请求后端商品详情接口
    const result = await getMallProductDetail(productId)

    // 判断后端是否返回成功
    if (result.code !== 200) {
      throw new Error(result.message)
    }

    // 保存后端返回的商品详情数据
    productDetail.value = result.data
    // 默认选择第一个 SKU
    const firstSku = result.data.skuList?.[0]

    if (firstSku) {
      selectedSkuId.value = firstSku.sku.id
    }
  } catch (error) {
    // 保存错误信息，交给页面显示
    errorMessage.value = error.message || '商品详情加载失败'
  } finally {
    // 无论成功还是失败，都结束加载状态
    loading.value = false
  }
}
// 选择商品规格
function selectSku(item) {
  selectedSkuId.value = item.sku.id
  quantity.value = 1
}
// 返回商城首页
function goHome() {
  router.push({ name: 'mall-home' })
}

// 确认当前选择的 SKU 和购买数量
function confirmSelection() {
  if (!selectedSku.value) {
    ElMessage.warning('请先选择商品规格')
    return
  }

  if (selectedSku.value.availableStock <= 0) {
    ElMessage.warning('当前规格库存不足')
    return
  }
  ElMessage.success(
      `已选择 ${selectedSku.value.sku.name}, 数量: ${quantity.value}`
  )
}

// 将当前选中的规格和数量保存到购物车
async function handleAddCart() {
  if (!selectedSku.value) {
    ElMessage.warning('请先选择商品规格')
    return
  }

  if (selectedSku.value.availableStock <= 0) {
    ElMessage.warning('当前规格库存不足')
    return
  }

  try {
    const result = await addCartItem({
      productId: productDetail.value.product.id,
      skuId: selectedSku.value.sku.id,
      productName: productDetail.value.product.name,
      skuName: selectedSku.value.sku.name,
      mainImage: productDetail.value.product.mainImage,
      price: selectedSku.value.sku.price,
      quantity: quantity.value
    })

    if (result.code !== 200) {
      throw new Error(result.message)
    }

    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error(error.message || '加入购物车失败')
  }
}

// 页面加载完成后查询商品详情
onMounted(loadProductDetail)
</script>

<template>
  <main class="product-detail-page">
    <MallHeader />

    <p v-if="loading">商品详情加载中...</p>

    <p v-else-if="errorMessage">
      {{ errorMessage }}
    </p>

    <!-- 商品详情主体 -->
    <section v-else-if="productDetail" class="detail-content">

      <!-- 商品图片区域 -->
      <div class="product-image">
        <img
            v-if="productDetail.product.mainImage"
            :src="productDetail.product.mainImage"
            :alt="productDetail.product.name"
        >

        <div v-else class="empty-image">
          暂无商品图片
        </div>
      </div>

      <!-- 商品基本信息 -->
      <div class="product-info">
        <h1>{{ productDetail.product.name }}</h1>

        <p class="subtitle">
          {{ productDetail.product.subtitle }}
        </p>

        <div class="product-meta">
          <span>品牌：{{ productDetail.brand?.name || '暂无品牌' }}</span>
          <span>分类：{{ productDetail.category?.name || '暂无分类' }}</span>
        </div>

        <strong class="product-price">
          ¥{{ productDetail.product.minPrice }}
        </strong>

        <p class="description">
          {{ productDetail.product.description }}
        </p>
      </div>
    </section>
    <!-- 商品规格区域 -->
    <section v-if="productDetail" class="sku-section">
      <h2>商品规格</h2>

      <!-- 循环展示可以选择的 SKU -->
      <div
          v-if="(productDetail.skuList || []).length > 0"
          class="sku-list"
      >
        <button
            v-for="item in productDetail.skuList || []"
            :key="item.sku.id"
            type="button"
            class="sku-item"
            :class="{ active: selectedSkuId === item.sku.id }"
            @click="selectSku(item)"
        >
          <div>
            <strong>{{ item.sku.name }}</strong>
          </div>

          <strong class="sku-price">
            ¥{{ item.sku.price }}
          </strong>

          <span class="stock">
      库存：{{ item.availableStock }}
    </span>
        </button>
      </div>

      <p v-else class="empty-text">
        暂无商品规格
      </p>
    </section>

    <!-- 商品轮播图区域 -->
    <section v-if="productDetail" class="image-section">
      <h2>商品图片</h2>

      <div
          v-if="(productDetail.imageList || []).length > 0"
          class="image-list"
      >
        <!-- 循环展示商品图片 -->
        <img
            v-for="image in productDetail.imageList || []"
            :key="image.id"
            :src="image.imageUrl"
            alt="商品图片"
        >
      </div>

      <p v-else class="empty-text">
        暂无商品图片
      </p>
      <!-- 选择购买数量 -->
      <div v-if="selectedSku" class="quantity-row">
        <span>购买数量</span>

        <el-input-number
            v-model="quantity"
            :min="1"
            :max="Math.max(selectedSku.availableStock, 1)"
            :disabled="selectedSku.availableStock <= 0"
        />
      </div>

      <!-- 商品操作按钮 -->
      <div class="action-row">
        <el-button type="primary" @click="confirmSelection">
          确认选择
        </el-button>

        <el-button type="success" @click="handleAddCart">
          加入购物车
        </el-button>

        <el-button @click="goHome">
          继续逛逛
        </el-button>
      </div>
    </section>
  </main>
</template>
<style scoped>
.product-detail-page {
  min-height: 100vh;
  max-width: 1200px;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 32px;
  background: #f5f7fa;
}

.detail-content {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
  background: #ffffff;
  border-radius: 8px;
}

.product-image {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  background: #f5f7fa;
  border-radius: 8px;
}

.product-image img {
  width: 100%;
  max-height: 360px;
  object-fit: contain;
}

.empty-image {
  color: #909399;
}

.product-info h1 {
  margin: 0 0 12px;
  font-size: 28px;
}

.subtitle {
  color: #909399;
}

.product-meta {
  display: flex;
  gap: 24px;
  margin: 24px 0;
  color: #606266;
}

.product-price {
  display: block;
  margin-bottom: 24px;
  color: #e64545;
  font-size: 28px;
}

.description {
  line-height: 1.8;
  color: #606266;
}
.sku-section,
.image-section {
  max-width: 1200px;
  margin: 24px auto 0;
  padding: 24px 32px;
  background: #ffffff;
  border-radius: 8px;
}

.sku-section h2,
.image-section h2 {
  margin: 0 0 18px;
  font-size: 20px;
}

.sku-list {
  display: grid;
  gap: 12px;
}

.sku-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 24px;
  align-items: center;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.sku-item p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 13px;
}

.sku-price {
  color: #e64545;
  font-size: 18px;
}

.stock {
  color: #67c23a;
}

.empty-text {
  margin: 0;
  color: #909399;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.image-list img {
  width: 180px;
  height: 180px;
  object-fit: contain;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}
.sku-item {
  width: 100%;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.sku-item.active {
  border-color: #f56c6c;
  background: #fff5f5;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 24px;
  color: #606266;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

@media (max-width: 760px) {
  .product-detail-page {
    padding: 12px;
  }

  .detail-content {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 18px;
  }

  .product-image {
    min-height: 240px;
    aspect-ratio: 4 / 3;
  }

  .product-info h1 {
    font-size: 24px;
  }

  .product-meta {
    flex-wrap: wrap;
    gap: 8px 18px;
    margin: 16px 0;
  }

  .sku-section,
  .image-section {
    padding: 18px;
  }

  .sku-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .quantity-row,
  .action-row {
    flex-wrap: wrap;
  }

  .action-row .el-button {
    flex: 1;
    min-width: 130px;
  }

  .image-list img {
    width: calc(50% - 8px);
    height: auto;
    aspect-ratio: 16 / 9;
    object-fit: cover;
  }
}
</style>
