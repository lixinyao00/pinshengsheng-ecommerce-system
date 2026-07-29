package com.pinshengsheng.product.vo;

import com.pinshengsheng.product.entity.Sku;

// 汇总 SKU 与库存信息，供后台列表展示
public class SkuStockVO {

    private Sku sku;
    private Integer availableStock;
    private Integer lockedStock;

    public Sku getSku() {
        return sku;
    }

    public void setSku(Sku sku) {
        this.sku = sku;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public Integer getLockedStock() {
        return lockedStock;
    }

    public void setLockedStock(Integer lockedStock) {
        this.lockedStock = lockedStock;
    }
}
