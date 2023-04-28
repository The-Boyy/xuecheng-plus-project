package com.xuecheng.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface XcOrdersGoodsMapper extends BaseMapper<XcOrdersGoods> {

    public int insertGoods(@Param("xcOrdersGoods") List<XcOrdersGoods> xcOrdersGoodsList);
}
