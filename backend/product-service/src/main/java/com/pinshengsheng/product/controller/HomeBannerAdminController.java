package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.HomeBannerSaveRequest;
import com.pinshengsheng.product.entity.HomeBanner;
import com.pinshengsheng.product.service.FileService;
import com.pinshengsheng.product.service.HomeBannerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 后台首页轮播图管理接口，图片文件仍然通过 FileService 保存到 MinIO
@RestController
@RequestMapping("/api/admin/banner")
public class HomeBannerAdminController {

    private final HomeBannerService homeBannerService;
    private final FileService fileService;

    public HomeBannerAdminController(
            HomeBannerService homeBannerService,
            FileService fileService) {
        this.homeBannerService = homeBannerService;
        this.fileService = fileService;
    }

    @GetMapping("/list")
    public ApiResponse<List<HomeBanner>> getBannerList() {
        return ApiResponse.success(homeBannerService.getAllBanners());
    }

    @PostMapping
    public ApiResponse<HomeBanner> createBanner(
            @RequestBody HomeBannerSaveRequest request) {
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            return ApiResponse.fail(400, "请先上传轮播图");
        }
        return ApiResponse.success(homeBannerService.createBanner(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HomeBanner> updateBanner(
            @PathVariable Long id,
            @RequestBody HomeBannerSaveRequest request) {
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            return ApiResponse.fail(400, "轮播图地址不能为空");
        }
        HomeBanner banner = homeBannerService.updateBanner(id, request);
        return banner == null
                ? ApiResponse.fail(404, "轮播图不存在")
                : ApiResponse.success(banner);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateBannerStatus(
            @PathVariable Long id,
            @RequestParam("status") Integer status) {
        if (!validStatus(status)) {
            return ApiResponse.fail(400, "轮播图状态只能是 0 或 1");
        }
        return homeBannerService.updateBannerStatus(id, status)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "轮播图不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        HomeBanner banner = homeBannerService.getBanner(id);
        if (banner == null) {
            return ApiResponse.fail(404, "轮播图不存在");
        }
        fileService.deleteImage(banner.getImageUrl());
        return homeBannerService.deleteBanner(id)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "轮播图不存在");
    }

    private boolean validStatus(Integer status) {
        return Integer.valueOf(0).equals(status)
                || Integer.valueOf(1).equals(status);
    }
}
