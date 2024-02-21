package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}