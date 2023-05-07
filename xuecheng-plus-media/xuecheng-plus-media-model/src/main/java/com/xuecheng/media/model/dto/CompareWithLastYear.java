package com.xuecheng.media.model.dto;

import lombok.Data;

@Data
public class CompareWithLastYear {

    private String date;
    private Boolean flag;
    private Double rate;
}
