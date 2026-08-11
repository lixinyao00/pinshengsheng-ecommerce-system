package com.pinshengsheng.product.dto;

// 新增或编辑首页轮播图时提交的字段
public class HomeBannerSaveRequest {

    private String imageUrl;
    private Integer sort;
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
