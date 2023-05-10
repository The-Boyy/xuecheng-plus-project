package com.xuecheng.user.service;

import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.model.dto.MenuDto;
import com.xuecheng.user.model.dto.MenuResultDto;
import com.xuecheng.user.model.dto.PermissionsDto;
import com.xuecheng.user.model.dto.RouterDto;

import java.util.List;

public interface MenuService {

    public List<MenuDto> queryMenuList(Long userId);

    public List<RouterDto> queryRouterList(Long userId);

    public MenuResultDto queryList(Long userId);
}
