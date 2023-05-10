package com.xuecheng.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.user.model.dto.PermissionsDto;
import com.xuecheng.user.model.po.XcPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<XcPermission> {

    public int deletePermissionByIds(@Param("roleId") Long roleId, @Param("deletePermissionIds") List<Long> deletePermissionIds);
    public List<PermissionsDto> queryPermissions(Long roleId);
    @Select("select menu_id from xc_permission where role_id = #{roleId}")
    public List<Long> queryPermissionIdsByRoleId(Long roleId);

    public int insertBatch(@Param("xcPermissionList") List<XcPermission> xcPermissionList);
}
