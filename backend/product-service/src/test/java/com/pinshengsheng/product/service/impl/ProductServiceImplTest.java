package com.pinshengsheng.product.service.impl;

import com.pinshengsheng.product.common.ProductDetailCacheService;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.mapper.ProductMapper;
import com.pinshengsheng.product.service.BrandService;
import com.pinshengsheng.product.service.CategoryService;
import com.pinshengsheng.product.service.ProductImageService;
import com.pinshengsheng.product.service.SkuService;
import com.pinshengsheng.product.vo.ProductDetailVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    @Test
    void shouldReturnCachedDetailDirectly() {
        ProductMapper productMapper = mock(ProductMapper.class);
        BrandService brandService = mock(BrandService.class);
        CategoryService categoryService = mock(CategoryService.class);
        ProductImageService productImageService = mock(ProductImageService.class);
        SkuService skuService = mock(SkuService.class);
        ProductDetailCacheService cacheService = mock(ProductDetailCacheService.class);

        ProductDetailVO cachedDetail = new ProductDetailVO();
        Product cachedProduct = new Product();
        cachedProduct.setId(1L);
        cachedProduct.setName("缓存商品");
        cachedDetail.setProduct(cachedProduct);

        when(cacheService.readProductDetailCache(1L))
                .thenReturn(ProductDetailCacheService.CacheLookupResult.hit(cachedDetail));

        ProductServiceImpl productService = new ProductServiceImpl(
                productMapper,
                brandService,
                categoryService,
                productImageService,
                skuService,
                cacheService
        );

        ProductDetailVO result = productService.getProductDetail(1L);

        assertEquals("缓存商品", result.getProduct().getName());
        verify(productMapper, never()).selectById(any());
    }

    @Test
    void shouldQueryDbAndWriteCacheWhenCacheMiss() {
        ProductMapper productMapper = mock(ProductMapper.class);
        BrandService brandService = mock(BrandService.class);
        CategoryService categoryService = mock(CategoryService.class);
        ProductImageService productImageService = mock(ProductImageService.class);
        SkuService skuService = mock(SkuService.class);
        ProductDetailCacheService cacheService = mock(ProductDetailCacheService.class);

        Product product = new Product();
        product.setId(2L);
        product.setName("数据库商品");
        product.setStatus(1);
        product.setBrandId(1L);
        product.setCategoryId(3L);
        product.setMinPrice(new BigDecimal("199.00"));

        when(cacheService.readProductDetailCache(2L))
                .thenReturn(ProductDetailCacheService.CacheLookupResult.miss());
        when(productMapper.selectById(2L)).thenReturn(product);

        ProductServiceImpl productService = new ProductServiceImpl(
                productMapper,
                brandService,
                categoryService,
                productImageService,
                skuService,
                cacheService
        );

        ProductDetailVO result = productService.getProductDetail(2L);

        assertEquals("数据库商品", result.getProduct().getName());
        verify(cacheService).writeProductDetailCache(eq(2L), any(ProductDetailVO.class));
    }

    @Test
    void shouldReturnNullWhenEmptyCacheHit() {
        ProductMapper productMapper = mock(ProductMapper.class);
        BrandService brandService = mock(BrandService.class);
        CategoryService categoryService = mock(CategoryService.class);
        ProductImageService productImageService = mock(ProductImageService.class);
        SkuService skuService = mock(SkuService.class);
        ProductDetailCacheService cacheService = mock(ProductDetailCacheService.class);

        when(cacheService.readProductDetailCache(999L))
                .thenReturn(ProductDetailCacheService.CacheLookupResult.empty());

        ProductServiceImpl productService = new ProductServiceImpl(
                productMapper,
                brandService,
                categoryService,
                productImageService,
                skuService,
                cacheService
        );

        assertNull(productService.getProductDetail(999L));
        verify(productMapper, never()).selectById(any());
    }
}
