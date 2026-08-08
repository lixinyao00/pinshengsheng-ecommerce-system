package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinshengsheng.product.dto.ProductSaveRequest;
import com.pinshengsheng.product.entity.Brand;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.entity.ProductImage;
import com.pinshengsheng.product.mapper.ProductMapper;
import com.pinshengsheng.product.service.*;
import com.pinshengsheng.product.vo.ProductDetailVO;
import com.pinshengsheng.product.vo.SkuStockVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.pinshengsheng.product.common.ProductDetailCacheService;
import com.pinshengsheng.product.common.ProductExistsBitmapService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.List;


// 商品业务实现类：用户端只看上架商品，后台可以管理全部商品
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final SkuService skuService;
    private final ProductImageService productImageService;
    private final ProductDetailCacheService cacheService;
    private final ProductExistsBitmapService productExistsBitmapService;
    private final Executor productDetailExecutor;


    // Spring 自动注入 ProductMapper
    public ProductServiceImpl(
            ProductMapper productMapper,
            BrandService brandService,
            CategoryService categoryService,
            SkuService skuService,
            ProductImageService productImageService,
            ProductDetailCacheService cacheService,
            ProductExistsBitmapService productExistsBitmapService,
            @Qualifier("productDetailExecutor") Executor productDetailExecutor) {
        this.productMapper = productMapper;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.skuService = skuService;
        this.productImageService = productImageService;
        this.cacheService = cacheService;
        this.productExistsBitmapService = productExistsBitmapService;
        this.productDetailExecutor = productDetailExecutor;
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
        ProductDetailVO cachedDetail = cacheService.get(id);

        if (cachedDetail != null) {
            return cachedDetail;
        }

        // Bitmap 为 0 时可以直接确认商品不存在，避免无效请求反复访问 MySQL
        if (!productExistsBitmapService.mayExist(id)) {
            return null;
        }

        Product product = getProductById(id);
        if (product == null) {
            return null;
        }

        // 品牌、分类、SKU 和图片相互独立，可以交给不同线程并行查询
        CompletableFuture<Brand> brandFuture = CompletableFuture.supplyAsync(
                () -> brandService.getBrandById(product.getBrandId()),
                productDetailExecutor
        );
        CompletableFuture<Category> categoryFuture = CompletableFuture.supplyAsync(
                () -> categoryService.getCategoryById(product.getCategoryId()),
                productDetailExecutor
        );
        CompletableFuture<List<SkuStockVO>> skuFuture = CompletableFuture.supplyAsync(
                () -> skuService.getSkuList(id),
                productDetailExecutor
        );
        CompletableFuture<List<ProductImage>> imageFuture = CompletableFuture.supplyAsync(
                () -> productImageService.getProductImages(id),
                productDetailExecutor
        );

        // 等待四项查询都完成后，再组装为前端需要的详情对象
        CompletableFuture.allOf(
                brandFuture,
                categoryFuture,
                skuFuture,
                imageFuture
        ).join();

        ProductDetailVO detailVO = new ProductDetailVO();
        detailVO.setProduct(product);
        detailVO.setBrand(brandFuture.join());
        detailVO.setCategory(categoryFuture.join());
        detailVO.setSkuList(skuFuture.join());
        detailVO.setImageList(imageFuture.join());
        cacheService.set(id, detailVO);

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
        productExistsBitmapService.markExists(product.getId());
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
        return productMapper.updateById(product) > 0;
    }
}
