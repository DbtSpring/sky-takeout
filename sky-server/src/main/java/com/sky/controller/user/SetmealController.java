package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "用户端-套餐接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @ApiOperation("根据categoryId查询套餐")
    @GetMapping("/list")
    @Cacheable(cacheNames="setmeal", key="#categoryId")
    public Result<List<Setmeal>> getListByCategoryId(Long categoryId){
        log.info("根据categoryId查询套餐,categoryId:{}",categoryId);
        List<Setmeal> list = setmealService.getListByCategoryId(categoryId);
        return Result.success(list);
    }

    @ApiOperation("根据setemealId查询dish")
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> getDishesBySetmealId(@PathVariable Long id){
        log.info("根据setmealId查询dish,setmealId:{}",id);
        List<DishItemVO> list = setmealService.getDishesBySetmealId(id);
        return Result.success(list);
    }
}
