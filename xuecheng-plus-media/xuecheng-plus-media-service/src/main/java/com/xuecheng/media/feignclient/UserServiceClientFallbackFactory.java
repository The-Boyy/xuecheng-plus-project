package com.xuecheng.media.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable throwable) {
        return new UserServiceClient() {
            @Override
            public String queryCompanyNameById(String companyId) {
                log.error("查询机构名称发生熔断，机构id:{},熔断异常:{}", companyId, throwable.toString(), throwable);
                return "";
            }
        };
    }
}
