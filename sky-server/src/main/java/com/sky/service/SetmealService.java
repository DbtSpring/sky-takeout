package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    public PageResult setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void add(SetmealDTO setmealDTO);

    void deleteBatch(List<Long> ids);

    SetmealVO getById(Long id);

    void update(SetmealDTO setmealDTO);

    void setStatusById(Integer status, Long id);

    //用户端用，主界面上
    List<Setmeal> getListByCategoryId(Long categoryId);

    List<DishItemVO> getDishesBySetmealId(Long id);
}
