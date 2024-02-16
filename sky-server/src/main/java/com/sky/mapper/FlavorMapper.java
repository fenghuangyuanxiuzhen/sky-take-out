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
}
