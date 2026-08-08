package com.pinshengsheng.product.service;

public interface StockService {
    boolean lockStock(Long skuId, Integer quantity);

    boolean releaseStock(Long skuId, Integer quantity);
}
