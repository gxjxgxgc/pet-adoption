package com.petadoption.petadoption.dto;

import com.petadoption.petadoption.response.ResponseCode;
import lombok.Data;

@Data
public class ApiResponse<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ========== 成功返回 ==========

    /**
     * 成功返回（无参数）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                null
        );
    }

    /**
     * 成功返回（仅消息）
     * @param message 提示消息
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(
                ResponseCode.SUCCESS.getCode(),
                message,
                null
        );
    }

    /**
     * 成功返回（带数据）
     * @param data 返回数据
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                data
        );
    }

    /**
     * 成功返回（自定义消息）
     * @param message 提示消息
     * @param data 返回数据
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                ResponseCode.SUCCESS.getCode(),
                message,
                data
        );
    }

    /**
     * 成功返回（使用枚举）
     * @param responseCode 响应状态码枚举
     * @param data 返回数据
     */
    public static <T> ApiResponse<T> success(ResponseCode responseCode, T data) {
        return new ApiResponse<>(
                responseCode.getCode(),
                responseCode.getMessage(),
                data
        );
    }

    // ========== 失败返回 ==========

    /**
     * 失败返回（默认错误）
     * @param message 错误消息
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                ResponseCode.ERROR.getCode(),
                message,
                null
        );
    }

    /**
     * 失败返回（使用枚举）
     * @param responseCode 响应状态码枚举
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return new ApiResponse<>(
                responseCode.getCode(),
                responseCode.getMessage(),
                null
        );
    }

    /**
     * 失败返回（使用枚举，带自定义消息）
     * @param responseCode 响应状态码枚举
     * @param message 错误消息
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        return new ApiResponse<>(
                responseCode.getCode(),
                message,
                null
        );
    }

    /**
     * 失败返回（指定错误码和消息）
     * @param code 错误码
     * @param message 错误消息
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // ========== 特殊场景 ==========

    /**
     * 未授权
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(
                ResponseCode.UNAUTHORIZED.getCode(),
                ResponseCode.UNAUTHORIZED.getMessage(),
                null
        );
    }

    /**
     * 禁止访问
     */
    public static <T> ApiResponse<T> forbidden() {
        return new ApiResponse<>(
                ResponseCode.FORBIDDEN.getCode(),
                ResponseCode.FORBIDDEN.getMessage(),
                null
        );
    }

    /**
     * 资源不存在
     */
    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(
                ResponseCode.NOT_FOUND.getCode(),
                ResponseCode.NOT_FOUND.getMessage(),
                null
        );
    }

    /**
     * 构建响应（使用枚举，无数据）
     */
    public static <T> ApiResponse<T> of(ResponseCode responseCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(responseCode.getCode());
        response.setMessage(responseCode.getMessage());
        return response;
    }


    /**
     * 构建响应（指定状态码和消息，无数据）
     * @param code 状态码
     * @param message 提示消息
     */
    public static <T> ApiResponse<T> of(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    /**
     * 构建响应（使用枚举，带数据）
     * @param responseCode 响应状态码枚举
     * @param data 返回数据
     */
    public static <T> ApiResponse<T> of(ResponseCode responseCode, T data) {
        return new ApiResponse<>(
                responseCode.getCode(),
                responseCode.getMessage(),
                data
        );
    }

}
