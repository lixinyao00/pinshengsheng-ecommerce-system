<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMallBrandList, getMallCategoryTree, getMallProductPage } from '../api/mall'
import MallHeader from '../components/MallHeader.vue'

const router = useRouter()
const loading = ref(true)
const errorMessage = ref('')
const productList = ref([])
const categoryTree = ref([])
const brandList = ref([])
const selectedCategoryId = ref(null)
const selectedBrandId = ref(null)

const visibleProducts = computed(() => productList.value.filter((product) => {
  const categoryMatched = !selectedCategoryId.value || product.categoryId === selectedCategoryId.value
  const brandMatched = !selectedBrandId.value || product.brandId === selectedBrandId.value
  return categoryMatched && brandMatched
}))

function openProductDetail(productId) {
  router.push(`/mall/product/${productId}`)
}

async function loadProducts() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [categoryResult, brandResult, productResult] = await Promise.all([
      getMallCategoryTree(),
      getMallBrandList(),
      getMallProductPage({ page: 1, size: 100 })
    ])

    if (categoryResult.code !== 200 || brandResult.code !== 200 || productResult.code !== 200) {
      throw new Error('商品列表加载失败')
    }

    categoryTree.value = categoryResult.data || []
    brandList.value = brandResult.data || []
    productList.value = productResult.data?.records || []
  } catch (error) {
    errorMessage.value = error.message || '商品列表加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadProducts)
</script>

<template>
  <main class="product-list-page">
    <MallHeader />

    <section class="page-intro">
      <p>拼省省精选</p>
      <h1>全部商品</h1>
      <span>按分类和品牌筛选，挑选适合你的商品。</span>
    </section>

    <section class="filter-section">
      <div class="filter-row">
        <span class="filter-label">分类</span>
        <el-button link :type="selectedCategoryId === null ? 'primary' : ''" @click="selectedCategoryId = null">
          全部
        </el-button>
        <el-button
          v-for="category in categoryTree"
          :key="category.id"
          link
          :type="selectedCategoryId === category.id ? 'primary' : ''"
          @click="selectedCategoryId = category.id"
        >
          {{ category.name }}
        </el-button>
      </div>

      <div class="filter-row">
        <span class="filter-label">品牌</span>
        <el-button link :type="selectedBrandId === null ? 'primary' : ''" @click="selectedBrandId = null">
          全部
        </el-button>
        <el-button
          v-for="brand in brandList"
          :key="brand.id"
          link
          :type="selectedBrandId === brand.id ? 'primary' : ''"
          @click="selectedBrandId = brand.id"
        >
          {{ brand.name }}
        </el-button>
      </div>
    </section>

    <el-card v-loading="loading" class="product-card-wrapper" shadow="never">
      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon />
      <div v-else class="product-grid">
        <el-card
          v-for="product in visibleProducts"
          :key="product.id"
          class="product-card"
          shadow="hover"
          @click="openProductDetail(product.id)"
        >
          <h3>{{ product.name }}</h3>
          <p>{{ product.subtitle || product.description }}</p>
          <strong>¥{{ product.minPrice }}</strong>
        </el-card>
      </div>
      <el-empty v-if="!loading && !errorMessage && visibleProducts.length === 0" description="暂时没有商品" />
    </el-card>
  </main>
</template>

<style scoped>
.product-list-page {
  min-height: 100vh;
  max-width: 1200px;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 24px;
  background: #f5f7fa;
}

.page-intro,
.filter-section,
.product-card-wrapper {
  max-width: 1200px;
  margin: 0 auto 20px;
}

.page-intro {
  padding: 32px 40px;
  border-radius: 10px;
  background: linear-gradient(110deg, #fff1f0, #fff8f2);
}

.page-intro p {
  margin: 0;
  color: #606266;
}

.page-intro h1 {
  margin: 8px 0 12px;
  font-size: 32px;
}

.page-intro span {
  color: #606266;
}

.filter-section {
  padding: 16px 22px;
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.filter-row {
  display: flex;
  min-height: 36px;
  align-items: center;
  gap: 16px;
}

.filter-label {
  width: 42px;
  color: #909399;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.product-card {
  min-height: 150px;
  cursor: pointer;
}

.product-card h3 {
  margin: 0 0 10px;
}

.product-card p {
  min-height: 40px;
  margin: 0 0 12px;
  color: #909399;
}

.product-card strong {
  color: #e64545;
  font-size: 20px;
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .product-list-page {
    padding: 12px;
  }

  .page-intro {
    padding: 24px;
  }

  .filter-row {
    flex-wrap: wrap;
    gap: 6px 12px;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
