package com.pinshengsheng.product.service;

import com.pinshengsheng.product.dto.ProductImageSaveRequest;
import com.pinshengsheng.product.entity.ProductImage;

import java.util.List;

public interface ProductImageService {

    List<ProductImage> getProductImages(Long productId);

    ProductImage addProductImage(Long productId, ProductImageSaveRequest request);

    ProductImage getProductImage(Long imageId);

    boolean deleteProductImage(Long imageId);
}
