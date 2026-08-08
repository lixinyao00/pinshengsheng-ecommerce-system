package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinshengsheng.product.dto.BrandSaveRequest;
import com.pinshengsheng.product.entity.Brand;
import com.pinshengsheng.product.mapper.BrandMapper;
import com.pinshengsheng.product.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 品牌的查询、保存和上下架规则集中在这里处理
@Service
public class BrandServiceImpl implements BrandService {
    private final BrandMapper brandMapper;

    public BrandServiceImpl(BrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    @Override
    public List<Brand> getEnabledBrands() {
        // 商城用户端只展示已启用的品牌
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Brand::getStatus, 1)
                .orderByAsc(Brand::getSort)
                .orderByDesc(Brand::getId);

        return brandMapper.selectList(queryWrapper);
    }

    @Override
    public List<Brand> getAllBrands() {
        // 后台管理页需要看到已下架品牌，因此不按状态过滤
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Brand::getSort).orderByDesc(Brand::getId);
        return brandMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public Brand createBrand(BrandSaveRequest request) {
        // 未传排序和状态时，使用默认排序和启用状态
        Brand brand = new Brand();
        copyRequest(request, brand);
        brand.setSort(request.getSort() == null ? 0 : request.getSort());
        brand.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        brandMapper.insert(brand);
        return brand;
    }

    @Override
    @Transactional
    public Brand updateBrand(Long id, BrandSaveRequest request) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            return null;
        }
        copyRequest(request, brand);
        if (request.getSort() != null) {
            brand.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            brand.setStatus(request.getStatus());
        }
        brandMapper.updateById(brand);
        return brand;
    }

    @Override
    @Transactional
    public boolean updateBrandStatus(Long id, Integer status) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            return false;
        }
        brand.setStatus(status);
        return brandMapper.updateById(brand) > 0;
    }

    @Override
    public Brand getBrandById(Long id) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null || !Integer.valueOf(1).equals(brand.getStatus())) {
            return null;
        }
        return brand;
    }

    private void copyRequest(BrandSaveRequest request, Brand brand) {
        // 新增和编辑共用字段复制，避免两处赋值逻辑不一致
        brand.setName(request.getName());
        brand.setLogo(request.getLogo());
    }
}
