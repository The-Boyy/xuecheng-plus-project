package com.xuecheng.user.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueryUserParamsDto {

    private String username;
    private String nickname;
    private String status;
}
