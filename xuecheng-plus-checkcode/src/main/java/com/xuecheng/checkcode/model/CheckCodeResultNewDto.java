package com.xuecheng.checkcode.model;

import lombok.Data;

@Data
public class CheckCodeResultNewDto {

    /**
     * key用于验证
     */
    private String code;

    private String key;

    /**
     * 混淆后的内容
     * 举例：
     * 1.图片验证码为:图片base64编码
     * 2.短信验证码为:null
     * 3.邮件验证码为: null
     * 4.邮件链接点击验证为：null
     * ...
     */
    private String aliasing;
}
