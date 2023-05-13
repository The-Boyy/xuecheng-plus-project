package com.xuecheng.user.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.model.dto.PermissionsDto;
import com.xuecheng.user.model.dto.QueryRoleParamsDto;
import com.xuecheng.user.model.dto.RoleDto;
import com.xuecheng.user.model.po.XcRole;
import com.xuecheng.user.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoleController {

    @Autowired
    RoleService roleService;

    @PostMapping("/role/list")
    public PageResult<RoleDto> queryRoleList(PageParams pageParams, @RequestBody(required = false)QueryRoleParamsDto queryRoleParamsDto){

        return roleService.queryList(pageParams, queryRoleParamsDto);
    }
    @GetMapping("/role/getRoleNameList")
    public ResultResponse<List<RoleDto>> queryRoleNameList(){
        return roleService.queryRoleNameList();
    }

    @GetMapping("/permissionList")
    public ResultResponse<List<PermissionsDto>> queryPermissions(@RequestParam(value = "roleId", required = false) Long roleId){

        return roleService.queryPermissions(roleId);
    }

    @PostMapping("/grantPermissions")
    public ResultResponse<?> grantPermissions(@RequestParam(value = "roleId") Long roleId, @RequestBody List<Long> permissionIds){

        return roleService.grantPermissions(roleId, permissionIds);
    }

    @PostMapping("/roleCreate")
    public ResultResponse<XcRole> increaseRole(@RequestBody XcRole role){

        return roleService.increaseRole(role);
    }

    @DeleteMapping("/roleDeleteById")
    public ResultResponse<?> deleteRoleById(String roleId){
        return roleService.deleteRoleById(roleId);
    }

    @PostMapping("/roleUpdate")
    public ResultResponse<XcRole> updateRole(@RequestBody XcRole role){
        return roleService.updateRole(role);
    }
}
