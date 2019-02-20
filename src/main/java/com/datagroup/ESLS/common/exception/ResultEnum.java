package com.datagroup.ESLS.common.exception;

import lombok.Getter;

@Getter
public enum ResultEnum {
    // 成功
    SUCCESS(0,"成功"),
    COMMUNITICATION_ERROR(1,"通信时Channel为空！路由器未连接"),
    FILE_ERROR(10,"文件导入或导出失败"),

    USER_LOGIN_ERROR(20,"用户登录认证失败"),
    TOKEN_INVALID(21,"token无效或过期"),
    USER_NOT_EXIST(22,"用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(23,"用户名或者密码错误"),
    USER_LOCKED(24,"账号锁定"),

    ORDER_NOT_EXIST(30,"用户未授权"),
    ORDERDETAIL_NOT_EXIST(13,"订单详情不存在"),
    ORDER_STATUS_ERROR(14,"订单状态不正确"),
    ORDER_UPDATE_FAIL(15,"订单更新失败"),
    ORDER_DETAIL_EMPTY(16,"订单详情为空"),
    CART_EMPTY(18,"购物车为空"),
    ORDER_OWNER_ERROR(19,"该订单不属于当前用户"),;
    private Integer code;
    private String message;
    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
