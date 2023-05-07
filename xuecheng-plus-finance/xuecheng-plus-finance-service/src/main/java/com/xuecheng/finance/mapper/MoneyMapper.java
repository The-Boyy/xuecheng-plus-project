package com.xuecheng.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.finance.model.po.Money;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MoneyMapper extends BaseMapper<Money> {
}
