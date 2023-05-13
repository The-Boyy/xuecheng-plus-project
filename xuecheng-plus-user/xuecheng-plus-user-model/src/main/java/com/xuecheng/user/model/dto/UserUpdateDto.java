package com.xuecheng.user.model.dto;

import com.xuecheng.user.model.po.XcUser;
import lombok.Data;

@Data
public class UserUpdateDto extends XcUser {

    private String roleId;
}
