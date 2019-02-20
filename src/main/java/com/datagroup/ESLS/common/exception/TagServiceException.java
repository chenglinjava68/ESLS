package com.datagroup.ESLS.common.exception;

import lombok.Data;

@Data
public class TagServiceException extends RuntimeException{
    private Integer code;
    private String message;

    public TagServiceException(ResultEnum resultEnum) {
        this.message = resultEnum.getMessage();
        this.code = resultEnum.getCode();
    }

    public TagServiceException(Integer code,String message) {
        this.code = code;
        this.message = message;
    }
    @Override
    public String toString(){
        return "错误码："+code+" "+"错误信息："+message;
    }
}
