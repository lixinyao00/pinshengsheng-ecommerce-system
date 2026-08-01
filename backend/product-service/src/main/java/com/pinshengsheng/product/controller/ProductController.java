package com.pinshengsheng.product.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private ProductService productService;
    //Spring 自动注入商品业务层
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 商城首页只展示已上架商品，并使用分页结构给前端留出扩展空间
    @GetMapping("/page")
    public ApiResponse<Page<Product>> getEnabledProducts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size) {
        if (page < 1 || size < 1 || size > 100) {
            return ApiResponse.fail(400, "页码必须大于 0，单页数量不能超过 100");
        }
        return ApiResponse.success(productService.getEnabledProducts(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);

        // 不存在或已下架时，不向前端暴露商品数据
        if (product == null) {
            return ApiResponse.fail(404, "商品不存在或已下架");
        }
        return ApiResponse.success(product);
    }

}
