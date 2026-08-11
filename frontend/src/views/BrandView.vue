<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createBrand,
  getBrandList,
  updateBrand,
  updateBrandStatus
} from '../api/brand'

// 保存品牌列表数据
const brandList = ref([])

// 控制表格加载状态
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const brandForm = reactive({
  id: null,
  name: '',
  logo: '',
  sort: 0,
  status: 1
})
const rules = {
  name: [
    { required: true, message: '请输入品牌名称',trigger:'blur' }
  ]
}

// 调用品牌接口，并把返回的 data 放进列表
async function loadBrandList() {
  loading.value = true

  try {
    const result = await getBrandList()

    if (result.code === 200) {
      brandList.value = result.data
      return
    }

    ElMessage.error(result.message)
  } catch {
    ElMessage.error('品牌列表加载失败，请检查 Gateway 和商品服务')
  } finally {
    loading.value = false
  }
}
// 恢复成新增品牌时的默认表单
function resetBrandForm() {
  Object.assign(brandForm, {
    id: null,
    name: '',
    logo: '',
    sort: 0,
    status: 1
  })
}

// 打开新增弹窗
function openCreateDialog() {
  resetBrandForm()
  dialogVisible.value = true
}

// 把当前行数据填入表单，供编辑使用
function openEditDialog(row) {
  Object.assign(brandForm, {
    id: row.id,
    name: row.name,
    logo: row.logo || '',
    sort: row.sort,
    status: row.status
  })

  dialogVisible.value = true
}

// 根据是否存在 id，决定调用新增还是编辑接口
async function submitBrand() {
  const valid = await formRef.value.validate().catch(() => false)

  if (!valid) {
    return
  }

  saving.value = true

  try {
    const data = {
      name: brandForm.name,
      logo: brandForm.logo,
      sort: brandForm.sort,
      status: brandForm.status
    }

    const result = brandForm.id
        ? await updateBrand(brandForm.id, data)
        : await createBrand(data)

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success(brandForm.id ? '品牌修改成功' : '品牌新增成功')
    dialogVisible.value = false
    await loadBrandList()
  } catch {
    ElMessage.error('品牌保存失败')
  } finally {
    saving.value = false
  }
}

// 点击启用或停用时，先确认再请求后端
async function changeBrandStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(
        `确定要${actionText}品牌“${row.name}”吗？`,
        '操作确认',
        { type: 'warning' }
    )

    const result = await updateBrandStatus(row.id, nextStatus)

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success(`品牌已${actionText}`)
    await loadBrandList()
  } catch {
    // 点击取消时不需要提示错误
  }
}

// 页面打开时自动查询一次品牌数据
onMounted(loadBrandList)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-header">
      <h1>品牌管理</h1>
      <div class="admin-page-actions">
        <el-button :loading="loading" @click="loadBrandList">
          刷新列表
        </el-button>
        <el-button type="primary" @click="openCreateDialog">
          新增品牌
        </el-button>
      </div>
    </div>

    <div class="admin-table-wrap">
    <el-table v-loading="loading" :data="brandList" border>
      <el-table-column prop="id" label="ID" width="80" />

      <el-table-column prop="name" label="品牌名称" min-width="160" />

      <el-table-column label="Logo" width="100">
        <template #default="{ row }">
          <el-image
              v-if="row.logo"
              :src="row.logo"
              fit="contain"
              class="brand-logo"
          />
          <span v-else>暂无</span>
        </template>
      </el-table-column>

      <el-table-column prop="sort" label="排序" width="100" />

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <!-- 把当前行数据填进弹窗，进入编辑模式 -->
          <el-button link type="primary" @click="openEditDialog(row)">
            编辑
          </el-button>

          <!-- 根据当前状态决定显示启用还是停用 -->
          <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="changeBrandStatus(row)"
          >
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
    <!-- 新增和编辑共用一个弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="brandForm.id ? '编辑品牌' : '新增品牌'"
        width="500px"
    >
      <el-form
          ref="formRef"
          :model="brandForm"
          :rules="rules"
          label-width="90px"
      >
        <!-- 品牌名称是必填字段 -->
        <el-form-item label="品牌名称" prop="name">
          <el-input v-model="brandForm.name" placeholder="例如：拼省数码" />
        </el-form-item>

        <!-- 当前先允许填写图片地址；MinIO 上传后会替换成上传组件 -->
        <el-form-item label="Logo 地址">
          <el-input v-model="brandForm.logo" placeholder="可暂时留空" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="brandForm.sort" :min="0" />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="brandForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>

        <!-- 保存时显示加载状态，避免重复提交 -->
        <el-button type="primary" :loading="saving" @click="submitBrand">
          保存
        </el-button>
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

.brand-logo {
  width: 50px;
  height: 50px;
}
.page-actions {
  display: flex;
  gap: 12px;
}
</style>
