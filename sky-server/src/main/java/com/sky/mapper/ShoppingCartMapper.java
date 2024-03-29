package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface ShoppingCartMapper {
    /**
     * 查找购物车商品是否存在
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 修改购物车商品数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumber(ShoppingCart shoppingCart);

    /**
     * 插入购物车表
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name,image,user_id,dish_id,setmeal_id,dish_flavor," +
            "number,amount,create_time) values (#{name},#{image},#{userId},#{dishId},#{setmealId}," +
            "#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 根据id删除购物车
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where id = #{id}")

    void deleteById(ShoppingCart shoppingCart);

    /**
     * 批量插入购物车
     * @param shoppingCartList
     */

    void BatchInsert(List<ShoppingCart> shoppingCartList);
}
