package com.xuecheng.orders.api;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.service.OrderService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class PayCallBackController {

    @Autowired
    OrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Value("${pay.alipay.CALLBACK_DOMAIN_NAME}")
    String CALLBACK_DOMAIN_NAME;

    @ApiOperation("接收支付结果通知")
    @PostMapping("/paynotify")
    public String paynotify(HttpServletRequest request) throws IOException, AlipayApiException {
        Map<String, String> params = convertRequestParamsToMap(request);
        String paramsJson = JSON.toJSONString(params);
        try {
            boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, AlipayConfig.SIGNTYPE);

            if (verify_result) {//验证成功
                new Thread(() -> {
                    try {
                        String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
                        //支付宝交易号

                        String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

                        //交易状态
                        String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
                        String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");


                        if (trade_status.equals("TRADE_SUCCESS")) {
                            PayStatusDto payStatusDto = new PayStatusDto();
                            payStatusDto.setTrade_status(trade_status);
                            payStatusDto.setTrade_no(trade_no);
                            payStatusDto.setOut_trade_no(out_trade_no);
                            payStatusDto.setTotal_amount(total_amount);
                            payStatusDto.setApp_id(APP_ID);
                            orderService.saveAliPayStatus(payStatusDto);
                        }
                    }catch (Exception e){
                        log.debug("支付宝回调成功,处理业务逻辑失败，signVerified=false, paramsJson:{}", paramsJson);
                        orderService.notifyPayFailMessage(paramsJson);
                    }
                }).start();
                log.info("支付宝回调签名认证成功");
                return "success";
            }else {
                log.debug("支付宝回调签名认证失败，signVerified=false, paramsJson:{}", paramsJson);
                return "failure";
            }
        }catch (Exception e){
            log.debug("支付宝回调签名认证失败，signVerified=false, paramsJson:{}", paramsJson);
            return "failure";
        }
    }

    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();

        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                stringBuilder.append((i == values.length - 1) ? values[i]:values[i] + ",");
            }
            params.put(name, stringBuilder.toString());
        }
        return params;
    }


}
