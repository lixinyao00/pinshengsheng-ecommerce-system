package com.pinshengsheng.product.vo;

import com.pinshengsheng.product.entity.Brand;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.entity.ProductImage;

import java.util.List;

// 汇总商品详情所需的数据
public class ProductDetailVO {

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<SkuStockVO> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<SkuStockVO> skuList) {
        this.skuList = skuList;
    }

    public List<ProductImage> getImageList() {
        return imageList;
    }

    public void setImageList(List<ProductImage> imageList) {
        this.imageList = imageList;
    }

    private Product product;
    private Brand brand;
    private Category category;
    private List<SkuStockVO> skuList;
    private List<ProductImage> imageList;
}