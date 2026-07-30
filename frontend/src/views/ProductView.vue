<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getBrandList } from '../api/brand'
import { getCategoryList } from '../api/category'
import { deleteImage as deleteMinioImage, uploadImage } from '../api/file'
import {
  createProductImage,
  createProduct,
  deleteProductImage,
  getProductImages,
  getProductPage,
  updateProduct,
  updateProductStatus
} from '../api/product'

// 保存当前页的商品记录
const productList = ref([])
const router = useRouter()

// 保存品牌和分类，用于把 ID 转成名称显示
const brandList = ref([])
const categoryList = ref([])

// 分页数据
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 控制商品表格加载状态
const loading = ref(false)

// 新增和编辑商品共用一个弹窗表单
const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const uploading = ref(false)

const carouselVisible = ref(false)
const carouselLoading = ref(false)
const carouselUploading = ref(false)
const carouselProduct = ref(null)
const carouselImages = ref([])

const productForm = reactive({
  id: null,
  brandId: null,
  categoryId: null,
  name: '',
  subtitle: '',
  mainImage: '',
  description: '',
  minPrice: 0,
  status: 1
})

const rules = {
  brandId: [{ required: true, message: '请选择品牌', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  minPrice: [{ required: true, message: '请输入最低价', trigger: 'blur' }]
}

// 查询品牌、分类这类关联数据
async function loadReferenceData() {
  try {
    const [brandResult, categoryResult] = await Promise.all([
      getBrandList(),
      getCategoryList()
    ])

    if (brandResult.code === 200) {
      brandList.value = brandResult.data
    }

    if (categoryResult.code === 200) {
      categoryList.value = categoryResult.data
    }
  } catch {
    ElMessage.error('品牌或分类数据加载失败')
  }
}

// 按页码查询商品
async function loadProductPage() {
  loading.value = true

  try {
    const result = await getProductPage({
      page: page.value,
      size: size.value
    })

    if (result.code === 200) {
      productList.value = result.data.records
      total.value = result.data.total
      return
    }

    ElMessage.error(result.message)
  } catch {
    ElMessage.error('商品列表加载失败，请检查 Gateway 和商品服务')
  } finally {
    loading.value = false
  }
}

// 根据商品中的 brandId 显示品牌名称
function getBrandName(brandId) {
  const brand = brandList.value.find((item) => item.id === brandId)
  return brand ? brand.name : '未设置品牌'
}

// 根据商品中的 categoryId 显示分类名称
function getCategoryName(categoryId) {
  const category = categoryList.value.find((item) => item.id === categoryId)
  return category ? category.name : '未设置分类'
}

// 分类下拉框保留层级缩进，选择子分类时更直观
function formatCategoryName(category) {
  const prefix = '— '.repeat(Math.max(category.levelNum - 1, 0))
  return `${prefix}${category.name}`
}

// 后端时间格式中的 T 替换为空格，便于阅读
function formatTime(time) {
  return time ? time.replace('T', ' ') : '-'
}

// 切换页码时重新查询当前页
function handleCurrentChange(currentPage) {
  page.value = currentPage
  loadProductPage()
}

// 修改每页数量时，回到第一页重新查询
function handleSizeChange(currentSize) {
  size.value = currentSize
  page.value = 1
  loadProductPage()
}

// 清空表单，用于创建一个全新的商品
function resetProductForm() {
  Object.assign(productForm, {
    id: null,
    brandId: null,
    categoryId: null,
    name: '',
    subtitle: '',
    mainImage: '',
    description: '',
    minPrice: 0,
    status: 1
  })
}

function openCreateDialog() {
  resetProductForm()
  dialogVisible.value = true
}

// 编辑时将表格行数据放回表单，用户只修改需要变化的字段
function openEditDialog(row) {
  Object.assign(productForm, {
    id: row.id,
    brandId: row.brandId,
    categoryId: row.categoryId,
    name: row.name,
    subtitle: row.subtitle || '',
    mainImage: row.mainImage || '',
    description: row.description || '',
    minPrice: row.minPrice,
    status: row.status
  })
  dialogVisible.value = true
}

async function submitProduct() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const data = {
      brandId: productForm.brandId,
      categoryId: productForm.categoryId,
      name: productForm.name,
      subtitle: productForm.subtitle,
      mainImage: productForm.mainImage,
      description: productForm.description,
      minPrice: productForm.minPrice,
      status: productForm.status
    }

    const result = productForm.id
      ? await updateProduct(productForm.id, data)
      : await createProduct(data)

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success(productForm.id ? '商品修改成功' : '商品新增成功')
    dialogVisible.value = false
    await loadProductPage()
  } catch {
    ElMessage.error('商品保存失败')
  } finally {
    saving.value = false
  }
}

