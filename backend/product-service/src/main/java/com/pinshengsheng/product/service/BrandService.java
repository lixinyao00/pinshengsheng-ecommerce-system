package com.pinshengsheng.product.service;

import com.pinshengsheng.product.dto.BrandSaveRequest;
import com.pinshengsheng.product.entity.Brand;

import java.util.List;

public interface BrandService {
    List<Brand> getEnabledBrands();

    Brand getBrandById(Long id);

    List<Brand> getAllBrands();

    Brand createBrand(BrandSaveRequest request);

    Brand updateBrand(Long id, BrandSaveRequest request);

    boolean updateBrandStatus(Long id, Integer status);
}
