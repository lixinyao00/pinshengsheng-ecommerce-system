package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.StockOperateRequest;
import com.pinshengsheng.product.service.StockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/sku/{skuId}/lock")
    public ApiResponse<Void> lockStock(
            @PathVariable Long skuId,
            @Valid @RequestBody StockOperateRequest request) {

        boolean success = stockService.lockStock(
                skuId,
                request.getQuantity()
        );

        return success
                ? ApiResponse.success(null)
                : ApiResponse.fail(409, "库存不足或库存正在处理中");
    }

    @PostMapping("/sku/{skuId}/release")
    public ApiResponse<Void> releaseStock(
            @PathVariable Long skuId,
            @Valid @RequestBody StockOperateRequest request) {
        boolean success = stockService.releaseStock(
                skuId,
                request.getQuantity()
        );

        return success
                ? ApiResponse.success(null)
                : ApiResponse.fail(409, "库存释放失败");
    }
}
