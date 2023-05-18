package com.xuecheng.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.user.model.dto.UserIdWithRoleNameDto;
import com.xuecheng.user.model.po.XcUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<XcUser> {

    public List<UserIdWithRoleNameDto> getRoleNamesByUserIds(@Param("userIds") String[] userIds);

    @Select("select name from xc_company where id = #{companyId}")
    public String queryCompanyNameById(String companyId);
}
