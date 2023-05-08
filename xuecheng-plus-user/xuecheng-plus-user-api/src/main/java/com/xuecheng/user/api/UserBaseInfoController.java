package com.xuecheng.user.api;

import com.xuecheng.user.model.dto.QueryUserParamsDto;
import com.xuecheng.user.model.dto.UserInfoDto;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.service.UserBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;


@RestController
@PreAuthorize("hasAuthority('super_admin')")
public class UserBaseInfoController {

    @Autowired
    UserBaseInfoService userBaseInfoService;

    @GetMapping("/hello")
    public String sayHello(){
        return "hello world!!!!!!";
    }

    @PostMapping("/list")
    public PageResult<UserInfoDto> list(PageParams pageParams, @RequestBody(required = false) QueryUserParamsDto queryUserParamsDto){
        return userBaseInfoService.queryUserBaseList(pageParams, queryUserParamsDto);
    }
}
