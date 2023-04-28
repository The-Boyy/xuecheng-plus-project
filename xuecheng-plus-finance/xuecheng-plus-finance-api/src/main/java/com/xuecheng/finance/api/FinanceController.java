package com.xuecheng.finance.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinanceController {

    @GetMapping("/hello")
    public String sayHello(){
        return "hello world";
    }
}
