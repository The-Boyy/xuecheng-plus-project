package com.xuecheng.user.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.user.mapper.UserBaseInfoMapper;
import com.xuecheng.user.model.dto.QueryUserParamsDto;
import com.xuecheng.user.model.dto.UserInfoDto;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.service.UserBaseInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserBaseInfoServiceImpl implements UserBaseInfoService {

    @Autowired
    UserBaseInfoMapper userBaseInfoMapper;
    @Override
    public PageResult<UserInfoDto> queryUserBaseList(PageParams pageParams, QueryUserParamsDto queryUserParamsDto) {

        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();

        if(queryUserParamsDto != null){
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getUsername()), XcUser::getUsername, queryUserParamsDto.getUsername());
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getNickname()), XcUser::getNickname, queryUserParamsDto.getUsername());
            queryWrapper.eq(StringUtils.isNotEmpty(queryUserParamsDto.getStatus()), XcUser::getStatus, queryUserParamsDto.getStatus());
            queryWrapper.eq(StringUtils.isNotEmpty(queryUserParamsDto.getSex()), XcUser::getSex, queryUserParamsDto.getSex());
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getEmail()), XcUser::getEmail, queryUserParamsDto.getEmail());
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getQq()), XcUser::getQq, queryUserParamsDto.getQq());
        }

        Page<XcUser> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<XcUser> pageResult = userBaseInfoMapper.selectPage(page, queryWrapper);

        List<XcUser> users = pageResult.getRecords();

        List<UserInfoDto> userInfoDtoList = new ArrayList<>();

        for (XcUser user : users) {
            UserInfoDto userInfoDto = new UserInfoDto();
            BeanUtils.copyProperties(user, userInfoDto);
            userInfoDtoList.add(userInfoDto);
        }

        int total = (int)pageResult.getTotal();

        return new PageResult<>(userInfoDtoList, total, pageParams.getPageNo(), pageParams.getPageSize());
    }
}
