package com.xuecheng.user.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.mapper.MenuMapper;
import com.xuecheng.user.mapper.PermissionMapper;
import com.xuecheng.user.model.dto.*;
import com.xuecheng.user.model.po.XcMenu;
import com.xuecheng.user.model.po.XcPermission;
import com.xuecheng.user.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    MenuMapper menuMapper;

    @Override
    public List<MenuDto> queryMenuList(Long userId) {
        return menuMapper.queryMenuList(userId);
    }

    @Override
    public List<RouterDto> queryRouterList(Long userId) {

        return menuMapper.queryRouterList(userId);
    }

    @Override
    public MenuResultDto queryList(Long userId) {

        MenuResultDto menuResultDto = new MenuResultDto();
        List<RouterDto> routerDtoList = menuMapper.queryRouterList(userId);
        RouterReturnDto routerReturnDto = new RouterReturnDto();
        routerReturnDto.setChildren(routerDtoList);
        routerReturnDto.setName("");
        routerReturnDto.setMeta("");
        routerReturnDto.setPath("/");
        menuResultDto.setRouterReturnDto(routerReturnDto);
        menuResultDto.setMenuDtoList(menuMapper.queryMenuList(userId));
        return menuResultDto;
    }

}
