package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 通过id查订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 统计不同状态的订单数
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 获得小于某个时间的某种状态的订单
     * @param status
     * @param time
     * @return
     */
    @Select("select * from  orders where status = #{status} and order_time < #{time} ")
    List<Orders> getByStatusAndCreateTimeTl(Integer status, LocalDateTime time);

    /**
     * 统计营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 按照一定的时间和状态范围统计订单数量
     * @param map
     * @return
     */


    Integer countOrderByMap(Map map);
}
