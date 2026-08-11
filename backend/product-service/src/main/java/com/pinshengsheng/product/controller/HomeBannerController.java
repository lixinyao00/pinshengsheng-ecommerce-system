package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.entity.HomeBanner;
import com.pinshengsheng.product.service.HomeBannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 商城用户端只读取已经启用的首页轮播图
@RestController
@RequestMapping("/api/product/banner")
public class HomeBannerController {

    private final HomeBannerService homeBannerService;

    public HomeBannerController(HomeBannerService homeBannerService) {
        this.homeBannerService = homeBannerService;
    }

    @GetMapping("/list")
    public ApiResponse<List<HomeBanner>> getBannerList() {
        return ApiResponse.success(homeBannerService.getEnabledBanners());
    }
}
