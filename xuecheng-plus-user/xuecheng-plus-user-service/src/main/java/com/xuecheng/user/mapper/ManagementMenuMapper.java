package com.xuecheng.user.mapper;

import com.xuecheng.user.model.dto.ManagementDto;
import com.xuecheng.user.model.dto.RouterDto;
import com.xuecheng.user.model.po.Router;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ManagementMenuMapper {

    public List<ManagementDto> queryMenuList(Long user_id);

    public List<RouterDto> queryRouterList(Long user_id);
}
