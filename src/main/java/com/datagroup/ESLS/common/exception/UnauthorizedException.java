package com.datagroup.ESLS.common.exception;

public class UnauthorizedException extends RuntimeException {
    private Integer code;
    private String message;

    public UnauthorizedException(ResultEnum resultEnum) {
        this.message = resultEnum.getMessage();
        this.code = resultEnum.getCode();
    }

    public UnauthorizedException(Integer code,String message) {
        this.code = code;
        this.message = message;
    }
    @Override
    public String toString(){
        return "错误码："+code+" "+"错误信息："+message;
    }
}
