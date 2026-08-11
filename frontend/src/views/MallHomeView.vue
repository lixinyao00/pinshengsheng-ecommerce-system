<script setup>
// 引入 Vue 的响应式和生命周期工具
import { computed, onMounted, ref } from 'vue'
// 获取路由跳转工具
import { useRouter } from 'vue-router'
// 引入商城首页相关接口
import {
  getMallBrandList,
  getMallBannerList,
  getMallCategoryTree,
  getMallProductPage
} from '../api/mall'
// 引入用户状态管理
import { useUserStore } from '../stores/user'
import MallHeader from '../components/MallHeader.vue'
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
const bannerList = ref([])
const bannerPlaceholder = '/home-banner-placeholder.svg'
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

// 打开当前用户的订单页面
function openOrderPage() {
  router.push('/mall/orders')
}

// 打开收货地址管理页面
function openAddressPage() {
  router.push('/mall/addresses')
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

    // 轮播图没有数据时保留占位图，不影响商城其他内容加载
    const bannerResult = await getMallBannerList().catch(() => null)
    bannerList.value = bannerResult?.code === 200 ? (bannerResult.data || []) : []
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
    <MallHeader />
    <!-- 商城首页轮播图区域，没有上传图片时显示本地占位图 -->
    <section class="mall-banner">
      <el-carousel
        v-if="bannerList.length > 0"
        height="230px"
        :interval="4500"
        arrow="hover"
      >
        <el-carousel-item v-for="banner in bannerList" :key="banner.id">
          <img class="home-banner-image" :src="banner.imageUrl" alt="拼省省首页轮播图">
        </el-carousel-item>
      </el-carousel>

      <div v-else class="banner-placeholder">
        <img class="home-banner-image" :src="bannerPlaceholder" alt="首页轮播图占位图">
      </div>
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
          <div class="product-cover">
            <img v-if="product.mainImage" :src="product.mainImage" :alt="product.name">
            <span v-else>暂无商品封面</span>
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
          <p>{{ product.subtitle || product.description }}</p>
            <div class="product-bottom"><strong>¥{{ product.minPrice }}</strong><span>查看详情 →</span></div>
          </div>
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
  max-width: 1200px;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 24px;
  background: var(--mall-page);
}
.mall-banner {
  margin-top: 20px;
  position: relative;
  height: 230px;
  overflow: hidden;
  box-sizing: border-box;
  padding: 0;
  border-radius: 20px;
  background: linear-gradient(115deg, #fff0ed 0%, #fff9f5 58%, #ffe9dd 100%);
  box-shadow: var(--mall-shadow);
}

.mall-banner :deep(.el-carousel),
.mall-banner :deep(.el-carousel__container) {
  height: 230px !important;
}

.home-banner-image {
  display: block;
  width: 100%;
  height: 230px;
  object-fit: cover;
  border-radius: 20px;
}

.banner-placeholder {
  height: 230px;
}
.filter-section {
  margin-top: 20px;
  padding: 16px 22px;
  background: #fff;
  border: 1px solid var(--mall-border);
  border-radius: 12px;
  box-shadow: 0 5px 18px rgba(46, 38, 35, 0.04);
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
  min-height: 242px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--mall-border);
  border-radius: 14px;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 14px 28px rgba(46, 38, 35, 0.12);
}

.product-cover {
  display: grid;
  height: 168px;
  margin: -20px -20px 16px;
  place-items: center;
  overflow: hidden;
  color: #a7adb7;
  font-size: 14px;
  background: linear-gradient(135deg, #fff1ed, #ffe1d4);
}

.product-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-cover img {
  transform: scale(1.05);
}

.product-info h3 {
  overflow: hidden;
  margin: 0 0 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-card h3 {
  margin: 0 0 10px;
}

.product-card p {
  min-height: 40px;
  margin: 0 0 12px;
  color: var(--mall-muted);
  line-height: 1.5;
}

.product-bottom {
  display: flex;
  align-items: end;
  justify-content: space-between;
}

.product-bottom strong {
  color: var(--mall-primary);
  font-size: 20px;
}

.product-bottom span {
  color: var(--mall-primary);
  font-size: 12px;
}

@media (max-width: 700px) {
  .mall-banner :deep(.el-carousel),
  .mall-banner :deep(.el-carousel__container),
  .mall-banner,
  .banner-placeholder {
    height: 168px !important;
  }

  .home-banner-image {
    height: 168px;
    border-radius: 14px;
  }

}

@media (max-width: 600px) {
  .mall-home-page {
    padding: 12px;
  }

  .mall-banner {
    margin-top: 12px;
  }

  .filter-section {
    padding: 12px 14px;
  }

  .filter-row {
    flex-wrap: wrap;
    gap: 4px 8px;
  }

  .filter-label {
    flex-basis: 100%;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .product-card {
    min-height: 0;
  }

  .product-cover {
    height: 118px;
    margin: -20px -20px 12px;
  }

  .product-card h3 {
    margin-bottom: 6px;
    font-size: 15px;
  }

  .product-card p {
    min-height: 36px;
    margin-bottom: 8px;
    font-size: 12px;
  }

  .product-bottom strong {
    font-size: 17px;
  }

  .product-bottom span {
    display: none;
  }
}
</style>
