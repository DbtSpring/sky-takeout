package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags = "用户端商铺 ShopController")
public class ShopController {
    public static final String KEY = "status";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer)redisTemplate.opsForValue().get(KEY);
        if(status == null){
            return Result.error("营业状态status尚不存在，请联系程序员");
        }
        log.info("获取营业状态为：{}",status == 1? "营业中":"已打烊");
        return Result.success(status);
    }
}
