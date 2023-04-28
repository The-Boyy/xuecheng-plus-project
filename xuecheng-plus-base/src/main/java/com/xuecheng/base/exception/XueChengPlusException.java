package com.xuecheng.base.exception;

public class XueChengPlusException extends RuntimeException{

    private String errMessage;

    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(CommonError error){
        throw new XueChengPlusException(error.getErrMessage());
    }
}
