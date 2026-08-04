package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinshengsheng.product.common.ProductDetailCacheService;
import com.pinshengsheng.product.entity.Brand;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.dto.ProductSaveRequest;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.entity.ProductImage;
import com.pinshengsheng.product.mapper.ProductMapper;
import com.pinshengsheng.product.service.BrandService;
import com.pinshengsheng.product.service.CategoryService;
import com.pinshengsheng.product.service.ProductImageService;
import com.pinshengsheng.product.service.ProductService;
import com.pinshengsheng.product.service.SkuService;
import com.pinshengsheng.product.vo.ProductDetailVO;
import com.pinshengsheng.product.vo.SkuStockVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

// 商品业务实现类：用户端只看上架商品，后台可以管理全部商品
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductMapper productMapper;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;
    private final SkuService skuService;
    private final ProductDetailCacheService productDetailCacheService;

    // Spring 自动注入 ProductMapper
    public ProductServiceImpl(
            ProductMapper productMapper,
            BrandService brandService,
            CategoryService categoryService,
            ProductImageService productImageService,
            SkuService skuService,
            ProductDetailCacheService productDetailCacheService) {
        this.productMapper = productMapper;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.productImageService = productImageService;
        this.skuService = skuService;
        this.productDetailCacheService = productDetailCacheService;
    }
    // 查询商品，同时过滤不存在或已下架的商品
    @Override
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null||!Integer.valueOf(1).equals(product.getStatus())) {
            return null;
        }
        return product;
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        ProductDetailCacheService.CacheLookupResult cacheResult =
                productDetailCacheService.readProductDetailCache(id);
        if (cacheResult.isHit()) {
            return cacheResult.isEmpty() ? null : cacheResult.getDetailVO();
        }

        ProductDetailVO detailVO = loadProductDetailFromDb(id);
        if (detailVO == null) {
            productDetailCacheService.writeEmptyCache(id);
            return null;
        }

        productDetailCacheService.writeProductDetailCache(id, detailVO);
        return detailVO;
    }

    private ProductDetailVO loadProductDetailFromDb(Long id) {
        Product product = getProductById(id);
        if (product == null) {
            log.info("商品详情查询数据库未命中，productId={}", id);
            return null;
        }
        log.info("商品详情查询数据库成功，productId={}", id);

        Brand brand = brandService.getBrandById(product.getBrandId());
        Category category = categoryService.getCategoryById(product.getCategoryId());
        List<ProductImage> images = productImageService.getProductImages(id);
        List<SkuStockVO> skuList = skuService.getSkuList(id);

        ProductDetailVO detailVO = new ProductDetailVO();
        detailVO.setProduct(product);
        detailVO.setBrand(brand);
        detailVO.setCategory(category);
        detailVO.setImages(images == null ? Collections.emptyList() : images);
        detailVO.setSkuList(skuList == null ? Collections.emptyList() : skuList);
        detailVO.setSelectedSku(selectDefaultSku(detailVO.getSkuList()));
        return detailVO;
    }

    @Override
    public Product createProduct(ProductSaveRequest request) {
        // DTO 只负责接收请求，转换成实体后再写入商品表
        Product product = new Product();
        product.setBrandId(request.getBrandId());
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setMainImage(request.getMainImage());
        product.setDescription(request.getDescription());
        product.setMinPrice(request.getMinPrice());
        product.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        productMapper.insert(product);
        return product;
    }

    @Override
    public Page<Product> getAllProducts(long page, long size) {
        Page<Product> productPage = new Page<>(page, size);

        // 后台需要看到全部商品，因此这里不按 status 过滤
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Product::getId);
        return productMapper.selectPage(productPage, queryWrapper);
    }
    @Override
    public Page<Product> getEnabledProducts(long page, long size) {
        // 创建分页对象
        Page<Product> productPage = new Page<>(page, size);

        // 只查询已经上架的商品
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.orderByDesc(Product::getId);

        return productMapper.selectPage(productPage, queryWrapper);
    }
    @Override
    public Product updateProduct(Long id, ProductSaveRequest request) {
        Product product = productMapper.selectById(id);
        // 先查再改，避免 updateById 对不存在的商品产生误导
        if(product == null){
            return null;
        }
        product.setBrandId(request.getBrandId());
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setMainImage(request.getMainImage());
        product.setDescription(request.getDescription());
        product.setMinPrice(request.getMinPrice());
        if (request.getStatus() != null){
            product.setStatus(request.getStatus());
        }
        productMapper.updateById(product);
        productDetailCacheService.deleteProductDetailCacheWithDoubleDelete(id);

        return product;
    }

    @Override
    public boolean updateProductStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if(product == null){
            return false;
        }
        // 目前约定：0 为下架，1 为上架
        if (!Integer.valueOf(0).equals(status)
                && !Integer.valueOf(1).equals(status)) {
            return false;
        }
        product.setStatus(status);
        boolean updated = productMapper.updateById(product) > 0;
        if (updated) {
            productDetailCacheService.deleteProductDetailCacheWithDoubleDelete(id);
        }
        return updated;
    }

    private SkuStockVO selectDefaultSku(List<SkuStockVO> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            return null;
        }

        for (SkuStockVO skuStockVO : skuList) {
            if (skuStockVO.getSku() != null
                    && Integer.valueOf(1).equals(skuStockVO.getSku().getStatus())) {
                return skuStockVO;
            }
        }
        return skuList.get(0);
    }

}
