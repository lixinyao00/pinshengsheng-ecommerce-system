package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinshengsheng.product.dto.ProductSaveRequest;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.mapper.ProductMapper;
import com.pinshengsheng.product.service.ProductService;
import org.springframework.stereotype.Service;


// 商品业务实现类：用户端只看上架商品，后台可以管理全部商品
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    // Spring 自动注入 ProductMapper
    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
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
