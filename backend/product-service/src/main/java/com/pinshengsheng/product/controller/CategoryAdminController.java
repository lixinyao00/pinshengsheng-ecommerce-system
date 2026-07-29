package com.pinshengsheng.product.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.product.dto.CategorySaveRequest;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
public class CategoryAdminController {

    private final CategoryService categoryService;

    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Category>> getCategoryList() {
        return ApiResponse.success(categoryService.getAllCategories());
    }

    @PostMapping
    public ApiResponse<Category> createCategory(
            @RequestBody CategorySaveRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ApiResponse.fail(400, "分类名称不能为空");
        }
        Category category = categoryService.createCategory(request);
        return category == null
                ? ApiResponse.fail(400, "父分类不存在")
                : ApiResponse.success(category);
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody CategorySaveRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ApiResponse.fail(400, "分类名称不能为空");
        }
        Category category = categoryService.updateCategory(id, request);
        return category == null
                ? ApiResponse.fail(400, "分类不存在或父分类无效")
                : ApiResponse.success(category);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateCategoryStatus(
            @PathVariable Long id,
            @RequestParam("status") Integer status) {
        if (!validStatus(status)) {
            return ApiResponse.fail(400, "分类状态只能是 0 或 1");
        }
        return categoryService.updateCategoryStatus(id, status)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "分类不存在");
    }

    private boolean validStatus(Integer status) {
        return Integer.valueOf(0).equals(status)
                || Integer.valueOf(1).equals(status);
    }
}
