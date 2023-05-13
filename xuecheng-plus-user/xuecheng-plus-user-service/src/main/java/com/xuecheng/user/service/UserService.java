package com.xuecheng.user.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.model.dto.QueryUserParamsDto;
import com.xuecheng.user.model.dto.UserInfoDto;
import com.xuecheng.user.model.dto.UserRegisterDto;
import com.xuecheng.user.model.dto.UserUpdateDto;
import com.xuecheng.user.model.po.XcUser;

public interface UserService {
    public PageResult<UserInfoDto> queryUserBaseList(PageParams pageParams, QueryUserParamsDto queryUserParamsDto);

    ResultResponse<XcUser> register(UserRegisterDto userRegisterDto, String username);

    ResultResponse<UserUpdateDto> findUserById(String userId);

    ResultResponse<XcUser> userUpdate(UserUpdateDto userUpdateDto, String username);

    ResultResponse<?> deleteUserById(String userId);
}
