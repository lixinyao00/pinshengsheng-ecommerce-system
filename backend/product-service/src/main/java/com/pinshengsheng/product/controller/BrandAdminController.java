package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.BrandSaveRequest;
import com.pinshengsheng.product.entity.Brand;
import com.pinshengsheng.product.service.BrandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 后台品牌管理接口，和商城用户端的品牌列表接口分开
@RestController
@RequestMapping("/api/admin/brand")
public class BrandAdminController {

    private final BrandService brandService;

    public BrandAdminController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Brand>> getBrandList() {
        // 后台列表需要包含已停用品牌，便于重新上架
        return ApiResponse.success(brandService.getAllBrands());
    }

    @PostMapping
    public ApiResponse<Brand> createBrand(@RequestBody BrandSaveRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ApiResponse.fail(400, "品牌名称不能为空");
        }
        return ApiResponse.success(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Brand> updateBrand(
            @PathVariable Long id,
            @RequestBody BrandSaveRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ApiResponse.fail(400, "品牌名称不能为空");
        }
        Brand brand = brandService.updateBrand(id, request);
        return brand == null
                ? ApiResponse.fail(404, "品牌不存在")
                : ApiResponse.success(brand);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateBrandStatus(
            @PathVariable Long id,
            @RequestParam("status") Integer status) {
        if (!validStatus(status)) {
            return ApiResponse.fail(400, "品牌状态只能是 0 或 1");
        }
        return brandService.updateBrandStatus(id, status)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "品牌不存在");
    }

    private boolean validStatus(Integer status) {
        // 当前项目只定义两种状态：0 停用、1 启用
        return Integer.valueOf(0).equals(status)
                || Integer.valueOf(1).equals(status);
    }
}
