package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e){

        log.error("系统异常{}", e.getErrMessage(), e);

        String errMessage = e.getErrMessage();
        return new RestErrorResponse(errMessage);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse Exception(MethodArgumentNotValidException e){

        BindingResult result = e.getBindingResult();
        List<String> errors = new ArrayList<>();
        result.getFieldErrors().forEach(item->{
            errors.add(item.getDefaultMessage());
        });

        String errorMessage = StringUtils.join(errors, ",");

        log.error("系统异常{}", errorMessage);

        return new RestErrorResponse(errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse Exception(Exception e){

        log.error("系统异常{}", e.getMessage(), e);

        if(e.getMessage().equals("不允许访问")){
            return new RestErrorResponse("您没有权限操作此功能");
        }

        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }
}
