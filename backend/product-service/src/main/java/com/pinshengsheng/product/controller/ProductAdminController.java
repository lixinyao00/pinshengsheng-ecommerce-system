package com.pinshengsheng.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.ProductSaveRequest;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 后台商品管理接口：新增、分页、编辑和上下架
@RestController
@RequestMapping("/api/admin/product")
public class ProductAdminController {

    private final ProductService productService;
    public ProductAdminController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<Product> createProduct(
            @RequestBody ProductSaveRequest request){
        // 在进入业务层前先校验商品最基本的必填字段
        if (request.getBrandId() == null
        || request.getCategoryId() == null
        || request.getName() == null
        || request.getName().isBlank()
        || request.getMinPrice() == null){
            return ApiResponse.fail(400,"品牌、分类、商品名称和最低价格不能为空");
        }

        Product product = productService.createProduct(request);
        return ApiResponse.success(product);
    }
    @GetMapping("/page")
    public ApiResponse<Page<Product>> getProductsPage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size){
        // 限制单页大小，避免一次查询过多商品
        if (page < 1 || size < 1|| size > 100){
            return ApiResponse.fail(400,"页码必须大于 0，单页数量不能超过 100");
        }
        Page<Product> productPage = productService.getAllProducts(page, size);
        return ApiResponse.success(productPage);
    }
    @PutMapping("/{id}")
    public ApiResponse<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductSaveRequest request) {
        if (request.getBrandId() == null
                || request.getCategoryId() == null
                || request.getName() == null
                || request.getName().isBlank()
                || request.getMinPrice() == null) {
            return ApiResponse.fail(400, "品牌、分类、商品名称和最低价格不能为空");
        }
        Product product = productService.updateProduct(id, request);
        if (product == null){
            return ApiResponse.fail(404,"商品不存在");
        }
        return ApiResponse.success(product);
    }
    @PutMapping("/{id}/status")
    public ApiResponse<Product> updateProductStatus(
            @PathVariable Long id,
            @RequestParam("status") Integer status
    ){
        // 状态由请求参数传入，例如 ?status=0
        if(!Integer.valueOf(1).equals(status)&&!Integer.valueOf(0).equals(status)){
            return ApiResponse.fail(400,"商品状态只能是 0 或 1");
        }
        boolean update = productService.updateProductStatus(id, status);
        if (!update){
            return ApiResponse.fail(404,"商品不存在");
        }
        return ApiResponse.success(null);
    }

}
