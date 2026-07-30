import request from '../utils/request'

// 分页查询后台商品列表
export function getProductPage(params) {
    return request.get('/api/admin/product/page', {
        params
    })
}
// 新增商品
export function createProduct(data) {
    return request.post('/api/admin/product', data)
}
// 修改指定 ID 的商品
export function updateProduct(id, data) {
    return request.put(`/api/admin/product/${id}`, data)
}
// 单独修改商品上下架状态：1 上架，0 下架
export function updateProductStatus(id, status) {
    return request.put(`/api/admin/product/${id}/status`, null, {
        params: { status }
    })
}