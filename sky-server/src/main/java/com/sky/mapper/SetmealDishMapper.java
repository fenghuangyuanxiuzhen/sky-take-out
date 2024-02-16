package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜id查询套餐id
     * @param ids
     * @return
     */
    List<Long> getSetMealIdByDishId(List<Long> ids);

    /**
     * 批量插入套餐菜
     * @param setmealDishes
     */
    void insert(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐菜
     * @param ids
     */

    void deleteBatch(List<Long> ids);

    /**
     * 根据套餐id查套餐菜
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")

    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据套餐id删除
     * @param id
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{id}")

    void deleteById(Long id);
}
