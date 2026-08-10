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

// 提交购物车中选中的商品，创建订单
export function createOrder(data) {
  return request.post('/api/order', data)
}

// 查询当前用户的订单列表
export function getMyOrderList() {
  return request.get('/api/order/list')
}

// 查询当前用户的订单详情
export function getMyOrderDetail(orderId) {
  return request.get(`/api/order/${orderId}`)
}

// 模拟支付订单
export function payOrder(orderId) {
  return request.put(`/api/order/${orderId}/pay`)
}

// 取消待支付订单
export function cancelOrder(orderId) {
  return request.put(`/api/order/${orderId}/cancel`)
}

// 确认已发货订单
export function completeOrder(orderId) {
  return request.put(`/api/order/${orderId}/complete`)
}
