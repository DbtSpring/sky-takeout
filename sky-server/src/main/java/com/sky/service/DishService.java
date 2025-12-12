package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void add(DishDTO dishDTO);

    PageResult dishPageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO getById(Long id);

    void update(DishDTO dishDTO);

    void setStatusById(Integer status, Long id);

    //管理端，新增套餐的时候，选择关联菜品的界面，需要根据CategoryId查询菜品。返回Dish即可
    List<Dish> getByCategoryId(Long categoryId);

    //用户端，主界面根据分类categoryId查询菜品。需要返回DishVO（包含flavor，后续客户点单要选flavor）
    List<DishVO> getListByCategoryId(Long categoryId);
}
