package com.xuecheng.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.user.model.dto.RoleDto;
import com.xuecheng.user.model.po.XcRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoleMapper extends BaseMapper<XcRole> {

    @Select("select id, role_name from xc_role")
    public List<RoleDto> queryRoleNameList();
}
