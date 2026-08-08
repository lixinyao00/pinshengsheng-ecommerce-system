<script setup>
// 引入 Vue 的响应式和生命周期工具
import { computed, onMounted, ref } from 'vue'
// 获取路由跳转工具
import { useRouter } from 'vue-router'
// 引入商城首页相关接口
import {
  getMallBrandList,
  getMallCategoryTree,
  getMallProductPage
} from '../api/mall'
// 引入用户状态管理
import { useUserStore } from '../stores/user'
// 获取当前登录用户信息
const userStore = useUserStore()
// 获取路由对象
const router = useRouter()
// 页面加载状态
const loading = ref(true)

// 页面错误提示
const errorMessage = ref('')

// 保存商品、分类和品牌数据
const productList = ref([])
const categoryTree = ref([])
const brandList = ref([])
// 当前选中的分类和品牌
const selectedCategoryId = ref(null)
const selectedBrandId = ref(null)
// 记录用户选择的分类
function selectCategory(categoryId) {
  selectedCategoryId.value = categoryId
}

// 记录用户选择的品牌
function selectBrand(brandId) {
  selectedBrandId.value = brandId
}
function handleLogout() {
  userStore.clearLoginInfo()
  router.replace('/mall/login')
}
// 打开商品详情页
function openProductDetail(productId) {
  router.push(`/mall/product/${productId}`)
}

// 打开每日签到页面
function openSignPage() {
  router.push('/mall/sign')
}

// 打开购物车页面
function openCartPage() {
  router.push('/mall/cart')
}
// 根据当前选择的分类和品牌计算可见商品
const visibleProducts = computed(() => {
  return productList.value.filter((product) => {
    const categoryMatched =
        !selectedCategoryId.value
        || product.categoryId === selectedCategoryId.value

    const brandMatched =
        !selectedBrandId.value
        || product.brandId === selectedBrandId.value

    return categoryMatched && brandMatched
  })
})
// 加载商城首页数据
async function loadMallHome() {
  loading.value = true
  errorMessage.value = ''

  try {
    // 同时请求分类、品牌和商品
    const [categoryResult, brandResult, productResult] = await Promise.all([
      getMallCategoryTree(),
      getMallBrandList(),
      getMallProductPage({page: 1, size: 100})
    ])
    // 判断三个接口是否都调用成功
    if (categoryResult.code !== 200) {
      throw new Error(categoryResult.message)
    }

    if (brandResult.code !== 200) {
      throw new Error(brandResult.message)
    }

    if (productResult.code !== 200) {
      throw new Error(productResult.message)
    }
    // 把接口返回的数据保存到页面状态
    categoryTree.value = categoryResult.data
    brandList.value = brandResult.data
    productList.value = productResult.data.records
  } catch (error) {
    // 保存错误信息，页面可以显示提示
    errorMessage.value = error.message || '商城数据加载失败'
  } finally {
    // 无论成功还是失败，都结束加载状态
    loading.value = false
  }
}

// 页面加载完成后，自动请求商城数据
onMounted(loadMallHome)

</script>

<template>
  <main class="mall-home-page">
    <!-- 商城顶部导航 -->
    <header class="mall-header">
      <div class="mall-logo">拼省省</div>
      <!-- 商城页面导航 -->
      <nav class="mall-nav">
        <span class="active">首页</span>
        <span>全部商品</span>
        <span @click="openSignPage">每日签到</span>
        <span @click="openCartPage">购物车</span>
      </nav>
      <span>
        你好，{{ userStore.username }}
      <el-button
          link
          type="danger"
          @click="handleLogout"
      >退出登录</el-button>
      </span>
    </header>
    <!-- 商城欢迎区域 -->
    <section class="mall-banner">
      <p>拼省省精选</p>
      <h1>好商品，拼着买更省</h1>
      <span>精选品质好物，价格透明，轻松挑选心仪商品。</span>
    </section>
    <!-- 商品筛选区域 -->
    <section class="filter-section">
      <div class="filter-row">
        <span class="filter-label">分类</span>

        <!-- 默认显示全部分类 -->
        <el-button
            link
            @click="selectCategory(null)"
        >
          全部
        </el-button>

        <!-- 循环显示后端返回的分类 -->
        <el-button
            v-for="category in categoryTree"
            :key="category.id"
            link
            @click="selectCategory(category.id)"
        >
          {{ category.name }}
        </el-button>
      </div>

      <div class="filter-row">
        <span class="filter-label">品牌</span>
        <!-- 默认显示全部品牌 -->
        <el-button
            link
            @click="selectBrand(null)"
        >
          全部
        </el-button>

        <!-- 循环显示后端返回的品牌 -->
        <el-button
          v-for="brand in brandList"
          :key="brand.id"
          link
        @click="selectBrand(brand.id)"
           >
          {{ brand.name }}
        </el-button>
      </div>
    </section>
    <!-- 商品展示区域 -->
    <section class="product-section">
      <div class="section-heading">
        <h2>热门商品</h2>
        <span>精选好物</span>
      </div>
      <!-- 商品列表 -->
      <div class="product-grid">
        <el-card
            v-for="product in visibleProducts"
          :key="product.id"
          class="product-card"
          shadow="hover"
        @click="openProductDetail(product.id)">
          <h3>{{ product.name }}</h3>
          <p>{{ product.subtitle || product.description }}</p>
          <strong>¥{{ product.minPrice }}</strong>
        </el-card>
      </div>
      <!-- 没有商品时显示提示 -->
      <el-empty
          v-if="visibleProducts.length === 0"
          description="暂时没有商品"
      />
    </section>
  </main>
</template>
<style scoped>
.mall-home-page {
  min-height: 100vh;
  padding: 24px;
  background: #f5f7fa;
}
.mall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 28px;
  background: #ffffff;
  border-radius: 8px;
}

.mall-logo {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}
.mall-nav {
  display: flex;
  gap: 24px;
  margin-right: auto;
  margin-left: 50px;
}

.mall-nav .active {
  color: #f56c6c;
  font-weight: 600;
}

.mall-nav span {
  cursor: pointer;
}
.mall-banner {
  margin-top: 20px;
  padding: 36px 48px;
  border-radius: 8px;
  background: linear-gradient(110deg, #fff1f0, #fff8f2);
}

.mall-banner h1 {
  margin: 8px 0 12px;
  font-size: 32px;
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
}
.filter-label {
  width: 42px;
  color: #909399;
  font-size: 14px;
}

.product-section {
  margin-top: 24px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.product-card {
  min-height: 150px;
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
</style>
