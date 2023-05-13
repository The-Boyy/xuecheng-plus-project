package com.xuecheng.base.utils;

import java.util.UUID;

public class UUIDUtil {

    public static Integer getUUIDForInt(){
        return Math.abs(UUID.randomUUID().toString().replace("-", "").hashCode());
    }

    public static void main(String[] args) {
        int i = 0;
        while (i++ < 100){
            System.out.println(getUUIDForInt());
        }
    }
}
