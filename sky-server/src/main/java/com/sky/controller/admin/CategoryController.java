package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @ApiOperation("category分页查询（name，type）")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("category分页查询（根据name，type），参数：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.categoryPageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("新增category")
    @PostMapping
    public Result add(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增category,参数：{}", categoryDTO);
        categoryService.add(categoryDTO);
        return Result.success();
    }

    @ApiOperation("根据id删除category")
    @DeleteMapping
    public Result deleteById(Integer id) {
        log.info("根据id删除category,id为:{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    @ApiOperation("修改category")
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改category,修改成：{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @ApiOperation("启用禁用category(根据id改status)")
    @PostMapping("/status/{status}")
    public Result updateStatus(Long id, @PathVariable Integer status){
        log.info("启用禁用category(根据id改status),id:{},status:{}",id,status);
        categoryService.updateStatus(id,status);
        return Result.success();
    }

    //这个类型查询和分页查询不一样，这个是给后面菜品和套餐管理传数据的接口
    @ApiOperation("category查询(type)")
    @GetMapping("/list")
    public Result<List<Category>> getByType(Integer type){
        log.info("category查询(根据type),type:{}",type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }
}
