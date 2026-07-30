import request from '../utils/request'

// 查询某个商品下的全部 SKU 和库存信息
export function getSkuList(productId) {
  return request.get('/api/admin/sku/list', {
    params: { productId }
  })
}

// 新增 SKU 时，同时由后端创建对应库存记录
export function createSku(data) {
  return request.post('/api/admin/sku', data)
}

// 修改 SKU 的规格、价格和可用库存
export function updateSku(id, data) {
  return request.put(`/api/admin/sku/${id}`, data)
}

// 单独调整 SKU 的上架状态
export function updateSkuStatus(id, status) {
  return request.put(`/api/admin/sku/${id}/status`, null, {
    params: { status }
  })
}

// 单独调整可用库存，锁定库存由下单流程维护
export function updateSkuStock(id, availableStock) {
  return request.put(`/api/admin/sku/${id}/stock`, null, {
    params: { availableStock }
  })
}
