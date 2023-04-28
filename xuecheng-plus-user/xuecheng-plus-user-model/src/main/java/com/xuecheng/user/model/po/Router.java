package com.xuecheng.user.model.po;

import lombok.Data;

@Data
public class Router {

    Integer id;
    String path;
    String name;
    String meta;
    String component;
}
