package com.xuecheng.user.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.model.dto.PermissionsDto;
import com.xuecheng.user.model.dto.QueryRoleParamsDto;
import com.xuecheng.user.model.dto.RoleDto;
import com.xuecheng.user.model.po.XcRole;

import java.util.List;

public interface RoleService {
    PageResult<RoleDto> queryList(PageParams pageParams, QueryRoleParamsDto queryRoleParamsDto);

    ResultResponse<List<PermissionsDto>> queryPermissions(Long roleId);

    ResultResponse<?> grantPermissions(Long roleId, List<Long> permissionIds);

    ResultResponse<List<RoleDto>> queryRoleNameList();

    ResultResponse<XcRole> increaseRole(XcRole role);

    ResultResponse<?> deleteRoleById(String roleId);

    ResultResponse<XcRole> updateRole(XcRole role);

    ResultResponse<XcRole> findRoleById(String roleId);
}
