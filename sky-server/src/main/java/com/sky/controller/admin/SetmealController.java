package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐 SetmealController")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询：{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.setmealPageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping()
    @ApiOperation("新增套餐(和setmeal-dish表)")
    @CacheEvict(cacheNames = "setmeal" , key="setmealDTO.categoryId")
    public Result add(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐(和setmeal-dish表),setmealDTO:{}",setmealDTO);
        setmealService.add(setmealDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("根据ids批量删除setmeal")
    @CacheEvict(cacheNames="setmeal" , allEntries = true)
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("根据ids批量删除setmeal,ids:{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询setmeal（回显）")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询setmeal（回显）,id:{}",id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    @PutMapping()
    @ApiOperation("修改setmeal和setmeal_dish")
    @CacheEvict(cacheNames="setmeal", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改setmeal和setmeal_dish,setmealDTO:{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售停售(根据id修改status)")
    @CacheEvict(cacheNames="setmeal", allEntries=true)
    public Result setStatusById(@PathVariable Integer status, Long id){
        log.info("起售停售(根据id修改status),id:{},status:{}",id,status);
        setmealService.setStatusById(status,id);
        return Result.success();
    }

}
