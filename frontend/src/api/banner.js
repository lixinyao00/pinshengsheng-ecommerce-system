import request from '../utils/request'

// 查询后台全部首页轮播图
export function getBannerList() {
  return request.get('/api/admin/banner/list')
}

// 新增一张已经上传到 MinIO 的首页轮播图
export function createBanner(data) {
  return request.post('/api/admin/banner', data)
}

// 修改首页轮播图信息
export function updateBanner(id, data) {
  return request.put(`/api/admin/banner/${id}`, data)
}

// 修改首页轮播图启用状态
export function updateBannerStatus(id, status) {
  return request.put(`/api/admin/banner/${id}/status`, null, {
    params: { status }
  })
}

// 删除首页轮播图记录和对应的 MinIO 文件
export function deleteBanner(id) {
  return request.delete(`/api/admin/banner/${id}`)
}
