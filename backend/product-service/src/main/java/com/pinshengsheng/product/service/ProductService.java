package com.pinshengsheng.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinshengsheng.product.dto.ProductSaveRequest;
import com.pinshengsheng.product.entity.Product;

public interface ProductService {
    Product getProductById(Long id);
    Product createProduct(ProductSaveRequest request);
    Page<Product> getAllProducts(long page, long size);
    Product updateProduct(Long id, ProductSaveRequest request);
    boolean updateProductStatus(Long id, Integer status);
}
