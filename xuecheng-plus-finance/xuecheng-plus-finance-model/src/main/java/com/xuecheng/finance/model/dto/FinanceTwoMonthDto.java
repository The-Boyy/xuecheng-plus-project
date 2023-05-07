package com.xuecheng.finance.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FinanceTwoMonthDto {

    private String date;
    private Double rate;
    private Boolean flag;
}
