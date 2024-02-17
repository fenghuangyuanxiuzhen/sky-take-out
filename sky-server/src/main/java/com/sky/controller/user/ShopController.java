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

/**
 * @author 杨楠
 * @version 1.0
 */
@RestController("UserShopController")
@RequestMapping("user/shop")
@Slf4j
@Api(tags = "店铺操作接口")
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;
    public static final String SHOP_KEY= "shop_status";

    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_KEY);
        log.info("获取店铺的营业状态为：{}",status == 1?"营业中":"打样中");
        return Result.success(status);

    }

}
