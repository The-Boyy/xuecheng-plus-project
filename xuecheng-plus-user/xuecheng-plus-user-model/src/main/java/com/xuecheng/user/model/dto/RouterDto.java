package com.xuecheng.user.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RouterDto {

    String path;
    String name;
    String meta;
    String component;
}
