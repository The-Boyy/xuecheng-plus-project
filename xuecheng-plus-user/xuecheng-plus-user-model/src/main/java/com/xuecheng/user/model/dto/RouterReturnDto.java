package com.xuecheng.user.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouterReturnDto {

    private String path;
    private String name;
    private String meta;
    private List<RouterDto> children;
}