async function changeProductStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '上架' : '下架'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}商品“${row.name}”吗？`,
      '操作确认',
      { type: 'warning' }
    )

    const result = await updateProductStatus(row.id, nextStatus)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success(`商品已${actionText}`)
    await loadProductPage()
  } catch {
    // 用户取消操作时不提示错误
  }
}

// Element Plus 选中文件后，交给后端上传到 MinIO 并回填图片地址
async function handleImageUpload({ file }) {
  uploading.value = true
  try {
    const result = await uploadImage(file)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    productForm.mainImage = result.data.url
    ElMessage.success('图片上传成功')
  } catch {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

// 删除当前表单关联的图片，并清空商品主图地址
async function removeProductImage() {
  if (!productForm.mainImage) {
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除当前商品主图吗？', '操作确认', {
      type: 'warning'
    })

    const result = await deleteMinioImage(productForm.mainImage)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    productForm.mainImage = ''
    ElMessage.success('图片已删除')
  } catch {
    // 用户取消操作时不提示错误
  }
}

async function loadCarouselImages() {
  if (!carouselProduct.value) {
    return
  }

  carouselLoading.value = true
  try {
    const result = await getProductImages(carouselProduct.value.id)
    if (result.code === 200) {
      carouselImages.value = result.data
      return
    }
    ElMessage.error(result.message)
  } catch {
    ElMessage.error('轮播图加载失败')
  } finally {
    carouselLoading.value = false
  }
}

async function openCarouselDialog(row) {
  carouselProduct.value = row
  carouselVisible.value = true
  await loadCarouselImages()
}

// 先上传到 MinIO，再把返回 URL 保存为该商品的轮播图记录
async function handleCarouselUpload({ file }) {
  if (!carouselProduct.value) {
    return
  }

  carouselUploading.value = true
  try {
    const uploadResult = await uploadImage(file)
    if (uploadResult.code !== 200) {
      ElMessage.error(uploadResult.message)
      return
    }

    const saveResult = await createProductImage(carouselProduct.value.id, {
      imageUrl: uploadResult.data.url,
      sort: carouselImages.value.length
    })

    if (saveResult.code !== 200) {
      // 轮播图记录保存失败时清理刚上传的孤立文件
      await deleteMinioImage(uploadResult.data.url)
      ElMessage.error(saveResult.message)
      return
    }

    ElMessage.success('轮播图上传成功')
    await loadCarouselImages()
  } catch {
    ElMessage.error('轮播图上传失败')
  } finally {
    carouselUploading.value = false
  }
}

async function removeCarouselImage(image) {
  try {
    await ElMessageBox.confirm('确定要删除这张轮播图吗？', '操作确认', {
      type: 'warning'
    })

    const result = await deleteProductImage(image.id)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success('轮播图已删除')
    await loadCarouselImages()
  } catch {
    // 用户取消操作时不提示错误
  }
}

// 从商品行跳到 SKU 页面，并把商品 ID 放进地址参数
function goSkuManagement(row) {
  router.push({ name: 'sku', query: { productId: row.id } })
}

// 页面打开时，先加载关联数据，再加载商品分页数据
onMounted(async () => {
  await loadReferenceData()
  await loadProductPage()
})
</script>

<template>
  <section>
    <div class="page-header">
      <h1>商品管理</h1>

      <div class="page-actions">
        <el-button :loading="loading" @click="loadProductPage">
          刷新列表
        </el-button>
        <el-button type="primary" @click="openCreateDialog">
          新增商品
        </el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="productList" border>
      <el-table-column prop="id" label="ID" width="80" />

      <el-table-column label="主图" width="100">
        <template #default="{ row }">
          <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              fit="cover"
              class="product-image"
          />
          <span v-else>暂无</span>
        </template>
      </el-table-column>

      <el-table-column prop="name" label="商品名称" min-width="180" />

      <el-table-column prop="subtitle" label="副标题" min-width="180" />

      <el-table-column label="品牌" width="130">
        <template #default="{ row }">
          {{ getBrandName(row.brandId) }}
        </template>
      </el-table-column>

      <el-table-column label="分类" width="130">
        <template #default="{ row }">
          {{ getCategoryName(row.categoryId) }}
        </template>
      </el-table-column>

      <el-table-column prop="minPrice" label="最低价" width="110">
        <template #default="{ row }">
          ¥{{ row.minPrice }}
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="270">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button link type="primary" @click="goSkuManagement(row)">
            SKU
          </el-button>
          <el-button link type="primary" @click="openCarouselDialog(row)">
            轮播图
          </el-button>
          <el-button
            link
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="changeProductStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 后端分页数据和前端页码组件双向绑定 -->
    <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />

    <!-- 商品主图暂时使用 URL 输入，MinIO 接入后再替换为上传组件 -->
    <el-dialog
      v-model="dialogVisible"
      :title="productForm.id ? '编辑商品' : '新增商品'"
      width="680px"
    >
      <el-form ref="formRef" :model="productForm" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brandId">
              <el-select v-model="productForm.brandId" style="width: 100%">
                <el-option
                  v-for="brand in brandList"
                  :key="brand.id"
                  :label="brand.name"
                  :value="brand.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="productForm.categoryId" style="width: 100%">
                <el-option
                  v-for="category in categoryList"
                  :key="category.id"
                  :label="formatCategoryName(category)"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="例如：拼省省蓝牙耳机 Pro" />
        </el-form-item>

        <el-form-item label="副标题">
          <el-input v-model="productForm.subtitle" placeholder="例如：主动降噪无线耳机" />
        </el-form-item>

        <el-form-item label="商品主图">
          <div class="image-upload-area">
            <el-upload
              accept="image/*"
              :show-file-list="false"
              :http-request="handleImageUpload"
              :disabled="uploading"
            >
              <el-button :loading="uploading">选择图片上传</el-button>
            </el-upload>

            <div v-if="productForm.mainImage" class="image-preview-wrapper">
              <el-image
                :src="productForm.mainImage"
                fit="cover"
                class="form-image-preview"
                :preview-src-list="[productForm.mainImage]"
              />
              <el-button link type="danger" @click="removeProductImage">
                删除图片
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="3"
            placeholder="简要描述商品卖点"
          />
        </el-form-item>

        <el-form-item label="最低价" prop="minPrice">
          <el-input-number
            v-model="productForm.minPrice"
            :min="0"
            :precision="2"
            :step="10"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitProduct">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 已保存商品可在这里维护多张轮播图 -->
    <el-dialog
      v-model="carouselVisible"
      :title="carouselProduct ? `${carouselProduct.name} - 轮播图` : '轮播图管理'"
      width="720px"
    >
      <el-upload
        accept="image/*"
        :show-file-list="false"
        :http-request="handleCarouselUpload"
        :disabled="carouselUploading"
      >
        <el-button type="primary" :loading="carouselUploading">
          上传轮播图
        </el-button>
      </el-upload>

      <div v-loading="carouselLoading" class="carousel-image-list">
        <div v-for="image in carouselImages" :key="image.id" class="carousel-image-item">
          <el-image
            :src="image.imageUrl"
            fit="cover"
            :preview-src-list="carouselImages.map((item) => item.imageUrl)"
            class="carousel-image-preview"
          />
          <span>排序：{{ image.sort }}</span>
          <el-button link type="danger" @click="removeCarouselImage(image)">
            删除
          </el-button>
        </div>

        <el-empty v-if="!carouselLoading && carouselImages.length === 0" description="暂无轮播图" />
      </div>
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

.product-image {
  width: 54px;
  height: 54px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 20px;
}

.page-actions {
  display: flex;
  gap: 12px;
}

.image-upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-image-preview {
  width: 80px;
  height: 80px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.image-preview-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.carousel-image-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.carousel-image-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #606266;
}

.carousel-image-preview {
  width: 100%;
  height: 140px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>
