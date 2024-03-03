package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

/**
 * @author 杨楠
 * @version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * 查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid  = #{openid} ")
    User getByOpenid(String openid);

    /**
     * 用户插入
     * @param user
     */

    void insert(User user);

    /**
     *
     * @param userId
     * @return
     */
    @Select("select  * from user where id = #{id} ")

    User getById(Long userId);

    /**
     * 统计用户
     * @param map
     * @return
     */

    Integer getCountUserByMap(HashMap map);
}
