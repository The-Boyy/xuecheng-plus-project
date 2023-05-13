package com.xuecheng.user.api;

import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.model.dto.QueryUserParamsDto;
import com.xuecheng.user.model.dto.UserInfoDto;
import com.xuecheng.user.model.dto.UserRegisterDto;
import com.xuecheng.user.model.dto.UserUpdateDto;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.service.UserService;
import com.xuecheng.user.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;


@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/hello")
    public String sayHello(){
        return "hello world!!!!!!";
    }

    @PostMapping("/list")
    public PageResult<UserInfoDto> list(PageParams pageParams, @RequestBody(required = false) QueryUserParamsDto queryUserParamsDto){
        return userService.queryUserBaseList(pageParams, queryUserParamsDto);
    }

    @PostMapping("/register")
    public ResultResponse<XcUser> register(@RequestBody UserRegisterDto userRegisterDto){

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        assert user != null;
        String username = user.getUsername();
        return userService.register(userRegisterDto, username);
    }

    @GetMapping("findUserById")
    public ResultResponse<UserUpdateDto> findUserById(String userId){
        return userService.findUserById(userId);
    }

    @PostMapping("/updateUser")
    public ResultResponse<XcUser> userUpdate(@RequestBody UserUpdateDto userUpdateDto){

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        assert user != null;
        String username = user.getUsername();
        return userService.userUpdate(userUpdateDto, username);
    }

    @DeleteMapping("/deleteUserById")
    public ResultResponse<?> deleteUserById(String userId){
        return userService.deleteUserById(userId);
    }
}
