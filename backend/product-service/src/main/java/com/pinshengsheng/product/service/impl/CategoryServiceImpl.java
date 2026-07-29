package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinshengsheng.product.dto.CategorySaveRequest;
import com.pinshengsheng.product.entity.Category;
import com.pinshengsheng.product.mapper.CategoryMapper;
import com.pinshengsheng.product.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pinshengsheng.product.vo.CategoryTreeVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> getEnabledCategories() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Category::getStatus, 1)
                .orderByAsc(Category::getLevelNum)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId);

        return categoryMapper.selectList(queryWrapper);
    }
    // 将扁平分类列表组装为树形结构
    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        List<Category> categories = getEnabledCategories();
        Map<Long, CategoryTreeVO> nodeMap = new HashMap<>();

        // 先把每条分类记录转换成树节点
        for (Category category : categories) {
            CategoryTreeVO node = new CategoryTreeVO();
            node.setId(category.getId());
            node.setParentId(category.getParentId());
            node.setName(category.getName());
            node.setLevelNum(category.getLevelNum());
            node.setSort(category.getSort());

            nodeMap.put(node.getId(), node);
        }

        List<CategoryTreeVO> rootNodes = new ArrayList<>();

        // 再根据 parentId 建立父子关系
        for (Category category : categories) {
            CategoryTreeVO node = nodeMap.get(category.getId());

            // parentId 为 0 的是顶级分类
            if (category.getParentId() == null
                    || Long.valueOf(0).equals(category.getParentId())) {
                rootNodes.add(node);
                continue;
            }

            CategoryTreeVO parentNode = nodeMap.get(category.getParentId());

            // 父分类存在时，把当前节点加入其 children
            if (parentNode != null) {
                parentNode.getChildren().add(node);
            }
        }

        return rootNodes;
    }

    @Override
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getLevelNum)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId);
        return categoryMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public Category createCategory(CategorySaveRequest request) {
        Category category = new Category();
        if (!fillCategory(request, category)) {
            return null;
        }
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategorySaveRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null || id.equals(request.getParentId())) {
            return null;
        }
        if (!fillCategory(request, category)) {
            return null;
        }
        if (request.getSort() != null) {
            category.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        categoryMapper.updateById(category);
        return category;
    }

    @Override
    @Transactional
    public boolean updateCategoryStatus(Long id, Integer status) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return false;
        }
        category.setStatus(status);
        return categoryMapper.updateById(category) > 0;
    }

    // 根据父分类自动计算当前分类层级
    private boolean fillCategory(CategorySaveRequest request, Category category) {
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        category.setParentId(parentId);
        category.setName(request.getName());

        if (Long.valueOf(0).equals(parentId)) {
            category.setLevelNum(1);
            return true;
        }

        Category parent = categoryMapper.selectById(parentId);
        if (parent == null) {
            return false;
        }
        category.setLevelNum(parent.getLevelNum() + 1);
        return true;
    }
}
