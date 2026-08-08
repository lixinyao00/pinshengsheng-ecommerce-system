package com.pinshengsheng.product.service;

import com.pinshengsheng.product.dto.CategorySaveRequest;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.vo.CategoryTreeVO;

import java.util.List;

public interface CategoryService {
    List<Category> getEnabledCategories();

    List<CategoryTreeVO> getCategoryTree();

    List<Category> getAllCategories();

    Category createCategory(CategorySaveRequest request);

    Category updateCategory(Long id, CategorySaveRequest request);

    boolean updateCategoryStatus(Long id, Integer status);

    Category getCategoryById(Long id);
}
