import request from '../utils/request'

// 查询当前用户的收货地址
export function getAddressList() {
  return request.get('/api/order/address/list')
}

// 新增收货地址
export function createAddress(data) {
  return request.post('/api/order/address', data)
}

// 修改指定收货地址
export function updateAddress(id, data) {
  return request.put(`/api/order/address/${id}`, data)
}

// 删除指定收货地址
export function deleteAddress(id) {
  return request.delete(`/api/order/address/${id}`)
}

// 设置默认收货地址
export function setDefaultAddress(id) {
  return request.put(`/api/order/address/${id}/default`)
}
