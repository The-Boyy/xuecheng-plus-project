package com.xuecheng.system.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class XcLogDto {

    private String description;
    private LocalDateTime createTime;
}
