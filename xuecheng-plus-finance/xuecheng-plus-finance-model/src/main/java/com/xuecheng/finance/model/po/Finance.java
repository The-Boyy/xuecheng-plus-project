package com.xuecheng.finance.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@TableName("finance")
public class Finance {

    private Long id;
    private Long money;
    private LocalDateTime createTime;
    private Integer status;
    private Long userId;
    private String description;
    private String code;
    private Long courseId;
    private Integer direction;
}
