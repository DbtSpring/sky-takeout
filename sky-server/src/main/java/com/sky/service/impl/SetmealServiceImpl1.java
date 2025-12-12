package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl1 implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    public PageResult setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.setmealPageQuery(setmealPageQueryDTO);

        System.out.println("!!!!!!!!");
        System.out.println(new PageResult(page.getTotal(), page.getResult()));
        System.out.println("!!!!!!!!!!!1");

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = Setmeal.builder().status(StatusConstant.DISABLE).build();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.add(setmeal);

        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for(SetmealDish s: setmealDishes){
            s.setSetmealId(setmealId);
        }
        setmealDishMapper.add(setmealDishes);

    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            if(setmealMapper.getStatusById(id) == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        ids.forEach(id ->{
            setmealMapper.deleteById(id);
            setmealDishMapper.deleteBySetmealId(id);
        });
    }

    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setmealMapper.getById(id);
        setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        return setmealVO;
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for(SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishMapper.add(setmealDishes);
    }

    @Override
    public void setStatusById(Integer status, Long id) {
        if(status == StatusConstant.ENABLE){
           List<Long> dishIds = setmealDishMapper.getDishIdsBySetmealId(id);
           dishIds.forEach(dishId -> {
               if(dishMapper.getStatusById(dishId) == StatusConstant.DISABLE){
                   throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
               }
           });
        }

        Setmeal setmeal = Setmeal.builder().status(status).id(id).build();
        setmealMapper.update(setmeal);
    }

    @Override
    public List<Setmeal> getListByCategoryId(Long categoryId){
        Setmeal querySetmeal = Setmeal.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        return setmealMapper.getList(querySetmeal);
    }

    @Override
    public List<DishItemVO> getDishesBySetmealId(Long setmealId) {
        return setmealMapper.getDishesBySetmealId(setmealId);
    }


}
