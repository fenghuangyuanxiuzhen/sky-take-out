package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface FlavorMapper {
    void insertBatch(List<DishFlavor> flavors);
}
