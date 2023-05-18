package com.xuecheng.media.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-api", fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {

    @GetMapping("/user/queryCompanyNameById")
    public String queryCompanyNameById(@RequestParam("companyId") String companyId);
}
