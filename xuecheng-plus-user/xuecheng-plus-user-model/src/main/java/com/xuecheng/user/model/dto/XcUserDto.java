package com.xuecheng.user.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class XcUserDto {

    private String username;
    private String nickname;
    private String email;
    private String qq;
    private String cellphone;
    private LocalDateTime birthday;
    private String userpic;
}
