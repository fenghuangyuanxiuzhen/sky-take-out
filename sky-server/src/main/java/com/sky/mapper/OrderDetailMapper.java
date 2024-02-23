package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface OrderDetailMapper {
    /**'
     * 批量插入订单详情
     * @param orderDetails
     */
    void batchInsert(List<OrderDetail> orderDetails);

    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getByOrderId(Long id);
}
