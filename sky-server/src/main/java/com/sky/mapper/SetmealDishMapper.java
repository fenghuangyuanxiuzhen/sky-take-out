package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface SetmealDishMapper {
    List<Long> getSetMealIdByDishId(List<Long> ids);
}
