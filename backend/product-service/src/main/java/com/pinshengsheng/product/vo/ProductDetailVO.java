package com.pinshengsheng.product.vo;

import com.pinshengsheng.product.entity.Brand;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.entity.ProductImage;

import java.util.List;

// 商品详情聚合对象，前端一次拿到商品、品牌、分类、图片和 SKU 信息
public class ProductDetailVO {

    private Product product;
    private Brand brand;
    private Category category;
    private List<ProductImage> images;
    private List<SkuStockVO> skuList;
    private SkuStockVO selectedSku;

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

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public List<SkuStockVO> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<SkuStockVO> skuList) {
        this.skuList = skuList;
    }

    public SkuStockVO getSelectedSku() {
        return selectedSku;
    }

    public void setSelectedSku(SkuStockVO selectedSku) {
        this.selectedSku = selectedSku;
    }
}
