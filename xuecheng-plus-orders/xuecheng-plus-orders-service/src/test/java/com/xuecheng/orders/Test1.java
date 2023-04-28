package com.xuecheng.orders;

import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 10:32
 */
@SpringBootTest
public class Test1 {

    @Autowired
    XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Test
    public void test() {

        List<XcOrdersGoods> xcOrdersGoodsList = new ArrayList<>();
        for(int i = 0; i< 5; i++){
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            xcOrdersGoods.setOrderId((long) (i * 100));
            xcOrdersGoods.setGoodsId(String.valueOf(i * 10));
            xcOrdersGoods.setGoodsName("name" + i);
            xcOrdersGoods.setGoodsPrice((float) (i * 1000));
            xcOrdersGoods.setGoodsDetail("detail" + i);
            xcOrdersGoods.setGoodsType("60201");
            xcOrdersGoodsList.add(xcOrdersGoods);
        }
        System.out.println(xcOrdersGoodsMapper.insertGoods(xcOrdersGoodsList));
    }
}
