package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.StockOperateRequest;
import com.pinshengsheng.product.service.LuaStockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stock/lua")
public class LuaStockController {

    private final LuaStockService luaStockService;

    public LuaStockController(LuaStockService luaStockService) {
        this.luaStockService = luaStockService;
    }

    @PostMapping("/sku/{skuId}/init")
    public ApiResponse<Map<String, Object>> initStock(
            @PathVariable Long skuId,
            @Valid @RequestBody StockOperateRequest request) {

        luaStockService.initStock(skuId, request.getQuantity());

        return ApiResponse.success(Map.of(
                "skuId", skuId,
                "availableStock", luaStockService.getStock(skuId)
        ));
    }

    @GetMapping("/sku/{skuId}")
    public ApiResponse<Map<String, Object>> getStock(@PathVariable Long skuId) {
        Long stock = luaStockService.getStock(skuId);

        if (stock == null) {
            return ApiResponse.fail(404, "演示库存不存在");
        }

        return ApiResponse.success(Map.of(
                "skuId", skuId,
                "availableStock", stock
        ));
    }

    @PostMapping("/sku/{skuId}/deduct")
    public ApiResponse<Map<String, Object>> deductStock(
            @PathVariable Long skuId,
            @Valid @RequestBody StockOperateRequest request) {

        boolean success = luaStockService.deductStock(
                skuId,
                request.getQuantity()
        );

        if (!success) {
            return ApiResponse.fail(409, "演示库存不足或尚未初始化");
        }

        return ApiResponse.success(Map.of(
                "skuId", skuId,
                "deducted", request.getQuantity(),
                "availableStock", luaStockService.getStock(skuId)
        ));
    }
}
