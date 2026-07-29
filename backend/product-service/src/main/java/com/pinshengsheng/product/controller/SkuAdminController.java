package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.SkuSaveRequest;
import com.pinshengsheng.product.service.SkuService;
import com.pinshengsheng.product.vo.SkuStockVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sku")
public class SkuAdminController {

    private final SkuService skuService;

    public SkuAdminController(SkuService skuService) {
        this.skuService = skuService;
    }

    @GetMapping("/list")
    public ApiResponse<List<SkuStockVO>> getSkuList(
            @RequestParam("productId") Long productId) {
        return ApiResponse.success(skuService.getSkuList(productId));
    }

    @PostMapping
    public ApiResponse<SkuStockVO> createSku(
            @RequestBody SkuSaveRequest request) {
        if (!validRequest(request)) {
            return ApiResponse.fail(400, "商品、SKU 编码、名称和价格不能为空");
        }
        SkuStockVO sku = skuService.createSku(request);
        return sku == null
                ? ApiResponse.fail(400, "商品不存在或 SKU 编码重复")
                : ApiResponse.success(sku);
    }

    @PutMapping("/{id}")
    public ApiResponse<SkuStockVO> updateSku(
            @PathVariable Long id,
            @RequestBody SkuSaveRequest request) {
        if (!validRequest(request)) {
            return ApiResponse.fail(400, "商品、SKU 编码、名称和价格不能为空");
        }
        SkuStockVO sku = skuService.updateSku(id, request);
        return sku == null
                ? ApiResponse.fail(400, "SKU 不存在、商品不存在或编码重复")
                : ApiResponse.success(sku);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateSkuStatus(
            @PathVariable Long id,
            @RequestParam("status") Integer status) {
        if (!Integer.valueOf(0).equals(status)
                && !Integer.valueOf(1).equals(status)) {
            return ApiResponse.fail(400, "SKU 状态只能是 0 或 1");
        }
        return skuService.updateSkuStatus(id, status)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "SKU 不存在");
    }

    @PutMapping("/{id}/stock")
    public ApiResponse<Void> updateStock(
            @PathVariable Long id,
            @RequestParam("availableStock") Integer availableStock) {
        if (availableStock == null || availableStock < 0) {
            return ApiResponse.fail(400, "可用库存不能小于 0");
        }
        return skuService.updateAvailableStock(id, availableStock)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "SKU 不存在");
    }

    private boolean validRequest(SkuSaveRequest request) {
        return request.getProductId() != null
                && request.getSkuCode() != null
                && !request.getSkuCode().isBlank()
                && request.getSkuName() != null
                && !request.getSkuName().isBlank()
                && request.getPrice() != null
                && request.getPrice().signum() >= 0
                && (request.getAvailableStock() == null
                || request.getAvailableStock() >= 0);
    }
}
