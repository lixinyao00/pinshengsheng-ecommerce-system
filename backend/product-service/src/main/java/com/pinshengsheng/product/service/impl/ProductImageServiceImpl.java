package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinshengsheng.product.common.ProductDetailCacheService;
import com.pinshengsheng.product.dto.ProductImageSaveRequest;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.entity.ProductImage;
import com.pinshengsheng.product.mapper.ProductImageMapper;
import com.pinshengsheng.product.mapper.ProductMapper;
import com.pinshengsheng.product.service.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductDetailCacheService productDetailCacheService;

    public ProductImageServiceImpl(
            ProductMapper productMapper,
            ProductImageMapper productImageMapper,
            ProductDetailCacheService productDetailCacheService) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.productDetailCacheService = productDetailCacheService;
    }

    @Override
    public List<ProductImage> getProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                .orderByAsc(ProductImage::getSort)
                .orderByAsc(ProductImage::getId);
        return productImageMapper.selectList(queryWrapper);
    }

    @Override
    public ProductImage addProductImage(Long productId, ProductImageSaveRequest request) {
        Product product = productMapper.selectById(productId);
        if (product == null || request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            return null;
        }

        ProductImage productImage = new ProductImage();
        productImage.setProductId(productId);
        productImage.setImageUrl(request.getImageUrl());
        productImage.setSort(request.getSort() == null ? 0 : request.getSort());
        productImageMapper.insert(productImage);
        productDetailCacheService.deleteProductDetailCacheWithDoubleDelete(productId);
        return productImage;
    }

    @Override
    public ProductImage getProductImage(Long imageId) {
        return productImageMapper.selectById(imageId);
    }

    @Override
    public boolean deleteProductImage(Long imageId) {
        ProductImage productImage = productImageMapper.selectById(imageId);
        if (productImage == null) {
            return false;
        }

        boolean deleted = productImageMapper.deleteById(imageId) > 0;
        if (deleted) {
            productDetailCacheService.deleteProductDetailCacheWithDoubleDelete(productImage.getProductId());
        }
        return deleted;
    }
}
