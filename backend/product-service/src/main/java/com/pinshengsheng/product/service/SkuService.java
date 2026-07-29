package com.pinshengsheng.product.service;

import com.pinshengsheng.product.dto.SkuSaveRequest;
import com.pinshengsheng.product.vo.SkuStockVO;

import java.util.List;

public interface SkuService {

    List<SkuStockVO> getSkuList(Long productId);

    SkuStockVO createSku(SkuSaveRequest request);

    SkuStockVO updateSku(Long id, SkuSaveRequest request);

    boolean updateSkuStatus(Long id, Integer status);

    boolean updateAvailableStock(Long skuId, Integer availableStock);
}
