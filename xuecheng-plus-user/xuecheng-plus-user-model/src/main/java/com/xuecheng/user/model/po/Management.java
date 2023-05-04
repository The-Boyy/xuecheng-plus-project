package com.xuecheng.user.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("management")
public class Management implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private Long parentId;

    private Long grade;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private Long status;

    private String path;

    private String description;

    private String icon;
}
