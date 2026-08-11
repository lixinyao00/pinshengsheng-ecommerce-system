package com.pinshengsheng.product.service;

import com.pinshengsheng.product.dto.HomeBannerSaveRequest;
import com.pinshengsheng.product.entity.HomeBanner;

import java.util.List;

public interface HomeBannerService {

    List<HomeBanner> getEnabledBanners();

    List<HomeBanner> getAllBanners();

    HomeBanner createBanner(HomeBannerSaveRequest request);

    HomeBanner updateBanner(Long id, HomeBannerSaveRequest request);

    boolean updateBannerStatus(Long id, Integer status);

    HomeBanner getBanner(Long id);

    boolean deleteBanner(Long id);
}
