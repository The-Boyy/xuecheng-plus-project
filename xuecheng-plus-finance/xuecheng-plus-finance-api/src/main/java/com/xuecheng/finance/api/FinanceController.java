package com.xuecheng.finance.api;

import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.finance.model.dto.FinanceDayDto;
import com.xuecheng.finance.model.dto.FinanceMonthDto;
import com.xuecheng.finance.model.dto.FinanceTwoMonthDto;
import com.xuecheng.finance.model.dto.MoneyMonthDto;
import com.xuecheng.finance.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FinanceController {

    @Autowired
    private FinanceService financeService;
    @GetMapping("/hello")
    public String sayHello(){
        return "hello world";
    }

    @GetMapping("/financeMonth")
    public ResultResponse<List<FinanceMonthDto>> queryFinanceMonth(){
        return financeService.queryFinanceMonth();
    }

    @GetMapping("/financeDay")
    public ResultResponse<List<FinanceDayDto>> queryFinanceDay(){
        return financeService.queryFinanceDay();
    }

    @GetMapping("/moneyMonth")
    public ResultResponse<List<MoneyMonthDto>> queryMoneyMonth(){
        return financeService.queryMoneyMonth();
    }

    @GetMapping("/financeTwoMonth")
    public ResultResponse<FinanceTwoMonthDto> queryTwoMonth(){
        return financeService.queryTwoMonth();
    }
}
