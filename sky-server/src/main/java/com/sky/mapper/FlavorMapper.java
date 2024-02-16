package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface FlavorMapper {
    /**
     * 批量插入口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 批量删除风味
     * @param ids
     */

    void deletes(List<Long> ids);

    /**
     * 根据id查口味
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where id = #{id}")

    List<DishFlavor> getByDishId(Long id);

    /**
     * 根据id单条删除
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteById(Long id);
}
