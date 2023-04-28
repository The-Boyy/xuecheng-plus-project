package com.xuecheng.user.service;

import com.xuecheng.user.model.dto.ManagementDto;
import com.xuecheng.user.model.dto.MenuResultDto;
import com.xuecheng.user.model.dto.RouterDto;
import com.xuecheng.user.model.po.Router;

import java.util.List;

public interface MenuService {

    public List<ManagementDto> queryMenuList(Long userId);

    public List<RouterDto> queryRouterList(Long userId);

    public MenuResultDto queryList(Long userId);
}
