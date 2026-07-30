import request from '../utils/request'

// 使用 multipart/form-data 上传图片，后端返回 MinIO 的图片访问地址
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post('/api/admin/file/image', formData)
}

// 删除已经上传到 MinIO 的图片
export function deleteImage(url) {
  return request.delete('/api/admin/file/image', {
    params: { url }
  })
}
