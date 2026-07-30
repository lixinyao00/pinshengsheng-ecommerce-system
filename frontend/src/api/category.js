import request from '../utils/request'

export function getCategoryList() {
    return request.get('/api/admin/category/list')
}
// 新增分类；parentId 为 0 时表示新增一级分类
export function createCategory(data) {
    return request.post('/api/admin/category', data)
}
// 编辑指定 ID 的分类
export function updateCategory(id, data) {
    return request.put(`/api/admin/category/${id}`, data)
}
// 单独修改分类状态：1 启用，0 停用
export function updateCategoryStatus(id, status) {
    return request.put(`/api/admin/category/${id}/status`, null, {
        params: { status }
    })
}