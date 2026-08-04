<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMallProductDetail } from '../api/mall'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const errorMessage = ref('')
const detail = ref(null)
const selectedSkuId = ref(null)
const selectedImage = ref('')
const quantity = ref(1)

const currentSku = computed(() => {
  const skuList = detail.value?.skuList || []
  return skuList.find((item) => item.sku?.id === selectedSkuId.value)
    || detail.value?.selectedSku
    || null
})

const mainImage = computed(() => {
  return selectedImage.value
    || detail.value?.product?.mainImage
    || detail.value?.images?.[0]?.imageUrl
    || ''
})

const skuAttributes = computed(() => {
  const json = currentSku.value?.sku?.attributesJson
  if (!json) {
    return []
  }

  try {
    return Object.entries(JSON.parse(json))
  } catch {
    return [['规格', json]]
  }
})

const currentPrice = computed(() => {
  return currentSku.value?.sku?.price ?? detail.value?.product?.minPrice ?? 0
})

const currentStock = computed(() => {
  return currentSku.value?.availableStock ?? 0
})

function goHome() {
  router.push({ name: 'mall-home' })
}

function selectSku(item) {
  selectedSkuId.value = item.sku.id
  quantity.value = 1
}

function selectImage(url) {
  selectedImage.value = url
}

function handleQuickAction() {
  if (!currentSku.value) {
    ElMessage.warning('请先选择一个 SKU')
    return
  }

  if (currentStock.value <= 0) {
    ElMessage.warning('当前规格库存不足')
    return
  }

  ElMessage.success(`已选中 ${quantity.value} 件 ${currentSku.value.sku.name}`)
}

async function loadDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    const result = await getMallProductDetail(route.params.id)
    if (result.code !== 200) {
      throw new Error(result.message || '商品详情加载失败')
    }

    detail.value = result.data
    selectedImage.value =
      result.data.images?.[0]?.imageUrl
      || result.data.product?.mainImage
      || ''

    const firstSku = result.data.selectedSku
      || result.data.skuList?.find((item) => item.sku?.status === 1)
      || result.data.skuList?.[0]
      || null

    selectedSkuId.value = firstSku?.sku?.id || null
    quantity.value = 1
  } catch (error) {
    errorMessage.value = error.message || '商品详情加载失败'
  } finally {
    loading.value = false
  }
}

watch(currentSku, () => {
  if (quantity.value > currentStock.value) {
    quantity.value = currentStock.value || 1
  }
})

onMounted(loadDetail)
</script>

