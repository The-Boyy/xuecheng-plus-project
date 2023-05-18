package com.xuecheng.content.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCourseInfoDto {

    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String users;
    private String tags;
    private String maxCategory;
    private String minCategory;
    private String grade;
    private String teachmode;
    private String description;
    private String pic;
    private LocalDateTime createDate;
    private LocalDateTime changeDate;
    private String createPeople;
    private String changePeople;
    private String auditStatus;
    private String status;
    private String mt;
    private String st;

    private String charge;

    private Float price;

    private Float originalPrice;

    private String qq;

    private String wechat;

    private String phone;

    private Integer validDays;
}
