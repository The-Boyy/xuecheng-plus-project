package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MediaServiceClientFallback implements MediaServiceClient{
    @Override
    public String upload(MultipartFile file, String objectName) throws IOException {

        return null;
    }
}
