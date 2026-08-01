import request from '../utils/request'

// 查询商城可展示的分类树
export function getMallCategoryTree() {
  return request.get('/api/product/category/tree')
}

// 查询商城可展示的品牌
export function getMallBrandList() {
  return request.get('/api/product/brand/list')
}

// 查询已上架商品，页面只调用用户端接口
export function getMallProductPage(params) {
  return request.get('/api/product/page', { params })
}
