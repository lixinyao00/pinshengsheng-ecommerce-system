package com.pinshengsheng.product.vo;

import java.util.ArrayList;
import java.util.List;

// 返回给前端的分类树节点
public class CategoryTreeVO {

    private Long id;
    private Long parentId;
    private String name;
    private Integer levelNum;
    private Integer sort;

    // 当前分类下的子分类
    private List<CategoryTreeVO> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevelNum() {
        return levelNum;
    }

    public void setLevelNum(Integer levelNum) {
        this.levelNum = levelNum;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<CategoryTreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryTreeVO> children) {
        this.children = children;
    }
}