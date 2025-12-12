package com.sky.controller.user;


import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags="用户端-菜品接口")
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;

    @ApiOperation("根据categoryId查询dish")
    @GetMapping("/list")
    public Result<List<DishVO>> getListByCategoryId(Long categoryId){
        log.info("根据categoryId查询dish, categoryId:{}",categoryId);

        String key = "dish_" + categoryId;
        List<DishVO> list = (List<DishVO>)redisTemplate.opsForValue().get(key);
        if(list != null && !list.isEmpty()){
            return Result.success(list);
        }

        list = dishService.getListByCategoryId(categoryId);
        redisTemplate.opsForValue().set(key, list);
        return Result.success(list);
    }

}
