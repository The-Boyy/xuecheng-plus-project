package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class PageParams {
    @ApiModelProperty(value = "页码",example = "1")
    //当前页码
    private Long pageNo = 1L;
    @ApiModelProperty(value = "每页记录数",example = "20")
    //每页记录数默认值
    private Long pageSize = 10L;
}