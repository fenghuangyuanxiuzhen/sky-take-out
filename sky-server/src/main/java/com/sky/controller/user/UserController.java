package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 杨楠
 * @version 1.0
 */
@RestController
@RequestMapping("/user/user")
@Slf4j
@Api(tags = "c端-用户接口")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    JwtProperties jwtProperties;
    @PostMapping("/login")
    public Result<UserLoginVO> login( @RequestBody UserLoginDTO userLoginDTO){
        log.info("微信登录授权码为：{}",userLoginDTO);
        //code 进去返回User  里边有openid  id  通过id 生成 token 装入UserLoginVo然后返回
        User user  =  userService.wxLogin(userLoginDTO);
        Long id = user.getId();
        Map claims = new HashMap();
        claims.put(JwtClaimsConstant.USER_ID,id);
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(id)
                .openid(user.getOpenid())
                .token(token).build();
        return Result.success(userLoginVO);


    }
}
