<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createBanner, deleteBanner, getBannerList, updateBannerStatus } from '../api/banner'
import { deleteImage, uploadImage } from '../api/file'

const bannerList = ref([])
const loading = ref(false)
const uploading = ref(false)

const bannerForm = reactive({
  sort: 0,
  status: 1
})

async function loadBannerList() {
  loading.value = true
  try {
    const result = await getBannerList()
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    bannerList.value = result.data || []
  } catch {
    ElMessage.error('首页轮播图加载失败')
  } finally {
    loading.value = false
  }
}

// 先把图片上传到 MinIO，再保存轮播图数据库记录
async function handleUpload({ file }) {
  uploading.value = true
  try {
    const uploadResult = await uploadImage(file)
    if (uploadResult.code !== 200) {
      ElMessage.error(uploadResult.message)
      return
    }

    const saveResult = await createBanner({
      imageUrl: uploadResult.data.url,
      sort: bannerForm.sort,
      status: bannerForm.status
    })

    if (saveResult.code !== 200) {
      await deleteImage(uploadResult.data.url)
      ElMessage.error(saveResult.message)
      return
    }

    ElMessage.success('首页轮播图上传成功')
    await loadBannerList()
  } catch {
    ElMessage.error('首页轮播图上传失败')
  } finally {
    uploading.value = false
  }
}

async function changeBannerStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(`确定要${actionText}这张首页轮播图吗？`, '操作确认', {
      type: 'warning'
    })
    const result = await updateBannerStatus(row.id, nextStatus)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success(`轮播图已${actionText}`)
    await loadBannerList()
  } catch {
    // 用户取消操作时不提示错误
  }
}

async function removeBanner(row) {
  try {
    await ElMessageBox.confirm('删除后商城首页将不再显示这张图片，确定继续吗？', '操作确认', {
      type: 'warning'
    })
    const result = await deleteBanner(row.id)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('轮播图已删除')
    await loadBannerList()
  } catch {
    // 用户取消操作时不提示错误
  }
}

onMounted(loadBannerList)
</script>

<template>
  <section class="admin-page banner-page">
    <div class="admin-page-header">
      <div>
        <h1>首页轮播图</h1>
        <p class="page-tip">上传后会显示在商城首页顶部，按排序值从小到大轮播。</p>
      </div>

      <div class="admin-page-actions banner-actions">
        <el-input-number v-model="bannerForm.sort" :min="0" :disabled="uploading" />
        <el-switch
          v-model="bannerForm.status"
          :active-value="1"
          :inactive-value="0"
          active-text="上传后启用"
          inactive-text="上传后停用"
          :disabled="uploading"
        />
        <el-upload
          accept="image/*"
          :show-file-list="false"
          :http-request="handleUpload"
          :disabled="uploading"
        >
          <el-button type="primary" :loading="uploading">上传轮播图</el-button>
        </el-upload>
        <el-button :loading="loading" @click="loadBannerList">刷新列表</el-button>
      </div>
    </div>

    <div class="admin-table-wrap">
      <el-table v-loading="loading" :data="bannerList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="预览图" min-width="260">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              fit="cover"
              class="banner-preview"
              :preview-src-list="[row.imageUrl]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="changeBannerStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="removeBanner(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-if="!loading && bannerList.length === 0" description="暂时没有轮播图，上传后会显示在商城首页" />
  </section>
</template>

<style scoped>
.page-tip {
  margin: 8px 0 0;
  color: #909399;
}

.banner-actions {
  align-items: center;
}

.banner-preview {
  width: 220px;
  height: 82px;
  border-radius: 6px;
}

@media (max-width: 768px) {
  .banner-actions {
    align-items: stretch;
  }

  .banner-actions .el-input-number,
  .banner-actions .el-switch,
  .banner-actions .el-upload,
  .banner-actions .el-button {
    width: 100%;
  }

  .banner-actions .el-upload .el-button {
    width: 100%;
  }
}
</style>
