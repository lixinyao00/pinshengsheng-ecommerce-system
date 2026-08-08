package com.pinshengsheng.product.service;

public interface LuaStockService {

    void initStock(Long skuId, Integer quantity);

    Long getStock(Long skuId);

    boolean deductStock(Long skuId, Integer quantity);
}
