<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { getProductPage } from '../api/product'
import {
  createSku,
  getSkuList,
  updateSku,
  updateSkuStatus,
  updateSkuStock
} from '../api/sku'

const route = useRoute()

// 商品下拉框的数据来源；当前阶段一次最多读取 100 条商品
const productList = ref([])
const selectedProductId = ref(null)
const skuList = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)

const skuForm = reactive({
  id: null,
  productId: null,
  skuName: '',
  price: 0,
  status: 1,
  availableStock: 0
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  skuName: [{ required: true, message: '请输入 SKU 名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

async function loadProducts() {
  try {
    const result = await getProductPage({ page: 1, size: 100 })
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    productList.value = result.data.records
    const productIdFromUrl = Number(route.query.productId)
    const exists = productList.value.some((item) => item.id === productIdFromUrl)
    selectedProductId.value = exists
      ? productIdFromUrl
      : productList.value[0]?.id || null
  } catch {
    ElMessage.error('商品数据加载失败')
  }
}

async function loadSkuList() {
  if (!selectedProductId.value) {
    skuList.value = []
    return
  }

  loading.value = true
  try {
    const result = await getSkuList(selectedProductId.value)
    if (result.code === 200) {
      skuList.value = result.data
      return
    }
    ElMessage.error(result.message)
  } catch {
    ElMessage.error('SKU 列表加载失败，请检查商品服务')
  } finally {
    loading.value = false
  }
}

function handleProductChange() {
  loadSkuList()
}

function resetSkuForm() {
  Object.assign(skuForm, {
    id: null,
    productId: selectedProductId.value,
    skuName: '',
    price: 0,
    status: 1,
    availableStock: 0
  })
}

function openCreateDialog() {
  if (!selectedProductId.value) {
    ElMessage.warning('请先选择商品')
    return
  }
  resetSkuForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  Object.assign(skuForm, {
    id: row.sku.id,
    productId: row.sku.productId,
    skuName: row.sku.name,
    price: row.sku.price,
    status: row.sku.status,
    availableStock: row.availableStock
  })
  dialogVisible.value = true
}

async function submitSku() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const data = {
      productId: skuForm.productId,
      skuName: skuForm.skuName,
      price: skuForm.price,
      status: skuForm.status,
      availableStock: skuForm.availableStock
    }

    const result = skuForm.id
      ? await updateSku(skuForm.id, data)
      : await createSku(data)

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    selectedProductId.value = skuForm.productId
    ElMessage.success(skuForm.id ? 'SKU 修改成功' : 'SKU 新增成功')
    dialogVisible.value = false
    await loadSkuList()
  } catch {
    ElMessage.error('SKU 保存失败')
  } finally {
    saving.value = false
  }
}

async function changeSkuStatus(row) {
  const nextStatus = row.sku.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '上架' : '下架'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText} SKU“${row.sku.name}”吗？`,
      '操作确认',
      { type: 'warning' }
    )
    const result = await updateSkuStatus(row.sku.id, nextStatus)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success(`SKU 已${actionText}`)
    await loadSkuList()
  } catch {
    // 用户取消操作时不提示错误
  }
}

async function changeStock(row) {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入新的可用库存',
      `修改 ${row.sku.name} 的库存`,
      {
        inputValue: String(row.availableStock),
        inputPattern: /^\d+$/,
        inputErrorMessage: '库存必须是大于等于 0 的整数'
      }
    )

    const result = await updateSkuStock(row.sku.id, Number(value))
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('库存修改成功')
    await loadSkuList()
  } catch {
    // 用户取消输入时不提示错误
  }
}

onMounted(async () => {
  await loadProducts()
  await loadSkuList()
})
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-header">
      <div>
        <h1>SKU 与库存管理</h1>
        <p>SKU 是商品可实际销售的规格，库存绑定在 SKU 上。</p>
      </div>

      <div class="admin-page-actions">
        <el-select
          v-model="selectedProductId"
          placeholder="请选择商品"
          style="width: 240px"
          @change="handleProductChange"
        >
          <el-option
            v-for="product in productList"
            :key="product.id"
            :label="product.name"
            :value="product.id"
          />
        </el-select>
        <el-button :loading="loading" @click="loadSkuList">刷新列表</el-button>
        <el-button type="primary" @click="openCreateDialog">新增 SKU</el-button>
      </div>
    </div>

    <div class="admin-table-wrap">
    <el-table v-loading="loading" :data="skuList" border>
      <el-table-column label="ID" width="80">
        <template #default="{ row }">{{ row.sku.id }}</template>
      </el-table-column>

      <el-table-column label="规格名称" min-width="200">
        <template #default="{ row }">{{ row.sku.name }}</template>
      </el-table-column>

      <el-table-column label="价格" width="110">
        <template #default="{ row }">¥{{ row.sku.price }}</template>
      </el-table-column>

      <el-table-column prop="availableStock" label="可用库存" width="110" />
      <el-table-column prop="lockedStock" label="锁定库存" width="110" />

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.sku.status === 1 ? 'success' : 'info'">
            {{ row.sku.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="primary" @click="changeStock(row)">库存</el-button>
          <el-button
            link
            :type="row.sku.status === 1 ? 'danger' : 'success'"
            @click="changeSkuStatus(row)"
          >
            {{ row.sku.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="skuForm.id ? '编辑 SKU' : '新增 SKU'"
      width="620px"
    >
      <el-form ref="formRef" :model="skuForm" :rules="rules" label-width="100px">
        <el-form-item label="所属商品" prop="productId">
          <el-select v-model="skuForm.productId" style="width: 100%">
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="规格名称" prop="skuName">
          <el-input v-model="skuForm.skuName" placeholder="例如：黑色标准版" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="skuForm.price" :min="0" :precision="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可用库存">
              <el-input-number v-model="skuForm.availableStock" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="状态">
          <el-radio-group v-model="skuForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitSku">保存</el-button>
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

.page-header h1,
.page-header p {
  margin: 0;
}

.page-header p {
  margin-top: 8px;
  color: #909399;
}

.page-actions {
  display: flex;
  gap: 12px;
}
</style>
