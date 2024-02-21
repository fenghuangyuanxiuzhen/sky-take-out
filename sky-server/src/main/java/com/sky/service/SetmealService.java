package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
public interface SetmealService {
    /**
     * 插入套餐
     * @param setmealDTO
     */
    void insert(SetmealDTO setmealDTO);

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除
     * @param ids
     */

    void delete(List<Long> ids);

    /**
     * 根据id查询套餐和套餐的菜品
     * @param id
     * @return
     */

    SetmealDTO getSetmealWithDishById(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */

    void update(SetmealDTO setmealDTO);

    /**
     * 停售、起售
     * @param status
     * @param id
     */

    void startOrStop(int status, Long id);
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
