package com.xuecheng.system.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xc_log")
public class XcLog {

    private Integer id;
    private String description;
    private LocalDateTime createTime;
}
