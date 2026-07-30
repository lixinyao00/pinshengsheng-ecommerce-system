package com.pinshengsheng.product.dto;

// 新增轮播图时由前端提交图片地址和显示排序
public class ProductImageSaveRequest {

    private String imageUrl;
    private Integer sort;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
