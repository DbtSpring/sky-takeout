package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.context.BaseContext1;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl1 implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO){
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext1.getThreadLocal());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //返回list结果，防止意外情况有重复数据。实际之取第一条
        if(list != null && list.size() == 1){//购物车中已经有一条了（但是number可能大于1）
            shoppingCart = list.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.setNumberById(shoppingCart);
        }else{//购物车中没有此菜品/套餐
            Long dishId = shoppingCart.getDishId();
            if(dishId != null){//是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{//是套餐
                SetmealVO setmealVO = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setImage(setmealVO.getImage());
                shoppingCart.setAmount(setmealVO.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }
    }

    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(BaseContext1.getThreadLocal()).build();
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void clean() {
        shoppingCartMapper.cleanByUserId(BaseContext1.getThreadLocal());
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext1.getThreadLocal());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if(list != null && list.size() == 1){
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if( number == 1){//直接删除
                shoppingCartMapper.deleteItem(shoppingCart);
            }else if(number > 1){//number--
                shoppingCart.setNumber(shoppingCart.getNumber() - 1 );
                shoppingCartMapper.setNumberById(shoppingCart);
            }

        }
    }
}
