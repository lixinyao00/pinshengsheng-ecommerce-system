package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.SkuSaveRequest;
import com.pinshengsheng.product.service.SkuService;
import com.pinshengsheng.product.vo.SkuStockVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 后台 SKU 管理接口：规格信息和库存信息在这里一起维护
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
        // 返回 SKU 和库存的组合对象，前端无需再发第二次库存请求
        return ApiResponse.success(skuService.getSkuList(productId));
    }

    @PostMapping
    public ApiResponse<SkuStockVO> createSku(
            @RequestBody SkuSaveRequest request) {
        if (!validRequest(request)) {
            return ApiResponse.fail(400, "商品、规格名称和价格不能为空");
        }
        SkuStockVO sku = skuService.createSku(request);
        return sku == null
                ? ApiResponse.fail(400, "商品不存在")
                : ApiResponse.success(sku);
    }

    @PutMapping("/{id}")
    public ApiResponse<SkuStockVO> updateSku(
            @PathVariable Long id,
            @RequestBody SkuSaveRequest request) {
        if (!validRequest(request)) {
            return ApiResponse.fail(400, "商品、规格名称和价格不能为空");
        }
        SkuStockVO sku = skuService.updateSku(id, request);
        return sku == null
                ? ApiResponse.fail(400, "SKU 不存在或商品不存在")
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
        // 可用库存不能为负数，锁定库存后续在下单流程中维护
        if (availableStock == null || availableStock < 0) {
            return ApiResponse.fail(400, "可用库存不能小于 0");
        }
        return skuService.updateAvailableStock(id, availableStock)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "SKU 不存在");
    }

    private boolean validRequest(SkuSaveRequest request) {
        // 在 Controller 先挡住明显无效的数据，SKU 编码由 Service 自动生成
        return request.getProductId() != null
                && request.getSkuName() != null
                && !request.getSkuName().isBlank()
                && request.getPrice() != null
                && request.getPrice().signum() >= 0
                && (request.getAvailableStock() == null
                || request.getAvailableStock() >= 0);
    }
}
