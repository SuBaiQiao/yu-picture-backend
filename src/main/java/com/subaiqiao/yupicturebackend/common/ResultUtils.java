package com.subaiqiao.yupicturebackend.common;

import com.subaiqiao.yupicturebackend.exception.ErrorCode;

/**
 * 响应工具类
 */
public class ResultUtils {
    /**
     * 成功
     * @param data 数据
     * @return 响应
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 错误
     * @param errorCode 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 错误
     * @param errorCode 错误信息
     * @param message 返回信息
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode, message);
    }

    /**
     * 错误
     * @param code 响应码
     * @param message 返回信息
     * @return 响应
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }
}
