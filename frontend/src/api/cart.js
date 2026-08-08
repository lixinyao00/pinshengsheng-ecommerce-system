import request from '../utils/request'

// 查询当前登录用户的购物车
export function getCart() {
  return request.get('/api/cart')
}

// 将一个商品规格加入购物车
export function addCartItem(data) {
  return request.post('/api/cart/items', data)
}

// 修改某个购物车商品的数量
export function updateCartQuantity(id, quantity) {
  return request.put(`/api/cart/items/${id}/quantity`, { quantity })
}

// 修改某个购物车商品的选中状态
export function updateCartSelected(id, selected) {
  return request.put(`/api/cart/items/${id}/selected`, { selected })
}

// 全选或取消全选当前购物车
export function updateAllCartSelected(selected) {
  return request.put('/api/cart/selected', { selected })
}

// 删除一条购物车商品
export function deleteCartItem(id) {
  return request.delete(`/api/cart/items/${id}`)
}

// 清空当前用户的购物车
export function clearCart() {
  return request.delete('/api/cart')
}
