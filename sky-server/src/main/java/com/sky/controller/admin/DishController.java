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
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author 杨楠
 * @version 1.0
 */
@RestController
@RequestMapping("admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("添加菜品：{}",dishDTO);
        String key = "dish_"+dishDTO.getCategoryId();
        dishService.saveWithFlavor(dishDTO);
        cleanCache(key);
        return Result.success();

    }
    @GetMapping("page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品：{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids){
        log.info("批量删除菜品：{}",ids);
        dishService.delete(ids);
        cleanCache("dish_*");

        return Result.success();
    }
    @GetMapping("{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getDishById(@PathVariable Long id){
        log.info("根据id查询菜品：{}",id);
        DishVO dishVO =  dishService.getDishWithFlavorById(id);
        return Result.success(dishVO);

    }
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息：{}",dishDTO);
        dishService.updateDishWithFlavor(dishDTO);
        cleanCache("dish_*");
        return Result.success();


    }
    @GetMapping("list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getDishByCategoryId(Long categoryId){
        log.info("根据分类id查询菜品：{}",categoryId);
        List<Dish> list = dishService.getDishByCategoryId(categoryId);
        return  Result.success(list);
    }
    @ApiOperation("菜品起售、停售")
    @PostMapping("status/{status}")
    public Result startOrStop(@PathVariable int status,Long id){
        dishService.startOrStop(status,id);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 清空redis缓存
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }






}
