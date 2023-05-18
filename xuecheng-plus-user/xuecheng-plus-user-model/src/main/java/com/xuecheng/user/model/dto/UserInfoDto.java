package com.xuecheng.user.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoDto {

    private String id;

    private String username;

    private String nickname;
    private String companyId;
    private String userpic;

    private String utype;

    private LocalDateTime birthday;

    private String sex;

    private String email;

    private String cellphone;

    private String qq;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String roleName;
}
