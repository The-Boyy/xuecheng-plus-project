package com.xuecheng.user.service.impl;

import com.xuecheng.user.mapper.ManagementMenuMapper;
import com.xuecheng.user.model.dto.ManagementDto;
import com.xuecheng.user.model.dto.MenuResultDto;
import com.xuecheng.user.model.dto.RouterDto;
import com.xuecheng.user.model.dto.RouterReturnDto;
import com.xuecheng.user.model.po.Router;
import com.xuecheng.user.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    ManagementMenuMapper managementMenuMapper;
    @Override
    public List<ManagementDto> queryMenuList(Long userId) {
        return managementMenuMapper.queryMenuList(userId);
    }

    @Override
    public List<RouterDto> queryRouterList(Long userId) {

        return managementMenuMapper.queryRouterList(userId);
    }

    @Override
    public MenuResultDto queryList(Long userId) {

        MenuResultDto menuResultDto = new MenuResultDto();
        List<RouterDto> routerDtoList = managementMenuMapper.queryRouterList(userId);
        RouterReturnDto routerReturnDto = new RouterReturnDto();
        routerReturnDto.setChildren(routerDtoList);
        routerReturnDto.setName("");
        routerReturnDto.setMeta("");
        routerReturnDto.setPath("/");
        menuResultDto.setRouterReturnDto(routerReturnDto);
        menuResultDto.setManagementDtoList(managementMenuMapper.queryMenuList(userId));
        return menuResultDto;
    }
}
