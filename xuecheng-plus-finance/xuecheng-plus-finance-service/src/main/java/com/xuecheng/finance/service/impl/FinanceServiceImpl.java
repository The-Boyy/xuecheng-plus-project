package com.xuecheng.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.finance.mapper.FinanceMapper;
import com.xuecheng.finance.mapper.MoneyMapper;
import com.xuecheng.finance.model.dto.FinanceDayDto;
import com.xuecheng.finance.model.dto.FinanceMonthDto;
import com.xuecheng.finance.model.dto.FinanceTwoMonthDto;
import com.xuecheng.finance.model.dto.MoneyMonthDto;
import com.xuecheng.finance.model.po.Finance;
import com.xuecheng.finance.model.po.Money;
import com.xuecheng.finance.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    FinanceMapper financeMapper;

    @Autowired
    MoneyMapper moneyMapper;

    @Override
    public ResultResponse<List<FinanceMonthDto>> queryFinanceMonth() {

        LambdaQueryWrapper<Finance> wrapper = new LambdaQueryWrapper<Finance>().orderByDesc(Finance::getCreateTime);

        List<Finance> finances = financeMapper.selectList(wrapper);

        Map<String, Long> map = getMap();

        for (Finance finance : finances) {

            int m = finance.getCreateTime().getMonth().getValue();
            int y = finance.getCreateTime().getYear();
            String key = y + "/" + m;
            if(map.containsKey(key)){
                map.put(key, map.get(key) + finance.getMoney());
            }else {
                break;
            }
        }
        List<FinanceMonthDto> financeMonthDtoList = new ArrayList<>();

        int year = getYear();
        int month = getMonth();

        for(int i = month + 1; i <= 12; i++){
            String key = year - 1 + "/" + i;
            FinanceMonthDto financeMonthDto = new FinanceMonthDto();
            financeMonthDto.setDate(key);
            financeMonthDto.setMoney(map.get(key));
            financeMonthDtoList.add(financeMonthDto);
        }
        for(int i = 1; i <= month; i++){
            String key = year + "/" + i;
            FinanceMonthDto financeMonthDto = new FinanceMonthDto();
            financeMonthDto.setDate(key);
            financeMonthDto.setMoney(map.get(key));
            financeMonthDtoList.add(financeMonthDto);
        }

        return ResultResponse.success(200, financeMonthDtoList);
    }

    @Override
    public ResultResponse<List<MoneyMonthDto>> queryMoneyMonth() {
        LambdaQueryWrapper<Money> wrapper = new LambdaQueryWrapper<Money>().orderByDesc(Money::getCreateTime);

        List<Money> moneys = moneyMapper.selectList(wrapper);
        Map<String, Long> map = getMap();
        for (Money money : moneys) {

            int m = money.getCreateTime().getMonth().getValue();
            int y = money.getCreateTime().getYear();
            String key = y + "/" + m;
            if(map.containsKey(key)){
                map.put(key, map.get(key) + money.getTotalMoney());
            }else {
                break;
            }
        }
        List<MoneyMonthDto> moneyMonthDtoList = new ArrayList<>();

        int year = getYear();
        int month = getMonth();

        for(int i = month + 1; i <= 12; i++){
            String key = year - 1 + "/" + i;
            MoneyMonthDto moneyMonthDto = new MoneyMonthDto();
            moneyMonthDto.setDate(key);
            moneyMonthDto.setTotalMoney(map.get(key));
            moneyMonthDtoList.add(moneyMonthDto);
        }
        for(int i = 1; i <= month; i++){
            String key = year + "/" + i;
            MoneyMonthDto moneyMonthDto = new MoneyMonthDto();
            moneyMonthDto.setDate(key);
            moneyMonthDto.setTotalMoney(map.get(key));
            moneyMonthDtoList.add(moneyMonthDto);
        }

        return ResultResponse.success(200, moneyMonthDtoList);
    }

    @Override
    public ResultResponse<List<FinanceDayDto>> queryFinanceDay() {
        LambdaQueryWrapper<Finance> wrapper = new LambdaQueryWrapper<Finance>().orderByDesc(Finance::getCreateTime);
        List<Finance> finances = financeMapper.selectList(wrapper);

        Map<String, Long> map = new HashMap<>();

        int month = getMonth();
        int day = getDay();
        for(int i = day; i > 0; i--){
            if(map.size() == 30){
                break;
            }
            String key = month + "/" + i;
            map.put(key, 0L);
        }
        if(month == 3){
            int year = getYear();
            if((year % 4 == 0 && year % 100 != 0) || (year & 400) == 0){
                for(int i = 29; i > 0; i--){
                    if(map.size() == 30){
                        break;
                    }
                    String key = month - 1 + "/" + i;
                    map.put(key, 0L);
                }
            }
        }else if(month == 1){
            for(int i = 31; i > 0; i--){
                if(map.size() == 30){
                    break;
                }
                String key = 12 + "/" + i;
                map.put(key, 0L);
            }
        }else if(month == 5 || month == 7 || month == 10 || month == 12){
            for(int i = 30; i > 0; i--){
                if(map.size() == 30){
                    break;
                }
                String key = month - 1 + "/" + i;
                map.put(key, 0L);
            }
        }else {
            for(int i = 31; i > 0; i--){
                if(map.size() == 30){
                    break;
                }
                String key = month - 1 + "/" + i;
                map.put(key, 0L);
            }
        }



        for (Finance finance : finances) {

            LocalDateTime createTime = finance.getCreateTime();
            int d = createTime.getDayOfMonth();
            int m = createTime.getMonth().getValue();
            String key = m + "/" + d;

            if(map.containsKey(key)){
                map.put(key, map.get(key) + finance.getMoney());
            }else {
                break;
            }
        }

        List<FinanceDayDto> financeDayDtoList = new ArrayList<>();
        for (Finance finance : finances) {
            LocalDateTime createTime = finance.getCreateTime();
            int d = createTime.getDayOfMonth();
            int m = createTime.getMonth().getValue();
            String key = m + "/" + d;
            if(map.containsKey(key)){
                FinanceDayDto financeDayDto = new FinanceDayDto();
                financeDayDto.setMoney(map.get(key));
                financeDayDto.setTime(key);
                financeDayDtoList.add(financeDayDto);
            }else {
                break;
            }
        }

        return ResultResponse.success(200, financeDayDtoList);
    }

    @Override
    public ResultResponse<FinanceTwoMonthDto> queryTwoMonth() {
        int month = getMonth();

        LambdaQueryWrapper<Finance> wrapper = new LambdaQueryWrapper<Finance>().orderByDesc(Finance::getCreateTime);

        List<Finance> finances = financeMapper.selectList(wrapper);

        Map<Integer, Double> map = new HashMap<>();

        int curMonth = (month + 11) % 12;
        int lastMonth = (month + 10) % 12;

        map.put(month, 0D);
        map.put(curMonth, 0D);
        map.put(lastMonth, 0D);

        for (Finance finance : finances) {
            int m = finance.getCreateTime().getMonth().getValue();
            if(map.containsKey(m)){
                map.put(m, finance.getMoney() + map.get(m));
            }else {
                break;
            }
        }

        FinanceTwoMonthDto financeTwoMonthDto = new FinanceTwoMonthDto();
        financeTwoMonthDto.setDate(month - 1 + "月");
        double sub = map.get(curMonth) - map.get(lastMonth);
        financeTwoMonthDto.setFlag(sub >= 0);
        financeTwoMonthDto.setRate(sub/ (map.get(lastMonth) + 0.0000001D) * 100);

        return ResultResponse.success(200, financeTwoMonthDto);
    }

    private int getMonth(){
        return LocalDateTime.now().getMonthValue();
    }

    private int getYear(){
        return LocalDateTime.now().getYear();
    }

    private int getDay(){
        return LocalDateTime.now().getDayOfMonth();
    }

    private Map<String, Long> getMap(){
        int month = getMonth();
        int year = getYear();

        Map<String, Long> map = new HashMap<>();
        for(int i = month + 1; i <= 12; i++){
            map.put(year - 1 + "/" + i, 0L);
        }
        for(int i = 1; i <= month; i++){
            map.put(year + "/" + i, 0L);
        }
        return map;
    }
}
