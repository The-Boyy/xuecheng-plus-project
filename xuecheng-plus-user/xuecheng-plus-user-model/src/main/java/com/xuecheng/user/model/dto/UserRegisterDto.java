package com.xuecheng.user.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegisterDto {

    private String username;

    private String nickname;

    private LocalDateTime birthday;

    private String sex;

    private String email;

    private String cellphone;

    private String qq;

    private String roleId;
}
