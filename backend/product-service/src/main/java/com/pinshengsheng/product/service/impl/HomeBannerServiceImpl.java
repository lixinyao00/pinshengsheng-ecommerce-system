package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinshengsheng.product.dto.HomeBannerSaveRequest;
import com.pinshengsheng.product.entity.HomeBanner;
import com.pinshengsheng.product.mapper.HomeBannerMapper;
import com.pinshengsheng.product.service.HomeBannerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 首页轮播图的查询、保存和启停规则集中在这里处理
@Service
public class HomeBannerServiceImpl implements HomeBannerService {

    private final HomeBannerMapper homeBannerMapper;

    public HomeBannerServiceImpl(HomeBannerMapper homeBannerMapper) {
        this.homeBannerMapper = homeBannerMapper;
    }

    @Override
    public List<HomeBanner> getEnabledBanners() {
        LambdaQueryWrapper<HomeBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeBanner::getStatus, 1)
                .orderByAsc(HomeBanner::getSort)
                .orderByDesc(HomeBanner::getId);
        return homeBannerMapper.selectList(wrapper);
    }

    @Override
    public List<HomeBanner> getAllBanners() {
        LambdaQueryWrapper<HomeBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(HomeBanner::getSort)
                .orderByDesc(HomeBanner::getId);
        return homeBannerMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public HomeBanner createBanner(HomeBannerSaveRequest request) {
        HomeBanner banner = new HomeBanner();
        copyRequest(request, banner);
        banner.setSort(request.getSort() == null ? 0 : request.getSort());
        banner.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        homeBannerMapper.insert(banner);
        return banner;
    }

    @Override
    @Transactional
    public HomeBanner updateBanner(Long id, HomeBannerSaveRequest request) {
        HomeBanner banner = homeBannerMapper.selectById(id);
        if (banner == null) {
            return null;
        }
        copyRequest(request, banner);
        if (request.getSort() != null) {
            banner.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            banner.setStatus(request.getStatus());
        }
        homeBannerMapper.updateById(banner);
        return banner;
    }

    @Override
    @Transactional
    public boolean updateBannerStatus(Long id, Integer status) {
        HomeBanner banner = homeBannerMapper.selectById(id);
        if (banner == null) {
            return false;
        }
        banner.setStatus(status);
        return homeBannerMapper.updateById(banner) > 0;
    }

    @Override
    public HomeBanner getBanner(Long id) {
        return homeBannerMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteBanner(Long id) {
        return homeBannerMapper.deleteById(id) > 0;
    }

    private void copyRequest(HomeBannerSaveRequest request, HomeBanner banner) {
        banner.setImageUrl(request.getImageUrl());
    }
}
