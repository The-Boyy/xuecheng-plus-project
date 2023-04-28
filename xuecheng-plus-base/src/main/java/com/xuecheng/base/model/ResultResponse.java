package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResultResponse<T> {

    private int code;
    private T data;

    public static <T> ResultResponse<T> success(int code, T result) {
        ResultResponse<T> response = new ResultResponse<T>();
        response.setCode(code);
        response.setData(result);
        return response;
    }
}
