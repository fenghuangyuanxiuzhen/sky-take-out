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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("添加菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
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
        return Result.success();


    }




}
