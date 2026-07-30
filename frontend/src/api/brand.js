import request from '../utils/request'

// 查询后台品牌列表，包含已停用品牌
export function getBrandList() {
    return request.get('/api/admin/brand/list')
}
export function createBrand(data) {
    return request.post('/api/admin/brand',data)
}
export function updateBrand(id, data) {
    return request.put(`/api/admin/brand/${id}`, data)
}
// 单独修改品牌状态：1 启用，0 停用
export function updateBrandStatus(id, status) {
    return request.put(`/api/admin/brand/${id}/status`, null, {
        params: { status }
    })
}