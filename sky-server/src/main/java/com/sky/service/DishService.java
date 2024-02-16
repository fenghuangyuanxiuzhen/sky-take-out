package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */

public interface DishService {
    /**
     * 新增菜品和菜品口味
     * @param dishDTO
     */

    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */

    void delete(List<Long> ids);

    /**
     * 通过id查口味和菜品
     * @param id
     * @return
     */

    DishVO getDishWithFlavorById(Long id);

    /**
     * 修改菜品和对应的口味信息
     * @param dishDTO
     */

    void updateDishWithFlavor(DishDTO dishDTO);
}
