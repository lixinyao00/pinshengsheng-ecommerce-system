package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinshengsheng.product.dto.BrandSaveRequest;
import com.pinshengsheng.product.entity.Brand;
import com.pinshengsheng.product.mapper.BrandMapper;
import com.pinshengsheng.product.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    private final BrandMapper brandMapper;

    public BrandServiceImpl(BrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    @Override
    public List<Brand> getEnabledBrands() {
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Brand::getStatus, 1)
                .orderByAsc(Brand::getSort)
                .orderByDesc(Brand::getId);

        return brandMapper.selectList(queryWrapper);
    }

    @Override
    public List<Brand> getAllBrands() {
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Brand::getSort).orderByDesc(Brand::getId);
        return brandMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public Brand createBrand(BrandSaveRequest request) {
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

    private void copyRequest(BrandSaveRequest request, Brand brand) {
        brand.setName(request.getName());
        brand.setLogo(request.getLogo());
    }
}
