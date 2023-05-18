package com.xuecheng.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.mapper.PermissionMapper;
import com.xuecheng.user.mapper.RoleMapper;
import com.xuecheng.user.mapper.UserRoleMapper;
import com.xuecheng.user.model.dto.PermissionsDto;
import com.xuecheng.user.model.dto.QueryRoleParamsDto;
import com.xuecheng.user.model.dto.RoleDto;
import com.xuecheng.user.model.po.XcMenu;
import com.xuecheng.user.model.po.XcPermission;
import com.xuecheng.user.model.po.XcRole;
import com.xuecheng.user.model.po.XcUserRole;
import com.xuecheng.user.service.RoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    RoleMapper roleMapper;

    @Autowired
    PermissionMapper permissionMapper;

    @Autowired
    UserRoleMapper userRoleMapper;

    @Override
    public PageResult<RoleDto> queryList(PageParams pageParams, QueryRoleParamsDto queryRoleParamsDto) {

        LambdaQueryWrapper<XcRole> wrapper = new LambdaQueryWrapper<>();

        if(queryRoleParamsDto != null){
            wrapper.like(StringUtils.isNotEmpty(queryRoleParamsDto.getRoleName()), XcRole::getRoleName, queryRoleParamsDto.getRoleName());
            wrapper.eq(StringUtils.isNotEmpty(queryRoleParamsDto.getStatus()), XcRole::getStatus, queryRoleParamsDto.getStatus());
        }

        Page<XcRole> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<XcRole> pageResult = roleMapper.selectPage(page, wrapper);

        List<XcRole> xcRoles = pageResult.getRecords();

        List<RoleDto> roleDtos = new ArrayList<>();

        for (XcRole xcRole : xcRoles) {

            RoleDto roleDto = new RoleDto();
            BeanUtils.copyProperties(xcRole, roleDto);

            roleDtos.add(roleDto);
        }

        int total = (int) pageResult.getTotal();

        return new PageResult<>(roleDtos, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    public ResultResponse<List<PermissionsDto>> queryPermissions(Long roleId) {

        List<PermissionsDto> permissionsDtos = permissionMapper.queryPermissions(roleId);

        Map<Long, PermissionsDto> mapTemp = permissionsDtos.stream().collect(Collectors.toMap(XcMenu::getId, value -> value, (key1, key2) -> key2));

        List<PermissionsDto> permissionsDtoList = new ArrayList<>();

        permissionsDtos.forEach(item -> {
            if(item.getParentId() == 0L){
                permissionsDtoList.add(item);
            }

            PermissionsDto parentDto = mapTemp.get(item.getParentId());

            if(parentDto != null){
                if(parentDto.getChildrenTreeNodes() == null){
                    parentDto.setChildrenTreeNodes(new ArrayList<>());
                }
                parentDto.getChildrenTreeNodes().add(item);
            }
        });
        return ResultResponse.success(200, permissionsDtoList);
    }

    @Transactional
    @Override
    public ResultResponse<?> grantPermissions(Long roleId, List<Long> permissionIds) {

        try {
            List<Long> permissionIdsByRoleId = permissionMapper.queryPermissionIdsByRoleId(roleId);

            Set<Long> set = new HashSet<>(permissionIds);

            List<Long> deletePermissionIds = new ArrayList<>();
            for (Long permissionId : permissionIdsByRoleId) {
                if(!set.contains(permissionId)){
                    deletePermissionIds.add(permissionId);
                }else {
                    set.remove(permissionId);
                }
            }

            if(deletePermissionIds.size() != 0){
                int deleteIds = permissionMapper.deletePermissionByIds(roleId, deletePermissionIds);
                if(deleteIds <= 0){
                    XueChengPlusException.cast("删除权限信息失败");
                }
            }

            List<XcPermission> xcPermissionList = new ArrayList<>();

            for (Long id : set) {
                XcPermission xcPermission = new XcPermission();
                xcPermission.setMenuId(id);
                xcPermission.setCreateTime(LocalDateTime.now());
                xcPermission.setRoleId(roleId);

                xcPermissionList.add(xcPermission);
            }

            if(xcPermissionList.size() > 0){
                int insertBatchNum = permissionMapper.insertBatch(xcPermissionList);
                if(insertBatchNum <= 0){
                    XueChengPlusException.cast("添加权限信息失败");
                }
            }

            return ResultResponse.success(200, null);
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("修改角色的权限信息失败");
        }

        return null;
    }

    @Override
    public ResultResponse<List<RoleDto>> queryRoleNameList() {

        return ResultResponse.success(200, roleMapper.queryRoleNameList());
    }

    @Override
    @Transactional
    public ResultResponse<XcRole> increaseRole(XcRole role) {

        if(role.getRoleName() == null || "".equals(role.getRoleName()) || role.getRoleCode() == null || "".equals(role.getRoleCode())){
            XueChengPlusException.cast("新增角色失败，信息不完整");
        }
        XcRole xcRole = roleMapper.selectOne(new LambdaQueryWrapper<XcRole>().eq(XcRole::getRoleName, role.getRoleName()));
        if(xcRole != null){
            XueChengPlusException.cast("角色名已存在，不可重复添加");
        }
        if(role.getDescription() == null || "".equals(role.getDescription())){
            role.setDescription(role.getRoleName());
        }
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setStatus("1");

        int insert = roleMapper.insert(role);
        if(insert <= 0){
            XueChengPlusException.cast("新增角色失败");
        }
        return ResultResponse.success(200, role);
    }

    @Override
    @Transactional
    public ResultResponse<?> deleteRoleById(String roleId) {

        Long id = Long.parseLong(roleId);

        XcRole xcRole = roleMapper.selectById(id);
        if(xcRole == null){
            XueChengPlusException.cast("角色不存在");
        }

        LambdaQueryWrapper<XcUserRole> wrapper = new LambdaQueryWrapper<XcUserRole>().eq(XcUserRole::getRoleId, id);

        List<XcUserRole> xcUserRoles = userRoleMapper.selectList(wrapper);
        if(xcUserRoles.size() != 0){
            int delete = userRoleMapper.delete(wrapper);
            if(delete <= 0){
                XueChengPlusException.cast("删除角色与用户关联信息失败");
            }
        }

        LambdaQueryWrapper<XcPermission> eq = new LambdaQueryWrapper<XcPermission>().eq(XcPermission::getRoleId, id);
        List<XcPermission> xcPermissionList = permissionMapper.selectList(eq);
        if(xcPermissionList.size() != 0){
            int delete = permissionMapper.delete(eq);
            if(delete <= 0){
                XueChengPlusException.cast("删除角色与权限管理信息失败");
            }
        }

        int i = roleMapper.deleteById(id);
        if(i <= 0){
            XueChengPlusException.cast("删除角色失败");
        }

        return ResultResponse.success(200, null);
    }

    @Override
    @Transactional
    public ResultResponse<XcRole> updateRole(XcRole role) {

        Long id = role.getId();

        XcRole xcRole = roleMapper.selectById(id);
        if(xcRole == null){
            XueChengPlusException.cast("角色不存在");
        }

        String roleName = role.getRoleName();
        String roleCode = role.getRoleCode();
        if(roleName == null || roleCode == null || "".equals(roleName) || "".equals(roleCode)){
            XueChengPlusException.cast("角色信息不完整");
        }

        role.setUpdateTime(LocalDateTime.now());

        int i = roleMapper.updateById(role);

        if(i <= 0){
            XueChengPlusException.cast("更新角色信息失败");
        }
        return ResultResponse.success(200, xcRole);
    }

    @Override
    public ResultResponse<XcRole> findRoleById(String roleId) {

        XcRole xcRole = roleMapper.selectById(Long.parseLong(roleId));
        if(xcRole == null){
            XueChengPlusException.cast("角色不存在");
        }

        return ResultResponse.success(200, xcRole);
    }
}
