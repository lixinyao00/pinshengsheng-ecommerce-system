// 引入统一请求工具
import request from '../utils/request'

// 查询商城分类树
export function getMallCategoryTree() {
    return request.get('/api/product/category/tree')
}
// 查询商城可展示的品牌
export function getMallBrandList() {
    return request.get('/api/product/brand/list')
}
// 查询商城已上架商品
export function getMallProductPage(param) {
    return request.get('/api/product/page', { params: param })
}
