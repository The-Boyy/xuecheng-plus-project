package com.xuecheng.finance.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@TableName("xc_money")
public class Money {

    private Long id;
    private Long totalMoney;
    private LocalDateTime createTime;
}
