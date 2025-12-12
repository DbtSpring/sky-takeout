package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.impl.AddressBookServiceImpl1;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags="用户端-地址簿接口")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;


    @PostMapping
    @ApiOperation("新增地址")
    public Result add(@RequestBody AddressBook addressBook){
        log.info("新增地址,addressBook:{}",addressBook);
        addressBookService.add(addressBook);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询本用户的所有地址")
    public Result<List<AddressBook>> list(){
        log.info("查询本用户的所有地址");
        return Result.success(addressBookService.list());
    }

    @GetMapping("/default")
    @ApiOperation("查询本用户的默认地址")
    public Result<List<AddressBook>> defaultList(){
        log.info("查询本用户的默认地址");
        return Result.success(addressBookService.defaultList());
    }

    @PutMapping("/default")
    @ApiOperation("根据id把地址设置为的默认")
    public Result setDefaultById(@RequestBody AddressBook addressBook){
        log.info("根据id把地址设置为的默认,id:{}",addressBook.getId());
        addressBookService.setDefaultById(addressBook);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址（回显）")
    public Result<AddressBook> getById(@PathVariable Long id){
        log.info("根据id查询地址（回显）,id:{}",id);
        return Result.success(addressBookService.getById(id));
    }

    @PutMapping()
    @ApiOperation("修改地址")
    public Result update(@RequestBody AddressBook addressBook){
        log.info("修改地址,addressBook:{}",addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    @DeleteMapping()
    @ApiOperation("根据id删除地址")
    public Result deleteById(Long id){
        log.info("根据id删除地址,id:{}",id);
        addressBookService.deleteById(id);
        return Result.success();
    }



}

