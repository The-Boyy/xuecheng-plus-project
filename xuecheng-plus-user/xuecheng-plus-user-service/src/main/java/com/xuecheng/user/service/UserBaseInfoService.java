package com.xuecheng.user.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.user.model.dto.QueryUserParamsDto;
import com.xuecheng.user.model.dto.UserInfoDto;
import com.xuecheng.user.model.po.XcUser;

public interface UserBaseInfoService {
    public PageResult<UserInfoDto> queryUserBaseList(PageParams pageParams, QueryUserParamsDto queryUserParamsDto);
}
