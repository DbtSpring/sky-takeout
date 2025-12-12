package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@Slf4j
@Api(tags = "菜品 DishController")
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品（包括口味）")
    public Result add(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品（包括口味）,dishDTO:{}", dishDTO);
        dishService.add(dishDTO);

        //清除redis缓存
        String pattern = "dish_" + dishDTO.getCategoryId();
        redisDelete(pattern);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> dishPageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.dishPageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping()
    @ApiOperation("根据id删除dish和dishFlavor")
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("根据id删除dish和dishFlavor,ids:{}",ids);
        dishService.deleteBatch(ids);

        String pattern = "dish_*";
        redisDelete(pattern);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询dish(回显)")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询dish(回显):{}",id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改dish")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改dish:{}",dishDTO);
        dishService.update(dishDTO);

        String pattern = "dish_*";
        redisDelete(pattern);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售(根据id改status)")
    public Result setStatusById(@PathVariable Integer status, Long id){
        log.info("菜品起售停售(根据id改status),id:{},status:{}",id , status);
        dishService.setStatusById(status, id);
        String pattern = "dish_*";
        redisDelete(pattern);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据categoryId查询dish")
    public Result<List<Dish>> geteByCategoryId(Long categoryId){
        log.info("根据categoryId查询dish,categoryId:{}",categoryId);
        List<Dish> list= dishService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    //批量删除key
    private void redisDelete(String pattern){
        Set<String> keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }


}
