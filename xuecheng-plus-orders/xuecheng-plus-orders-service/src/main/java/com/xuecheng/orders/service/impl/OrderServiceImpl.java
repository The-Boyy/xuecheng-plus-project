package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    XcOrdersMapper xcOrdersMapper;

    @Autowired
    XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    XcPayRecordMapper xcPayRecordMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderServiceImpl currentProxy;

    @Value("${pay.qrcodeurl}")
    String qucodeurl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Autowired
    MqMessageService mqMessageService;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        //插入订单表以及商品明细表
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);
        //插入支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();
        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String url = String.format(qucodeurl, payNo);
        String qrCode = null;//二维码图片
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码出错");
        }

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        return xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {

        //查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);

        //拿到支付结果，更新订单表和支付记录表的支付状态
        currentProxy.saveAliPayStatus(payStatusDto);
        //返回最新的支付记录信息
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordByPayno, payRecordDto);

        return payRecordDto;
    }

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo){

        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        //bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        String body = null;

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if(!response.isSuccess()){
                XueChengPlusException.cast("请求支付宝查询支付结果失败");
            }
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            XueChengPlusException.cast("请求支付宝查询支付结果异常");
        }
        Map bodyMap = JSON.parseObject(body, Map.class);
        Map alipay_trade_query_response =(Map) bodyMap.get("alipay_trade_query_response");

        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");

        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        payStatusDto.setApp_id(APP_ID);

        return payStatusDto;
    }

    /**
     * @description 保存支付宝支付结果
     * @param payStatusDto  支付结果信息
     * @return void
     * @author Mr.M
     * @date 2022/10/4 16:52
     */

    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto){

        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        if(payRecordByPayno == null){
            XueChengPlusException.cast("找不到相关的支付记录");
        }
        Long orderId = payRecordByPayno.getOrderId();
        XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
        if(xcOrders == null){
            XueChengPlusException.cast("找不到相关联的订单");
        }
        String status = payRecordByPayno.getStatus();
        if("601002".equals(status)){
            return;
        }
        //支付成功
        String trade_status = payStatusDto.getTrade_status();
        if(trade_status.equals("TRADE_SUCCESS")){
            payRecordByPayno.setStatus("601002");
            payRecordByPayno.setOutPayNo(payStatusDto.getTrade_no());
            payRecordByPayno.setOutPayChannel("Alipay");
            payRecordByPayno.setPaySuccessTime(LocalDateTime.now());
            xcPayRecordMapper.updateById(payRecordByPayno);

            //更新订单表
            xcOrders.setStatus("600002");
            xcOrdersMapper.updateById(xcOrders);

            MqMessage mqMessage = mqMessageService.addMessage(PayNotifyConfig.PAY_NOTIFY, xcOrders.getOutBusinessId(), xcOrders.getOrderType(), null);
            notifyPayResult(mqMessage);
        }else {
            XueChengPlusException.cast("还未支付");
        }

    }

    @Override
    public void notifyPayResult(MqMessage message) {

        String jsonString = JSON.toJSONString(message);
        //创建一个持久化消息
        Message messageObj = MessageBuilder.withBody(jsonString.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

        //消息id,唯一
        Long id = message.getId();
        //correlationData可以指定回调方法
        CorrelationData correlationData = new CorrelationData(id.toString());

        correlationData.getFuture().addCallback(result->{
            boolean ack = result.isAck();
            if(ack){
                //消息发送到了交换机
                log.debug("发送消息成功:{}",jsonString);
                //将消息从数据库表mq_message删除
                mqMessageService.completed(id);
            }else {
                //消息发送失败
                log.debug("消息发送失败:{}",jsonString);
                XueChengPlusException.cast("消息发送到交换机发送失败");
            }
        },ex->{
            //发生异常
            log.debug("消息发送发生异常:{}",jsonString);
            System.out.println("消息发送发生异常");
            XueChengPlusException.cast("消息发送到交换机发生异常");
        });
        //发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAY_NOTIFY_EXCHANGE, PayNotifyConfig.PAY_PROCESS_KEY, messageObj, correlationData);
    }

    @Override
    public void notifyPayFailMessage(String paramsJson) {
        MqMessage mqMessage = mqMessageService.addMessage(PayNotifyConfig.PAY_NOTIFY, null, null, null);
        mqMessage.setReturnfailureMsg(paramsJson);
        mqMessage.setReturnfailureDate(LocalDateTime.now());

        String message = JSON.toJSONString(mqMessage);
        //创建一个持久化消息
        Message messageObj = MessageBuilder.withBody(message.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

        //消息id,唯一
        Long id = mqMessage.getId();
        //correlationData可以指定回调方法
        CorrelationData correlationData = new CorrelationData(id.toString());

        correlationData.getFuture().addCallback(result->{
            boolean ack = result.isAck();
            if(ack){
                //消息发送到了交换机
                log.debug("发送消息成功:{}",message);
            }else {
                //消息发送失败
                log.debug("消息发送失败:{}",message);
                XueChengPlusException.cast("消息发送到交换机发送失败");
            }
        },ex->{
            //发生异常
            log.debug("消息发送发生异常:{}",message);
            System.out.println("消息发送发生异常");
            XueChengPlusException.cast("消息发送到交换机发生异常");
        });
        //发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAY_NOTIFY_EXCHANGE, PayNotifyConfig.PAY_PROCESS_FAIL_KEY, messageObj, correlationData);
    }


    //保存订单信息
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){

        //进行幂等性判断,同一个选课记录只能有一个订单
        XcOrders xcOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if(xcOrders != null){
            return xcOrders;
        }
        //插入订单主表
        xcOrders = new XcOrders();
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001");//未支付
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201");//购买课程
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());

        Long orderId = xcOrders.getId();

        int insert = xcOrdersMapper.insert(xcOrders);

        if(insert <= 0){
            XueChengPlusException.cast("插入订单失败");
        }

        //插入订单明细表
        //将前端传入的明细json串转成list
        String orderDetailJson = addOrderDto.getOrderDetail();

        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoods.forEach(goods -> goods.setOrderId(orderId));
        int insertGoods = xcOrdersGoodsMapper.insertGoods(xcOrdersGoods);
        if(insertGoods <= 0){
            XueChengPlusException.cast("插入订单明细表失败");
        }
        return xcOrders;
    }

    //根据业务id查询订单,业务id是选课记录表中的主键
    public XcOrders getOrderByBusinessId(String businessId){
        XcOrders orders = xcOrdersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

    //保存支付记录
    public XcPayRecord createPayRecord(XcOrders orders){

        Long ordersId = orders.getId();
        XcOrders xcOrders = xcOrdersMapper.selectById(ordersId);
        //如果此订单不存在，不能添加支付记录
        if(xcOrders == null) {
            XueChengPlusException.cast("订单不存在");
        }
        //如果此订单支付结果为成功，不再添加支付记录，避免重复支付
        String status = xcOrders.getStatus();
        if("601002".equals(status)){
            XueChengPlusException.cast("订单已支付");
        }

        LambdaQueryWrapper<XcPayRecord> queryWrapper = new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getOrderId, ordersId);
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(queryWrapper);
        if(xcPayRecord != null){
            //已存在支付记录
            return xcPayRecord;
        }
        xcPayRecord = new XcPayRecord();
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());//支付记录号，传给支付宝
        xcPayRecord.setOrderId(ordersId);
        xcPayRecord.setOrderName(xcOrders.getOrderName());
        xcPayRecord.setCreateDate(LocalDateTime.now());
        xcPayRecord.setStatus("601001");//未支付
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setTotalPrice(xcOrders.getTotalPrice());
        xcPayRecord.setUserId(xcOrders.getUserId());

        int insert = xcPayRecordMapper.insert(xcPayRecord);
        if(insert <= 0){
            XueChengPlusException.cast("插入支付记录失败");
        }

        return xcPayRecord;
    }
}
