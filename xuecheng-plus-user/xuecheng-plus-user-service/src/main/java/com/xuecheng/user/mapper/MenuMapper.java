package com.xuecheng.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.model.dto.MenuDto;
import com.xuecheng.user.model.dto.PermissionsDto;
import com.xuecheng.user.model.dto.RouterDto;
import com.xuecheng.user.model.po.XcMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<XcMenu> {

    public List<MenuDto> queryMenuList(Long user_id);

    public List<RouterDto> queryRouterList(Long user_id);

}
