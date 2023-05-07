package com.xuecheng.finance.service;

import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.finance.model.dto.FinanceDayDto;
import com.xuecheng.finance.model.dto.FinanceMonthDto;
import com.xuecheng.finance.model.dto.FinanceTwoMonthDto;
import com.xuecheng.finance.model.dto.MoneyMonthDto;

import java.util.List;

public interface FinanceService {
    ResultResponse<List<FinanceMonthDto>> queryFinanceMonth();

    ResultResponse<List<MoneyMonthDto>> queryMoneyMonth();

    ResultResponse<List<FinanceDayDto>> queryFinanceDay();

    ResultResponse<FinanceTwoMonthDto> queryTwoMonth();
}
