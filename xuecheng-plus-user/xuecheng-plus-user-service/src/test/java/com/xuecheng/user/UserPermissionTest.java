package com.xuecheng.user;

import com.xuecheng.user.mapper.PermissionMapper;
import com.xuecheng.user.model.po.XcPermission;
import com.xuecheng.user.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class UserPermissionTest {

    @Autowired
    PermissionMapper permissionMapper;

    @Autowired
    RoleService roleService;

    @Test
    public void deletePermissionTest(){

        List<Long> deleteIds = new ArrayList<>();

        deleteIds.add(2L);
        deleteIds.add(3L);

        int i = permissionMapper.deletePermissionByIds(6L, deleteIds);

        System.out.println(i);
    }

    @Test
    public void insertPermissionTest(){
        List<XcPermission> xcPermissionList = new ArrayList<>();

        XcPermission xcPermission = new XcPermission();
        xcPermission.setRoleId(8L);
        xcPermission.setMenuId(43L);
        xcPermission.setCreateTime(LocalDateTime.now());
        xcPermissionList.add(xcPermission);

        xcPermission = new XcPermission();
        xcPermission.setRoleId(8L);
        xcPermission.setMenuId(44L);
        xcPermission.setCreateTime(LocalDateTime.now());
        xcPermissionList.add(xcPermission);

        int i = permissionMapper.insertBatch(xcPermissionList);
        System.out.println(i);
    }

    @Test
    public void updateRolePermission(){

        Long roleId = 6L;

        List<Long> permissionIds = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            permissionIds.add((long)i);
        }

        roleService.grantPermissions(roleId, permissionIds);
    }
}
