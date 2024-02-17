package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.web.bind.annotation.*;

/**
 * @author 杨楠
 * @version 1.0
 */
@RestController("AdminShopController")
@RequestMapping("admin/shop")
@Slf4j
@Api(tags = "店铺操作接口")
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;
    public static final String SHOP_KEY= "shop_status";
    @ApiOperation("设置营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺的营业状态为：{}",status == 1?"营业中":"打样中");
        redisTemplate.opsForValue().set(SHOP_KEY,status);
        return Result.success();

    }
    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_KEY);
        log.info("获取店铺的营业状态为：{}",status == 1?"营业中":"打样中");
        return Result.success(status);

    }

}
