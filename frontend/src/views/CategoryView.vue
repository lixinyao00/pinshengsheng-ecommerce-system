<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCategory,
         getCategoryList,
         updateCategory,
         updateCategoryStatus
        } from '../api/category'

// 保存后台返回的全部分类数据
const categoryList = ref([])

// 控制表格的加载状态
const loading = ref(false)
// 控制新增、编辑分类弹窗

const dialogVisible = ref(false)

// 保存 Element Plus 表单实例，用于提交前校验
const formRef = ref()

// 防止保存时重复点击
const saving = ref(false)

// 新增和编辑共用的分类表单
const categoryForm = reactive({
  id: null,
  parentId: 0,
  name: '',
  sort: 0,
  status: 1
})

// 分类名称不能为空
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

// 查询分类列表
async function loadCategoryList() {
  loading.value = true

  try {
    const result = await getCategoryList()

    if (result.code === 200) {
      categoryList.value = result.data
      return
    }

    ElMessage.error(result.message)
  } catch {
    ElMessage.error('分类列表加载失败，请检查 Gateway 和商品服务')
  } finally {
    loading.value = false
  }
}
// 恢复新增分类时的默认表单
function resetCategoryForm() {
  Object.assign(categoryForm, {
    id: null,
    parentId: 0,
    name: '',
    sort: 0,
    status: 1
  })
}

// 打开新增一级分类弹窗
function openCreateDialog() {
  resetCategoryForm()
  dialogVisible.value = true
}

// 以当前分类作为父分类，新增它的子分类
function openCreateChildDialog(row) {
  resetCategoryForm()
  categoryForm.parentId = row.id
  dialogVisible.value = true
}

// 将当前行数据回填到表单，进入编辑模式
function openEditDialog(row) {
  Object.assign(categoryForm, {
    id: row.id,
    parentId: row.parentId || 0,
    name: row.name,
    sort: row.sort,
    status: row.status
  })

  dialogVisible.value = true
}

// 根据 id 判断是新增还是编辑
async function submitCategory() {
  const valid = await formRef.value.validate().catch(() => false)

  if (!valid) {
    return
  }

  saving.value = true

  try {
    const data = {
      parentId: categoryForm.parentId,
      name: categoryForm.name,
      sort: categoryForm.sort,
      status: categoryForm.status
    }

    const result = categoryForm.id
        ? await updateCategory(categoryForm.id, data)
        : await createCategory(data)

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success(categoryForm.id ? '分类修改成功' : '分类新增成功')
    dialogVisible.value = false
    await loadCategoryList()
  } catch {
    ElMessage.error('分类保存失败')
  } finally {
    saving.value = false
  }
}

// 点击启用、停用后，重新查询列表确认数据已更新
async function changeCategoryStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(
        `确定要${actionText}分类“${row.name}”吗？`,
        '操作确认',
        { type: 'warning' }
    )

    const result = await updateCategoryStatus(row.id, nextStatus)

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success(`分类已${actionText}`)
    await loadCategoryList()
  } catch {
    // 用户取消确认框时，不需要额外提示
  }
}

// 根据 parentId 找出父分类名称
function getParentName(row) {
  if (!row.parentId) {
    return '顶级分类'
  }

  const parent = categoryList.value.find(
      (category) => category.id === row.parentId
  )

  return parent ? parent.name : '父分类不存在'
}

// 根据层级给分类名称增加缩进，方便看出父子关系
function formatCategoryName(row) {
  const prefix = '— '.repeat(Math.max(row.levelNum - 1, 0))
  return `${prefix}${row.name}`
}

// 页面进入时自动加载分类数据
onMounted(loadCategoryList)
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-header">
      <h1>分类管理</h1>

      <!-- 重新查询数据库中的分类数据 -->
      <div class="admin-page-actions">
        <!-- 重新从数据库查询分类 -->
        <el-button :loading="loading" @click="loadCategoryList">
          刷新列表
        </el-button>

        <!-- 新增一级分类时，parentId 默认是 0 -->
        <el-button type="primary" @click="openCreateDialog">
          新增一级分类
        </el-button>
      </div>
    </div>

    <div class="admin-table-wrap">
    <el-table v-loading="loading" :data="categoryList" border>
      <el-table-column prop="id" label="ID" width="80" />

      <el-table-column label="分类名称" min-width="200">
        <template #default="{ row }">
          {{ formatCategoryName(row) }}
        </template>
      </el-table-column>

      <el-table-column label="父分类" min-width="140">
        <template #default="{ row }">
          {{ getParentName(row) }}
        </template>
      </el-table-column>

      <el-table-column prop="levelNum" label="层级" width="90">
        <template #default="{ row }">
          {{ row.levelNum }} 级
        </template>
      </el-table-column>

      <el-table-column prop="sort" label="排序" width="90" />

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230">
        <template #default="{ row }">
          <!-- 自动把当前分类设为父分类 -->
          <el-button link type="success" @click="openCreateChildDialog(row)">
            新增子分类
          </el-button>

          <el-button link type="primary" @click="openEditDialog(row)">
            编辑
          </el-button>

          <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="changeCategoryStatus(row)"
          >
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
      <!-- 新增和编辑分类共用一个弹窗 -->
      <el-dialog
          v-model="dialogVisible"
          :title="categoryForm.id ? '编辑分类' : '新增分类'"
          width="500px"
      >
        <el-form
            ref="formRef"
            :model="categoryForm"
            :rules="rules"
            label-width="90px"
        >
          <el-form-item label="父分类">
            <el-select
                v-model="categoryForm.parentId"
                :disabled="Boolean(categoryForm.id)"
                style="width: 100%"
            >
              <el-option label="顶级分类" :value="0" />

              <!-- 新增时可选择已有分类作为父分类 -->
              <el-option
                  v-for="category in categoryList"
                  :key="category.id"
                  :label="formatCategoryName(category)"
                  :value="category.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="分类名称" prop="name">
            <el-input
                v-model="categoryForm.name"
                placeholder="例如：游戏耳机"
            />
          </el-form-item>

          <el-form-item label="排序">
            <el-input-number v-model="categoryForm.sort" :min="0" />
          </el-form-item>

          <el-form-item label="状态">
            <el-radio-group v-model="categoryForm.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>

          <el-button type="primary" :loading="saving" @click="submitCategory">
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
.page-actions {
  display: flex;
  gap: 12px;
}
</style>
