<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  createAddress,
  deleteAddress,
  getAddressList,
  setDefaultAddress,
  updateAddress
} from '../api/address'
import { useUserStore } from '../stores/user'
import MallHeader from '../components/MallHeader.vue'

const router = useRouter()
const userStore = useUserStore()

const addressList = ref([])
const loading = ref(true)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()

const addressForm = reactive({
  id: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入收货电话', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

function goHome() {
  router.push('/mall/home')
}

function goCart() {
  router.push('/mall/cart')
}

function goOrders() {
  router.push('/mall/orders')
}

function resetForm() {
  Object.assign(addressForm, {
    id: null,
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: 0
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(address) {
  Object.assign(addressForm, {
    id: address.id,
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detailAddress: address.detailAddress,
    isDefault: address.isDefault
  })
  dialogVisible.value = true
}

async function loadAddresses() {
  loading.value = true
  try {
    const result = await getAddressList()
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    addressList.value = result.data || []
  } catch {
    ElMessage.error('收货地址加载失败，请检查订单服务')
  } finally {
    loading.value = false
  }
}

async function submitAddress() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const data = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.province,
      city: addressForm.city,
      district: addressForm.district,
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault
    }
    const result = addressForm.id
      ? await updateAddress(addressForm.id, data)
      : await createAddress(data)

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    ElMessage.success(addressForm.id ? '地址修改成功' : '地址新增成功')
    dialogVisible.value = false
    await loadAddresses()
  } catch {
    ElMessage.error('收货地址保存失败')
  } finally {
    saving.value = false
  }
}

async function handleSetDefault(address) {
  if (address.isDefault === 1) {
    return
  }

  try {
    const result = await setDefaultAddress(address.id)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('默认地址设置成功')
    await loadAddresses()
  } catch {
    ElMessage.error('默认地址设置失败')
  }
}

async function handleDelete(address) {
  try {
    await ElMessageBox.confirm(
      `确定要删除收货地址“${address.receiverName}”吗？`,
      '操作确认',
      { type: 'warning' }
    )
    const result = await deleteAddress(address.id)
    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('收货地址已删除')
    await loadAddresses()
  } catch {
    // 用户取消删除时不提示错误
  }
}

function handleLogout() {
  userStore.clearLoginInfo()
  router.replace('/mall/login')
}

onMounted(loadAddresses)
</script>

<template>
  <main class="address-page">
    <MallHeader />

    <el-card v-loading="loading" class="address-card" shadow="never">
      <div class="page-header">
        <h1>收货地址</h1>
        <el-button type="primary" @click="openCreateDialog">新增地址</el-button>
      </div>

      <el-empty v-if="addressList.length === 0" description="暂无收货地址" />
      <section v-else class="address-list">
        <article v-for="address in addressList" :key="address.id" class="address-item">
          <div class="address-content">
            <div class="address-title">
              <strong>{{ address.receiverName }}</strong>
              <span>{{ address.receiverPhone }}</span>
              <el-tag v-if="address.isDefault === 1" type="success">默认地址</el-tag>
            </div>
            <p>
              {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
            </p>
          </div>
          <div class="address-actions">
            <el-button link type="primary" @click="openEditDialog(address)">编辑</el-button>
            <el-button
              link
              type="success"
              :disabled="address.isDefault === 1"
              @click="handleSetDefault(address)"
            >
              设为默认
            </el-button>
            <el-button link type="danger" @click="handleDelete(address)">删除</el-button>
          </div>
        </article>
      </section>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="addressForm.id ? '编辑收货地址' : '新增收货地址'"
      width="560px"
    >
      <el-form ref="formRef" :model="addressForm" :rules="rules" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="例如：广东省" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="例如：广州市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="例如：天河区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" placeholder="请输入街道、门牌号" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAddress">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.address-page {
  min-height: 100vh;
  max-width: 1200px;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 24px;
  background: #f5f7fa;
}

.address-card {
  max-width: 1200px;
  margin: 0 auto;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.page-header,
.address-item,
.address-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
}

.address-list {
  display: grid;
  gap: 14px;
}

.address-item {
  gap: 20px;
  padding: 18px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.address-content {
  min-width: 0;
}

.address-title {
  justify-content: flex-start;
  gap: 14px;
}

.address-title span,
.address-content p {
  color: #606266;
}

.address-content p {
  margin: 10px 0 0;
}

@media (max-width: 760px) {
  .address-page {
    padding: 12px;
  }

  .address-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .address-actions {
    align-self: flex-end;
  }
}
</style>
