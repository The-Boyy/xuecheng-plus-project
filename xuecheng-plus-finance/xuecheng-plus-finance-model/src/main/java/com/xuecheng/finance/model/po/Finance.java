package com.xuecheng.finance.model.po;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class Finance {

    private Long id;
    private Long money;
    private LocalDateTime createTime;
    private Integer status;
    private Long userId;
    private String description;
    private String code;
    private Long courseId;
}
