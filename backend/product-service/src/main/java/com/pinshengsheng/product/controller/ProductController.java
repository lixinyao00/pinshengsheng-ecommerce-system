package com.pinshengsheng.product.controller;
import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @GetMapping("{id}")
    public ApiResponse<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);

        // 不存在或已下架时，不向前端暴露商品数据
        if (product == null) {
            return ApiResponse.fail(404, "商品不存在或已下架");
        }
        return ApiResponse.success(product);
    }

}
