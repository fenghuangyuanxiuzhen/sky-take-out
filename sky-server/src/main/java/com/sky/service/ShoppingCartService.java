package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
public interface ShoppingCartService {
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 根据id查购物车
     * @return
     */

    List<ShoppingCart> list();

    /**
     * 清空购物车
     */

    void clean();

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */

    void sub(ShoppingCartDTO shoppingCartDTO);
}
