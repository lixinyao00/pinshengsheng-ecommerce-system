package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.service.CategoryService;
import com.pinshengsheng.product.vo.CategoryTreeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product/category")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Category>> getCategoryList() {
        return ApiResponse.success(categoryService.getEnabledCategories());
    }
    // 查询树形分类，供前端按层级展示
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeVO>> getCategoryTree() {
        return ApiResponse.success(categoryService.getCategoryTree());
    }
}
