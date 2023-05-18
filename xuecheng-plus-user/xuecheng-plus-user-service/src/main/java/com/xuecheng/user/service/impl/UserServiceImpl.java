package com.xuecheng.user.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.base.utils.UUIDUtil;
import com.xuecheng.user.mapper.UserMapper;
import com.xuecheng.user.mapper.UserRoleMapper;
import com.xuecheng.user.model.dto.*;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.model.po.XcUserRole;
import com.xuecheng.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Value("${minio.address}")
    String MINIO_ADDRESS;
    @Value("${password.default}")
    String defaultPassword;

    @Autowired
    UserRoleMapper userRoleMapper;

    @Override
    public PageResult<UserInfoDto> queryUserBaseList(PageParams pageParams, QueryUserParamsDto queryUserParamsDto) {

        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();

        if(queryUserParamsDto != null){
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getUsername()), XcUser::getUsername, queryUserParamsDto.getUsername());
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getNickname()), XcUser::getNickname, queryUserParamsDto.getUsername());
            queryWrapper.eq(StringUtils.isNotEmpty(queryUserParamsDto.getStatus()), XcUser::getStatus, queryUserParamsDto.getStatus());
            queryWrapper.eq(StringUtils.isNotEmpty(queryUserParamsDto.getSex()), XcUser::getSex, queryUserParamsDto.getSex());
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getEmail()), XcUser::getEmail, queryUserParamsDto.getEmail());
            queryWrapper.like(StringUtils.isNotEmpty(queryUserParamsDto.getQq()), XcUser::getQq, queryUserParamsDto.getQq());
        }

        Page<XcUser> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<XcUser> pageResult = userMapper.selectPage(page, queryWrapper);

        List<XcUser> users = pageResult.getRecords();

        List<UserInfoDto> userInfoDtoList = new ArrayList<>();

        String[] userIds = new String[users.size()];

        for(int i = 0; i < users.size(); i++){
            userIds[i] = users.get(i).getId();
        }

        List<UserIdWithRoleNameDto> roleNamesByUserIds = userMapper.getRoleNamesByUserIds(userIds);

        Map<String, String> map = new HashMap<>();
        for (UserIdWithRoleNameDto roleNamesByUserId : roleNamesByUserIds) {
            if(roleNamesByUserId.getRoleName() != null){
                map.put(roleNamesByUserId.getUserId(), roleNamesByUserId.getRoleName());
            }else {
                map.put(roleNamesByUserId.getUserId(), null);
            }
        }

        for (XcUser user : users) {
            UserInfoDto userInfoDto = new UserInfoDto();
            BeanUtils.copyProperties(user, userInfoDto);
            userInfoDto.setRoleName(map.get(user.getId()));
            userInfoDtoList.add(userInfoDto);
        }

        int total = (int)pageResult.getTotal();

        return new PageResult<>(userInfoDtoList, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Transactional
    @Override
    public ResultResponse<XcUser> register(UserRegisterDto userRegisterDto, String adminName) {

        XcUser xcUser = new XcUser();
        try {
            BeanUtils.copyProperties(userRegisterDto, xcUser);

            xcUser.setId(String.valueOf(UUIDUtil.getUUIDForInt()));

            xcUser.setCreateTime(LocalDateTime.now());
            xcUser.setUpdateTime(LocalDateTime.now());

            String nickname = xcUser.getNickname();
            if(nickname == null || "".equals(nickname)){
                xcUser.setNickname(xcUser.getUsername());
            }
            xcUser.setName(xcUser.getNickname());

            if(xcUser.getSex() == null || "".equals(xcUser.getSex())){
                xcUser.setSex("-1");
            }

            xcUser.setStatus("1");

            String username = xcUser.getUsername();
            String cellphone = xcUser.getCellphone();

            if(username == null || "".equals(username) || cellphone == null || "".equals(cellphone)){
                XueChengPlusException.cast("请输入用户的完整信息");
            }

            XcUser selectOne = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));

            if(selectOne != null){
                XueChengPlusException.cast("用户已存在，不可重复注册");
            }

            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            xcUser.setPassword(bCryptPasswordEncoder.encode(defaultPassword));

            xcUser.setUserpic(MINIO_ADDRESS + "/mediafiles/2023/04/25/cb9651e9dd2d029551d5f22780db20e5.png");

            int insert = userMapper.insert(xcUser);
            if(insert <= 0){
                XueChengPlusException.cast("注册失败,插入用户失败");
            }

            XcUserRole xcUserRole = new XcUserRole();
            xcUserRole.setUserId(xcUser.getId());
            xcUserRole.setRoleId(Long.parseLong(userRegisterDto.getRoleId()));
            xcUserRole.setCreateTime(LocalDateTime.now());
            xcUserRole.setCreator(adminName);
            int insert1 = userRoleMapper.insert(xcUserRole);
            if(insert1 <= 0){
                XueChengPlusException.cast("注册失败，插入角色中间表失败");
            }

        }catch (Exception e){
            e.printStackTrace();
            XueChengPlusException.cast("注册用户失败");
        }

        return ResultResponse.success(200, xcUser);
    }

    @Override
    public ResultResponse<UserUpdateDto> findUserById(String userId) {

        XcUser xcUser = userMapper.selectById(userId);

        if(xcUser == null){
            XueChengPlusException.cast("用户不存在");
        }

        XcUserRole xcUserRole = userRoleMapper.selectOne(new LambdaQueryWrapper<XcUserRole>().eq(XcUserRole::getUserId, userId));

        if(xcUserRole == null){
            XueChengPlusException.cast("用户角色查询不存在");
        }

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        BeanUtils.copyProperties(xcUser, userUpdateDto);
        userUpdateDto.setRoleId(String.valueOf(xcUserRole.getRoleId()));

        return ResultResponse.success(200, userUpdateDto);
    }

    @Transactional
    @Override
    public ResultResponse<XcUser> userUpdate(UserUpdateDto userUpdateDto, String username) {

        String userId = userUpdateDto.getId();
        if(userUpdateDto.getUsername() == null || "".equals(userUpdateDto.getUsername()) || userUpdateDto.getCellphone() == null || "".equals(userUpdateDto.getCellphone())){
            XueChengPlusException.cast("请输入用户的必要完整信息");
        }

        LambdaQueryWrapper<XcUserRole> wrapper = new LambdaQueryWrapper<XcUserRole>().eq(XcUserRole::getUserId, userId);

        XcUserRole xcUserRole = userRoleMapper.selectOne(wrapper);
        if(xcUserRole == null){
            XueChengPlusException.cast("用户角色查询不存在");
        }

        if(!String.valueOf(xcUserRole.getRoleId()).equals(userUpdateDto.getRoleId())){
            XcUserRole xcUserRoleUpdate = new XcUserRole();
            BeanUtils.copyProperties(xcUserRole, xcUserRoleUpdate);
            xcUserRoleUpdate.setRoleId(Long.parseLong(userUpdateDto.getRoleId()));
            xcUserRoleUpdate.setCreator(username);
            int i = userRoleMapper.updateById(xcUserRoleUpdate);
            if(i <= 0){
                XueChengPlusException.cast("更新用户角色信息失败");
            }
        }

        XcUser xcUser = userMapper.selectById(userId);
        if(xcUser == null){
            XueChengPlusException.cast("用户不存在");
        }
        xcUser.setCellphone(userUpdateDto.getCellphone());
        xcUser.setEmail(userUpdateDto.getEmail());
        xcUser.setNickname(userUpdateDto.getNickname());
        xcUser.setSex(userUpdateDto.getSex());
        xcUser.setQq(userUpdateDto.getQq());
        xcUser.setUsername(userUpdateDto.getUsername());
        xcUser.setUpdateTime(LocalDateTime.now());

        String userpic = userUpdateDto.getUserpic();
        if(!(userpic == null || "".equals(userpic))){
            xcUser.setUserpic(MINIO_ADDRESS + userpic);
        }

        int i = userMapper.updateById(xcUser);
        if(i <= 0){
            XueChengPlusException.cast("更新用户信息失败");
        }

        return ResultResponse.success(200, xcUser);
    }

    @Override
    @Transactional
    public ResultResponse<?> deleteUserById(String userId) {

        XcUser xcUser = userMapper.selectById(userId);
        if(xcUser == null){
            XueChengPlusException.cast("用户不存在");
        }
        int deleteUserRole = userRoleMapper.delete(new LambdaQueryWrapper<XcUserRole>().eq(XcUserRole::getUserId, userId));
        if(deleteUserRole <= 0){
            XueChengPlusException.cast("删除用户角色中间表失败");
        }
        int deleteUser = userMapper.deleteById(userId);
        if(deleteUser <= 0){
            XueChengPlusException.cast("删除用户失败");
        }

        return ResultResponse.success(200, null);
    }

    @Override
    public String queryCompanyNameById(String companyId) {

        return userMapper.queryCompanyNameById(companyId);
    }
}