<template>
  <main class="detail-page">
    <header class="detail-header">
      <div class="brand-mark">拼省省</div>
      <el-button link @click="goHome">返回首页</el-button>
    </header>

    <el-skeleton v-if="loading" :rows="8" animated />

    <el-result
      v-else-if="errorMessage"
      icon="error"
      :title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="loadDetail">重新加载</el-button>
      </template>
    </el-result>

    <section v-else-if="detail" class="detail-shell">
      <div class="gallery-panel">
        <el-image
          v-if="mainImage"
          :src="mainImage"
          fit="cover"
          class="main-image"
          :preview-src-list="(detail.images || []).map((item) => item.imageUrl)"
        />
        <div v-else class="empty-image">暂无商品图片</div>

        <div class="thumb-row">
          <button
            v-if="detail.product?.mainImage"
            class="thumb-item"
            :class="{ active: selectedImage === detail.product.mainImage }"
            @click="selectImage(detail.product.mainImage)"
          >
            主图
          </button>
          <button
            v-for="image in detail.images"
            :key="image.id"
            class="thumb-item"
            :class="{ active: selectedImage === image.imageUrl }"
            @click="selectImage(image.imageUrl)"
          >
            图{{ image.sort }}
          </button>
        </div>
      </div>

      <div class="info-panel">
        <p class="sub-title">{{ detail.product.subtitle || '拼省省精选商品' }}</p>
        <h1>{{ detail.product.name }}</h1>
        <p class="description">{{ detail.product.description }}</p>

        <div class="meta-row">
          <span>品牌：{{ detail.brand?.name || '未设置' }}</span>
          <span>分类：{{ detail.category?.name || '未设置' }}</span>
        </div>

        <div class="price-box">
          <span class="price-label">价格</span>
          <strong>¥{{ currentPrice }}</strong>
          <span class="stock-text">库存 {{ currentStock }}</span>
        </div>

        <section class="sku-section">
          <div class="section-title">选择规格</div>
          <div class="sku-grid">
            <button
              v-for="item in detail.skuList"
              :key="item.sku.id"
              class="sku-card"
              :class="{ active: selectedSkuId === item.sku.id }"
              @click="selectSku(item)"
            >
              <span class="sku-name">{{ item.sku.name }}</span>
              <span class="sku-price">¥{{ item.sku.price }}</span>
              <span class="sku-stock">库存 {{ item.availableStock }}</span>
            </button>
          </div>
        </section>

        <section v-if="skuAttributes.length" class="attribute-section">
          <div class="section-title">规格属性</div>
          <div class="attribute-list">
            <span
              v-for="[key, value] in skuAttributes"
              :key="key"
              class="attribute-chip"
            >
              {{ key }}：{{ value }}
            </span>
          </div>
        </section>

        <section class="buy-section">
          <div class="qty-row">
            <span class="section-title">购买数量</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="Math.max(currentStock, 1)"
            />
          </div>

          <div class="action-row">
            <el-button type="primary" @click="handleQuickAction">
              确认选择
            </el-button>
            <el-button @click="goHome">继续逛逛</el-button>
          </div>
        </section>
      </div>
    </section>
  </main>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, #fff4f2, transparent 30%),
    linear-gradient(180deg, #f7f8fb 0%, #eef2f7 100%);
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 18px 24px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.05);
}

.brand-mark {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 800;
}

.detail-shell {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 24px;
}

.gallery-panel,
.info-panel {
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.06);
}

.main-image,
.empty-image {
  width: 100%;
  height: 420px;
  border-radius: 18px;
}

.empty-image {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #f5f7fa;
}

.thumb-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.thumb-item {
  padding: 8px 14px;
  border: 1px solid #e4e7ed;
  border-radius: 999px;
  background: #fff;
  cursor: pointer;
}

.thumb-item.active {
  border-color: #f56c6c;
  color: #f56c6c;
}

.sub-title {
  margin: 0 0 6px;
  color: #f56c6c;
  font-weight: 700;
}

h1 {
  margin: 0;
  font-size: 30px;
}

.description {
  margin: 14px 0 18px;
  color: #606266;
  line-height: 1.8;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #606266;
  margin-bottom: 18px;
}

.price-box {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #fff2f0, #fffaf5);
}

.price-label,
.stock-text {
  color: #909399;
}

.price-box strong {
  color: #e64545;
  font-size: 34px;
}

.sku-section,
.attribute-section,
.buy-section {
  margin-top: 22px;
}

.section-title {
  margin-bottom: 12px;
  font-weight: 700;
}

.sku-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.sku-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 16px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.sku-card.active {
  border-color: #f56c6c;
  box-shadow: 0 8px 24px rgba(245, 108, 108, 0.14);
}

.sku-name {
  font-weight: 700;
}

.sku-price {
  color: #e64545;
  font-size: 18px;
}

.sku-stock {
  color: #909399;
  font-size: 13px;
}

.attribute-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.attribute-chip {
  padding: 8px 12px;
  border-radius: 999px;
  background: #f5f7fa;
  color: #606266;
}

.qty-row,
.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-row {
  margin-top: 18px;
}

@media (max-width: 960px) {
  .detail-shell {
    grid-template-columns: 1fr;
  }

  .sku-grid {
    grid-template-columns: 1fr;
  }
}
</style>
