<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMallBrandList, getMallCategoryTree, getMallProductPage } from '../api/mall'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const errorMessage = ref('')
const productList = ref([])
const categoryTree = ref([])
const brandList = ref([])
const selectedCategoryId = ref(null)
const selectedBrandId = ref(null)

// 找出当前分类及其子分类，父分类也能筛选到子分类商品
function collectCategoryIds(category) {
  return [category.id, ...(category.children || []).flatMap(collectCategoryIds)]
}

const visibleCategoryIds = computed(() => {
  if (!selectedCategoryId.value) {
    return null
  }

  const category = categoryTree.value.find((item) => item.id === selectedCategoryId.value)
  return category ? collectCategoryIds(category) : [selectedCategoryId.value]
})

// 分类和品牌筛选在当前页数据上完成，后续接详情和搜索时再下沉到接口
const visibleProducts = computed(() => productList.value.filter((product) => {
  const categoryMatched = !visibleCategoryIds.value
    || visibleCategoryIds.value.includes(product.categoryId)
  const brandMatched = !selectedBrandId.value || product.brandId === selectedBrandId.value
  return categoryMatched && brandMatched
}))

function getBrandName(brandId) {
  const brand = brandList.value.find((item) => item.id === brandId)
  return brand ? brand.name : '拼省优选'
}

function selectCategory(categoryId) {
  selectedCategoryId.value = categoryId
}

function selectBrand(brandId) {
  selectedBrandId.value = brandId
}

function handleLogout() {
  userStore.clearLoginInfo()
  router.replace('/mall/login')
}

async function loadMallHome() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [categoryResult, brandResult, productResult] = await Promise.all([
      getMallCategoryTree(),
      getMallBrandList(),
      getMallProductPage({ page: 1, size: 100 })
    ])

    if (categoryResult.code !== 200) {
      throw new Error(categoryResult.message)
    }
    if (brandResult.code !== 200) {
      throw new Error(brandResult.message)
    }
    if (productResult.code !== 200) {
      throw new Error(productResult.message)
    }

    categoryTree.value = categoryResult.data
    brandList.value = brandResult.data
    productList.value = productResult.data.records
  } catch (error) {
    errorMessage.value = error.message || '商城数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadMallHome)
</script>

<template>
  <div class="mall-page">
    <header class="mall-header">
      <div class="header-inner">
        <div class="mall-logo">拼省省</div>
        <nav class="mall-nav">
          <el-link class="nav-link active" :underline="false">首页</el-link>
          <el-link class="nav-link" :underline="false">全部商品</el-link>
        </nav>
        <div class="user-actions">
          <span>你好，{{ userStore.username }}</span>
          <el-button link type="danger" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </header>

    <main class="mall-content">
      <section class="mall-hero">
        <div>
          <p class="hero-label">拼省省精选</p>
          <h1>好商品，拼着买更省</h1>
          <p>精选品质好物，价格透明，轻松挑选你的下一件心仪商品。</p>
        </div>
      </section>

      <section class="filter-section">
        <div class="filter-row">
          <span class="filter-label">分类</span>
          <el-button link :class="{ selected: !selectedCategoryId }" @click="selectCategory(null)">
            全部
          </el-button>
          <el-button
            v-for="category in categoryTree"
            :key="category.id"
            link
            :class="{ selected: selectedCategoryId === category.id }"
            @click="selectCategory(category.id)"
          >
            {{ category.name }}
          </el-button>
        </div>
        <div class="filter-row">
          <span class="filter-label">品牌</span>
          <el-button link :class="{ selected: !selectedBrandId }" @click="selectBrand(null)">
            全部
          </el-button>
          <el-button
            v-for="brand in brandList"
            :key="brand.id"
            link
            :class="{ selected: selectedBrandId === brand.id }"
            @click="selectBrand(brand.id)"
          >
            {{ brand.name }}
          </el-button>
        </div>
      </section>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
      />

      <section class="product-section">
        <div class="section-heading">
          <h2>热门商品</h2>
          <span v-if="!loading">共 {{ visibleProducts.length }} 件</span>
        </div>

        <div v-loading="loading" class="product-grid">
          <el-card v-for="product in visibleProducts" :key="product.id" class="product-card" shadow="hover">
            <el-image v-if="product.mainImage" :src="product.mainImage" fit="cover" class="product-image" />
            <div v-else class="product-image image-placeholder">拼省省</div>
            <div class="product-info">
              <span class="product-brand">{{ getBrandName(product.brandId) }}</span>
              <h3>{{ product.name }}</h3>
              <p>{{ product.subtitle || product.description || '精选好物，欢迎选购' }}</p>
              <strong>¥{{ product.minPrice }}</strong>
            </div>
          </el-card>
        </div>

        <el-empty
          v-if="!loading && !errorMessage && visibleProducts.length === 0"
          description="当前筛选条件下暂无商品"
        />
      </section>
    </main>
  </div>
</template>

<style scoped>
.mall-page {
  min-height: 100vh;
  background: #f5f7fa;
  color: #303133;
}

.mall-header {
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.header-inner,
.mall-content {
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
}

.header-inner {
  display: flex;
  height: 64px;
  align-items: center;
  justify-content: space-between;
}

.mall-logo {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}

.mall-nav {
  display: flex;
  gap: 28px;
  margin-right: auto;
  margin-left: 60px;
}

.nav-link.active,
.selected {
  color: #f56c6c !important;
  font-weight: 600;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #606266;
  font-size: 14px;
}

.mall-hero {
  display: flex;
  min-height: 210px;
  align-items: center;
  margin-top: 24px;
  padding: 30px 52px;
  border-radius: 8px;
  background: linear-gradient(110deg, #fff1f0, #fff8f2);
}

.hero-label {
  margin: 0 0 10px;
  color: #f56c6c;
  font-weight: 600;
}

.mall-hero h1 {
  margin: 0 0 12px;
  font-size: 34px;
}

.mall-hero p:last-child {
  margin: 0;
  color: #606266;
}

.filter-section {
  margin-top: 20px;
  padding: 16px 22px;
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.filter-row {
  display: flex;
  min-height: 36px;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #f2f6fc;
}

.filter-row:last-child {
  border-bottom: 0;
}

.filter-label {
  width: 42px;
  color: #909399;
  font-size: 14px;
}

.product-section {
  margin-top: 24px;
  padding-bottom: 40px;
}

.section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-heading h2 {
  margin: 0;
}

.section-heading span {
  color: #909399;
  font-size: 14px;
}

.product-grid {
  display: grid;
  min-height: 300px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.product-card {
  overflow: hidden;
}

.product-image {
  display: block;
  width: 100%;
  height: 190px;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fdf0ec;
  color: #f56c6c;
  font-size: 28px;
  font-weight: 700;
}

.product-info {
  padding-top: 12px;
}

.product-brand {
  color: #f56c6c;
  font-size: 13px;
}

.product-info h3 {
  overflow: hidden;
  margin: 6px 0;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.product-info p {
  overflow: hidden;
  height: 20px;
  margin: 0 0 12px;
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.product-info strong {
  color: #e64545;
  font-size: 20px;
}

@media (max-width: 800px) {
  .header-inner,
  .mall-content {
    width: min(100% - 24px, 600px);
  }

  .mall-nav {
    display: none;
  }

  .mall-hero {
    padding: 26px;
  }

  .mall-hero h1 {
    font-size: 28px;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
