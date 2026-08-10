import request from '../utils/request'

// 查询后台订单，可按订单状态筛选
export function getAdminOrderList(status) {
  return request.get('/api/admin/order/list', {
    params: status === '' ? {} : { status }
  })
}

// 查询订单详情和商品明细
export function getAdminOrderDetail(orderId) {
  return request.get(`/api/admin/order/${orderId}`)
}

// 管理员处理订单发货
export function shipAdminOrder(orderId) {
  return request.put(`/api/admin/order/${orderId}/ship`)
}

// 查询当前用户的收货地址
export function getAddressList() {
  return request.get('/api/order/address/list')
}

// 提交购物车中选中的商品，创建订单
export function createOrder(data) {
  return request.post('/api/order', data)
}
