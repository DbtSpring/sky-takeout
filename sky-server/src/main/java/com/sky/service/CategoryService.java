package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult categoryPageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void add(CategoryDTO categoryDTO);

    void deleteById(Integer id);

    void update(CategoryDTO categoryDTO);

    void updateStatus(Long id, Integer status);

    List<Category> getByType(Integer type);
}
