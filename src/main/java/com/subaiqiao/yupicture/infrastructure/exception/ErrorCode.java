package com.subaiqiao.yupicture.infrastructure.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    ACCESS_DENIED(40101, "权限不足"),
    NOT_FOUND(40400, "请求资源不存在"),
    FORBIDDEN(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "服务器内部错误"),
    OPERATION_ERROR(50001, "操作失败");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
