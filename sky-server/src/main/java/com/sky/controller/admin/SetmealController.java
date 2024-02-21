package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@RestController
@RequestMapping("admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    SetmealService setmealService;
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    @ApiOperation("新增套餐")
    public Result insert(@RequestBody SetmealDTO setmealDTO){
        log.info("套餐增加：{}",setmealDTO);
        setmealService.insert(setmealDTO);
        return Result.success();
    }
    @GetMapping("page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐：{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除：{}",ids);
        setmealService.delete(ids);
        return Result.success();

    }
    @GetMapping("{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealDTO> getById(@PathVariable Long id){
        log.info("根据id查询套餐：{}",id);
        SetmealDTO setmealDTO = setmealService.getSetmealWithDishById(id);
        return Result.success(setmealDTO);

    }
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐：{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();

    }
    @ApiOperation("套餐起售、停售")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    @PostMapping("status/{status}")
    public Result startOrStop(@PathVariable int status,Long id){
        setmealService.startOrStop(status,id);
        return Result.success();
    }



}
