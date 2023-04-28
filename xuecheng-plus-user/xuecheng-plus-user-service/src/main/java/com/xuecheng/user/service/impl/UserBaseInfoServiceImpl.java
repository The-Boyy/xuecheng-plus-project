package com.xuecheng.user.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.user.mapper.UserBaseInfoMapper;
import com.xuecheng.user.model.dto.QueryUserParamsDto;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.service.UserBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBaseInfoServiceImpl implements UserBaseInfoService {

    @Autowired
    UserBaseInfoMapper userBaseInfoMapper;
    @Override
    public PageResult<XcUser> queryUserBaseList(PageParams pageParams, QueryUserParamsDto queryUserParamsDto) {

        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();

        if(queryUserParamsDto != null){
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getUsername()), XcUser::getUsername, queryUserParamsDto.getUsername());
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getNickname()), XcUser::getNickname, queryUserParamsDto.getUsername());
            queryWrapper.eq(StringUtils.isNotEmpty(queryUserParamsDto.getStatus()), XcUser::getStatus, queryUserParamsDto.getStatus());
        }

        Page<XcUser> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<XcUser> pageResult = userBaseInfoMapper.selectPage(page, queryWrapper);

        List<XcUser> users = pageResult.getRecords();

        int total = (int)pageResult.getTotal();

        return new PageResult<>(users, total, pageParams.getPageNo(), pageParams.getPageSize());
    }
}
